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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.ArrayList;

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

    public List<String> generateDesignationsForDepartment(String departmentName) {
        try {
            log.info("Starting designation generation for department: {} using Gemini AI", departmentName);
            
            if (departmentName == null || departmentName.trim().isEmpty()) {
                log.warn("Department name is null or empty");
                return Collections.emptyList();
            }
            
            String prompt = buildDesignationGenerationPrompt(departmentName);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            log.debug("Sending request to Gemini API for department: {}", departmentName);
            
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
                    log.info("Successfully received response from Gemini API for department: {}", departmentName);
                    return parseDesignationResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                    return Collections.emptyList();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.error("Rate limit exceeded for Gemini API. Department: {}", departmentName);
                    log.error("Rate limit error details: {}", e.getResponseBodyAsString());
                    throw new RuntimeException("Rate limit exceeded for Gemini API");
                } else {
                    log.error("HTTP error calling Gemini API for department: {}", departmentName, e);
                    return Collections.emptyList();
                }
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof java.net.SocketTimeoutException) {
                    log.error("Request timeout for Gemini API. Department: {}", departmentName, e);
                    throw new RuntimeException("Request timeout for Gemini API", e);
                } else {
                    log.error("Connection error calling Gemini API for department: {}", departmentName, e);
                    return Collections.emptyList();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Request timeout for Gemini API. Department: {}", departmentName, e);
                    throw e;
                } else {
                    log.error("Runtime error calling Gemini API for department: {}", departmentName, e);
                    return Collections.emptyList();
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API for department: {}", departmentName, e);
            return Collections.emptyList();
        }
    }

    private String buildDesignationGenerationPrompt(String departmentName) {
        return "You are a job designation generator for the " + departmentName + " department. Generate a comprehensive list of job titles and designations that are commonly used in this department across different companies and industries.\n\n" +
               "IMPORTANT: Return ONLY valid JSON without any comments, explanations, or markdown formatting.\n\n" +
               "JSON Schema:\n" +
               "{\n" +
               "  \"designations\": [\n" +
               "    {\n" +
               "      \"name\": \"\",\n" +
               "      \"level\": 1,\n" +
               "      \"description\": \"\"\n" +
               "    }\n" +
               "  ]\n" +
               "}\n\n" +
               "Instructions:\n" +
               "- Return ONLY the JSON object, no additional text or formatting\n" +
               "- Do not include any comments, explanations, or markdown code blocks\n" +
               "- Generate 15-25 relevant designations for the " + departmentName + " department\n" +
               "- Include designations from entry-level to senior/executive level\n" +
               "- Use standard industry terminology and job titles\n" +
               "- For \"level\": Use 1 for entry-level, 2 for mid-level, 3 for senior-level, 4 for lead/manager, 5 for director/executive\n" +
               "- For \"description\": Provide a brief description of the role and responsibilities\n" +
               "- Ensure designations are relevant to the " + departmentName + " department\n" +
               "- Include both technical and non-technical roles as appropriate\n" +
               "- Use current industry-standard job titles\n\n" +
               "Generate designations for: " + departmentName + " Department\n\n" +
               "Return ONLY the JSON object:";
    }

    private List<String> parseDesignationResponse(String responseBody) {
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
                            JsonNode designationData = objectMapper.readTree(jsonText);
                            return mapJsonToDesignations(designationData);
                        } catch (Exception jsonParseException) {
                            log.error("Failed to parse extracted JSON: {}", jsonText, jsonParseException);
                            return Collections.emptyList();
                        }
                    }
                }
            }
            
            log.error("Unexpected Gemini API response structure: {}", responseBody);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error parsing Gemini response", e);
            return Collections.emptyList();
        }
    }

    private List<String> mapJsonToDesignations(JsonNode designationData) {
        try {
            List<String> designations = new java.util.ArrayList<>();
            JsonNode designationsArray = designationData.get("designations");
            
            if (designationsArray != null && designationsArray.isArray()) {
                for (JsonNode designation : designationsArray) {
                    String name = getStringValue(designation, "name");
                    if (name != null && !name.trim().isEmpty()) {
                        designations.add(name.trim());
                    }
                }
            }
            
            return designations;
        } catch (Exception e) {
            log.error("Error mapping JSON to designations list", e);
            return Collections.emptyList();
        }
    }

    // New methods for interview question generation

    public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("Generating {} general interview questions for designation: {}, topic: {}, difficulty: {}", 
                    numQuestions, designation, topicName, difficultyLevel);
            
            String prompt = buildGeneralQuestionGenerationPrompt(designation, topicName, difficultyLevel, numQuestions);
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
            
            if (response.getStatusCodeValue() == 200) {
                log.info("Successfully generated general interview questions for designation: {}", designation);
                return parseQuestionGenerationResponse(response.getBody());
            } else {
                log.error("Gemini API error generating questions: {} - {}", response.getStatusCodeValue(), response.getBody());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error generating general interview questions for designation: {}", designation, e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("Generating {} company-specific interview questions for company: {}, designation: {}, topic: {}, difficulty: {}", 
                    numQuestions, companyName, designation, topicName, difficultyLevel);
            
            String prompt = buildCompanySpecificQuestionGenerationPrompt(companyName, designation, topicName, difficultyLevel, numQuestions);
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
            
            if (response.getStatusCodeValue() == 200) {
                log.info("Successfully generated company-specific interview questions for company: {}", companyName);
                return parseQuestionGenerationResponse(response.getBody());
            } else {
                log.error("Gemini API error generating company questions: {} - {}", response.getStatusCodeValue(), response.getBody());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error generating company-specific interview questions for company: {}", companyName, e);
            return Collections.emptyList();
        }
    }

    private String buildGeneralQuestionGenerationPrompt(String designation, String topicName, String difficultyLevel, int numQuestions) {
        return String.format(
            "You are an expert technical interviewer and educator. Generate %d high-quality interview questions for a %s position focusing on the topic: '%s'. " +
            "The questions should be at %s difficulty level.\n\n" +
            "Requirements:\n" +
            "1. Questions should be practical and relevant to real-world scenarios\n" +
            "2. Include a mix of theoretical, practical, behavioral, problem-solving, and system design questions\n" +
            "3. Provide detailed, comprehensive answers with examples and explanations\n" +
            "4. Use HTML formatting for better readability\n" +
            "5. Include code examples where relevant\n" +
            "6. Tag each question with appropriate categories\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"question_text\": \"<h3>Question Title</h3><p>Detailed question with context...</p>\",\n" +
            "      \"answer_text\": \"<h4>Answer</h4><p>Comprehensive answer with examples...</p><h4>Key Points</h4><ul><li>Point 1</li><li>Point 2</li></ul>\",\n" +
            "      \"difficulty_level\": \"%s\",\n" +
            "      \"question_type\": \"THEORETICAL|PRACTICAL|BEHAVIORAL|PROBLEM_SOLVING|SYSTEM_DESIGN\",\n" +
            "      \"tags\": \"tag1,tag2,tag3\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Focus on creating questions that would help candidates prepare for actual interviews at top tech companies.",
            numQuestions, designation, topicName, difficultyLevel.toLowerCase(), difficultyLevel
        );
    }

    private String buildCompanySpecificQuestionGenerationPrompt(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        return String.format(
            "You are an expert technical interviewer familiar with %s's interview process and technical requirements. " +
            "Generate %d company-specific interview questions for a %s position at %s, focusing on the topic: '%s'. " +
            "The questions should be at %s difficulty level.\n\n" +
            "Requirements:\n" +
            "1. Questions should reflect %s's technology stack, business domain, and interview style\n" +
            "2. Include questions that reference %s's products, services, or technical challenges\n" +
            "3. Provide detailed, comprehensive answers with company-specific context\n" +
            "4. Use HTML formatting for better readability\n" +
            "5. Include code examples where relevant\n" +
            "6. Tag each question with appropriate categories\n" +
            "7. Add company context explaining why this question is relevant to %s\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"question_text\": \"<h3>Question Title</h3><p>Detailed question with %s context...</p>\",\n" +
            "      \"answer_text\": \"<h4>Answer</h4><p>Comprehensive answer with %s-specific examples...</p><h4>Key Points</h4><ul><li>Point 1</li><li>Point 2</li></ul>\",\n" +
            "      \"difficulty_level\": \"%s\",\n" +
            "      \"question_type\": \"THEORETICAL|PRACTICAL|BEHAVIORAL|PROBLEM_SOLVING|SYSTEM_DESIGN\",\n" +
            "      \"company_context\": \"Explanation of why this question is relevant to %s\",\n" +
            "      \"tags\": \"tag1,tag2,tag3\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Focus on creating questions that would help candidates prepare for actual interviews at %s.",
            companyName, numQuestions, designation, companyName, topicName, difficultyLevel.toLowerCase(),
            companyName, companyName, companyName, companyName, companyName, difficultyLevel, companyName, companyName
        );
    }

    private List<Map<String, Object>> parseQuestionGenerationResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    String jsonText = extractJsonFromText(text);
                    
                    if (jsonText != null) {
                        JsonNode questionsData = objectMapper.readTree(jsonText);
                        return mapJsonToQuestions(questionsData);
                    }
                }
            }
            
            log.warn("Could not parse question generation response: {}", responseBody);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error parsing question generation response", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> mapJsonToQuestions(JsonNode questionsData) {
        try {
            List<Map<String, Object>> questions = new ArrayList<>();
            
            if (questionsData.has("questions") && questionsData.get("questions").isArray()) {
                for (JsonNode question : questionsData.get("questions")) {
                    Map<String, Object> questionMap = new HashMap<>();
                    questionMap.put("question_text", getStringValue(question, "question_text"));
                    questionMap.put("answer_text", getStringValue(question, "answer_text"));
                    questionMap.put("difficulty_level", getStringValue(question, "difficulty_level"));
                    questionMap.put("question_type", getStringValue(question, "question_type"));
                    questionMap.put("tags", getStringValue(question, "tags"));
                    
                    if (question.has("company_context")) {
                        questionMap.put("company_context", getStringValue(question, "company_context"));
                    }
                    
                    questions.add(questionMap);
                }
            }
            
            return questions;
        } catch (Exception e) {
            log.error("Error mapping JSON to questions", e);
            return Collections.emptyList();
        }
    }

    // New method for generating topics for a designation
    public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
        try {
            log.info("Generating top 10 topics for designation: {}", designationName);
            
            String prompt = buildTopicGenerationPrompt(designationName);
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
            
            if (response.getStatusCodeValue() == 200) {
                log.info("Successfully generated topics for designation: {}", designationName);
                return parseTopicGenerationResponse(response.getBody());
            } else {
                log.error("Gemini API error generating topics: {} - {}", response.getStatusCodeValue(), response.getBody());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error generating topics for designation: {}", designationName, e);
            return Collections.emptyList();
        }
    }

    private String buildTopicGenerationPrompt(String designationName) {
        return String.format(
            "You are an expert in career development and technical skills assessment. Generate the top 10 most important topics/skills for a %s position.\n\n" +
            "Requirements:\n" +
            "1. Include both technical and non-technical skills\n" +
            "2. Technical skills can be programming languages, frameworks, tools, methodologies\n" +
            "3. Non-technical skills can be soft skills, management skills, business skills\n" +
            "4. Focus on skills that are most relevant for this role in today's job market\n" +
            "5. Provide a brief description for each topic explaining why it's important\n" +
            "6. Categorize each topic as TECHNICAL, BEHAVIORAL, or PRODUCT\n" +
            "7. Assign appropriate difficulty levels (BEGINNER, INTERMEDIATE, ADVANCED)\n" +
            "8. Estimate preparation time in hours\n\n" +
            "Examples:\n" +
            "- Software Engineer: Java, Python, System Design, Agile Methodologies, Problem Solving\n" +
            "- Engineering Manager: Stakeholder Management, Team Leadership, Technical Architecture, Project Management, Communication\n" +
            "- Data Scientist: Machine Learning, Python, Statistical Analysis, Data Visualization, Business Acumen\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"topics\": [\n" +
            "    {\n" +
            "      \"topic_name\": \"Topic Name\",\n" +
            "      \"topic_description\": \"Brief description of why this topic is important for %s\",\n" +
            "      \"difficulty_level\": \"BEGINNER|INTERMEDIATE|ADVANCED\",\n" +
            "      \"category\": \"TECHNICAL|BEHAVIORAL|PRODUCT\",\n" +
            "      \"estimated_prep_time_hours\": 10\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Generate exactly 10 topics that would be most valuable for a %s to master for career success.",
            designationName, designationName, designationName
        );
    }

    private List<Map<String, Object>> parseTopicGenerationResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText();
                    String jsonText = extractJsonFromText(text);
                    
                    if (jsonText != null) {
                        JsonNode topicsData = objectMapper.readTree(jsonText);
                        return mapJsonToTopics(topicsData);
                    }
                }
            }
            
            log.warn("Could not parse topic generation response: {}", responseBody);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error parsing topic generation response", e);
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> mapJsonToTopics(JsonNode topicsData) {
        try {
            List<Map<String, Object>> topics = new ArrayList<>();
            
            if (topicsData.has("topics") && topicsData.get("topics").isArray()) {
                for (JsonNode topic : topicsData.get("topics")) {
                    Map<String, Object> topicMap = new HashMap<>();
                    topicMap.put("topic_name", getStringValue(topic, "topic_name"));
                    topicMap.put("topic_description", getStringValue(topic, "topic_description"));
                    topicMap.put("difficulty_level", getStringValue(topic, "difficulty_level"));
                    topicMap.put("category", getStringValue(topic, "category"));
                    topicMap.put("estimated_prep_time_hours", topic.has("estimated_prep_time_hours") ? topic.get("estimated_prep_time_hours").asInt() : 10);
                    
                    topics.add(topicMap);
                }
            }
            
            return topics;
        } catch (Exception e) {
            log.error("Error mapping JSON to topics", e);
            return Collections.emptyList();
        }
    }
} 