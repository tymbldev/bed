package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.PendingContent;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.PendingContentRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import com.tymbl.common.service.DropdownService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DesignationTaggerService {

  private final DesignationRepository designationRepository;
  private final SimilarContentRepository similarContentRepository;
  private final PendingContentRepository pendingContentRepository;
  private final AIRestService aiRestService;
  private final ObjectMapper objectMapper;
  private final DropdownService dropdownService;

  /**
   * Tag designation using exact match, like operator, and similar content
   */
  @Transactional
  public DesignationTaggingResult tagDesignation(String jobTitle, Long sourceId,
      String portalName) {
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
        Optional<Designation> designation = designationRepository.findByName(
            bestMatch.getParentName());
        if (designation.isPresent()) {
          // Store additional synonyms found during the search
          for (SimilarContent sc : similarDesignations) {
            if (!sc.getSimilarName().equalsIgnoreCase(normalizedJobTitle)) {
              storeSynonymMapping(ContentType.DESIGNATION, bestMatch.getParentName(), 
                  sc.getSimilarName(), sc.getConfidenceScore().doubleValue());
            }
          }
          
          return new DesignationTaggingResult(
              designation.get().getId(),
              designation.get().getName(),
              bestMatch.getConfidenceScore().doubleValue());
        }
      }
    }

    // 3. Try LIKE operator on designations table
    List<Designation> likeMatches = designationRepository.findByNameContainingIgnoreCase(
        normalizedJobTitle);
    if (!likeMatches.isEmpty()) {
      // Use AI to find the best match
      Designation bestMatch = findBestDesignationMatchUsingAI(normalizedJobTitle, likeMatches);
      if (bestMatch != null) {
        // Store this mapping in similar content for future use
        storeDesignationMapping(bestMatch.getName(), normalizedJobTitle, 0.85);
        
        // Store synonyms for other similar matches found
        for (Designation match : likeMatches) {
          if (!match.getName().equalsIgnoreCase(bestMatch.getName())) {
            storeSynonymMapping(ContentType.DESIGNATION, bestMatch.getName(), 
                match.getName(), 0.75);
          }
        }
        
        return new DesignationTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try GenAI with all designations as fallback
    Designation genAIMatch = findBestDesignationUsingGenAI(normalizedJobTitle);
    if (genAIMatch != null) {
      storeDesignationMapping(genAIMatch.getName(), normalizedJobTitle, 0.85);
      
      // Store additional synonyms from the token matching process
      storeAdditionalSynonymsFromGenAI(normalizedJobTitle, genAIMatch);
      
      return new DesignationTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Log NO_MATCH case to PendingContent for future analysis
    logPendingContent(normalizedJobTitle, sourceId, portalName, 
        "No match found after all tagging strategies (exact, similar content, LIKE, GenAI)");

    return new DesignationTaggingResult();
  }

  /**
   * Find best designation match using AI
   */
  private Designation findBestDesignationMatchUsingAI(String jobTitle,
      List<Designation> designations) {
    try {
      StringBuilder prompt = new StringBuilder();
      prompt.append(
          "You are a job title functional similarity expert. Your task is to identify designations that are FUNCTIONALLY SIMILAR, not just containing similar words.\n\n");
      prompt.append("CRITICAL RULES:\n");
      prompt.append("1. FUNCTIONAL SIMILARITY ONLY: Match only if the roles have the same core job function/responsibility\n");
      prompt.append("2. AVOID WORD-BASED MATCHING: Do NOT match based on shared words if the functions are different\n");
      prompt.append("3. EXAMPLES OF FUNCTIONAL SIMILARITY:\n");
      prompt.append("   - 'Software Developer' = 'C++ Python Developer' (both develop software)\n");
      prompt.append("   - 'Software Developer' = 'Lead Python Developer' (both develop software, different seniority)\n");
      prompt.append("   - 'Software Developer' = 'Software Engineer III' (both develop software, different seniority)\n");
      prompt.append("   - 'Database Administrator' = 'SQL Server Administrator' (both manage databases)\n");
      prompt.append("   - 'Project Manager' = 'Senior Project Manager' (both manage projects, different seniority)\n");
      prompt.append("4. EXAMPLES OF NON-SIMILAR (DIFFERENT FUNCTIONS):\n");
      prompt.append("   - 'Cloud Database Administrator' ≠ 'Dialer Administrator' (database vs telephony systems)\n");
      prompt.append("   - 'Change Management Consultant' ≠ 'Dialer Management' (business process vs telephony)\n");
      prompt.append("   - 'Software Developer' ≠ 'Database Administrator' (development vs administration)\n");
      prompt.append("   - 'Project Manager' ≠ 'Software Developer' (management vs development)\n");
      prompt.append("   - 'Marketing Manager' ≠ 'Sales Manager' (marketing vs sales functions)\n\n");
      prompt.append("Input job title: '").append(jobTitle).append("'\n\n");
      prompt.append("Available designations:\n");

      for (int i = 0; i < designations.size(); i++) {
        Designation designation = designations.get(i);
        prompt.append(i + 1).append(". ").append(designation.getName()).append("\n");
      }

      prompt.append(
          "\nCRITICAL: Only match if the designations have the SAME CORE JOB FUNCTION.\n");
      prompt.append("Consider the primary responsibility, not just shared keywords.\n");
      prompt.append("Respond with the EXACT designation name from the list above, or 'NO_MATCH' if none are functionally similar.\n");
      prompt.append("Only respond with the exact designation name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());

      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedDesignationName = aiResponse.trim();

        for (Designation designation : designations) {
          if (designation.getName().equalsIgnoreCase(selectedDesignationName)) {
            return designation;
          }
        }
      }

    } catch (Exception e) {
      log.warn("Error in AI designation matching: {}", e.getMessage());
    }

    return null;
  }

  /**
   * Find best designation using GenAI with tokenized matching
   */
  private Designation findBestDesignationUsingGenAI(String jobTitle) {
    try {
      // Get all designations from dropdown service (cached and optimized)
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      
      // Tokenize the input job title
      List<String> tokens = tokenizeJobTitle(jobTitle);
      log.info("Tokenized job title '{}' into tokens: {}", jobTitle, tokens);
      
      // Filter designations that contain at least one token (case-insensitive)
      List<Designation> matchedDesignations = allDesignations.stream()
          .filter(designation -> containsAnyToken(designation.getName(), tokens))
          .collect(Collectors.toList());
      
      log.info("Found {} designations matching tokens from '{}'", matchedDesignations.size(), jobTitle);
      
      // Log some examples of matches for infoging
      if (!matchedDesignations.isEmpty()) {
        List<String> exampleMatches = matchedDesignations.stream()
            .limit(5)
            .map(Designation::getName)
            .collect(Collectors.toList());
        log.info("Example matches for '{}': {}", jobTitle, exampleMatches);
      }
      
      // If no matches found, return null
      if (matchedDesignations.isEmpty()) {
        log.info("No designations found matching any tokens from '{}'", jobTitle);
        return null;
      }
      
      // If only one match, return it directly
      if (matchedDesignations.size() == 1) {
        log.info("Single match found for '{}': {}", jobTitle, matchedDesignations.get(0).getName());
        return matchedDesignations.get(0);
      }

      // Use GenAI to find the best match from filtered designations
      StringBuilder prompt = new StringBuilder();
      prompt.append(
          "You are a job title functional similarity expert. Your task is to identify designations that are FUNCTIONALLY SIMILAR, not just containing similar words.\n\n");
      prompt.append("CRITICAL RULES:\n");
      prompt.append("1. FUNCTIONAL SIMILARITY ONLY: Match only if the roles have the same core job function/responsibility\n");
      prompt.append("2. AVOID WORD-BASED MATCHING: Do NOT match based on shared words if the functions are different\n");
      prompt.append("3. EXAMPLES OF FUNCTIONAL SIMILARITY:\n");
      prompt.append("   - 'Software Developer' = 'C++ Python Developer' (both develop software)\n");
      prompt.append("   - 'Software Developer' = 'Lead Python Developer' (both develop software, different seniority)\n");
      prompt.append("   - 'Software Developer' = 'Software Engineer III' (both develop software, different seniority)\n");
      prompt.append("   - 'Database Administrator' = 'SQL Server Administrator' (both manage databases)\n");
      prompt.append("   - 'Project Manager' = 'Senior Project Manager' (both manage projects, different seniority)\n");
      prompt.append("   - 'Data Analyst' = 'Business Intelligence Analyst' (both analyze data)\n");
      prompt.append("4. EXAMPLES OF NON-SIMILAR (DIFFERENT FUNCTIONS):\n");
      prompt.append("   - 'Cloud Database Administrator' ≠ 'Dialer Administrator' (database vs telephony systems)\n");
      prompt.append("   - 'Change Management Consultant' ≠ 'Dialer Management' (business process vs telephony)\n");
      prompt.append("   - 'Software Developer' ≠ 'Database Administrator' (development vs administration)\n");
      prompt.append("   - 'Project Manager' ≠ 'Software Developer' (management vs development)\n");
      prompt.append("   - 'Marketing Manager' ≠ 'Sales Manager' (marketing vs sales functions)\n");
      prompt.append("   - 'Network Administrator' ≠ 'System Administrator' (network vs system management)\n\n");
      prompt.append("Input job title: '").append(jobTitle).append("'\n\n");
      prompt.append("Available designations (filtered by token matching):\n");

      for (int i = 0; i < matchedDesignations.size(); i++) {
        Designation designation = matchedDesignations.get(i);
        prompt.append(i + 1).append(". ").append(designation.getName()).append("\n");
      }

      prompt.append(
          "\nCRITICAL: Only match if the designations have the SAME CORE JOB FUNCTION.\n");
      prompt.append("Consider the primary responsibility and work domain, not just shared keywords.\n");
      prompt.append("Respond with the EXACT designation name from the list above, or 'NO_MATCH' if none are functionally similar.\n");
      prompt.append("Only respond with the exact designation name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());

      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedDesignationName = aiResponse.trim();

        for (Designation designation : matchedDesignations) {
          if (designation.getName().equalsIgnoreCase(selectedDesignationName)) {
            log.info("GenAI selected designation '{}' for job title '{}'", designation.getName(), jobTitle);
            return designation;
          }
        }
      }
      
      // Fallback: If GenAI returns NO_MATCH or fails, return the designation with the most token matches
      log.info("GenAI returned NO_MATCH or failed, using fallback token-based selection for '{}'", jobTitle);
      return findBestDesignationByTokenCount(jobTitle, matchedDesignations, tokens);

    } catch (Exception e) {
      log.warn("Error in GenAI designation matching: {}", e.getMessage());
    }

    return null;
  }

  /**
   * Tokenize job title by splitting on spaces, commas, and other delimiters
   */
  private List<String> tokenizeJobTitle(String jobTitle) {
    if (jobTitle == null || jobTitle.trim().isEmpty()) {
      return java.util.Collections.emptyList();
    }
    
    // Split on spaces, commas, hyphens, forward slashes, and other common delimiters
    String[] tokens = jobTitle.trim()
        .split("[\\s,\\-/&+()]+");
    
    // Filter out empty tokens and convert to lowercase for case-insensitive matching
    return java.util.Arrays.stream(tokens)
        .filter(token -> !token.trim().isEmpty())
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toList());
  }

  /**
   * Check if a designation name contains any of the given tokens (case-insensitive)
   * Improved matching to handle word boundaries and partial matches
   */
  private boolean containsAnyToken(String designationName, List<String> tokens) {
    if (designationName == null || designationName.trim().isEmpty() || tokens.isEmpty()) {
      return false;
    }
    
    String lowerDesignationName = designationName.toLowerCase();
    
    return tokens.stream()
        .anyMatch(token -> {
          // Check for exact word match or substring match
          String lowerToken = token.toLowerCase();
          
          // Direct substring match
          if (lowerDesignationName.contains(lowerToken)) {
            return true;
          }
          
          // Check for word boundary matches (e.g., "engineer" in "software engineer")
          String[] designationWords = lowerDesignationName.split("[\\s\\-]+");
          for (String word : designationWords) {
            if (word.equals(lowerToken) || word.startsWith(lowerToken) || word.endsWith(lowerToken)) {
              return true;
            }
          }
          
          return false;
        });
  }

  /**
   * Find the best designation by counting token matches as a fallback
   */
  private Designation findBestDesignationByTokenCount(String jobTitle, List<Designation> matchedDesignations, List<String> tokens) {
    if (matchedDesignations.isEmpty()) {
      return null;
    }
    
    // Score each designation by how many tokens it contains
    Designation bestDesignation = null;
    int bestScore = 0;
    
    for (Designation designation : matchedDesignations) {
      int score = countTokenMatches(designation.getName(), tokens);
      if (score > bestScore) {
        bestScore = score;
        bestDesignation = designation;
      }
    }
    
    if (bestDesignation != null) {
      log.info("Fallback selected designation '{}' with {} token matches for '{}'", 
          bestDesignation.getName(), bestScore, jobTitle);
    }
    
    return bestDesignation;
  }

  /**
   * Count how many tokens from the job title are found in the designation name
   */
  private int countTokenMatches(String designationName, List<String> tokens) {
    if (designationName == null || designationName.trim().isEmpty() || tokens.isEmpty()) {
      return 0;
    }
    
    String lowerDesignationName = designationName.toLowerCase();
    int matchCount = 0;
    
    for (String token : tokens) {
      String lowerToken = token.toLowerCase();
      
      // Direct substring match
      if (lowerDesignationName.contains(lowerToken)) {
        matchCount++;
        continue;
      }
      
      // Check for word boundary matches
      String[] designationWords = lowerDesignationName.split("[\\s\\-]+");
      for (String word : designationWords) {
        if (word.equals(lowerToken) || word.startsWith(lowerToken) || word.endsWith(lowerToken)) {
          matchCount++;
          break; // Count each token only once per designation
        }
      }
    }
    
    return matchCount;
  }

  /**
   * Generic method to store synonyms in similar content table
   */
  private void storeSynonymMapping(ContentType contentType, String parentName, String similarName,
      double confidence) {
    try {
      // Check if mapping already exists to avoid duplicates
      if (similarContentRepository.existsByParentNameAndSimilarNameAndType(parentName, similarName, contentType)) {
        log.debug("Synonym mapping already exists: '{}' -> '{}' for type {}", similarName, parentName, contentType);
        return;
      }

      SimilarContent similarContent = new SimilarContent();
      similarContent.setType(contentType);
      similarContent.setParentName(parentName);
      similarContent.setSimilarName(similarName);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setProcessed(true);

      similarContentRepository.save(similarContent);
      log.info("Stored synonym mapping: '{}' -> '{}' with confidence {} for type {}",
          similarName, parentName, confidence, contentType);

    } catch (Exception e) {
      log.warn("Failed to store synonym mapping: {}", e.getMessage());
    }
  }

  /**
   * Store designation mapping in similar content table (legacy method for backward compatibility)
   */
  private void storeDesignationMapping(String parentDesignationName, String similarDesignationName,
      double confidence) {
    storeSynonymMapping(ContentType.DESIGNATION, parentDesignationName, similarDesignationName, confidence);
  }

  /**
   * Log untagged designation to PendingContent for future analysis
   */
  private void logPendingContent(String entityName, Long sourceId, String portalName, String notes) {
    try {
      // Check if this entity name is already in pending content to avoid duplicates (regardless of source)
      if (pendingContentRepository.existsByEntityNameAndEntityType(
          entityName, PendingContent.EntityType.DESIGNATION)) {
        log.debug("Pending content already exists for designation: '{}' (entity name already logged)", entityName);
        return;
      }

      PendingContent pendingContent = PendingContent.builder()
          .entityName(entityName)
          .entityType(PendingContent.EntityType.DESIGNATION)
          .sourceTable("external_jobs") // Assuming this comes from external jobs
          .sourceId(sourceId)
          .portalName(portalName)
          .notes(notes)
          .build();

      pendingContentRepository.save(pendingContent);
      log.info("Logged untagged designation to PendingContent: '{}' (sourceId: {}, portal: {})", 
          entityName, sourceId, portalName);

    } catch (Exception e) {
      log.warn("Failed to log pending content for designation '{}': {}", entityName, e.getMessage());
    }
  }

  /**
   * Store additional synonyms found during GenAI token matching process
   */
  private void storeAdditionalSynonymsFromGenAI(String jobTitle, Designation selectedDesignation) {
    try {
      // Get all designations and find token matches
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      List<String> tokens = tokenizeJobTitle(jobTitle);
      
      // Find other designations that match the tokens
      List<Designation> tokenMatches = allDesignations.stream()
          .filter(designation -> containsAnyToken(designation.getName(), tokens))
          .filter(designation -> !designation.getName().equalsIgnoreCase(selectedDesignation.getName()))
          .collect(Collectors.toList());
      
      // Store synonyms for the top token matches
      for (Designation match : tokenMatches.stream().limit(5).collect(Collectors.toList())) {
        int tokenCount = countTokenMatches(match.getName(), tokens);
        double confidence = Math.min(0.8, 0.6 + (tokenCount * 0.1)); // Higher confidence for more token matches
        
        storeSynonymMapping(ContentType.DESIGNATION, selectedDesignation.getName(), 
            match.getName(), confidence);
      }
      
      log.info("Stored {} additional synonyms for '{}' -> '{}'", 
          Math.min(5, tokenMatches.size()), jobTitle, selectedDesignation.getName());
      
    } catch (Exception e) {
      log.warn("Failed to store additional synonyms from GenAI: {}", e.getMessage());
    }
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
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
            "Designation Matching");

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(),
              response.getBody());
        }
      }

      // Fallback: Return null if AI service is not available
      log.info("AI service not available for designation matching");
      return null;

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
      JsonNode responseNode = objectMapper.readTree(responseBody);

      // Navigate to the text content
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.info("Extracted text from Gemini response: {}", generatedText);
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
   * Result class for designation tagging
   */
  public static class DesignationTaggingResult {

    private Long designationId;
    private String designationName;
    private Double confidence = 0.0;
    private String error;

    // Constructors
    public DesignationTaggingResult() {
    }

    public DesignationTaggingResult(Long designationId, String designationName, Double confidence) {
      this.designationId = designationId;
      this.designationName = designationName;
      this.confidence = confidence;
    }

    // Getters and setters
    public Long getDesignationId() {
      return designationId;
    }

    public void setDesignationId(Long designationId) {
      this.designationId = designationId;
    }

    public String getDesignationName() {
      return designationName;
    }

    public void setDesignationName(String designationName) {
      this.designationName = designationName;
    }

    public Double getConfidence() {
      return confidence;
    }

    public void setConfidence(Double confidence) {
      this.confidence = confidence;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }
  }
}
