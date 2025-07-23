package com.tymbl.jobs.service;

import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CompanyCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyCleanupService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    /**
     * Process all unprocessed companies for cleanup
     */
    @Transactional
    public Map<String, Object> processAllCompanies() {
        logger.info("Starting company cleanup process for all unprocessed companies");
        
        List<Company> unprocessedCompanies = companyRepository.findByCleanupProcessedFalse();
        logger.info("Found {} unprocessed companies for cleanup", unprocessedCompanies.size());
        
        int processedCount = 0;
        int junkMarkedCount = 0;
        int renamedCount = 0;
        int errorCount = 0;
        
        for (Company company : unprocessedCompanies) {
            try {
                Map<String, Object> result = processSingleCompany(company);
                
                if (result.containsKey("junkMarked")) {
                    junkMarkedCount++;
                } else if (result.containsKey("renamed")) {
                    renamedCount++;
                }
                processedCount++;
                
                logger.info("Processed company: {} - Result: {}", company.getName(), result);
                
            } catch (Exception e) {
                errorCount++;
                logger.error("Error processing company {}: {}", company.getName(), e.getMessage());
                
                // Mark as processed to avoid infinite retries
                company.setCleanupProcessed(true);
                company.setCleanupProcessedAt(LocalDateTime.now());
                companyRepository.save(company);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProcessed", processedCount);
        summary.put("junkMarked", junkMarkedCount);
        summary.put("renamed", renamedCount);
        summary.put("errors", errorCount);
        summary.put("message", "Company cleanup process completed. Review junk-marked companies before deletion.");
        
        logger.info("Company cleanup completed - Processed: {}, Junk Marked: {}, Renamed: {}, Errors: {}", 
                   processedCount, junkMarkedCount, renamedCount, errorCount);
        
        return summary;
    }

    /**
     * Process a single company for cleanup
     */
    @Transactional
    public Map<String, Object> processSingleCompany(Company company) {
        logger.info("Processing company for cleanup: {}", company.getName());
        
        String prompt = buildCleanupPrompt(company.getName());
        String aiResponse = callGeminiAPI(prompt);
        
        Map<String, Object> result = parseCleanupResponse(aiResponse, company.getName());
        
        if (result.containsKey("delete")) {
            // Mark as junk instead of deleting
            String parentCompanyName = (String) result.get("parentCompany");
            String reason = (String) result.get("reason");
            
            logger.info("Marking company {} as junk - Parent: {}, Reason: {}", 
                       company.getName(), parentCompanyName, reason);
            
            company.setIsJunk(true);
            company.setJunkReason(reason);
            company.setParentCompanyName(parentCompanyName);
            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);
            
            result.put("junkMarked", true);
            result.put("reason", "Marked as junk - " + reason);
        } else if (result.containsKey("rename")) {
            String newName = (String) result.get("newName");
            logger.info("Renaming company {} to {}", company.getName(), newName);
            company.setName(newName);
            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);
            result.put("renamed", true);
        } else {
            // No action needed, mark as processed
            logger.info("No action needed for company: {}", company.getName());
            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);
            result.put("noAction", true);
        }
        
        return result;
    }

    /**
     * Process a specific company by name
     */
    @Transactional
    public Map<String, Object> processCompanyByName(String companyName) {
        logger.info("Processing specific company for cleanup: {}", companyName);
        
        Optional<Company> companyOpt = companyRepository.findByNameIgnoreCase(companyName);
        if (!companyOpt.isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Company not found: " + companyName);
            return error;
        }
        
        Company company = companyOpt.get();
        if (company.getCleanupProcessed()) {
            Map<String, Object> info = new HashMap<>();
            info.put("info", "Company already processed for cleanup");
            info.put("processedAt", company.getCleanupProcessedAt());
            return info;
        }
        
        return processSingleCompany(company);
    }

    /**
     * Reset cleanup processed flag for all companies
     */
    @Transactional
    public Map<String, Object> resetCleanupProcessedFlag() {
        logger.info("Resetting cleanup processed flag for all companies");
        
        List<Company> allCompanies = companyRepository.findAll();
        for (Company company : allCompanies) {
            company.setCleanupProcessed(false);
            company.setCleanupProcessedAt(null);
        }
        companyRepository.saveAll(allCompanies);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Cleanup processed flag reset for all companies");
        result.put("totalCompanies", allCompanies.size());
        
        logger.info("Reset cleanup processed flag for {} companies", allCompanies.size());
        
        return result;
    }

    /**
     * Get all junk-marked companies for manual review
     */
    public Map<String, Object> getJunkMarkedCompanies() {
        logger.info("Retrieving all junk-marked companies for review");
        
        List<Company> junkCompanies = companyRepository.findByIsJunkTrue();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalJunkCompanies", junkCompanies.size());
        result.put("junkCompanies", junkCompanies.stream().map(company -> {
            Map<String, Object> companyData = new HashMap<>();
            companyData.put("id", company.getId());
            companyData.put("name", company.getName());
            companyData.put("junkReason", company.getJunkReason());
            companyData.put("parentCompanyName", company.getParentCompanyName());
            companyData.put("cleanupProcessedAt", company.getCleanupProcessedAt());
            return companyData;
        }).collect(java.util.stream.Collectors.toList()));
        result.put("message", "Junk-marked companies retrieved for review");
        
        logger.info("Retrieved {} junk-marked companies for review", junkCompanies.size());
        
        return result;
    }

    /**
     * Clear junk flag for a specific company (undo junk marking)
     */
    @Transactional
    public Map<String, Object> clearJunkFlag(Long companyId) {
        logger.info("Clearing junk flag for company ID: {}", companyId);
        
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (!companyOpt.isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Company not found with ID: " + companyId);
            return error;
        }
        
        Company company = companyOpt.get();
        company.setIsJunk(false);
        company.setJunkReason(null);
        company.setParentCompanyName(null);
        companyRepository.save(company);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("companyId", companyId);
        result.put("companyName", company.getName());
        result.put("message", "Junk flag cleared for company: " + company.getName());
        
        logger.info("Junk flag cleared for company: {}", company.getName());
        
        return result;
    }

    /**
     * Build the prompt for company cleanup analysis
     */
    private String buildCleanupPrompt(String companyName) {
        return String.format(
            "Analyze the company name \"%s\" and determine if it's:\n" +
            "1. A product/service name that should be mapped to its parent company\n" +
            "2. A junk/incorrect entry that should be deleted\n" +
            "3. A valid company name that should remain unchanged\n" +
            "\n" +
            "Examples:\n" +
            "- \"AWS\" → Should be mapped to \"Amazon Web Services\" or deleted if Amazon exists\n" +
            "- \"Google Cloud\" → Should be mapped to \"Google\" or deleted if Google exists\n" +
            "- \"Microsoft Azure\" → Should be mapped to \"Microsoft\" or deleted if Microsoft exists\n" +
            "- \"Netflix\" → Valid company, no action needed\n" +
            "- \"Random Junk Name\" → Should be deleted\n" +
            "\n" +
            "Respond in JSON format:\n" +
            "{\n" +
            "    \"action\": \"delete|rename|keep\",\n" +
            "    \"reason\": \"explanation\",\n" +
            "    \"parentCompany\": \"parent company name if delete action\",\n" +
            "    \"newName\": \"new name if rename action\"\n" +
            "}\n" +
            "\n" +
            "Only include parentCompany if action is \"delete\" and you know the parent company.\n" +
            "Only include newName if action is \"rename\".", 
            companyName);
    }

    /**
     * Call Gemini API for company cleanup analysis
     */
    private String callGeminiAPI(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);
            content.put("parts", parts);
            
            List<Map<String, Object>> contents = new ArrayList<>();
            contents.add(content);
            requestBody.put("contents", contents);
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 1);
            generationConfig.put("maxOutputTokens", 500);
            requestBody.put("generationConfig", generationConfig);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts2 = (List<Map<String, Object>>) content2.get("parts");
                    if (parts2 != null && !parts2.isEmpty()) {
                        return (String) parts2.get(0).get("text");
                    }
                }
            }
            
            throw new RuntimeException("Invalid response from Gemini API");
            
        } catch (Exception e) {
            logger.error("Error calling Gemini API: {}", e.getMessage());
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage());
        }
    }

    /**
     * Parse the AI response for cleanup actions
     */
    private Map<String, Object> parseCleanupResponse(String response, String originalName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract JSON from response
            Pattern jsonPattern = Pattern.compile("\\{[^}]*\\}");
            Matcher matcher = jsonPattern.matcher(response);
            
            if (matcher.find()) {
                String jsonStr = matcher.group();
                // Simple JSON parsing (in production, use proper JSON library)
                
                if (jsonStr.contains("\"action\":\"delete\"")) {
                    result.put("delete", true);
                    // Extract parent company name
                    Pattern parentPattern = Pattern.compile("\"parentCompany\":\"([^\"]+)\"");
                    Matcher parentMatcher = parentPattern.matcher(jsonStr);
                    if (parentMatcher.find()) {
                        result.put("parentCompany", parentMatcher.group(1));
                    }
                } else if (jsonStr.contains("\"action\":\"rename\"")) {
                    result.put("rename", true);
                    // Extract new name
                    Pattern newNamePattern = Pattern.compile("\"newName\":\"([^\"]+)\"");
                    Matcher newNameMatcher = newNamePattern.matcher(jsonStr);
                    if (newNameMatcher.find()) {
                        result.put("newName", newNameMatcher.group(1));
                    }
                } else {
                    result.put("keep", true);
                }
                
                // Extract reason
                Pattern reasonPattern = Pattern.compile("\"reason\":\"([^\"]+)\"");
                Matcher reasonMatcher = reasonPattern.matcher(jsonStr);
                if (reasonMatcher.find()) {
                    result.put("reason", reasonMatcher.group(1));
                }
            } else {
                // Fallback parsing
                if (response.toLowerCase().contains("delete")) {
                    result.put("delete", true);
                } else if (response.toLowerCase().contains("rename")) {
                    result.put("rename", true);
                } else {
                    result.put("keep", true);
                }
                result.put("reason", "Parsed from AI response");
            }
            
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            result.put("error", "Failed to parse AI response");
        }
        
        return result;
    }
} 