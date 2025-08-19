package com.tymbl.jobs.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.SimilarContent;
import com.tymbl.common.entity.SimilarContent.ContentType;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.SimilarContentRepository;
import com.tymbl.common.service.AIRestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CityTaggerService {

  private final CityRepository cityRepository;
  private final SimilarContentRepository similarContentRepository;
  private final AIRestService aiRestService;

  /**
   * Tag city using exact match, similar content, and AI-powered matching
   */
  @Transactional
  public CityTaggingResult tagCity(String cityName, Long sourceId, String portalName) {
    CityTaggingResult result = new CityTaggingResult();
    
    if (cityName == null || cityName.trim().isEmpty()) {
      return result;
    }

    try {
      String normalizedCityName = cityName.trim();
      
      // 1. Try exact match first
      Optional<City> exactMatch = cityRepository.findByName(normalizedCityName);
      if (exactMatch.isPresent()) {
        City city = exactMatch.get();
        result.setCityId(city.getId());
        result.setCityName(city.getName());
        result.setConfidence(1.0);
        log.info("Exact city match found: '{}' -> '{}' (ID: {})", cityName, city.getName(), city.getId());
        return result;
      }

      // 2. Try similar content matching
      List<SimilarContent> similarContents = similarContentRepository.findByTypeAndSearchTerm(
          ContentType.CITY, cityName);
      
      if (!similarContents.isEmpty()) {
        // Extract city names from similar content (using parentName since entityId is null)
        List<String> cityNames = similarContents.stream()
            .map(SimilarContent::getParentName)
            .distinct()
            .collect(Collectors.toList());

        if (!cityNames.isEmpty()) {
          // Fetch cities by names
          List<City> similarCities = cityRepository.findByNameIn(cityNames);
          if (!similarCities.isEmpty()) {
            City bestMatch = findBestCityMatchBySimilarity(cityName, similarCities);
            if (bestMatch != null) {
              result.setCityId(bestMatch.getId());
              result.setCityName(bestMatch.getName());
              result.setConfidence(0.6);
              log.info("Similar content city match found: '{}' -> '{}' (ID: {})", cityName, bestMatch.getName(), bestMatch.getId());
              return result;
            }
          }
        }
      }

      // 3. Try LIKE operator matching
      List<City> likeMatches = cityRepository.findByNameContainingIgnoreCaseOrderByNameAsc(normalizedCityName);
      if (!likeMatches.isEmpty()) {
        if (likeMatches.size() == 1) {
          City city = likeMatches.get(0);
          result.setCityId(city.getId());
          result.setCityName(city.getName());
          result.setConfidence(0.8);
          log.info("Single LIKE city match found: '{}' -> '{}' (ID: {})", cityName, city.getName(), city.getId());
          return result;
        } else {
          log.warn("Multiple cities found with name '{}', using first result", cityName);
          City city = likeMatches.get(0);
          result.setCityId(city.getId());
          result.setCityName(city.getName());
          result.setConfidence(0.7);
          return result;
        }
      }

      // 4. Try AI-powered matching if available
      City aiMatch = validateCityMatchWithGenAI(cityName, likeMatches.isEmpty() ? new ArrayList<>() : likeMatches);
      if (aiMatch != null) {
        result.setCityId(aiMatch.getId());
        result.setCityName(aiMatch.getName());
        result.setConfidence(0.9);
        log.info("AI-powered city match found: '{}' -> '{}' (ID: {})", cityName, aiMatch.getName(), aiMatch.getId());
        return result;
      }

      log.info("NO_MATCH for city: '{}' (sourceId: {}, portal: {}) - No match found after all tagging strategies", 
               cityName, sourceId, portalName);
      
    } catch (Exception e) {
      log.error("Error tagging city for external job {}: {}", sourceId, e.getMessage(), e);
      result.setError(e.getMessage());
    }
    
    return result;
  }

  /**
   * Find best city match using string similarity
   */
  private City findBestCityMatchBySimilarity(String inputCityName, List<City> cities) {
    if (cities.isEmpty()) {
      return null;
    }

    City bestMatch = null;
    double bestScore = 0.0;

    for (City city : cities) {
      double score = calculateSimilarityScore(inputCityName, city.getName());
      if (score > bestScore && score > 0.7) { // Minimum similarity threshold
        bestScore = score;
        bestMatch = city;
      }
    }

    return bestMatch;
  }

  /**
   * Calculate similarity score between two city names
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
   * Validate city match with GenAI by presenting top similarity matches
   */
  private City validateCityMatchWithGenAI(String inputCityName, List<City> topMatches) {
    try {
      if (topMatches == null || topMatches.isEmpty()) {
        return null;
      }

      // Create prompt for GenAI validation
      StringBuilder prompt = new StringBuilder();
      prompt.append("Given the input city name '").append(inputCityName).append("', ");
      prompt.append("which of the following cities is the best match? ");
      prompt.append("Consider abbreviations, common names, and regional variations.\n\n");
      prompt.append("Available cities:\n");
      
      for (int i = 0; i < Math.min(topMatches.size(), 5); i++) {
        City city = topMatches.get(i);
        prompt.append(i + 1).append(". ").append(city.getName());
        prompt.append("\n");
      }
      
      prompt.append("\nRespond with only the number (1-").append(Math.min(topMatches.size(), 5)).append(") of the best match.");

      String aiResponse = callGenAIService(prompt.toString());
      if (aiResponse != null) {
        // Parse AI response to get the selected city
        try {
          int selectedIndex = Integer.parseInt(aiResponse.trim()) - 1;
          if (selectedIndex >= 0 && selectedIndex < topMatches.size()) {
            City selectedCity = topMatches.get(selectedIndex);
            
            // Validate the selection
            if (isValidCityMatch(inputCityName, selectedCity.getName())) {
              return selectedCity;
            }
          }
        } catch (NumberFormatException e) {
          log.warn("Failed to parse AI response as number: {}", aiResponse);
        }
      }

      // If AI validation fails, return the first match if it's reasonable
      if (!topMatches.isEmpty()) {
        City firstMatch = topMatches.get(0);
        if (isValidCityMatch(inputCityName, firstMatch.getName())) {
          log.info("AI validation failed, using first reasonable match: '{}' -> '{}'", inputCityName, firstMatch.getName());
          return firstMatch;
        }
      }

      return null;
      
    } catch (Exception e) {
      log.warn("Error in GenAI city matching for '{}': {}", inputCityName, e.getMessage());
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
        ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "City Matching");
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
          // Parse the response to extract the generated text
          return extractTextFromGeminiResponse(response.getBody());
        } else {
          log.warn("Gemini API call failed with status: {} - {}", response.getStatusCode(), response.getBody());
        }
      }
      
      // Fallback: Return null if AI service is not available
      log.info("AI service not available for city matching");
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
   * Validate if a city match is reasonable
   */
  private boolean isValidCityMatch(String inputName, String matchedName) {
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
   * Store city mapping in similar_content table
   */
  private void storeCityMapping(String similarCityName, String parentCityName, double confidence) {
    try {
      SimilarContent similarContent = new SimilarContent();
      similarContent.setType(ContentType.CITY);
      similarContent.setParentName(parentCityName);
      similarContent.setSimilarName(similarCityName);
      similarContent.setConfidenceScore(BigDecimal.valueOf(confidence));
      similarContent.setProcessed(true);
      
      similarContentRepository.save(similarContent);
      log.debug("Stored city mapping: '{}' -> '{}' with confidence {}", similarCityName, parentCityName, confidence);
      
    } catch (Exception e) {
      log.warn("Failed to store city mapping: {}", e.getMessage());
    }
  }

  /**
   * Result class for city tagging
   */
  public static class CityTaggingResult {
    private Long cityId;
    private String cityName;
    private Double confidence;
    private String error;

    // Getters and setters
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
  }
}
