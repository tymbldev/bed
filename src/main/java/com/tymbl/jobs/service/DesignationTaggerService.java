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
      storeDesignationMapping(genAIMatch.getName(), normalizedJobTitle, 0.85);
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
   * Find best designation using GenAI with tokenized matching
   */
  private Designation findBestDesignationUsingGenAI(String jobTitle) {
    try {
      // Get all designations from dropdown service (cached and optimized)
      List<Designation> allDesignations = dropdownService.getAllDesignations();
      
      // Tokenize the input job title
      List<String> tokens = tokenizeJobTitle(jobTitle);
      log.debug("Tokenized job title '{}' into tokens: {}", jobTitle, tokens);
      
      // Filter designations that contain at least one token (case-insensitive)
      List<Designation> matchedDesignations = allDesignations.stream()
          .filter(designation -> containsAnyToken(designation.getName(), tokens))
          .collect(Collectors.toList());
      
      log.debug("Found {} designations matching tokens from '{}'", matchedDesignations.size(), jobTitle);
      
      // Log some examples of matches for debugging
      if (!matchedDesignations.isEmpty()) {
        List<String> exampleMatches = matchedDesignations.stream()
            .limit(5)
            .map(Designation::getName)
            .collect(Collectors.toList());
        log.debug("Example matches for '{}': {}", jobTitle, exampleMatches);
      }
      
      // If no matches found, return null
      if (matchedDesignations.isEmpty()) {
        log.debug("No designations found matching any tokens from '{}'", jobTitle);
        return null;
      }
      
      // If only one match, return it directly
      if (matchedDesignations.size() == 1) {
        log.debug("Single match found for '{}': {}", jobTitle, matchedDesignations.get(0).getName());
        return matchedDesignations.get(0);
      }

      // Use GenAI to find the best match from filtered designations
      StringBuilder prompt = new StringBuilder();
      prompt.append(
          "You are a job title matching expert. Your task is to find the BEST matching designation from the list below.\n\n");
      prompt.append("Input job title: '").append(jobTitle).append("'\n\n");
      prompt.append("Available designations (filtered by token matching):\n");

      for (int i = 0; i < matchedDesignations.size(); i++) {
        Designation designation = matchedDesignations.get(i);
        prompt.append(i + 1).append(". ").append(designation.getName()).append("\n");
      }

      prompt.append(
          "\nIMPORTANT: Look for designations that contain the key words from the input job title.\n");
      prompt.append(
          "For 'SENIOR, SOFTWARE ENGINEER', prioritize designations containing 'Senior', 'Software', and 'Engineer'.\n");
      prompt.append(
          "The best match should be the most specific and relevant designation.\n\n");
      prompt.append(
          "Respond with the EXACT designation name from the list above, or 'NO_MATCH' if none are suitable.\n");
      prompt.append(
          "Only respond with the exact designation name or 'NO_MATCH', no additional text.");

      String aiResponse = callGenAIService(prompt.toString());

      if (aiResponse != null && !aiResponse.trim().equalsIgnoreCase("NO_MATCH")) {
        String selectedDesignationName = aiResponse.trim();

        for (Designation designation : matchedDesignations) {
          if (designation.getName().equalsIgnoreCase(selectedDesignationName)) {
            log.debug("GenAI selected designation '{}' for job title '{}'", designation.getName(), jobTitle);
            return designation;
          }
        }
      }
      
      // Fallback: If GenAI returns NO_MATCH or fails, return the designation with the most token matches
      log.debug("GenAI returned NO_MATCH or failed, using fallback token-based selection for '{}'", jobTitle);
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
      log.debug("Fallback selected designation '{}' with {} token matches for '{}'", 
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
