package com.tymbl.jobs.service;

import com.tymbl.common.entity.PendingContent;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.PendingContentRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.ArrayList;
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
public class CompanyTaggerService {

  private final CompanyRepository companyRepository;
  private final SimilarContentRepository similarContentRepository;
  private final PendingContentRepository pendingContentRepository;
  private final AIRestService aiRestService;

  /**
   * Tag company name using exact match, like operator, and similar content
   */
  @Transactional
  public CompanyTaggingResult tagCompany(String companyName, Long sourceId, String portalName) {
    CompanyTaggingResult result = new CompanyTaggingResult();

    if (companyName == null || companyName.trim().isEmpty()) {
      return result;
    }

    try {
      // Try exact match first
      Optional<Company> exactMatch = companyRepository.findByNameIgnoreCase(companyName.trim());
      if (exactMatch.isPresent()) {
        Company company = exactMatch.get();
        result.setCompanyId(company.getId());
        result.setCompanyName(company.getName());
        result.setConfidence(1.0);
        log.info("Exact company match found: '{}' -> ID: {}", companyName, company.getId());
        return result;
      }

      // Try partial match with LIKE operator
      List<Company> likeMatches = companyRepository.findByNameContainingIgnoreCase(
          companyName.trim());
      if (!likeMatches.isEmpty()) {
        Company bestMatch = findBestCompanyMatchBySimilarity(companyName, likeMatches);
        if (bestMatch != null) {
          result.setCompanyId(bestMatch.getId());
          result.setCompanyName(bestMatch.getName());
          result.setConfidence(0.8);
          log.info("Partial company match found: '{}' -> '{}' (ID: {})", companyName,
              bestMatch.getName(), bestMatch.getId());
          return result;
        }
      }

      // Try similar content matching
      List<SimilarContent> similarContents = similarContentRepository.findByTypeAndSearchTerm(
          ContentType.COMPANY, companyName);

      if (!similarContents.isEmpty()) {
        // Extract company names from similar content (using parentName since entityId is null)
        List<String> companyNames = similarContents.stream()
            .map(SimilarContent::getParentName)
            .distinct()
            .collect(Collectors.toList());

        if (!companyNames.isEmpty()) {
          // Fetch companies by names
          List<Company> similarMatches = companyRepository.findByNameIn(companyNames);
          if (!similarMatches.isEmpty()) {
            Company bestMatch = findBestCompanyMatchBySimilarity(companyName, similarMatches);
            if (bestMatch != null) {
              result.setCompanyId(bestMatch.getId());
              result.setCompanyName(bestMatch.getName());
              result.setConfidence(0.6);
              log.info("Similar content company match found: '{}' -> '{}' (ID: {})", companyName,
                  bestMatch.getName(), bestMatch.getId());
              return result;
            }
          }
        }
      }

      // Try AI-powered matching if available
      Company aiMatch = validateCompanyMatchWithGenAI(companyName,
          likeMatches.isEmpty() ? new ArrayList<>() : likeMatches);
      if (aiMatch != null) {
        result.setCompanyId(aiMatch.getId());
        result.setCompanyName(aiMatch.getName());
        result.setConfidence(0.9);
        log.info("AI-powered company match found: '{}' -> '{}' (ID: {})", companyName,
            aiMatch.getName(), aiMatch.getId());
        return result;
      }

      log.info("No company match found for: '{}'", companyName);
      
      // Log NO_MATCH case to PendingContent for future analysis
      logPendingContent(companyName, sourceId, portalName, 
          "No match found after all tagging strategies (exact, LIKE, similar content, AI)");

    } catch (Exception e) {
      log.error("Error tagging company '{}' for external job {}: {}", companyName, sourceId,
          e.getMessage(), e);
      result.setError(e.getMessage());
      
      // Log error case to PendingContent for future analysis
      logPendingContent(companyName, sourceId, portalName, 
          "Error during tagging: " + e.getMessage());
    }

    return result;
  }

