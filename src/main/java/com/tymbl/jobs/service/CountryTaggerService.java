package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.PendingContent;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.PendingContentRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import java.math.BigDecimal;
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
public class CountryTaggerService {

  private final CountryRepository countryRepository;
  private final SimilarContentRepository similarContentRepository;
  private final PendingContentRepository pendingContentRepository;
  private final AIRestService aiRestService;

  /**
   * Tag country using exact match, similar content, and AI-powered matching
   */
  @Transactional
  public CountryTaggingResult tagCountry(String countryName, Long sourceId, String portalName) {
    CountryTaggingResult result = new CountryTaggingResult();

    if (countryName == null || countryName.trim().isEmpty()) {
      return result;
    }

    try {
      String normalizedCountryName = countryName.trim();

      // 1. Try exact match first
      Optional<Country> exactMatch = countryRepository.findByName(normalizedCountryName);
      if (exactMatch.isPresent()) {
        Country country = exactMatch.get();
        result.setCountryId(country.getId());
        result.setCountryName(country.getName());
        result.setConfidence(1.0);
        log.info("Exact country match found: '{}' -> '{}' (ID: {})", countryName, country.getName(),
            country.getId());
        return result;
      }

      // 2. Try similar content matching
      List<SimilarContent> similarContents = similarContentRepository.findByTypeAndSearchTerm(
          ContentType.COUNTRY, countryName);

      if (!similarContents.isEmpty()) {
        // Extract country names from similar content (using parentName since entityId is null)
        List<String> countryNames = similarContents.stream()
            .map(SimilarContent::getParentName)
            .distinct()
            .collect(Collectors.toList());

        if (!countryNames.isEmpty()) {
          // Fetch countries by names
          List<Country> similarCountries = countryRepository.findByNameIn(countryNames);
          if (!similarCountries.isEmpty()) {
            Country bestMatch = findBestCountryMatchBySimilarity(countryName, similarCountries);
            if (bestMatch != null) {
              result.setCountryId(bestMatch.getId());
              result.setCountryName(bestMatch.getName());
              result.setConfidence(0.6);
              log.info("Similar content country match found: '{}' -> '{}' (ID: {})", countryName,
                  bestMatch.getName(), bestMatch.getId());
              return result;
            }
          }
        }
      }

      // 3. Try LIKE operator matching
      List<Country> likeMatches = countryRepository.findByNameContainingIgnoreCase(
          normalizedCountryName);
      if (!likeMatches.isEmpty()) {
        if (likeMatches.size() == 1) {
          Country country = likeMatches.get(0);
          result.setCountryId(country.getId());
          result.setCountryName(country.getName());
          result.setConfidence(0.8);
          log.info("Single LIKE country match found: '{}' -> '{}' (ID: {})", countryName,
              country.getName(), country.getId());
          return result;
        } else {
          log.warn("Multiple countries found with name '{}', using first result", countryName);
          Country country = likeMatches.get(0);
          result.setCountryId(country.getId());
          result.setCountryName(country.getName());
          result.setConfidence(0.7);
          return result;
        }
      }

      // 4. Try AI-powered matching if available
      Country aiMatch = validateCountryMatchWithGenAI(countryName,
          likeMatches.isEmpty() ? new ArrayList<>() : likeMatches);
      if (aiMatch != null) {
        result.setCountryId(aiMatch.getId());
        result.setCountryName(aiMatch.getName());
        result.setConfidence(0.9);
        log.info("AI-powered country match found: '{}' -> '{}' (ID: {})", countryName,
            aiMatch.getName(), aiMatch.getId());
        return result;
      }

      // Log NO_MATCH case to PendingContent for future analysis
      logPendingContent(countryName, sourceId, portalName, 
          "No match found after all tagging strategies (exact, similar content, AI)");

    } catch (Exception e) {
      log.error("Error tagging country for external job {}: {}", sourceId, e.getMessage(), e);
      result.setError(e.getMessage());
      
      // Log error case to PendingContent for future analysis
      logPendingContent(countryName, sourceId, portalName, 
          "Error during tagging: " + e.getMessage());
    }

    return result;
  }

  /**
   * Find best country match using string similarity
   */
  private Country findBestCountryMatchBySimilarity(String inputCountryName,
      List<Country> countries) {
    if (countries.isEmpty()) {
      return null;
    }

    Country bestMatch = null;
    double bestScore = 0.0;

    for (Country country : countries) {
      double score = calculateSimilarityScore(inputCountryName, country.getName());
      if (score > bestScore && score > 0.7) { // Minimum similarity threshold
        bestScore = score;
        bestMatch = country;
      }
    }

    return bestMatch;
  }

