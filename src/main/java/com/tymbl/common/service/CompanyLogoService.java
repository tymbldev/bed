package com.tymbl.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tymbl.common.service.AIRestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyLogoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AIRestService aiRestService;

    private final CompanyRepository companyRepository;

    /**
     * Fetch logo URL for a single company using GenAI
     */
    public Map<String, Object> fetchLogoForCompany(Company company) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Fetching logo for company: {} (ID: {})", company.getName(), company.getId());
            
            // Check if already processed
            if (company.getLogoUrlFetched() != null && company.getLogoUrlFetched() == 1) {
                result.put("alreadyProcessed", true);
                result.put("success", true);
                result.put("message", "Logo already fetched");
                result.put("logoUrl", company.getLogoUrl());
                return result;
            }

            // Generate logo URL using AI
            String logoUrl = generateLogoUrlForCompany(company.getName());
            
            if (logoUrl == null || logoUrl.trim().isEmpty()) {
                company.setLogoUrlFetched(2); // Mark as failed
                companyRepository.save(company);
                
                result.put("success", false);
                result.put("error", "Failed to generate logo URL");
                return result;
            }
            
            // Save the logo URL
            company.setLogoUrl(logoUrl);
            company.setLogoUrlFetched(1); // Mark as fetched
            companyRepository.save(company);
            
            result.put("success", true);
            result.put("companyId", company.getId());
            result.put("companyName", company.getName());
            result.put("logoUrl", logoUrl);
            result.put("message", "Logo URL fetched successfully");
            
            log.info("Fetched logo URL '{}' for company: {} (ID: {})", logoUrl, company.getName(), company.getId());
            
        } catch (Exception e) {
            log.error("Error fetching logo for company: {} (ID: {})", company.getName(), company.getId(), e);
            company.setLogoUrlFetched(2); // Mark as failed
            companyRepository.save(company);
            
            result.put("success", false);
            result.put("error", "Error fetching logo: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Fetch logos for all companies in batches using database pagination
     */
    public Map<String, Object> fetchLogosForAllCompaniesInBatches() {
        log.info("Starting logo fetching for all companies using database pagination");
        
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalLogosFetched = 0;
        int totalErrors = 0;
        int batchSize = 10; // Process 10 companies at a time
        int pageNumber = 0;
        
        while (true) {
            // Fetch companies that haven't been processed for logo fetching (status = 0 or null)
            Page<Company> companyPage = companyRepository.findByLogoUrlFetched(0, PageRequest.of(pageNumber, batchSize));
            
            List<Company> companies = companyPage.getContent();
            
            if (companies.isEmpty()) {
                log.info("No more companies to process for logo fetching. Completed at page: {}", pageNumber);
                break;
            }
            
            log.info("Processing logo fetching batch {} with {} companies", pageNumber + 1, companies.size());
            
            for (Company company : companies) {
                try {
                    Map<String, Object> result = fetchLogoForCompany(company);
                    companyResults.add(result);
                    
                    if ((Boolean) result.get("success")) {
                        totalLogosFetched++;
                        log.info("Successfully fetched logo for company: {} (ID: {})", company.getName(), company.getId());
                    } else {
                        totalErrors++;
                        log.error("Failed to fetch logo for company: {} (ID: {})", company.getName(), company.getId());
                    }
                    
                    totalProcessed++;
                    
                } catch (Exception e) {
                    totalErrors++;
                    log.error("Error fetching logo for company: {} (ID: {})", company.getName(), company.getId(), e);
                    
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("companyId", company.getId());
                    errorResult.put("companyName", company.getName());
                    errorResult.put("success", false);
                    errorResult.put("error", "Error fetching logo: " + e.getMessage());
                    companyResults.add(errorResult);
                }
            }
            
            pageNumber++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", totalProcessed);
        result.put("totalLogosFetched", totalLogosFetched);
        result.put("totalErrors", totalErrors);
        result.put("companyResults", companyResults);
        result.put("message", "Logo fetching completed using database pagination");
        
        log.info("Completed logo fetching using database pagination. Total processed: {}, Logos fetched: {}, Errors: {}", 
                totalProcessed, totalLogosFetched, totalErrors);
        
        return result;
    }

    /**
     * Generate logo URL for a company using GenAI
     */
    private String generateLogoUrlForCompany(String companyName) {
        try {
            log.info("[Gemini] Generating logo URL for company: {}", companyName);
            String prompt = buildLogoUrlGenerationPrompt(companyName);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);
            
            ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Logo URL Generation for " + companyName);
            String logoUrl = parseLogoUrlResponse(response.getBody());
            log.info("[Gemini] Parsed logo URL '{}' for company: {}", logoUrl, companyName);
            return logoUrl;
        } catch (Exception e) {
            log.error("[Gemini] Error generating logo URL for company: {}", companyName, e);
            return null;
        }
    }

    /**
     * Build prompt for logo URL generation
     */
    private String buildLogoUrlGenerationPrompt(String companyName) {
        return String.format(
            "You are a business and technology expert helping to find the official logo URL for companies. " +
            "Given the company name, provide the direct download URL to the company's official logo.\n\n" +
            "CRITICAL REQUIREMENTS:\n" +
            "1. Return ONLY a direct download URL to the company's official logo\n" +
            "2. The URL should be publicly accessible and downloadable\n" +
            "3. Prefer high-resolution PNG or SVG formats\n" +
            "4. Use official company websites, CDNs, or reliable logo hosting services\n" +
            "5. Ensure the URL is currently active and accessible\n\n" +
            "PREFERRED SOURCES:\n" +
            "- Official company websites (e.g., company.com/logo.png)\n" +
            "- Company CDNs or asset servers\n" +
            "- Reliable logo hosting services\n" +
            "- Company press kits or media resources\n\n" +
            "EXAMPLES OF GOOD LOGO URLS:\n" +
            "- https://www.apple.com/ac/globalnav/7/en_US/images/be15095f-5a20-57d0-ad14-cf4c638e223a/globalnav_apple_image__b5er5ngrzxqq_large.svg\n" +
            "- https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png\n" +
            "- https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/Google_2015_logo.svg/272px-Google_2015_logo.svg.png\n" +
            "- https://www.microsoft.com/en-us/-/media/microsoft/images/logos/microsoft-logo.png\n\n" +
            "RESPONSE FORMAT:\n" +
            "Return ONLY the logo URL, nothing else. No explanations, no quotes, no additional text.\n\n" +
            "Company name: %s\n" +
            "Logo URL:",
            companyName
        );
    }

    /**
     * Parse the logo URL from the GenAI response
     */
    private String parseLogoUrlResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
            
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                
                if (parts != null && !parts.isEmpty()) {
                    Map<String, Object> part = parts.get(0);
                    String text = (String) part.get("text");
                    
                    if (text != null && !text.trim().isEmpty()) {
                        // Clean up the response - remove any extra text and extract just the URL
                        String cleanedText = text.trim();
                        
                        // If the response contains a URL, extract it
                        if (cleanedText.startsWith("http")) {
                            // Find the first URL in the text
                            String[] lines = cleanedText.split("\n");
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("http") && (line.contains(".png") || line.contains(".jpg") || 
                                    line.contains(".jpeg") || line.contains(".svg") || line.contains(".gif"))) {
                                    return line;
                                }
                            }
                            // If no specific image extension found, return the first http URL
                            for (String line : lines) {
                                line = line.trim();
                                if (line.startsWith("http")) {
                                    return line;
                                }
                            }
                        }
                        
                        return cleanedText;
                    }
                }
            }
            
            log.warn("Could not parse logo URL from response: {}", responseBody);
            return null;
        } catch (Exception e) {
            log.error("Error parsing logo URL response: {}", responseBody, e);
            return null;
        }
    }


} 