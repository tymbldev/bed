package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.CompanyContentRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiCompanyService {
    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;
    private final IndustryRepository industryRepository;
    private final CompanyContentRepository companyContentRepository;
    private final CompanyRepository companyRepository;

    public Optional<Company> generateCompanyInfo(String companyName) {
        try {
            log.info("Starting company information generation for: {} using Gemini AI", companyName);
            if (companyName == null || companyName.trim().isEmpty()) {
                log.warn("Company name is null or empty");
                return Optional.empty();
            }
            String prompt = buildCompanyGenerationPrompt(companyName);
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

    public Map<String, Object> detectCompanyIndustries(String companyName, String companyDescription, String specialties) {
        try {
            log.info("Starting company industry detection for: {} using Gemini AI", companyName);
            if (companyName == null || companyName.trim().isEmpty()) {
                log.warn("Company name is null or empty");
                return new HashMap<>();
            }
            String prompt = buildIndustryDetectionPrompt(companyName, companyDescription, specialties);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            log.debug("Sending request to Gemini API for industry detection: {}", companyName);
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
                    log.info("Successfully received response from Gemini API for industry detection: {}", companyName);
                    return parseIndustryResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                    return new HashMap<>();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.error("Rate limit exceeded for Gemini API. Company: {}", companyName);
                    log.error("Rate limit error details: {}", e.getResponseBodyAsString());
                    throw new RuntimeException("Rate limit exceeded for Gemini API");
                } else {
                    log.error("HTTP error calling Gemini API for industry detection: {}", companyName, e);
                    return new HashMap<>();
                }
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof java.net.SocketTimeoutException) {
                    log.error("Request timeout for Gemini API. Company: {}", companyName, e);
                    throw new RuntimeException("Request timeout for Gemini API", e);
                } else {
                    log.error("Connection error calling Gemini API for industry detection: {}", companyName, e);
                    return new HashMap<>();
                }
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                    log.error("Request timeout for Gemini API. Company: {}", companyName, e);
                    throw e;
                } else {
                    log.error("Runtime error calling Gemini API for industry detection: {}", companyName, e);
                    return new HashMap<>();
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API for industry detection: {}", companyName, e);
            return new HashMap<>();
        }
    }

    /**
     * Generate a list of companies (name, website) for a given industry, excluding provided names.
     * @param industryName The industry to generate companies for
     * @param excludeNames List of company names to ignore (case-insensitive)
     * @return List of maps with keys 'name' and 'website'
     */
    public List<Map<String, String>> generateCompanyListForIndustry(String industryName, List<String> excludeNames) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Give a random list of 500 real INDIAN companies in the '")
                  .append(industryName)
                  .append("' industry. Only include companies that are based in India or are Indian in origin. For each, provide company name and website. ");
            if (excludeNames != null && !excludeNames.isEmpty()) {
                prompt.append("Ignore these companies (do not include them in your response): ");
                prompt.append(String.join(", ", excludeNames));
                prompt.append(". ");
            }
            prompt.append("Your response MUST strictly match this schema: [{\"name\": string, \"website\": string}, ...] (500 items). ");
            prompt.append("Return ONLY a JSON array of 500 objects with 'name' and 'website' fields. ");
            prompt.append("Do NOT include any explanation, disclaimer, markdown, or text before or after the array—just the JSON array. ");
            prompt.append("Example: [{\"name\":\"Infosys\",\"website\":\"https://www.infosys.com\"}]");
            prompt.append("IMPORTANT: My system will parse your response as JSON. If you add any note, remark, explanation, or text before or after the array, the process will fail. Do NOT add anything except the JSON array.");

            Map<String, Object> requestBody = buildRequestBody(prompt.toString());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            Thread.sleep(1000); // Add a delay to avoid hitting rate limits too quickly
            if (response.getStatusCodeValue() == 200) {
                return parseCompanyListResponse(response.getBody());
            } else {
                log.error("Gemini API error (company list): {} - {}", response.getStatusCodeValue(), response.getBody());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error generating company list for industry {}: {}", industryName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private String buildCompanyGenerationPrompt(String companyName) {
        return "Generate detailed company information for: " + companyName + ". For each of the following fields, provide a detailed, well-written, and comprehensive response (not a short summary): " +
                "description, about_us, mission, vision, culture, specialties, company_size, headquarters, career_page_url, website, logo_url, linkedin_url. " +
                "For the fields website, logo_url, linkedin_url, and career_page_url: The value must be a valid URL (starting with http/https and domain structure). If not present, return null. Do NOT return explanations, search instructions, or noisy text—only a valid URL or null. " +
                "IMPORTANT: For ALL fields, if the information is not available or requires research, return the value as blank or null (do NOT use placeholders, example text, or instructions like '[Insert ...]'). " +
                "Accuracy in response format is critical. Do not include explanations, instructions, or any text other than the required value for each field. " +
                "Each field should be as detailed and informative as possible, suitable for a company profile page. Do NOT provide short summaries. Return the result as a JSON object with these fields as keys.";
    }

    private String buildIndustryDetectionPrompt(String companyName, String companyDescription, String specialties) {
        // Get all available industries from database
        List<Industry> allIndustries = industryRepository.findAll();
        List<String> industryNames = allIndustries.stream()
            .map(Industry::getName)
            .collect(Collectors.toList());
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following company details and identify its industries:\n");
        prompt.append("Company Name: ").append(companyName).append("\n");
        prompt.append("Company Description: ").append(companyDescription).append("\n");
        prompt.append("Specialties: ").append(specialties).append("\n\n");
        
        prompt.append("AVAILABLE PRIMARY INDUSTRIES (choose the most appropriate one):\n");
        for (String industry : industryNames) {
            prompt.append("- ").append(industry).append("\n");
        }
        
        prompt.append("\nSECONDARY INDUSTRIES (choose 2-4 additional relevant industries from the list above):\n");
        prompt.append("Examples of secondary industry combinations:\n");
        prompt.append("- For a fintech company: Primary: 'FinTech', Secondary: 'Financial Services, Software Development, Data Analytics & Business Intelligence'\n");
        prompt.append("- For a healthtech startup: Primary: 'Healthcare & HealthTech', Secondary: 'Software Development, Artificial Intelligence & Machine Learning (AI/ML), Mobile Applications'\n");
        prompt.append("- For an e-commerce platform: Primary: 'E-commerce & Online Retail', Secondary: 'Software Development, Marketing & Advertising Technology (MarTech/AdTech), Logistics & Supply Chain'\n");
        prompt.append("- For a cybersecurity firm: Primary: 'Cybersecurity', Secondary: 'Software Development, Information Technology & Services, Professional Services & Consulting'\n");
        prompt.append("- For an AI company: Primary: 'Artificial Intelligence & Machine Learning (AI/ML)', Secondary: 'Software Development, Data Analytics & Business Intelligence, Professional Services & Consulting'\n");
        
        prompt.append("\nINSTRUCTIONS:\n");
        prompt.append("1. For PRIMARY INDUSTRY: Select exactly ONE industry from the available list that best represents the company's main business focus.\n");
        prompt.append("2. For SECONDARY INDUSTRIES: Select 2-4 additional industries from the same list that are also relevant to the company's operations.\n");
        prompt.append("3. Ensure all selected industries are from the provided list - do not create new industry names.\n");
        prompt.append("4. Consider the company's description, specialties, and business model when making selections.\n\n");
        
        prompt.append("Provide response in this JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"primaryIndustry\": \"[selected primary industry name]\",\n");
        prompt.append("  \"secondaryIndustries\": [\"[industry1]\", \"[industry2]\", \"[industry3]\"]\n");
        prompt.append("}");
        
        return prompt.toString();
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
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.debug("Raw generated text from Gemini: {}", generatedText);
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

    private Map<String, Object> parseIndustryResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.debug("Raw generated text from Gemini: {}", generatedText);
                        String jsonText = extractJsonFromText(generatedText);
                        log.debug("Extracted JSON text: {}", jsonText);
                        try {
                            JsonNode industryData = objectMapper.readTree(jsonText);
                            return mapJsonToIndustries(industryData);
                        } catch (Exception jsonParseException) {
                            log.error("Failed to parse extracted JSON: {}", jsonText, jsonParseException);
                            return new HashMap<>();
                        }
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for industries: {}", responseBody);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Error parsing Gemini response for industries", e);
            return new HashMap<>();
        }
    }

    private List<Map<String, String>> parseCompanyListResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        String jsonText = extractJsonFromText(generatedText);
                        try {
                            JsonNode arr = objectMapper.readTree(jsonText);
                            if (arr.isArray()) {
                                List<Map<String, String>> result = new ArrayList<>();
                                for (JsonNode node : arr) {
                                    String name = getStringValue(node, "name");
                                    String website = getStringValue(node, "website");
                                    if (!name.isEmpty()) {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("website", website);
                                        result.add(map);
                                    }
                                }
                                return result;
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse company list JSON: {}", jsonText, e);
                        }
                    }
                }
            }
            log.error("Unexpected Gemini API response structure (company list): {}", responseBody);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error parsing Gemini response (company list)", e);
            return Collections.emptyList();
        }
    }

    private String extractJsonFromText(String text) {
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        text = text.trim();
        // If it's a JSON array, return as is
        if (text.startsWith("[") && text.endsWith("]")) {
            return text;
        }
        // If it's a JSON object, return as is
        if (text.startsWith("{") && text.endsWith("}")) {
            return text;
        }
        // Try to extract the first JSON array from the text
        int arrayStart = text.indexOf('[');
        int arrayEnd = text.lastIndexOf(']');
        if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
            return text.substring(arrayStart, arrayEnd + 1);
        }
        // Fallback to old logic (try to extract JSON object)
        int objStart = text.indexOf('{');
        int objEnd = text.lastIndexOf('}');
        if (objStart != -1 && objEnd != -1 && objEnd > objStart) {
            return text.substring(objStart, objEnd + 1);
        }
        // If nothing found, return as is (will likely fail to parse)
        return text;
    }

    private Optional<Company> mapJsonToCompany(JsonNode companyData) {
        try {
            Company company = new Company();
            CompanyContent companyContent = new CompanyContent();
            boolean aiError = false;
            String name = getStringValue(companyData, "name");
            if (!containsWebSearchPlaceholder(name)) company.setName(name); else aiError = true;
            String description = getStringValue(companyData, "description");
            if (!containsWebSearchPlaceholder(description)) company.setDescription(description); else aiError = true;
            String logoUrl = getStringValue(companyData, "logo_url");
            if (!containsWebSearchPlaceholder(logoUrl) && isValidUrl(logoUrl)) company.setLogoUrl(logoUrl);
            else { company.setLogoUrl(""); aiError = true; }
            String website = getStringValue(companyData, "website");
            if (!containsWebSearchPlaceholder(website) && isValidUrl(website)) company.setWebsite(website);
            else { company.setWebsite(""); aiError = true; }
            String careerPageUrl = getStringValue(companyData, "career_page_url");
            if (!containsWebSearchPlaceholder(careerPageUrl)) company.setCareerPageUrl(careerPageUrl); else aiError = true;
            String aboutUs = getStringValue(companyData, "about_us");
            if (!containsWebSearchPlaceholder(aboutUs)) companyContent.setAboutUsOriginal(aboutUs); else aiError = true;
            String culture = getStringValue(companyData, "culture");
            if (!containsWebSearchPlaceholder(culture)) companyContent.setCultureOriginal(culture); else aiError = true;
            String mission = getStringValue(companyData, "mission");
            if (!containsWebSearchPlaceholder(mission)) company.setMission(mission); else aiError = true;
            String vision = getStringValue(companyData, "vision");
            if (!containsWebSearchPlaceholder(vision)) company.setVision(vision); else aiError = true;
            String companySize = getStringValue(companyData, "company_size");
            if (!containsWebSearchPlaceholder(companySize)) company.setCompanySize(companySize); else aiError = true;
            String headquarters = getStringValue(companyData, "headquarters");
            if (!containsWebSearchPlaceholder(headquarters)) company.setHeadquarters(headquarters); else aiError = true;
            String linkedinUrl = getStringValue(companyData, "linkedin_url");
            if (!containsWebSearchPlaceholder(linkedinUrl) && isValidUrl(linkedinUrl)) company.setLinkedinUrl(linkedinUrl);
            else { company.setLinkedinUrl(""); aiError = true; }
            String specialties = getStringValue(companyData, "specialties");
            if (!containsWebSearchPlaceholder(specialties)) company.setSpecialties(specialties); else aiError = true;
            company.setAiError(aiError);
            
            // Save company first to get the ID, then save company content
            company = companyRepository.save(company);
            companyContent.setCompanyId(company.getId());
            companyContentRepository.save(companyContent);
            
            return Optional.of(company);
        } catch (Exception e) {
            log.error("Error mapping JSON to Company", e);
            return Optional.empty();
        }
    }

    private boolean containsWebSearchPlaceholder(String value) {
        return value != null && value.contains("This information requires a web search");
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return false;
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> mapJsonToIndustries(JsonNode industryData) {
        Map<String, Object> industries = new HashMap<>();
        
        try {
            // Handle new format with primary and secondary industries
            if (industryData.has("primaryIndustry") && industryData.has("secondaryIndustries")) {
                String primaryIndustry = getStringValue(industryData, "primaryIndustry");
                List<String> secondaryIndustries = new ArrayList<>();
                
                JsonNode secondaryArray = industryData.get("secondaryIndustries");
                if (secondaryArray.isArray()) {
                    for (JsonNode industryNode : secondaryArray) {
                        if (industryNode.isTextual()) {
                            secondaryIndustries.add(industryNode.asText());
                        }
                    }
                }
                
                industries.put("primaryIndustry", primaryIndustry);
                industries.put("secondaryIndustries", secondaryIndustries);
                
                // Also maintain backward compatibility with the old "industries" format
                List<String> allIndustries = new ArrayList<>();
                allIndustries.add(primaryIndustry);
                allIndustries.addAll(secondaryIndustries);
                industries.put("industries", allIndustries);
                
                log.debug("Parsed industry data - Primary: {}, Secondary: {}", primaryIndustry, secondaryIndustries);
            } else if (industryData.isArray()) {
                // Handle old array format for backward compatibility
                List<String> detectedIndustries = new ArrayList<>();
                for (JsonNode industryNode : industryData) {
                    if (industryNode.isTextual()) {
                        detectedIndustries.add(industryNode.asText());
                    }
                }
                industries.put("industries", detectedIndustries);
                
                // If we have industries, treat the first one as primary and rest as secondary
                if (!detectedIndustries.isEmpty()) {
                    industries.put("primaryIndustry", detectedIndustries.get(0));
                    industries.put("secondaryIndustries", detectedIndustries.subList(1, detectedIndustries.size()));
                }
            } else {
                log.warn("Unexpected industry data format: {}", industryData);
            }
        } catch (Exception e) {
            log.error("Error parsing industry data: {}", industryData, e);
        }
        
        return industries;
    }

    private String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText("");
        }
        return "";
    }
} 