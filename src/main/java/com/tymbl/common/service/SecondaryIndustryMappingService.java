package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.SecondaryIndustryMapping;
import com.tymbl.common.repository.SecondaryIndustryMappingRepository;
import com.tymbl.jobs.repository.CompanyRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecondaryIndustryMappingService {

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SecondaryIndustryMappingRepository secondaryIndustryMappingRepository;
    private final CompanyRepository companyRepository;

    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;

    @Transactional
    public Map<String, Object> processSecondaryIndustries() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> industryResults = new ArrayList<>();
        
        try {
            log.info("Starting secondary industry processing");
            
            // Get all unique secondary industries from companies table
            List<String> allSecondaryIndustries = getAllUniqueSecondaryIndustries();
            log.info("Found {} unique secondary industries to process", allSecondaryIndustries.size());
            
            int totalIndustries = allSecondaryIndustries.size();
            int totalProcessed = 0;
            int totalErrors = 0;
            int totalSkipped = 0;
            
            // Process each secondary industry
            for (String industryName : allSecondaryIndustries) {
                Map<String, Object> industryResult = new HashMap<>();
                industryResult.put("industryName", industryName);
                
                try {
                    // Check if already processed
                    if (secondaryIndustryMappingRepository.existsByName(industryName)) {
                        industryResult.put("success", false);
                        industryResult.put("message", "Already processed");
                        industryResult.put("skipped", true);
                        totalSkipped++;
                        log.info("Skipping already processed industry: {}", industryName);
                        continue;
                    }
                    
                    // Generate mapping using AI
                    String mappedName = generateMappedName(industryName);
                    
                    if (mappedName != null && !mappedName.trim().isEmpty()) {
                        // Get or create mapped ID
                        Long mappedId = getOrCreateMappedId(mappedName);
                        
                        // Save the mapping
                        SecondaryIndustryMapping mapping = new SecondaryIndustryMapping();
                        mapping.setName(industryName);
                        mapping.setMappedId(mappedId);
                        mapping.setMappedName(mappedName);
                        mapping.setProcessed(true);
                        
                        secondaryIndustryMappingRepository.save(mapping);
                        
                        industryResult.put("success", true);
                        industryResult.put("mappedName", mappedName);
                        industryResult.put("mappedId", mappedId);
                        totalProcessed++;
                        
                        log.info("Processed industry '{}' → mapped to '{}' (ID: {})", industryName, mappedName, mappedId);
                    } else {
                        industryResult.put("success", false);
                        industryResult.put("error", "No mapped name generated");
                        totalErrors++;
                        log.warn("Failed to generate mapped name for industry: {}", industryName);
                    }
                    
                } catch (Exception e) {
                    industryResult.put("success", false);
                    industryResult.put("error", "Error processing industry: " + e.getMessage());
                    totalErrors++;
                    log.error("Error processing industry: {}", industryName, e);
                }
                
                industryResults.add(industryResult);
            }
            
            result.put("totalIndustries", totalIndustries);
            result.put("totalProcessed", totalProcessed);
            result.put("totalErrors", totalErrors);
            result.put("totalSkipped", totalSkipped);
            result.put("industryResults", industryResults);
            result.put("message", "Secondary industry processing completed");
            
            log.info("Secondary industry processing completed. Total: {}, Processed: {}, Errors: {}, Skipped: {}", 
                    totalIndustries, totalProcessed, totalErrors, totalSkipped);
            
        } catch (Exception e) {
            log.error("Error in secondary industry processing", e);
            result.put("error", "Error in secondary industry processing: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> processSingleSecondaryIndustry(String industryName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Processing single secondary industry: {}", industryName);
            
            // Check if already processed
            if (secondaryIndustryMappingRepository.existsByName(industryName)) {
                SecondaryIndustryMapping existing = secondaryIndustryMappingRepository.findByName(industryName).orElse(null);
                result.put("success", false);
                result.put("message", "Already processed");
                result.put("existingMapping", existing != null ? Map.of(
                    "mappedName", existing.getMappedName(),
                    "mappedId", existing.getMappedId()
                ) : null);
                return result;
            }
            
            // Generate mapping using AI
            String mappedName = generateMappedName(industryName);
            
            if (mappedName != null && !mappedName.trim().isEmpty()) {
                // Get or create mapped ID
                Long mappedId = getOrCreateMappedId(mappedName);
                
                // Save the mapping
                SecondaryIndustryMapping mapping = new SecondaryIndustryMapping();
                mapping.setName(industryName);
                mapping.setMappedId(mappedId);
                mapping.setMappedName(mappedName);
                mapping.setProcessed(true);
                
                secondaryIndustryMappingRepository.save(mapping);
                
                result.put("success", true);
                result.put("industryName", industryName);
                result.put("mappedName", mappedName);
                result.put("mappedId", mappedId);
                
                log.info("Successfully processed industry '{}' → mapped to '{}' (ID: {})", industryName, mappedName, mappedId);
            } else {
                result.put("success", false);
                result.put("error", "No mapped name generated for industry: " + industryName);
                log.warn("No mapped name generated for industry: {}", industryName);
            }
            
        } catch (Exception e) {
            log.error("Error processing single industry: {}", industryName, e);
            result.put("success", false);
            result.put("error", "Error processing industry: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> resetProcessedFlag() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Resetting processed flag for all secondary industry mappings");
            secondaryIndustryMappingRepository.resetProcessedFlag();
            
            result.put("success", true);
            result.put("message", "Processed flag reset successfully for all secondary industry mappings");
            
            log.info("Successfully reset processed flag for all secondary industry mappings");
            
        } catch (Exception e) {
            log.error("Error resetting processed flag", e);
            result.put("success", false);
            result.put("error", "Error resetting processed flag: " + e.getMessage());
        }
        
        return result;
    }

    private List<String> getAllUniqueSecondaryIndustries() {
        // Get all secondary industries from companies table
        List<String> allSecondaryIndustries = companyRepository.findAll().stream()
            .filter(company -> company.getSecondaryIndustries() != null && !company.getSecondaryIndustries().trim().isEmpty())
            .map(company -> company.getSecondaryIndustries().split(","))
            .flatMap(Arrays::stream)
            .map(String::trim)
            .filter(industry -> !industry.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        
        log.info("Extracted {} unique secondary industries from companies table", allSecondaryIndustries.size());
        return allSecondaryIndustries;
    }

    private String generateMappedName(String industryName) {
        try {
            log.info("[Gemini] Generating mapped name for industry: {}", industryName);
            String prompt = buildMappingPrompt(industryName);
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
                String mappedName = parseMappedNameResponse(response.getBody());
                log.info("[Gemini] Generated mapped name '{}' for industry: {}", mappedName, industryName);
                return mappedName;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCode().value(), response.getBody());
                return null;
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating mapped name for industry: {}", industryName, e);
            return null;
        }
    }

    private String buildMappingPrompt(String industryName) {
        return String.format(
            "You are a business and industry classification expert helping to standardize secondary industry names. " +
            "Given the secondary industry name '%s', provide the most appropriate standardized/canonical name that should be used as the parent category.\n\n" +
            "INSTRUCTIONS:\n" +
            "1. Identify the most appropriate standardized name for this industry\n" +
            "2. Consider common variations and synonyms\n" +
            "3. Use the most widely recognized and professional term\n" +
            "4. Group similar variations under the same parent name\n" +
            "5. Prefer official industry classifications over informal terms\n\n" +
            "EXAMPLES:\n" +
            "- 'Fortune 500' → 'Fortune 500'\n" +
            "- 'Fortune500' → 'Fortune 500'\n" +
            "- 'Fortune 500 Top' → 'Fortune 500'\n" +
            "- 'F500' → 'Fortune 500'\n" +
            "- 'Tech' → 'Technology'\n" +
            "- 'IT' → 'Information Technology'\n" +
            "- 'Software' → 'Software Development'\n" +
            "- 'SaaS' → 'Software as a Service'\n" +
            "- 'E-commerce' → 'E-commerce'\n" +
            "- 'Ecommerce' → 'E-commerce'\n" +
            "- 'Online Retail' → 'E-commerce'\n" +
            "- 'FinTech' → 'Financial Technology'\n" +
            "- 'Fintech' → 'Financial Technology'\n" +
            "- 'Banking' → 'Banking & Financial Services'\n" +
            "- 'Finance' → 'Banking & Financial Services'\n" +
            "- 'Healthcare' → 'Healthcare'\n" +
            "- 'Health Care' → 'Healthcare'\n" +
            "- 'Medical' → 'Healthcare'\n" +
            "- 'AI/ML' → 'Artificial Intelligence & Machine Learning'\n" +
            "- 'Machine Learning' → 'Artificial Intelligence & Machine Learning'\n" +
            "- 'Artificial Intelligence' → 'Artificial Intelligence & Machine Learning'\n\n" +
            "QUALITY REQUIREMENTS:\n" +
            "- Return only the standardized name, no explanations or additional text\n" +
            "- Use consistent capitalization and formatting\n" +
            "- Ensure the name is professional and widely recognized\n" +
            "- Group similar variations under the same parent name\n" +
            "- If the name is already well-standardized, return it as is\n\n" +
            "Return ONLY the standardized name for industry '%s':",
            industryName
        );
    }

    private String parseMappedNameResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for mapped name: {}", generatedText);
                        
                        // Clean up the response
                        String mappedName = generatedText.trim();
                        // Remove quotes if present
                        mappedName = mappedName.replaceAll("^[\"']+|[\"']+$", "");
                        // Remove any extra whitespace
                        mappedName = mappedName.trim();
                        
                        log.info("[Gemini] Parsed mapped name: '{}'", mappedName);
                        return mappedName;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for mapped name: {}", responseBody);
            return null;
        } catch (Exception e) {
            log.error("Error parsing Gemini response for mapped name", e);
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

    private Long getOrCreateMappedId(String mappedName) {
        // Check if this mapped name already exists
        List<SecondaryIndustryMapping> existingMappings = secondaryIndustryMappingRepository.findByMappedName(mappedName);
        
        if (!existingMappings.isEmpty()) {
            // Use existing mapped ID
            return existingMappings.get(0).getMappedId();
        } else {
            // Create new mapped ID
            Long nextId = secondaryIndustryMappingRepository.getNextMappedId();
            log.info("Created new mapped ID {} for mapped name: {}", nextId, mappedName);
            return nextId;
        }
    }
} 