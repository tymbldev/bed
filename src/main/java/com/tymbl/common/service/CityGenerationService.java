package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityGenerationService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public Map<String, Object> generateCitiesForAllCountries() {
        List<Country> countries = countryRepository.findByCitiesProcessedFalse();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> countryResults = new ArrayList<>();
        
        int totalCountries = countries.size();
        int totalCitiesGenerated = 0;
        int totalErrors = 0;
        
        log.info("Starting city generation for {} unprocessed countries", totalCountries);
        
        for (Country country : countries) {
            Map<String, Object> countryResult = new HashMap<>();
            countryResult.put("countryId", country.getId());
            countryResult.put("countryName", country.getName());
            countryResult.put("countryCode", country.getCode());
            
            try {
                // Process each country in its own transaction
                Map<String, Object> countryProcessResult = processCountryInTransaction(country);
                
                if ((Boolean) countryProcessResult.get("success")) {
                    countryResult.put("success", true);
                    countryResult.put("citiesGenerated", countryProcessResult.get("citiesGenerated"));
                    countryResult.put("citiesSaved", countryProcessResult.get("citiesSaved"));
                    countryResult.put("cities", countryProcessResult.get("cities"));
                    totalCitiesGenerated += (Integer) countryProcessResult.get("citiesSaved");
                    
                    log.info("Generated {} cities for country: {} ({})", 
                            countryProcessResult.get("citiesSaved"), country.getName(), country.getCode());
                } else {
                    countryResult.put("success", false);
                    countryResult.put("error", countryProcessResult.get("error"));
                    totalErrors++;
                    log.warn("Failed to generate cities for country: {} ({}), Error: {}", 
                            country.getName(), country.getCode(), countryProcessResult.get("error"));
                }
            } catch (Exception e) {
                countryResult.put("success", false);
                countryResult.put("error", "Error processing country: " + e.getMessage());
                totalErrors++;
                log.error("Error generating cities for country: {} ({}), Error: {}", 
                        country.getName(), country.getCode(), e.getMessage());
            }
            
            countryResults.add(countryResult);
        }
        
        result.put("totalCountries", totalCountries);
        result.put("totalCitiesGenerated", totalCitiesGenerated);
        result.put("totalErrors", totalErrors);
        result.put("countryResults", countryResults);
        result.put("message", "City generation completed for unprocessed countries");
        
        log.info("City generation completed. Total countries: {}, Total cities: {}, Errors: {}", 
                totalCountries, totalCitiesGenerated, totalErrors);
        
        return result;
    }

    @Transactional
    public Map<String, Object> processCountryInTransaction(Country country) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> cities = generateCitiesForCountry(country.getName());
            
            if (!cities.isEmpty()) {
                // Save cities to database
                int citiesSaved = saveCitiesToDatabase(cities, country);
                
                // Mark country as processed
                country.setCitiesProcessed(true);
                countryRepository.save(country);
                
                result.put("success", true);
                result.put("citiesGenerated", cities.size());
                result.put("citiesSaved", citiesSaved);
                result.put("cities", cities);
                
                log.info("Transaction completed successfully for country: {} ({}). Cities saved: {}", 
                        country.getName(), country.getCode(), citiesSaved);
            } else {
                result.put("success", false);
                result.put("error", "No cities generated for country");
                log.warn("No cities generated for country: {} ({})", country.getName(), country.getCode());
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Error processing country: " + e.getMessage());
            log.error("Transaction failed for country: {} ({}), Error: {}", 
                    country.getName(), country.getCode(), e.getMessage());
        }
        
        return result;
    }

    private List<String> generateCitiesForCountry(String countryName) {
        try {
            log.info("[Gemini] Generating cities for country: {}", countryName);
            String prompt = buildCityGenerationPrompt(countryName);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCode().value());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCode().value() == 200) {
                List<String> cities = parseCitiesResponse(response.getBody());
                log.info("[Gemini] Parsed {} cities for country: {}", cities.size(), countryName);
                return cities;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCode().value(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating cities for country: {}", countryName, e);
            return new ArrayList<>();
        }
    }

    private String buildCityGenerationPrompt(String countryName) {
        return String.format(
            "You are a geographic data expert helping to populate a database with major cities for countries. " +
            "Given the country '%s', provide 15-25 major cities that are important for business, technology, and employment opportunities. " +
            "Consider factors like:\n" +
            "- Major business and financial centers\n" +
            "- Technology hubs and startup ecosystems\n" +
            "- Large metropolitan areas with significant job markets\n" +
            "- Cities with major universities and research institutions\n" +
            "- International business centers\n" +
            "- Cities with significant tech companies or headquarters\n\n" +
            "Return ONLY the city names separated by '||||' (4 pipe characters). " +
            "Do not include any explanations, comments, or additional text. " +
            "Example format: City 1||||City 2||||City 3\n\n" +
            "Major cities for '%s':",
            countryName, countryName
        );
    }

    private List<String> parseCitiesResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for cities: {}", generatedText);
                        
                        // Parse 4-pipe separated values
                        String[] cityParts = generatedText.split("\\|\\|\\|\\|");
                        List<String> cities = new ArrayList<>();
                        for (String part : cityParts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                // Remove any quotes or brackets that might be present
                                trimmed = trimmed.replaceAll("^[\"\\[\\s]+", "").replaceAll("[\"\\]\\s]+$", "");
                                if (!trimmed.isEmpty()) {
                                    cities.add(trimmed);
                                }
                            }
                        }
                        
                        log.info("[Gemini] Parsed {} cities from 4-pipe separated response", cities.size());
                        return cities;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for cities: {}", responseBody);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing Gemini response for cities", e);
            return new ArrayList<>();
        }
    }

    private int saveCitiesToDatabase(List<String> cities, Country country) {
        int savedCount = 0;
        int errorCount = 0;
        
        log.info("Starting to save {} cities for country: {} ({})", cities.size(), country.getName(), country.getCode());
        
        for (String cityName : cities) {
            try {
                // Check if city already exists for this country
                boolean cityExists = cityRepository.findByCountryIdOrderByNameAsc(country.getId())
                    .stream()
                    .anyMatch(city -> city.getName().equalsIgnoreCase(cityName));
                
                if (!cityExists) {
                    City city = new City(cityName, country);
                    cityRepository.save(city);
                    savedCount++;
                    log.debug("Saved city: {} for country: {}", cityName, country.getName());
                } else {
                    log.debug("City already exists: {} for country: {}", cityName, country.getName());
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Error saving city: {} for country: {}, Error: {}", cityName, country.getName(), e.getMessage());
            }
        }
        
        log.info("City save completed for country: {} ({}). Saved: {}, Errors: {}", 
                country.getName(), country.getCode(), savedCount, errorCount);
        
        return savedCount;
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});
        contents.put("contents", new Object[]{content});
        return contents;
    }

    /**
     * Process a single country for city generation
     * This method processes one country in its own transaction
     */
    @Transactional
    public Map<String, Object> generateCitiesForSingleCountry(Long countryId) {
        try {
            Country country = countryRepository.findById(countryId)
                .orElseThrow(() -> new RuntimeException("Country not found with ID: " + countryId));
            
            if (country.isCitiesProcessed()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Country has already been processed for city generation");
                result.put("countryId", countryId);
                result.put("countryName", country.getName());
                return result;
            }
            
            return processCountryInTransaction(country);
        } catch (Exception e) {
            log.error("Error processing single country with ID: {}", countryId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Error processing country: " + e.getMessage());
            result.put("countryId", countryId);
            return result;
        }
    }

    /**
     * Reset cities processed flag for all countries
     * This allows reprocessing of city generation for all countries
     */
    @Transactional
    public void resetCitiesProcessedFlag() {
        countryRepository.resetCitiesProcessedFlag();
        log.info("Reset cities processed flag for all countries");
    }
} 