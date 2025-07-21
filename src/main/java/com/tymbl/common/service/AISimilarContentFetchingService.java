package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AISimilarContentFetchingService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;

    // ============================================================================
    // SIMILAR DESIGNATIONS METHODS
    // ============================================================================

    public List<String> generateSimilarDesignations(String designationName) {
        try {
            log.info("[Gemini] Generating similar designations for: {}", designationName);
            String prompt = buildSimilarDesignationsPrompt(designationName);
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
                List<String> similarDesignations = parseSimilarDesignationsResponse(response.getBody());
                log.info("[Gemini] Parsed {} similar designations for: {}", similarDesignations.size(), designationName);
                return similarDesignations;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCode().value(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating similar designations for: {}", designationName, e);
            return new ArrayList<>();
        }
    }

    private String buildSimilarDesignationsPrompt(String designationName) {
        return String.format(
            "You are a career advisor helping professionals find similar job roles they can transition to. " +
            "Given the designation '%s', provide 8-12 similar designations that a person in this role could reasonably switch to. " +
            "Consider factors like:\n" +
            "- Similar skill requirements\n" +
            "- Related industry domains\n" +
            "- Comparable responsibility levels\n" +
            "- Natural career progression paths\n" +
            "- Lateral moves within the same field\n\n" +
            "Return ONLY the similar designation names separated by '||||' (4 pipe characters). " +
            "Do not include any explanations, comments, or additional text. " +
            "Example format: Designation 1||||Designation 2||||Designation 3\n\n" +
            "Similar designations for '%s':",
            designationName, designationName
        );
    }

    private List<String> parseSimilarDesignationsResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for similar designations: {}", generatedText);
                        
                        // Parse 4-pipe separated values
                        String[] designationParts = generatedText.split("\\|\\|\\|\\|");
                        List<String> designations = new ArrayList<>();
                        for (String part : designationParts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                // Remove any quotes or brackets that might be present
                                trimmed = trimmed.replaceAll("^[\"\\[\\s]+", "").replaceAll("[\"\\]\\s]+$", "");
                                if (!trimmed.isEmpty()) {
                                    designations.add(trimmed);
                                }
                            }
                        }
                        
                        log.info("[Gemini] Parsed {} designations from 4-pipe separated response", designations.size());
                        return designations;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for similar designations: {}", responseBody);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing Gemini response for similar designations", e);
            return new ArrayList<>();
        }
    }

    // ============================================================================
    // SIMILAR COMPANIES METHODS
    // ============================================================================

    public List<String> generateSimilarCompanies(String companyName, String industry, String description) {
        try {
            log.info("[Gemini] Generating similar companies for: {} in industry: {}", companyName, industry);
            String prompt = buildSimilarCompaniesPrompt(companyName, industry, description);
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
                List<String> similarCompanies = parseSimilarCompaniesResponse(response.getBody());
                log.info("[Gemini] Parsed {} similar companies for: {}", similarCompanies.size(), companyName);
                return similarCompanies;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCode().value(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating similar companies for: {}", companyName, e);
            return new ArrayList<>();
        }
    }

    private String buildSimilarCompaniesPrompt(String companyName, String industry, String description) {
        return String.format(
            "You are a career advisor helping professionals find similar companies they can work for. " +
            "Given the company '%s' in the '%s' industry with description: '%s', provide 8-12 similar companies that a person could reasonably switch to. " +
            "Consider factors like:\n" +
            "- Same or related industry sectors\n" +
            "- Similar company size and stage\n" +
            "- Comparable business models or services\n" +
            "- Similar technology stack or domain expertise\n" +
            "- Geographic proximity or remote work opportunities\n" +
            "- Similar company culture or values\n\n" +
            "Return ONLY the similar company names separated by '||||' (4 pipe characters). " +
            "Do not include any explanations, comments, or additional text. " +
            "Example format: Company 1||||Company 2||||Company 3\n\n" +
            "Similar companies for '%s':",
            companyName, industry, description != null ? description : "No description available", companyName
        );
    }

    private List<String> parseSimilarCompaniesResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for similar companies: {}", generatedText);
                        
                        // Parse 4-pipe separated values
                        String[] companyParts = generatedText.split("\\|\\|\\|\\|");
                        List<String> companies = new ArrayList<>();
                        for (String part : companyParts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                // Remove any quotes or brackets that might be present
                                trimmed = trimmed.replaceAll("^[\"\\[\\s]+", "").replaceAll("[\"\\]\\s]+$", "");
                                if (!trimmed.isEmpty()) {
                                    companies.add(trimmed);
                                }
                            }
                        }
                        
                        log.info("[Gemini] Parsed {} companies from 4-pipe separated response", companies.size());
                        return companies;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for similar companies: {}", responseBody);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing Gemini response for similar companies", e);
            return new ArrayList<>();
        }
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

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
} 