  /**
   * Calculate similarity score between two country names
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
   * Validate country match with GenAI by presenting top similarity matches
   */
  private Country validateCountryMatchWithGenAI(String inputCountryName, List<Country> topMatches) {
    try {
      if (topMatches == null || topMatches.isEmpty()) {
        return null;
      }

      // Create prompt for GenAI validation
      StringBuilder prompt = new StringBuilder();
      prompt.append("Given the input country name '").append(inputCountryName).append("', ");
      prompt.append("which of the following countries is the best match? ");
      prompt.append("Consider abbreviations, common names, and regional variations.\n\n");
      prompt.append("Available countries:\n");

      for (int i = 0; i < Math.min(topMatches.size(), 5); i++) {
        Country country = topMatches.get(i);
        prompt.append(i + 1).append(". ").append(country.getName());
        prompt.append("\n");
      }

      prompt.append("\nRespond with only the number (1-").append(Math.min(topMatches.size(), 5))
          .append(") of the best match.");

      String aiResponse = callGenAIService(prompt.toString());
      if (aiResponse != null) {
        // Parse AI response to get the selected country
        try {
          int selectedIndex = Integer.parseInt(aiResponse.trim()) - 1;
          if (selectedIndex >= 0 && selectedIndex < topMatches.size()) {
            Country selectedCountry = topMatches.get(selectedIndex);

            // Validate the selection
            if (isValidCountryMatch(inputCountryName, selectedCountry.getName())) {
              return selectedCountry;
            }
          }
        } catch (NumberFormatException e) {
          log.warn("Failed to parse AI response as number: {}", aiResponse);
        }
      }

      // If AI validation fails, return the first match if it's reasonable
      if (!topMatches.isEmpty()) {
        Country firstMatch = topMatches.get(0);
        if (isValidCountryMatch(inputCountryName, firstMatch.getName())) {
          log.info("AI validation failed, using first reasonable match: '{}' -> '{}'",
              inputCountryName, firstMatch.getName());
          return firstMatch;
        }
      }

      return null;

    } catch (Exception e) {
      log.warn("Error in GenAI country matching for '{}': {}", inputCountryName, e.getMessage());
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
            "Country Matching");

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(),
              response.getBody());
        }
      }

      // Fallback: Return null if AI service is not available
      log.info("AI service not available for country matching");
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
   * Validate if a country match is reasonable
   */
  private boolean isValidCountryMatch(String inputName, String matchedName) {
    if (inputName == null || matchedName == null) {
      return false;
    }

    String input = inputName.toLowerCase().trim();
    String matched = matchedName.toLowerCase().trim();

    // Exact match
    if (input.equals(matched)) {
      return true;
    }

    // Contains match
    if (input.contains(matched) || matched.contains(input)) {
      return true;
    }

    // Word overlap
    String[] inputWords = input.split("\\s+");
    String[] matchedWords = matched.split("\\s+");

    for (String inputWord : inputWords) {
      for (String matchedWord : matchedWords) {
        if (inputWord.equals(matchedWord) && inputWord.length() > 2) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Store country mapping in similar_content table
   */
  private void storeCountryMapping(String similarCountryName, String parentCountryName,
      double confidence) {
    try {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setType(ContentType.COUNTRY);
      similarContent.setParentName(parentCountryName);
      similarContent.setSimilarName(similarCountryName);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setProcessed(true);

      similarContentRepository.save(similarContent);
      log.debug("Stored country mapping: '{}' -> '{}' with confidence {}", similarCountryName,
          parentCountryName, confidence);

    } catch (Exception e) {
      log.warn("Failed to store country mapping: {}", e.getMessage());
    }
  }

  /**
   * Log untagged country to PendingContent for future analysis
   */
  private void logPendingContent(String entityName, Long sourceId, String portalName, String notes) {
    try {
      // Check if this entity name is already in pending content to avoid duplicates (regardless of source)
      if (pendingContentRepository.existsByEntityNameAndEntityType(
          entityName, PendingContent.EntityType.COUNTRY)) {
        log.debug("Pending content already exists for country: '{}' (entity name already logged)", entityName);
        return;
      }

      PendingContent pendingContent = PendingContent.builder()
          .entityName(entityName)
          .entityType(PendingContent.EntityType.COUNTRY)
          .sourceTable("external_jobs") // Assuming this comes from external jobs
          .sourceId(sourceId)
          .portalName(portalName)
          .notes(notes)
          .build();

      pendingContentRepository.save(pendingContent);
      log.info("Logged untagged country to PendingContent: '{}' (sourceId: {}, portal: {})", 
          entityName, sourceId, portalName);

    } catch (Exception e) {
      log.warn("Failed to log pending content for country '{}': {}", entityName, e.getMessage());
    }
  }

  /**
   * Result class for country tagging
   */
  public static class CountryTaggingResult {

    private Long countryId;
    private String countryName;
    private Double confidence;
    private String error;

    // Getters and setters
    public Long getCountryId() {
      return countryId;
    }

    public void setCountryId(Long countryId) {
      this.countryId = countryId;
    }

    public String getCountryName() {
      return countryName;
    }

    public void setCountryName(String countryName) {
      this.countryName = countryName;
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
