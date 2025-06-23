package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<Company> extractCompanyInfo(String documentText) {
        try {
            log.info("Starting company information extraction using Gemini AI");
            
            if (documentText == null || documentText.trim().isEmpty()) {
                log.warn("Document text is null or empty");
                return Optional.empty();
            }
            
            String prompt = buildPrompt(documentText);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            log.debug("Sending request to Gemini API with document text length: {}", documentText.length());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            
            log.debug("Gemini API response status: {}", response.getStatusCodeValue());
            
            if (response.getStatusCodeValue() == 200) {
                log.info("Successfully received response from Gemini API");
                return parseGeminiResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return Optional.empty();
        }
    }

    private String buildPrompt(String documentText) {
        return "You are a data extraction assistant. I will give you the raw HTML or text content of a LinkedIn company page (extracted using document.text()). Your task is to extract the following company attributes and return them in valid JSON format, matching the schema exactly.\n\n" +
               "Schema:\n\n" +
               "{\n" +
               "  \"name\": \"\",\n" +
               "  \"description\": \"\",\n" +
               "  \"logo_url\": \"\",\n" +
               "  \"website\": \"\",\n" +
               "  \"about_us\": \"\",\n" +
               "  \"culture\": \"\",\n" +
               "  \"mission\": \"\",\n" +
               "  \"vision\": \"\",\n" +
               "  \"company_size\": \"\",\n" +
               "  \"headquarters\": \"\",\n" +
               "  \"industry\": \"\",\n" +
               "  \"linkedin_url\": \"\",\n" +
               "  \"specialties\": \"\",\n" +
               "  \"is_crawled\": true,\n" +
               "  \"last_crawled_at\": \"" + Instant.now().toString() + "\"\n" +
               "}\n\n" +
               "Instructions:\n" +
               "- Only extract what is available in the text.\n" +
               "- Set \"is_crawled\": true always.\n" +
               "- Set \"last_crawled_at\" to the current UTC timestamp in ISO format (e.g. \"2025-06-24T12:34:56Z\").\n" +
               "- Leave fields blank (\"\") if not found.\n" +
               "- For \"about_us\": Extract the detailed, comprehensive description of the company. Look for longer paragraphs, detailed company descriptions, and comprehensive information about what the company does, its history, achievements, and full business description. Do not use short summaries or brief descriptions.\n" +
               "- For \"description\": Use a concise summary or tagline of the company.\n" +
               "- Ensure valid JSON output.\n\n" +
               "Input:\n\n" +
               "<<< \n" +
               documentText +
               "\n >>>";
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

    private Optional<Company> parseGeminiResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            
            // Navigate to the generated text content
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        
                        // Extract JSON from the generated text (it might be wrapped in markdown)
                        String jsonText = extractJsonFromText(generatedText);
                        JsonNode companyData = objectMapper.readTree(jsonText);
                        
                        return mapJsonToCompany(companyData);
                    }
                }
            }
            
            log.error("Unexpected Gemini API response structure: {}", responseBody);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return Optional.empty();
        }
    }

    private String extractJsonFromText(String text) {
        // Remove markdown code blocks if present
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        text = text.trim();
        
        // Find JSON object boundaries
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start >= 0 && end >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return text;
    }

    private Optional<Company> mapJsonToCompany(JsonNode companyData) {
        try {
            Company company = new Company();
            
            company.setName(getStringValue(companyData, "name"));
            company.setDescription(getStringValue(companyData, "description"));
            company.setLogoUrl(getStringValue(companyData, "logo_url"));
            company.setWebsite(getStringValue(companyData, "website"));
            company.setAboutUs(getStringValue(companyData, "about_us"));
            company.setCulture(getStringValue(companyData, "culture"));
            company.setMission(getStringValue(companyData, "mission"));
            company.setVision(getStringValue(companyData, "vision"));
            company.setCompanySize(getStringValue(companyData, "company_size"));
            company.setHeadquarters(getStringValue(companyData, "headquarters"));
            company.setIndustry(getStringValue(companyData, "industry"));
            company.setLinkedinUrl(getStringValue(companyData, "linkedin_url"));
            company.setSpecialties(getStringValue(companyData, "specialties"));
            company.setCrawled(true);
            
            // Parse the timestamp
            String lastCrawledAt = getStringValue(companyData, "last_crawled_at");
            if (lastCrawledAt != null && !lastCrawledAt.isEmpty()) {
                try {
                    Instant instant = Instant.parse(lastCrawledAt);
                    company.setLastCrawledAt(LocalDateTime.ofInstant(instant, ZoneOffset.UTC));
                } catch (Exception e) {
                    log.warn("Could not parse last_crawled_at timestamp: {}", lastCrawledAt);
                    company.setLastCrawledAt(LocalDateTime.now());
                }
            } else {
                company.setLastCrawledAt(LocalDateTime.now());
            }
            
            return Optional.of(company);
        } catch (Exception e) {
            log.error("Error mapping JSON to Company object", e);
            return Optional.empty();
        }
    }

    private String getStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode != null ? fieldNode.asText() : "";
    }
} 