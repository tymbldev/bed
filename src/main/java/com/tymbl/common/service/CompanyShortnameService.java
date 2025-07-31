package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class CompanyShortnameService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;

    private final CompanyRepository companyRepository;

    // Bean for transactional operations
    private final CompanyShortnameTransactionService transactionService;

    public Map<String, Object> generateShortnamesForAllCompanies(List<String> companyNames) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> companyResults = new ArrayList<>();
        
        int totalCompanies = companyNames.size();
        int totalShortnamesGenerated = 0;
        int totalErrors = 0;
        
        log.info("Starting AI-powered shortname generation for {} companies", totalCompanies);
        
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
        result.put("message", "AI-powered shortname generation completed for all companies");
        
        log.info("AI-powered shortname generation completed. Total companies: {}, Total shortnames: {}, Errors: {}", 
                totalCompanies, totalShortnamesGenerated, totalErrors);
        
        return result;
    }

    public Map<String, Object> generateShortnameForSingleCompany(String companyName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Generating AI-powered shortname for company: {}", companyName);
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
            String prompt = buildRobustShortnameGenerationPrompt(companyName);
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

    /**
     * Internal method for generating shortnames that can be called by the transaction service
     */
    public String generateShortnameForCompanyInternal(String companyName) {
        return generateShortnameForCompany(companyName);
    }

    private String buildRobustShortnameGenerationPrompt(String companyName) {
        return String.format(
            "You are a business and technology expert helping to identify the most commonly used and recognized shortnames for companies. " +
            "Given the company name '%s', provide the most widely recognized and commonly used shortname or nickname for this company.\n\n" +
            "CRITICAL REQUIREMENTS:\n" +
            "1. Focus on the CORE company name that people actually use in conversation\n" +
            "2. Remove ALL legal suffixes, descriptive text, and extra information\n" +
            "3. Return the shortest, most recognizable version of the company name\n" +
            "4. Consider how people actually refer to the company in daily conversation\n" +
            "5. If no widely recognized shortname exists, return the cleaned core name\n\n" +
            "COMPREHENSIVE PATTERNS TO HANDLE:\n" +
            "LEGAL SUFFIXES: Remove 'Limited', 'Ltd', 'Ltd.', 'Private Limited', 'Pvt Ltd', 'Incorporated', 'Inc', 'Inc.', 'Corporation', 'Corp', 'Corp.', 'Company', 'Co', 'Co.'\n" +
            "BUSINESS TYPES: Remove 'Group', 'Grp', 'Holdings', 'Holding', 'Enterprises', 'Enterprise', 'Industries', 'Industry', 'Solutions', 'Solution'\n" +
            "TECHNOLOGY TERMS: Remove 'Technologies', 'Technology', 'Tech', 'Tech.', 'Systems', 'System', 'Services', 'Service'\n" +
            "GEOGRAPHIC TERMS: Remove 'International', 'Intl', 'Intl.', 'Global', 'Worldwide', 'World Wide'\n" +
            "INTERNATIONAL FORMATS: Remove 'PLC', 'LLC', 'AG', 'GmbH', 'SA', 'NV', 'BV', 'AB', 'OY', 'AS', 'KK'\n" +
            "ASIAN FORMATS: Remove 'Kabushiki Kaisha', 'K.K.', 'Yugen Kaisha', 'Y.K.', 'Godo Kaisha', 'G.K.'\n" +
            "EUROPEAN FORMATS: Remove 'Societe Anonyme', 'S.A.', 'Societa per Azioni', 'S.p.A.', 'Sociedad Anonima', 'Naamloze Vennootschap', 'N.V.', 'Besloten Vennootschap', 'B.V.'\n" +
            "PUNCTUATION: Remove trailing periods, commas, semicolons, and other punctuation\n" +
            "EXTRA TEXT: Remove any additional descriptive text, taglines, or marketing phrases\n\n" +
            "EXAMPLES:\n" +
            "- 'Zydus Lifesciences Limited' → 'Zydus Lifesciences'\n" +
            "- 'Microsoft Corporation' → 'Microsoft'\n" +
            "- 'Apple Inc.' → 'Apple'\n" +
            "- 'Google LLC' → 'Google'\n" +
            "- 'Amazon.com, Inc.' → 'Amazon'\n" +
            "- 'Tesla, Inc.' → 'Tesla'\n" +
            "- 'Netflix, Inc.' → 'Netflix'\n" +
            "- 'Meta Platforms, Inc.' → 'Meta'\n" +
            "- 'Alphabet Inc.' → 'Alphabet'\n" +
            "- 'NVIDIA Corporation' → 'NVIDIA'\n\n" +
            "RESPONSE FORMAT:\n" +
            "Return ONLY the shortname, nothing else. No explanations, no quotes, no additional text.\n\n" +
            "Company name: %s\n" +
            "Shortname:",
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

    /**
     * Process all companies shortname generation and deduplication using database pagination
     */
    public Map<String, Object> processAllCompaniesShortnameGenerationAndDeduplicationInBatches() {
        log.info("Starting shortname generation and deduplication for all companies using database pagination");
        
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalShortnamesGenerated = 0;
        int totalDeleted = 0;
        int totalErrors = 0;
        int batchSize = 10; // Process 10 companies at a time
        int pageNumber = 0;
        
        while (true) {
            // Fetch companies in batches using database pagination
            Page<Company> companyPage = companyRepository.findByShortnameGeneratedFalse(
                PageRequest.of(pageNumber, batchSize)
            );
            
            List<Company> companies = companyPage.getContent();
            
            if (companies.isEmpty()) {
                log.info("No more companies to process. Completed at page: {}", pageNumber);
                break;
            }
            
            log.info("Processing shortname generation and deduplication batch {} with {} companies", pageNumber + 1, companies.size());
            
            for (Company company : companies) {
                try {
                    // Use the transaction service to ensure proper transaction handling
                    Map<String, Object> result = transactionService.processCompanyShortnameGenerationAndDeduplicationInTransaction(company);
                    companyResults.add(result);
                    
                    if ((Boolean) result.get("success")) {
                        String action = (String) result.get("action");
                        if ("created".equals(action) || "updated".equals(action)) {
                            totalShortnamesGenerated++;
                        } else if ("deleted".equals(action)) {
                            totalDeleted++;
                        }
                        log.info("Successfully processed company: {} (ID: {}) - Action: {}", company.getName(), company.getId(), action);
                    } else {
                        totalErrors++;
                        log.error("Failed to process company: {} (ID: {})", company.getName(), company.getId());
                    }
                    
                    totalProcessed++;
                    
                } catch (Exception e) {
                    totalErrors++;
                    log.error("Error processing company: {} (ID: {})", company.getName(), company.getId(), e);
                    
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("companyId", company.getId());
                    errorResult.put("companyName", company.getName());
                    errorResult.put("success", false);
                    errorResult.put("error", "Error processing company: " + e.getMessage());
                    companyResults.add(errorResult);
                }
            }
            
            pageNumber++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", totalProcessed);
        result.put("totalShortnamesGenerated", totalShortnamesGenerated);
        result.put("totalDeleted", totalDeleted);
        result.put("totalErrors", totalErrors);
        result.put("companyResults", companyResults);
        result.put("message", "Shortname generation and deduplication completed using database pagination");
        
        log.info("Completed shortname generation and deduplication using database pagination. Total processed: {}, Shortnames generated: {}, Deleted: {}, Errors: {}", 
                totalProcessed, totalShortnamesGenerated, totalDeleted, totalErrors);
        
        return result;
    }
} 