  /**
   * Find companies by similar content using the similar_content table
   */
  private List<Company> findCompaniesBySimilarContent(String companyName) {
    try {
      List<SimilarContent> similarContents = similarContentRepository.findByTypeAndSearchTerm(
          ContentType.COMPANY, companyName);

      if (similarContents.isEmpty()) {
        return new ArrayList<>();
      }

      // Extract company names from similar content (using parentName since entityId is null)
      List<String> companyNames = similarContents.stream()
          .map(SimilarContent::getParentName)
          .distinct()
          .collect(Collectors.toList());

      if (companyNames.isEmpty()) {
        return new ArrayList<>();
      }

      // Fetch companies by names
      return companyRepository.findByNameIn(companyNames);

    } catch (Exception e) {
      log.error("Error finding companies by similar content for: {}", companyName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Find best company match using string similarity
   */
  private Company findBestCompanyMatchBySimilarity(String inputCompanyName,
      List<Company> companies) {
    if (companies.isEmpty()) {
      return null;
    }

    Company bestMatch = null;
    double bestScore = 0.0;

    for (Company company : companies) {
      double score = calculateSimilarityScore(inputCompanyName, company.getName());
      if (score > bestScore && score > 0.7) { // Minimum similarity threshold
        bestScore = score;
        bestMatch = company;
      }
    }

    return bestMatch;
  }

  /**
   * Calculate similarity score between two company names
   */
  private double calculateSimilarityScore(String name1, String name2) {
    if (name1 == null || name2 == null) {
      return 0.0;
    }

    String normalized1 = name1.toLowerCase().replaceAll("[^a-z0-9]", "");
    String normalized2 = name2.toLowerCase().replaceAll("[^a-z0-9]", "");

    if (normalized1.equals(normalized2)) {
      return 1.0;
    }

    if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
      return 0.8;
    }

    // Simple word overlap similarity
    String[] words1 = normalized1.split("\\s+");
    String[] words2 = normalized2.split("\\s+");

    int commonWords = 0;
    for (String word1 : words1) {
      for (String word2 : words2) {
        if (word1.equals(word2) && word1.length() > 2) {
          commonWords++;
        }
      }
    }

    if (words1.length == 0 || words2.length == 0) {
      return 0.0;
    }

    return (double) commonWords / Math.max(words1.length, words2.length);
  }

  /**
   * Validate company match with GenAI by presenting top similarity matches
   */
  private Company validateCompanyMatchWithGenAI(String inputCompanyName, List<Company> topMatches) {
    try {
      if (topMatches == null || topMatches.isEmpty()) {
        return null;
      }

      StringBuilder prompt = new StringBuilder();
      prompt.append(
          "CRITICAL: You are matching company names. Be EXTREMELY strict and accurate.\n\n");
      prompt.append("Input company name: '").append(inputCompanyName).append("'\n\n");
      prompt.append("Available matches (ranked by similarity):\n");

      for (int i = 0; i < topMatches.size(); i++) {
        Company company = topMatches.get(i);
        prompt.append(i + 1).append(". ").append(company.getName()).append("\n");
      }

      prompt.append("\nSTRICT MATCHING RULES:\n");
      prompt.append("1. **EXACT MATCH**: Company names must be essentially the same\n");
      prompt.append(
          "2. **SUBSIDIARY**: Only if clearly a subsidiary (e.g., 'Microsoft India' → 'Microsoft')\n");
      prompt.append(
          "3. **ABBREVIATION**: Only official abbreviations (e.g., 'IBM' → 'International Business Machines')\n");
      prompt.append("4. **REJECT**: If names are different companies, even in same industry\n");
      prompt.append("5. **REJECT**: If names are similar but clearly different entities\n\n");
      prompt.append("EXAMPLES OF WHAT TO REJECT:\n");
      prompt.append("- 'Nala Robotics' vs 'Natobotics' → REJECT (different companies)\n");
      prompt.append("- 'Silicon Signals' vs 'Silicon Labs' → REJECT (different companies)\n");
      prompt.append("- 'Kobster.com' vs 'Kobie' → REJECT (different companies)\n\n");
      prompt.append("Respond with:\n");
      prompt.append("- The EXACT company name from the list above\n");
      prompt.append("- 'NO_MATCH' if none are suitable\n\n");
      prompt.append("Only respond with the exact company name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());

      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedCompanyName = aiResponse.trim();

        // Find the selected company in our list
        for (Company company : topMatches) {
          if (company.getName().equalsIgnoreCase(selectedCompanyName)) {
            // Additional validation: Check if this match makes sense
            if (isValidCompanyMatch(inputCompanyName, company.getName())) {
              log.info("GenAI selected company match: '{}' -> '{}'", inputCompanyName,
                  company.getName());
              return company;
            } else {
              log.warn("GenAI selected match rejected by validation: '{}' -> '{}'",
                  inputCompanyName, company.getName());
              return null;
            }
          }
        }

        // If exact match not found, try partial matching with validation
        for (Company company : topMatches) {
          if (company.getName().toLowerCase().contains(selectedCompanyName.toLowerCase()) ||
              selectedCompanyName.toLowerCase().contains(company.getName().toLowerCase())) {
            if (isValidCompanyMatch(inputCompanyName, company.getName())) {
              log.info("GenAI selected company match (partial): '{}' -> '{}'", inputCompanyName,
                  company.getName());
              return company;
            } else {
              log.warn("GenAI partial match rejected by validation: '{}' -> '{}'", inputCompanyName,
                  company.getName());
              return null;
            }
          }
        }

        log.warn("GenAI response '{}' could not be matched to any company in the list",
            selectedCompanyName);
        return null;
      } else {
        log.info("GenAI found no suitable match for: '{}'", inputCompanyName);
        return null;
      }

    } catch (Exception e) {
      log.error("Error validating company matches with GenAI for: {}", inputCompanyName, e);
      return null;
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
            "Company Matching");

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(),
              response.getBody());
        }
      }

