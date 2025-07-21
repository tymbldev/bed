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
        return generateSimilarCompanies(companyName, industry, description, null, null, null);
    }
    
    public List<String> generateSimilarCompanies(String companyName, String industry, String description, 
                                                String companySize, String specialties, String headquarters) {
        try {
            log.info("[Gemini] Generating similar companies for: {} in industry: {} with size: {}, specialties: {}, headquarters: {}", 
                    companyName, industry, companySize, specialties, headquarters);
            String prompt = buildEnhancedSimilarCompaniesPrompt(companyName, industry, description, companySize, specialties, headquarters);
            log.info("[Gemini] Enhanced prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
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
        return buildEnhancedSimilarCompaniesPrompt(companyName, industry, description, null, null, null);
    }
    
    private String buildEnhancedSimilarCompaniesPrompt(String companyName, String industry, String description, 
                                                      String companySize, String specialties, String headquarters) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an expert career advisor specializing in job transitions and company analysis. ");
        promptBuilder.append("Your task is to find highly relevant similar companies for career transitions.\n\n");
        
        promptBuilder.append("TARGET COMPANY DETAILS:\n");
        promptBuilder.append(String.format("- NAME: '%s'\n", companyName));
        promptBuilder.append(String.format("- INDUSTRY: '%s'\n", industry));
        promptBuilder.append(String.format("- DESCRIPTION: '%s'\n", description != null ? description : "No description available"));
        
        if (companySize != null && !companySize.trim().isEmpty()) {
            promptBuilder.append(String.format("- COMPANY SIZE: '%s'\n", companySize));
        }
        if (specialties != null && !specialties.trim().isEmpty()) {
            promptBuilder.append(String.format("- SPECIALTIES: '%s'\n", specialties));
        }
        if (headquarters != null && !headquarters.trim().isEmpty()) {
            promptBuilder.append(String.format("- HEADQUARTERS: '%s'\n", headquarters));
        }
        
        promptBuilder.append("\nProvide 8-12 similar companies that would be realistic career transition targets.\n\n");
        
        promptBuilder.append("PRIMARY CRITERIA (Must match):\n");
        promptBuilder.append("1. EXACT SAME INDUSTRY or closely related sub-industry\n");
        promptBuilder.append("2. SIMILAR COMPANY SIZE (startup-to-startup, enterprise-to-enterprise, mid-size-to-mid-size)\n");
        promptBuilder.append("3. COMPARABLE BUSINESS MODEL (B2B, B2C, SaaS, consulting, e-commerce, etc.)\n");
        promptBuilder.append("4. SIMILAR TECHNOLOGY DOMAIN or SPECIALTIES (if applicable)\n");
        promptBuilder.append("5. SIMILAR GEOGRAPHIC PRESENCE (local, national, global)\n\n");
        
        promptBuilder.append("SECONDARY CRITERIA (Should consider):\n");
        promptBuilder.append("6. COMPARABLE FUNDING STAGE (if applicable)\n");
        promptBuilder.append("7. SIMILAR CULTURE/COMPANY TYPE (remote-first, traditional, etc.)\n");
        promptBuilder.append("8. SIMILAR MARKET POSITION (leader, challenger, niche player)\n\n");
        
        promptBuilder.append("AVOID:\n");
        promptBuilder.append("- Companies in completely different industries\n");
        promptBuilder.append("- Companies that are too large or too small compared to target\n");
        promptBuilder.append("- Companies with vastly different business models\n");
        promptBuilder.append("- Companies that are direct competitors (unless specifically relevant)\n");
        promptBuilder.append("- Companies that are subsidiaries or divisions of larger companies\n");
        promptBuilder.append("- Companies that are too niche or obscure\n");
        promptBuilder.append("- Companies that are in decline or have poor reputation\n\n");
        
        promptBuilder.append("QUALITY REQUIREMENTS:\n");
        promptBuilder.append("- Use only well-known, established company names\n");
        promptBuilder.append("- Ensure companies are still active and relevant\n");
        promptBuilder.append("- Prefer companies with similar market presence and reputation\n");
        promptBuilder.append("- Consider companies that would be realistic career transition targets\n\n");
        
        promptBuilder.append("Return ONLY the company names separated by '||||' (4 pipe characters). ");
        promptBuilder.append("Do not include any explanations, comments, or additional text.\n\n");
        promptBuilder.append(String.format("Similar companies for '%s':", companyName));
        
        return promptBuilder.toString();
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