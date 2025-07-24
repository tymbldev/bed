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
public class CompanyShortnameService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;

    public Map<String, Object> generateShortnamesForAllCompanies(List<String> companyNames) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> companyResults = new ArrayList<>();
        
        int totalCompanies = companyNames.size();
        int totalShortnamesGenerated = 0;
        int totalErrors = 0;
        
        log.info("Starting shortname generation for {} companies", totalCompanies);
        
        for (String companyName : companyNames) {
            Map<String, Object> companyResult = new HashMap<>();
            companyResult.put("companyName", companyName);
            
            try {
                String shortname = generateShortnameForCompany(companyName);
                
                if (shortname != null && !shortname.trim().isEmpty()) {
                    companyResult.put("success", true);
                    companyResult.put("shortname", shortname);
                    totalShortnamesGenerated++;
                    
                    log.info("Generated shortname '{}' for company: {}", shortname, companyName);
                } else {
                    companyResult.put("success", false);
                    companyResult.put("error", "No shortname generated for company: " + companyName);
                    totalErrors++;
                    log.warn("Failed to generate shortname for company: {}, Error: No shortname returned", companyName);
                }
            } catch (Exception e) {
                companyResult.put("success", false);
                companyResult.put("error", "Error processing company: " + e.getMessage());
                totalErrors++;
                log.error("Error generating shortname for company: {}, Error: {}", companyName, e.getMessage());
            }
            
            companyResults.add(companyResult);
        }
        
        result.put("totalCompanies", totalCompanies);
        result.put("totalShortnamesGenerated", totalShortnamesGenerated);
        result.put("totalErrors", totalErrors);
        result.put("companyResults", companyResults);
        result.put("message", "Shortname generation completed for all companies");
        
        log.info("Shortname generation completed. Total companies: {}, Total shortnames: {}, Errors: {}", 
                totalCompanies, totalShortnamesGenerated, totalErrors);
        
        return result;
    }

    public Map<String, Object> generateShortnameForSingleCompany(String companyName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Generating shortname for company: {}", companyName);
            String shortname = generateShortnameForCompany(companyName);
            
            if (shortname != null && !shortname.trim().isEmpty()) {
                result.put("success", true);
                result.put("companyName", companyName);
                result.put("shortname", shortname);
                
                log.info("Successfully generated shortname '{}' for company: {}", shortname, companyName);
            } else {
                result.put("success", false);
                result.put("error", "No shortname generated for company: " + companyName);
                log.warn("No shortname generated for company: {}", companyName);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Error generating shortname: " + e.getMessage());
            log.error("Error generating shortname for company: {}", companyName, e);
        }
        
        return result;
    }

    private String generateShortnameForCompany(String companyName) {
        try {
            log.info("[Gemini] Generating shortname for company: {}", companyName);
            String prompt = buildShortnameGenerationPrompt(companyName);
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
                String shortname = parseShortnameResponse(response.getBody());
                log.info("[Gemini] Parsed shortname '{}' for company: {}", shortname, companyName);
                return shortname;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCode().value(), response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating shortname for company: {}", companyName, e);
            return null;
        }
    }

    private String buildShortnameGenerationPrompt(String companyName) {
        return String.format(
            "You are a business and technology expert helping to identify the commonly used shortnames or nicknames for companies. " +
            "Given the company name '%s', provide the most widely recognized and commonly used shortname or nickname for this company.\n\n" +
            "INSTRUCTIONS:\n" +
            "1. Identify the most popular and widely recognized shortname for the company\n" +
            "2. Consider how people commonly refer to the company in conversation\n" +
            "3. Focus on shortnames that are widely known and used in the industry\n" +
            "4. Prefer official or semi-official shortnames over informal nicknames\n" +
            "5. If the company doesn't have a widely recognized shortname, return the original name\n\n" +
            "EXAMPLES:\n" +
            "- 'Zomato' → 'Zomato' (no widely recognized shortname)\n" +
            "- 'Google' → 'Google' (no widely recognized shortname)\n" +
            "- 'Microsoft' → 'MS' or 'Microsoft'\n" +
            "- 'International Business Machines' → 'IBM'\n" +
            "- 'Apple Inc.' → 'Apple'\n" +
            "- 'Amazon.com' → 'Amazon'\n" +
            "- 'Netflix' → 'Netflix' (no widely recognized shortname)\n" +
            "- 'Meta Platforms' → 'Meta'\n" +
            "- 'Alphabet Inc.' → 'Alphabet'\n" +
            "- 'United Parcel Service' → 'UPS'\n" +
            "- 'Federal Express' → 'FedEx'\n" +
            "- 'International Business Machines Corporation' → 'IBM'\n" +
            "- 'General Electric' → 'GE'\n" +
            "- 'Procter & Gamble' → 'P&G'\n" +
            "- 'Johnson & Johnson' → 'J&J'\n\n" +
            "QUALITY REQUIREMENTS:\n" +
            "- Return only the shortname, no explanations or additional text\n" +
            "- Use the most widely recognized and commonly used shortname\n" +
            "- If no widely recognized shortname exists, return the original company name\n" +
            "- Ensure the shortname is currently in use and relevant\n" +
            "- Prefer official abbreviations over informal nicknames\n\n" +
            "Return ONLY the shortname for company '%s':",
            companyName
        );
    }

    private String parseShortnameResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for shortname: {}", generatedText);
                        
                        // Clean up the response
                        String shortname = generatedText.trim();
                        // Remove quotes if present
                        shortname = shortname.replaceAll("^[\"']+|[\"']+$", "");
                        // Remove any extra whitespace
                        shortname = shortname.trim();
                        
                        log.info("[Gemini] Parsed shortname: '{}'", shortname);
                        return shortname;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for shortname: {}", responseBody);
            return null;
        } catch (Exception e) {
            log.error("Error parsing Gemini response for shortname", e);
            return null;
        }
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
} 