      // Fallback: Use string similarity as a placeholder for GenAI
      log.info("Using string similarity as GenAI fallback for prompt: {}",
          prompt.substring(0, Math.min(100, prompt.length())));
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
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.databind.JsonNode responseNode = objectMapper.readTree(responseBody);

      // Navigate to the text content
      com.fasterxml.jackson.databind.JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        com.fasterxml.jackson.databind.JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          com.fasterxml.jackson.databind.JsonNode parts = content.get("parts");
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
   * Validate if company match makes sense
   */
  private boolean isValidCompanyMatch(String inputName, String matchedName) {
    if (inputName == null || matchedName == null) {
      return false;
    }

    String normalizedInput = inputName.toLowerCase().replaceAll("[^a-z0-9]", "");
    String normalizedMatched = matchedName.toLowerCase().replaceAll("[^a-z0-9]", "");

    // Exact match
    if (normalizedInput.equals(normalizedMatched)) {
      return true;
    }

    // One contains the other (subsidiary case)
    if (normalizedInput.contains(normalizedMatched) || normalizedMatched.contains(
        normalizedInput)) {
      return true;
    }

    // High similarity score
    double similarity = calculateSimilarityScore(normalizedInput, normalizedMatched);
    return similarity > 0.8;
  }

  /**
   * Log untagged company to PendingContent for future analysis
   */
  private void logPendingContent(String entityName, Long sourceId, String portalName, String notes) {
    try {
      // Check if this entity name is already in pending content to avoid duplicates (regardless of source)
      if (pendingContentRepository.existsByEntityNameAndEntityType(
          entityName, PendingContent.EntityType.COMPANY)) {
        log.debug("Pending content already exists for company: '{}' (entity name already logged)", entityName);
        return;
      }

      PendingContent pendingContent = PendingContent.builder()
          .entityName(entityName)
          .entityType(PendingContent.EntityType.COMPANY)
          .sourceTable("external_jobs") // Assuming this comes from external jobs
          .sourceId(sourceId)
          .portalName(portalName)
          .notes(notes)
          .build();

      pendingContentRepository.save(pendingContent);
      log.info("Logged untagged company to PendingContent: '{}' (sourceId: {}, portal: {})", 
          entityName, sourceId, portalName);

    } catch (Exception e) {
      log.warn("Failed to log pending content for company '{}': {}", entityName, e.getMessage());
    }
  }

  /**
   * Result class for company tagging
   */
  public static class CompanyTaggingResult {

    private Long companyId;
    private String companyName;
    private Double confidence = 0.0;
    private String error;

    // Getters and setters
    public Long getCompanyId() {
      return companyId;
    }

    public void setCompanyId(Long companyId) {
      this.companyId = companyId;
    }

    public String getCompanyName() {
      return companyName;
    }

    public void setCompanyName(String companyName) {
      this.companyName = companyName;
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
