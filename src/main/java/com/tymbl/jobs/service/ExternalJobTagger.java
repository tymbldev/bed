package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.Company;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import com.tymbl.jobs.entity.ExternalJobDetail;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalJobTagger {

  private final CompanyRepository companyRepository;
  private final DesignationRepository designationRepository;
  private final SimilarContentRepository similarContentRepository;
  private final com.tymbl.common.service.AIRestService aiRestService;
  private final com.tymbl.common.service.DropdownService dropdownService;

  /**
   * Tag external job detail with company and designation
   */
  @Transactional
  public TaggingResult tagExternalJob(ExternalJobDetail externalJob) {
    TaggingResult result = new TaggingResult();
    
    try {
      // Tag company
      CompanyTaggingResult companyResult = tagCompany(externalJob.getCompanyName());
      result.setCompanyId(companyResult.getCompanyId());
      result.setCompanyName(companyResult.getCompanyName());
      result.setCompanyConfidence(companyResult.getConfidence());
      
      // Tag designation
      DesignationTaggingResult designationResult = tagDesignation(externalJob.getJobTitle());
      result.setDesignationId(designationResult.getDesignationId());
      result.setDesignationName(designationResult.getDesignationName());
      result.setDesignationConfidence(designationResult.getConfidence());
      
      log.info("Tagged external job {}: Company={} (ID: {}, Confidence: {}), Designation={} (ID: {}, Confidence: {})",
          externalJob.getId(),
          result.getCompanyName(), result.getCompanyId(), result.getCompanyConfidence(),
          result.getDesignationName(), result.getDesignationId(), result.getDesignationConfidence());
      
    } catch (Exception e) {
      log.error("Error tagging external job {}: {}", externalJob.getId(), e.getMessage(), e);
      result.setError(e.getMessage());
    }
    
    return result;
  }

  /**
   * Tag company name using exact match, like operator, and similar content
   */
  private CompanyTaggingResult tagCompany(String companyName) {
    if (companyName == null || companyName.trim().isEmpty()) {
      return new CompanyTaggingResult();
    }

    String normalizedCompanyName = companyName.trim();

    // 1. Try exact match first
    Optional<Company> exactMatch = companyRepository.findByName(normalizedCompanyName);
    if (exactMatch.isPresent()) {
      Company company = exactMatch.get();
      return new CompanyTaggingResult(company.getId(), company.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarCompanies = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.COMPANY, normalizedCompanyName);
    
    if (!similarCompanies.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarCompanies.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedCompanyName))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Company> company = companyRepository.findByName(bestMatch.getParentName());
        if (company.isPresent()) {
          return new CompanyTaggingResult(
              company.get().getId(), 
              company.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on companies table
    List<Company> likeMatches = companyRepository.findByNameContainingIgnoreCase(normalizedCompanyName);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Company bestMatch = findBestCompanyMatchUsingAI(normalizedCompanyName, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeCompanyMapping(bestMatch.getName(), normalizedCompanyName, 0.85);
        return new CompanyTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try LIKE operator on similar content table
    List<SimilarContent> similarContentMatches = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.COMPANY, normalizedCompanyName);
    
    if (!similarContentMatches.isEmpty()) {
      // Use AI to find the best match from similar content
      SimilarContent bestMatch = findBestSimilarContentMatchUsingAI(
          normalizedCompanyName, similarContentMatches, ContentType.COMPANY);
      
      if (bestMatch != null) {
        Optional<Company> company = companyRepository.findByName(bestMatch.getParentName());
        if (company.isPresent()) {
          return new CompanyTaggingResult(
              company.get().getId(), 
              company.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 5. Final fallback: Use GenAI to find best match from all companies
    CompanyTaggingResult aiResult = findBestCompanyUsingGenAI(normalizedCompanyName);
    if (aiResult.getCompanyId() != null) {
      // Store this mapping in similar content for future use
      storeCompanyMapping(aiResult.getCompanyName(), normalizedCompanyName, aiResult.getConfidence());
      return aiResult;
    }

    return new CompanyTaggingResult();
  }

  /**
   * Tag designation using exact match, like operator, and similar content
   */
  private DesignationTaggingResult tagDesignation(String jobTitle) {
    if (jobTitle == null || jobTitle.trim().isEmpty()) {
      return new DesignationTaggingResult();
    }

    String normalizedJobTitle = jobTitle.trim();

    // 1. Try exact match first
    Optional<Designation> exactMatch = designationRepository.findByName(normalizedJobTitle);
    if (exactMatch.isPresent()) {
      Designation designation = exactMatch.get();
      return new DesignationTaggingResult(designation.getId(), designation.getName(), 1.0);
    }

    // 2. Try similar content table
    List<SimilarContent> similarDesignations = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.DESIGNATION, normalizedJobTitle);
    
    if (!similarDesignations.isEmpty()) {
      // Find the best match by confidence score
      SimilarContent bestMatch = similarDesignations.stream()
          .filter(sc -> sc.getSimilarName().equalsIgnoreCase(normalizedJobTitle))
          .max((a, b) -> a.getConfidenceScore().compareTo(b.getConfidenceScore()))
          .orElse(null);
      
      if (bestMatch != null) {
        Optional<Designation> designation = designationRepository.findByName(bestMatch.getParentName());
        if (designation.isPresent()) {
          return new DesignationTaggingResult(
              designation.get().getId(), 
              designation.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on designations table
    List<Designation> likeMatches = designationRepository.findByNameContainingIgnoreCase(normalizedJobTitle);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Designation bestMatch = findBestDesignationMatchUsingAI(normalizedJobTitle, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeDesignationMapping(bestMatch.getName(), normalizedJobTitle, 0.85);
        return new DesignationTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try LIKE operator on similar content table
    List<SimilarContent> similarContentMatches = similarContentRepository.findByTypeAndSearchTerm(
        ContentType.DESIGNATION, normalizedJobTitle);
    
    if (!similarContentMatches.isEmpty()) {
      // Use AI to find the best match from similar content
      SimilarContent bestMatch = findBestSimilarContentMatchUsingAI(
          normalizedJobTitle, similarContentMatches, ContentType.DESIGNATION);
      
      if (bestMatch != null) {
        Optional<Designation> designation = designationRepository.findByName(bestMatch.getParentName());
        if (designation.isPresent()) {
          return new DesignationTaggingResult(
              designation.get().getId(), 
              designation.get().getName(), 
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 5. Final fallback: Use GenAI to find best match from all designations
    DesignationTaggingResult aiResult = findBestDesignationUsingGenAI(normalizedJobTitle);
    if (aiResult.getDesignationId() != null) {
      // Store this mapping in similar content for future use
      storeDesignationMapping(aiResult.getDesignationName(), normalizedJobTitle, aiResult.getConfidence());
      return aiResult;
    }

    return new DesignationTaggingResult();
  }

  /**
   * Use AI to find the best company match
   */
  private Company findBestCompanyMatchUsingAI(String searchTerm, List<Company> candidates) {
    try {
      // Simple string similarity matching instead of AI
      Company bestMatch = findBestCompanyMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for company '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use AI to find the best designation match
   */
  private Designation findBestDesignationMatchUsingAI(String searchTerm, List<Designation> candidates) {
    try {
      // Simple string similarity matching instead of AI
      Designation bestMatch = findBestDesignationMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for designation '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use AI to find the best similar content match
   */
  private SimilarContent findBestSimilarContentMatchUsingAI(String searchTerm, List<SimilarContent> candidates, ContentType type) {
    try {
      // Simple string similarity matching instead of AI
      SimilarContent bestMatch = findBestSimilarContentMatchBySimilarity(searchTerm, candidates);
      if (bestMatch != null) {
        return bestMatch;
      }
    } catch (Exception e) {
      log.warn("AI matching failed for similar content '{}': {}", searchTerm, e.getMessage());
    }
    
    // Fallback: return the first candidate if AI fails
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  /**
   * Use GenAI to find best company match from all companies in the database
   */
  private CompanyTaggingResult findBestCompanyUsingGenAI(String companyName) {
    try {
      // Get all companies from the cached DropdownService instead of database
      List<com.tymbl.jobs.entity.Company> allCompanies = dropdownService.getAllCompanies();
      if (allCompanies.isEmpty()) {
        log.warn("No companies found in cache for GenAI matching");
        return new CompanyTaggingResult();
      }

      // Create a prompt for GenAI
      String prompt = createCompanyMatchingPrompt(companyName, allCompanies);
      
      // Call GenAI service
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Parse AI response to find the best match
        com.tymbl.jobs.entity.Company bestMatch = parseGenAICompanyResponse(companyName, allCompanies, aiResponse);
        if (bestMatch != null) {
          double confidence = calculateStringSimilarity(companyName.toLowerCase(), bestMatch.getName().toLowerCase());
          return new CompanyTaggingResult(bestMatch.getId(), bestMatch.getName(), confidence);
        }
      }
      
    } catch (Exception e) {
      log.warn("GenAI matching failed for company '{}': {}", companyName, e.getMessage());
    }
    
    return new CompanyTaggingResult();
  }

  /**
   * Use GenAI to find best designation match from all designations in the database
   */
  private DesignationTaggingResult findBestDesignationUsingGenAI(String jobTitle) {
    try {
      // Get all designations from the cached DropdownService instead of database
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      if (allDesignations.isEmpty()) {
        log.warn("No designations found in cache for GenAI matching");
        return new DesignationTaggingResult();
      }

      // Create a prompt for GenAI
      String prompt = createDesignationMatchingPrompt(jobTitle, allDesignations);
      
      // Call GenAI service
      String aiResponse = callGenAIService(prompt);
      
      if (aiResponse != null && !aiResponse.trim().isEmpty()) {
        // Parse AI response to find the best match
        Designation bestMatch = parseGenAIDesignationResponse(jobTitle, allDesignations, aiResponse);
        if (bestMatch != null) {
          double confidence = calculateStringSimilarity(jobTitle.toLowerCase(), bestMatch.getName().toLowerCase());
          return new DesignationTaggingResult(bestMatch.getId(), bestMatch.getName(), confidence);
        }
      }
      
    } catch (Exception e) {
      log.warn("GenAI matching failed for designation '{}': {}", jobTitle, e.getMessage());
    }
    
    return new DesignationTaggingResult();
  }

  /**
   * Find best company match using string similarity
   */
  private Company findBestCompanyMatchBySimilarity(String searchTerm, List<Company> candidates) {
    if (candidates.isEmpty()) return null;
    
    Company bestMatch = null;
    double bestScore = 0.0;
    
    for (Company company : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), company.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = company;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best designation match using string similarity
   */
  private Designation findBestDesignationMatchBySimilarity(String searchTerm, List<Designation> candidates) {
    if (candidates.isEmpty()) return null;
    
    Designation bestMatch = null;
    double bestScore = 0.0;
    
    for (Designation designation : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), designation.getName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = designation;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Find best similar content match using string similarity
   */
  private SimilarContent findBestSimilarContentMatchBySimilarity(String searchTerm, List<SimilarContent> candidates) {
    if (candidates.isEmpty()) return null;
    
    SimilarContent bestMatch = null;
    double bestScore = 0.0;
    
    for (SimilarContent content : candidates) {
      double score = calculateStringSimilarity(searchTerm.toLowerCase(), content.getSimilarName().toLowerCase());
      if (score > bestScore) {
        bestScore = score;
        bestMatch = content;
      }
    }
    
    // Only return if similarity is above threshold
    return bestScore > 0.6 ? bestMatch : null;
  }

  /**
   * Calculate string similarity using Levenshtein distance
   */
  private double calculateStringSimilarity(String s1, String s2) {
    if (s1.equals(s2)) return 1.0;
    
    int distance = calculateLevenshteinDistance(s1, s2);
    int maxLength = Math.max(s1.length(), s2.length());
    
    return maxLength == 0 ? 1.0 : (1.0 - (double) distance / maxLength);
  }

  /**
   * Calculate Levenshtein distance between two strings
   */
  private int calculateLevenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];
    
    for (int i = 0; i <= s1.length(); i++) {
      dp[i][0] = i;
    }
    
    for (int j = 0; j <= s2.length(); j++) {
      dp[0][j] = j;
    }
    
    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j], Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
        }
      }
    }
    
    return dp[s1.length()][s2.length()];
  }

  /**
   * Store company mapping in similar content table
   */
  private void storeCompanyMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.COMPANY)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.COMPANY);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new company mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Store designation mapping in similar content table
   */
  private void storeDesignationMapping(String parentName, String similarName, double confidence) {
    if (!similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, ContentType.DESIGNATION)) {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setType(ContentType.DESIGNATION);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setSource("AI_MAPPING");
      
      similarContentRepository.save(similarContent);
      log.info("Stored new designation mapping: '{}' -> '{}' (confidence: {})", similarName, parentName, confidence);
    }
  }

  /**
   * Create a prompt for GenAI to match company names
   */
  private String createCompanyMatchingPrompt(String companyName, List<com.tymbl.jobs.entity.Company> companies) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the company name '").append(companyName).append("', ");
    prompt.append("find the BEST MATCHING company from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. **Exact Matches**: Perfect name matches\n");
    prompt.append("2. **Parent Company**: If the input is a subsidiary, division, or regional office, find the parent company\n");
    prompt.append("   Examples: 'Walmart Global Tech India' → 'Walmart', 'Microsoft India' → 'Microsoft'\n");
    prompt.append("3. **Abbreviations**: Official names vs. common abbreviations\n");
    prompt.append("   Examples: 'IBM' vs 'International Business Machines', 'HP' vs 'Hewlett-Packard'\n");
    prompt.append("4. **Legal vs. Trading Names**: Company's legal name vs. how it's commonly known\n");
    prompt.append("5. **Mergers & Acquisitions**: Company names that changed due to business events\n");
    prompt.append("6. **Regional Variations**: Same company with different names in different regions\n\n");
    prompt.append("IMPORTANT GUIDELINES:\n");
    prompt.append("- If the input contains the main company name (e.g., 'Walmart' in 'Walmart Global Tech India'), return that company\n");
    prompt.append("- If it's clearly a subsidiary/division of a known company, return the parent company\n");
    prompt.append("- If no clear match exists, respond with 'NO_MATCH'\n");
    prompt.append("- Don't return companies that are just similar in the same industry\n\n");
    prompt.append("Available companies:\n");
    
    int counter = 1;
    for (com.tymbl.jobs.entity.Company company : companies) {
      prompt.append(counter++).append(". ").append(company.getName()).append("\n");
    }
    
    prompt.append("\nRESPONSE INSTRUCTIONS:\n");
    prompt.append("- Return ONLY the exact company name from the numbered list above\n");
    prompt.append("- If no suitable match is found, respond with exactly 'NO_MATCH' (no quotes, no extra text)\n");
    prompt.append("- Do not include any explanations, reasoning, or additional text\n");
    prompt.append("- Do not include the number from the list, only the company name\n");
    prompt.append("- Examples of valid responses: 'Microsoft', 'Walmart', 'NO_MATCH'\n");
    
    return prompt.toString();
  }

  /**
   * Create a prompt for GenAI to match designation names
   */
  private String createDesignationMatchingPrompt(String jobTitle, List<Designation> designations) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Given the job title '").append(jobTitle).append("', ");
    prompt.append("find the BEST MATCHING designation from the list below. ");
    prompt.append("Consider the following matching scenarios:\n\n");
    prompt.append("1. **Exact Matches**: Perfect title matches\n");
    prompt.append("2. **Core Role Variations**: Same job role with different titles\n");
    prompt.append("   Examples: 'Software Engineer' vs 'Software Developer', 'Programmer' vs 'Developer'\n");
    prompt.append("3. **Seniority Levels**: Different levels of the same role\n");
    prompt.append("   Examples: 'Senior Developer' vs 'Developer', 'Lead Engineer' vs 'Engineer'\n");
    prompt.append("4. **Industry Standards**: Official titles vs. company-specific variations\n");
    prompt.append("5. **Regional Variations**: Same role with different names in different regions\n");
    prompt.append("6. **Modern vs. Traditional**: Contemporary titles vs. legacy titles\n\n");
    prompt.append("IMPORTANT GUIDELINES:\n");
    prompt.append("- If the input contains the core role (e.g., 'Developer' in 'Senior Full Stack Developer'), return the closest match\n");
    prompt.append("- Consider that 'Software Engineer' and 'Software Developer' are essentially the same role\n");
    prompt.append("- Seniority differences (Junior/Senior/Lead) should still match the base role\n");
    prompt.append("- If no clear match exists, respond with 'NO_MATCH'\n");
    prompt.append("- Don't return designations that are just similar roles in the same field\n\n");
    prompt.append("Available designations:\n");
    
    int counter = 1;
    for (Designation designation : designations) {
      prompt.append(counter++).append(". ").append(designation.getName()).append("\n");
    }
    
    prompt.append("\nRESPONSE INSTRUCTIONS:\n");
    prompt.append("- Return ONLY the exact designation name from the numbered list above\n");
    prompt.append("- If no suitable match is found, respond with exactly 'NO_MATCH' (no quotes, no extra text)\n");
    prompt.append("- Do not include any explanations, reasoning, or additional text\n");
    prompt.append("- Do not include the number from the list, only the designation name\n");
    prompt.append("- Examples of valid responses: 'Software Engineer', 'Developer', 'NO_MATCH'\n");
    
    return prompt.toString();
  }

  /**
   * Call GenAI service to get response
   */
  private String callGenAIService(String prompt) {
    try {
      // Use AIRestService to call Gemini API
      if (aiRestService != null) {
        // Build request body using AIRestService
        Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);
        
        // Call Gemini API
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Company/Designation Matching");
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(), response.getBody());
        }
      }
      
      // Fallback: Use string similarity as a placeholder for GenAI
      log.info("Using string similarity as GenAI fallback for prompt: {}", prompt.substring(0, Math.min(100, prompt.length())));
      return "AI_SERVICE_UNAVAILABLE";
      
    } catch (Exception e) {
      log.warn("Failed to call GenAI service: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Extract text from Gemini API response
   */
  private String extractTextFromGeminiResponse(String responseBody) {
    try {
      // Parse JSON response
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode responseNode = objectMapper.readTree(responseBody);
      
      // Navigate to the text content
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.debug("Extracted text from Gemini response: {}", generatedText);
            return generatedText;
          }
        }
      }
      
      log.warn("Unexpected Gemini API response structure: {}", responseBody);
      return null;
      
    } catch (Exception e) {
      log.warn("Failed to parse Gemini response: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Parse GenAI response to find the best company match
   */
  private com.tymbl.jobs.entity.Company parseGenAICompanyResponse(String companyName, List<com.tymbl.jobs.entity.Company> companies, String aiResponse) {
    if (aiResponse == null || aiResponse.trim().isEmpty() || "AI_SERVICE_UNAVAILABLE".equals(aiResponse)) {
      // Fallback to string similarity when AI is not available
      return findBestCompanyMatchBySimilarity(companyName, companies);
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }

    for (com.tymbl.jobs.entity.Company company : companies) {
      if (company.getName().equalsIgnoreCase(normalizedResponse)) {
        return company;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestCompanyMatchBySimilarity(companyName, companies);
  }

  /**
   * Parse GenAI response to find the best designation match
   */
  private Designation parseGenAIDesignationResponse(String jobTitle, List<Designation> designations, String aiResponse) {
    if (aiResponse == null || aiResponse.trim().isEmpty() || "AI_SERVICE_UNAVAILABLE".equals(aiResponse)) {
      // Fallback to string similarity when AI is not available
      return findBestDesignationMatchBySimilarity(jobTitle, designations);
    }
    
    // Try to find exact match from AI response
    String normalizedResponse = aiResponse.trim();
    if ("NO_MATCH".equalsIgnoreCase(normalizedResponse)) {
      return null; // No exact match found
    }
    
    for (Designation designation : designations) {
      if (designation.getName().equalsIgnoreCase(normalizedResponse)) {
        return designation;
      }
    }
    
    // If no exact match, fallback to string similarity
    return findBestDesignationMatchBySimilarity(jobTitle, designations);
  }

  // Result classes
  public static class TaggingResult {
    private Long companyId;
    private String companyName;
    private Double companyConfidence;
    private Long designationId;
    private String designationName;
    private Double designationConfidence;
    private String error;

    // Getters and setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getCompanyConfidence() { return companyConfidence; }
    public void setCompanyConfidence(Double companyConfidence) { this.companyConfidence = companyConfidence; }
    
    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }
    
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    
    public Double getDesignationConfidence() { return designationConfidence; }
    public void setDesignationConfidence(Double designationConfidence) { this.designationConfidence = designationConfidence; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
  }

  public static class CompanyTaggingResult {
    private Long companyId;
    private String companyName;
    private Double confidence;

    public CompanyTaggingResult() {}

    public CompanyTaggingResult(Long companyId, String companyName, Double confidence) {
      this.companyId = companyId;
      this.companyName = companyName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }

  public static class DesignationTaggingResult {
    private Long designationId;
    private String designationName;
    private Double confidence;

    public DesignationTaggingResult() {}

    public DesignationTaggingResult(Long designationId, String designationName, Double confidence) {
      this.designationId = designationId;
      this.designationName = designationName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }
    
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
  }
}
