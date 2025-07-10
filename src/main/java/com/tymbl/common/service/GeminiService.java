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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;


    public Optional<Company> generateCompanyInfo(String companyName, String linkedinUrl) {
        try {
            log.info("Starting company information generation for: {} using Gemini AI", companyName);
            
            if (companyName == null || companyName.trim().isEmpty()) {
                log.warn("Company name is null or empty");
                return Optional.empty();
            }
            
            String prompt = buildCompanyGenerationPrompt(companyName, linkedinUrl);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            log.debug("Sending request to Gemini API for company: {}", companyName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_API_URL + "?key=" + apiKey,
                    HttpMethod.POST,
                    request,
                    String.class
                );
                
                log.debug("Gemini API response status: {}", response.getStatusCodeValue());
                
                if (response.getStatusCodeValue() == 200) {
                    log.info("Successfully received response from Gemini API for company: {}", companyName);
                    return parseGeminiResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                    return Optional.empty();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.error("Rate limit exceeded for Gemini API. Company: {}", companyName);
                    log.error("Rate limit error details: {}", e.getResponseBodyAsString());
                    throw new RuntimeException("Rate limit exceeded for Gemini API");
                } else {
                    log.error("HTTP error calling Gemini API for company: {}", companyName, e);
                    return Optional.empty();
                }
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof java.net.SocketTimeoutException) {
                    log.error("Request timeout for Gemini API. Company: {}", companyName, e);
                    throw new RuntimeException("Request timeout for Gemini API", e);
                } else {
                    log.error("Connection error calling Gemini API for company: {}", companyName, e);
                    return Optional.empty();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Request timeout for Gemini API. Company: {}", companyName, e);
                    throw e;
                } else {
                    log.error("Runtime error calling Gemini API for company: {}", companyName, e);
                    return Optional.empty();
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API for company: {}", companyName, e);
            return Optional.empty();
        }
    }

    public Optional<Company> extractCompanyInfoFromDocument(String documentText) {
        try {
            log.info("Starting company information extraction from document using Gemini AI");
            
            if (documentText == null || documentText.trim().isEmpty()) {
                log.warn("Document text is null or empty");
                return Optional.empty();
            }
            
            String prompt = buildPrompt(documentText);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            log.debug("Sending document extraction request to Gemini API");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    GEMINI_API_URL + "?key=" + apiKey,
                    HttpMethod.POST,
                    request,
                    String.class
                );
                
                log.debug("Gemini API response status: {}", response.getStatusCodeValue());
                
                if (response.getStatusCodeValue() == 200) {
                    log.info("Successfully received response from Gemini API for document extraction");
                    return parseGeminiResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                    return Optional.empty();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.error("Rate limit exceeded for Gemini API during document extraction");
                    log.error("Rate limit error details: {}", e.getResponseBodyAsString());
                    throw new RuntimeException("Rate limit exceeded for Gemini API");
                } else {
                    log.error("HTTP error calling Gemini API for document extraction", e);
                    return Optional.empty();
                }
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof java.net.SocketTimeoutException) {
                    log.error("Request timeout for Gemini API during document extraction", e);
                    throw new RuntimeException("Request timeout for Gemini API", e);
                } else {
                    log.error("Connection error calling Gemini API for document extraction", e);
                    return Optional.empty();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Request timeout for Gemini API during document extraction", e);
                    throw e;
                } else {
                    log.error("Runtime error calling Gemini API for document extraction", e);
                    return Optional.empty();
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API for document extraction", e);
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
               "  \"career_page_url\": \"\",\n" +
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
               "- For \"website\": Extract the main company website URL if available.\n" +
               "- For \"career_page_url\": Extract the company's career/jobs page URL if available.\n" +
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
                        
                        log.debug("Raw generated text from Gemini: {}", generatedText);
                        
                        // Extract JSON from the generated text (it might be wrapped in markdown)
                        String jsonText = extractJsonFromText(generatedText);
                        
                        log.debug("Extracted JSON text: {}", jsonText);
                        
                        try {
                            JsonNode companyData = objectMapper.readTree(jsonText);
                            return mapJsonToCompany(companyData);
                        } catch (Exception jsonParseException) {
                            log.error("Failed to parse extracted JSON: {}", jsonText, jsonParseException);
                            return Optional.empty();
                        }
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
        
        // Remove any comments or explanations before the JSON
        int jsonStart = text.indexOf('{');
        if (jsonStart > 0) {
            text = text.substring(jsonStart);
        }
        
        // Remove any text after the JSON object
        int jsonEnd = text.lastIndexOf('}');
        if (jsonEnd >= 0) {
            text = text.substring(0, jsonEnd + 1);
        }
        
        // Clean up any remaining whitespace or newlines
        text = text.trim();
        
        // Validate that we have a proper JSON object
        if (!text.startsWith("{") || !text.endsWith("}")) {
            log.warn("Extracted text does not appear to be valid JSON: {}", text);
            return "{}";
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
            company.setCareerPageUrl(getStringValue(companyData, "career_page_url"));
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

    private String buildCompanyGenerationPrompt(String companyName, String linkedinUrl) {
        return "You are a company information generator. Generate comprehensive and detailed information for the company: " + companyName + "\n\n" +
               "IMPORTANT: Return ONLY valid JSON without any comments, explanations, or markdown formatting.\n\n" +
               "JSON Schema:\n" +
               "{\n" +
               "  \"name\": \"" + companyName + "\",\n" +
               "  \"description\": \"\",\n" +
               "  \"logo_url\": \"\",\n" +
               "  \"website\": \"\",\n" +
               "  \"career_page_url\": \"\",\n" +
               "  \"about_us\": \"\",\n" +
               "  \"culture\": \"\",\n" +
               "  \"mission\": \"\",\n" +
               "  \"vision\": \"\",\n" +
               "  \"company_size\": \"\",\n" +
               "  \"headquarters\": \"\",\n" +
               "  \"industry\": \"\",\n" +
               "  \"linkedin_url\": \"" + linkedinUrl + "\",\n" +
               "  \"specialties\": \"\",\n" +
               "  \"is_crawled\": true,\n" +
               "  \"last_crawled_at\": \"" + Instant.now().toString() + "\"\n" +
               "}\n\n" +
               "Instructions:\n" +
               "- Return ONLY the JSON object, no additional text or formatting\n" +
               "- Do not include any comments, explanations, or markdown code blocks\n" +
               "- Ensure all string values are properly escaped\n" +
               "- For \"about_us\": Include detailed, comprehensive description with company history, achievements, business model, and full business description. Make it extensive and informative.\n" +
               "- For \"description\": Use a concise summary or tagline\n" +
               "- For \"website\": Provide the main company website URL (e.g., https://www.company.com)\n" +
               "- For \"career_page_url\": Provide the company's career/jobs page URL (e.g., https://careers.company.com or https://www.company.com/careers)\n" +
               "- For \"company_size\": Include employee count and growth information\n" +
               "- For \"headquarters\": Include city, state, and country\n" +
               "- For \"industry\": Be specific about the industry sector\n" +
               "- For \"specialties\": List key products, services, or technologies\n" +
               "- For \"culture\": Describe company culture, values, and work environment\n" +
               "- For \"mission\": Include company mission statement\n" +
               "- For \"vision\": Include company vision statement\n" +
               "- Ensure all information is accurate and up-to-date\n" +
               "- Make the content detailed and comprehensive, not brief summaries\n" +
               "- Provide comprehensive, detailed information about the company\n\n" +
               "Generate detailed information for: " + companyName + "\n\n" +
               "Return ONLY the JSON object:";
    }
} 