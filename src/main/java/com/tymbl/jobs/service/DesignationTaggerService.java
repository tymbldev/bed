package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.DesignationRepository;
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
        return new DesignationTaggingResult(bestMatch.getId(), bestMatch.getName(), 0.85);
      }
    }

    // 4. Try GenAI with all designations as fallback
    Designation genAIMatch = findBestDesignationUsingGenAI(normalizedJobTitle);
    if (genAIMatch != null) {
      return new DesignationTaggingResult(genAIMatch.getId(), genAIMatch.getName(), 0.7);
    }

    // 5. Log NO_MATCH case for future analysis
    log.info(
        "NO_MATCH for designation: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies",
        normalizedJobTitle, sourceId, portalName);

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
          "You are a job title matching expert. Match the input job title to the best available designation.\n\n");
      prompt.append("Input job title: '").append(jobTitle).append("'\n\n");
      prompt.append("Available designations:\n");

      for (int i = 0; i < designations.size(); i++) {
        Designation designation = designations.get(i);
        prompt.append(i + 1).append(". ").append(designation.getName()).append("\n");
      }

      prompt.append(
          "\nRespond with the EXACT designation name from the list above, or 'NO_MATCH' if none are suitable.\n");
      prompt.append(
          "Only respond with the exact designation name or 'NO_MATCH', no additional text.");

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
   * Find best designation using GenAI with all designations
   */
  private Designation findBestDesignationUsingGenAI(String jobTitle) {
    try {
      // Get designations from dropdown service (cached and optimized)
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      List<Designation> topDesignations = allDesignations.stream()
          .limit(20)
          .collect(Collectors.toList());

      StringBuilder prompt = new StringBuilder();
      prompt.append(
          "You are a job title matching expert. Match the input job title to the best available designation.\n\n");
      prompt.append("Input job title: '").append(jobTitle).append("'\n\n");
      prompt.append("Available designations (top 20 by usage):\n");

      for (int i = 0; i < topDesignations.size(); i++) {
        Designation designation = topDesignations.get(i);
        prompt.append(i + 1).append(". ").append(designation.getName()).append("\n");
      }

      prompt.append(
          "\nRespond with the EXACT designation name from the list above, or 'NO_MATCH' if none are suitable.\n");
      prompt.append(
          "Only respond with the exact designation name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());

      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedDesignationName = aiResponse.trim();

        for (Designation designation : topDesignations) {
          if (designation.getName().equalsIgnoreCase(selectedDesignationName)) {
            return designation;
          }
        }
      }

    } catch (Exception e) {
      log.warn("Error in GenAI designation matching: {}", e.getMessage());
    }

    return null;
  }

  /**
   * Store designation mapping in similar content table
   */
  private void storeDesignationMapping(String parentDesignationName, String similarDesignationName,
      double confidence) {
    try {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setType(ContentType.DESIGNATION);
      similarContent.setParentName(parentDesignationName);
      similarContent.setSimilarName(similarDesignationName);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setProcessed(true);

      similarContentRepository.save(similarContent);
      log.debug("Stored designation mapping: '{}' -> '{}' with confidence {}",
          similarDesignationName, parentDesignationName, confidence);

    } catch (Exception e) {
      log.warn("Failed to store designation mapping: {}", e.getMessage());
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
