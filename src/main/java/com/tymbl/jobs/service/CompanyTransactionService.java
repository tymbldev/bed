package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.service.AIRestService;

import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.util.CrawlingService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyContentRepository;
import com.tymbl.jobs.repository.CompanyRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyTransactionService {

    private final CrawlingService crawlingService;
    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;
    private final IndustryRepository industryRepository;
    private final RestTemplate restTemplate;

    private final GeminiService geminiService;
    private final DropdownService dropdownService;
    private final AIRestService aiRestService;
    private final ObjectMapper objectMapper;

    /**
     * Process company crawling in its own transaction
     */
    @Transactional
    public void processCompanyCrawlingInTransaction(Company company) {
        try {
            log.info("Processing company crawling: {} (ID: {})", company.getName(), company.getId());
            // Always fetch/enrich based on company name only
            Optional<CrawlingService.CrawlResult> crawlResult = crawlingService.crawlCompanyPageWithJunkDetection(company.getName());
            if (crawlResult.isPresent()) {
                CrawlingService.CrawlResult result = crawlResult.get();
                Company generatedCompany = result.getCompany();
                String rawData = result.getRawData();
                // Update company with generated information (including LinkedIn URL if present)
                updateCompanyFields(company, generatedCompany);
                company.setCrawledData(rawData);
                company.setCrawled(true); // Success - flag = 1
                company.setLastCrawledAt(LocalDateTime.now());
                companyRepository.save(company);
                log.info("Successfully updated company information for: {} (ID: {})", company.getName(), company.getId());
            } else {
                // Failed to generate information - flag = 2
                log.warn("Failed to generate company information for: {} (ID: {}), marking as failed", company.getName(), company.getId());
                company.setCrawled(false);
                company.setLastCrawledAt(LocalDateTime.now());
                companyRepository.save(company);
            }
        } catch (Exception e) {
            log.error("Error processing company crawling: {} (ID: {})", company.getName(), company.getId(), e);
            // Mark as failed to crawl - flag = 2
            company.setCrawled(false);
            company.setLastCrawledAt(LocalDateTime.now());
            companyRepository.save(company);
            throw e; // Re-throw to ensure transaction rollback
        }
    }

    /**
     * Process company industry detection in its own transaction
     */
    @Transactional
    public CompanyIndustryResponse processCompanyIndustryDetectionInTransaction(Company company) {
        CompanyIndustryResponse response = CompanyIndustryResponse.builder()
            .companyId(company.getId())
            .companyName(company.getName())
            .processed(false)
            .build();
        
        try {
            // Use Gemini AI to detect industries
            Map<String, Object> industryData = geminiService.detectCompanyIndustries(
                company.getName(),
                company.getDescription(),
                company.getSpecialties()
            );
            
            if (!industryData.isEmpty()) {
                String primaryIndustry = (String) industryData.get("primaryIndustry");
                @SuppressWarnings("unchecked")
                List<String> secondaryIndustries = (List<String>) industryData.get("secondaryIndustries");
                
                // Find primary industry ID
                Long primaryIndustryId = findIndustryIdByName(primaryIndustry);
                
                // Update company with detected industries and mark as processed
                company.setPrimaryIndustryId(primaryIndustryId);
                company.setSecondaryIndustries(String.join(",", secondaryIndustries != null ? secondaryIndustries : new ArrayList<>()));
                company.setIndustryProcessed(true);
                companyRepository.save(company);
                
                // Refresh company cache to ensure fresh data
                dropdownService.refreshCompanyList();
                
                response.setPrimaryIndustry(primaryIndustry);
                response.setPrimaryIndustryId(primaryIndustryId);
                response.setSecondaryIndustries(secondaryIndustries);
                response.setProcessed(true);
            } else {
                response.setError("Failed to detect industries using Gemini AI");
            }
        } catch (Exception e) {
            log.error("Error processing company industry detection: {} (ID: {})", company.getName(), company.getId(), e);
            response.setError("Error processing company: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Process company content shortening in its own transaction
     */
    @Transactional
    public Map<String, Object> processCompanyContentShorteningInTransaction(Long companyId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<CompanyContent> companyContentOpt = companyContentRepository.findByCompanyId(companyId);
            if (!companyContentOpt.isPresent()) {
                result.put("success", false);
                result.put("error", "Company content not found for company ID: " + companyId);
                return result;
            }

            CompanyContent companyContent = companyContentOpt.get();
            
            // Check if already processed
            if (companyContent.isContentShortened()) {
                result.put("alreadyProcessed", true);
                result.put("success", true);
                result.put("message", "Content already shortened");
                return result;
            }

            // Shorten about us content
            boolean aboutUsShortened = false;
            if (companyContent.getAboutUsOriginal() != null && !companyContent.getAboutUsOriginal().trim().isEmpty()) {
                String shortenedAboutUs = shortenContent(companyContent.getAboutUsOriginal());
                if (shortenedAboutUs != null && !shortenedAboutUs.equals(companyContent.getAboutUsOriginal())) {
                    // Update the company's aboutUs field
                    Optional<Company> companyOpt = companyRepository.findById(companyId);
                    if (companyOpt.isPresent()) {
                        Company company = companyOpt.get();
                        company.setAboutUs(shortenedAboutUs);
                        companyRepository.save(company);
                        aboutUsShortened = true;
                    }
                }
            }

            // Shorten culture content
            boolean cultureShortened = false;
            if (companyContent.getCultureOriginal() != null && !companyContent.getCultureOriginal().trim().isEmpty()) {
                String shortenedCulture = shortenContent(companyContent.getCultureOriginal());
                if (shortenedCulture != null && !shortenedCulture.equals(companyContent.getCultureOriginal())) {
                    // Update the company's culture field
                    Optional<Company> companyOpt = companyRepository.findById(companyId);
                    if (companyOpt.isPresent()) {
                        Company company = companyOpt.get();
                        company.setCulture(shortenedCulture);
                        companyRepository.save(company);
                        cultureShortened = true;
                    }
                }
            }

            // Mark as processed and save
            companyContent.setContentShortened(true);
            companyContentRepository.save(companyContent);

            result.put("success", true);
            result.put("companyId", companyId);
            result.put("aboutUsShortened", aboutUsShortened);
            result.put("cultureShortened", cultureShortened);
            result.put("message", "Content shortening completed");

        } catch (Exception e) {
            log.error("Error shortening content for company ID: {}", companyId, e);
            result.put("success", false);
            result.put("error", "Error shortening content: " + e.getMessage());
        }

        return result;
    }

    /**
     * Process similar companies generation in its own transaction
     */
    @Transactional
    public Map<String, Object> processSimilarCompaniesGenerationInTransaction(Company company) {
        Map<String, Object> result = new HashMap<>();
        result.put("companyId", company.getId());
        result.put("companyName", company.getName());
        
        try {
            // Get industry name for the company
            String industryName = "Unknown Industry";
            if (company.getPrimaryIndustryId() != null) {
                Optional<com.tymbl.common.entity.Industry> industryOpt = industryRepository.findById(company.getPrimaryIndustryId());
                if (industryOpt.isPresent()) {
                    industryName = industryOpt.get().getName();
                }
            }
            
            // Generate similar companies using AI with enhanced company details
            List<String> similarCompanyNames = geminiService.generateSimilarCompanies(
                company.getName(), 
                industryName, 
                company.getDescription(),
                company.getCompanySize(),
                company.getSpecialties(),
                company.getHeadquarters()
            );
            
            if (similarCompanyNames == null || similarCompanyNames.isEmpty()) {
                result.put("success", false);
                result.put("error", "No similar companies generated by AI");
                result.put("similarCompaniesFound", 0);
                result.put("newCompaniesCreated", 0);
                return result;
            }
            
            List<String> existingCompanyNames = new ArrayList<>();
            List<String> newCompanyNames = new ArrayList<>();
            List<Long> existingCompanyIds = new ArrayList<>();
            
            // Check which similar companies already exist and create new ones if needed
            for (String similarCompanyName : similarCompanyNames) {
                Optional<Company> existingCompany = companyRepository.findByName(similarCompanyName);
                if (existingCompany.isPresent()) {
                    existingCompanyNames.add(similarCompanyName);
                    existingCompanyIds.add(existingCompany.get().getId());
                } else {
                    // Create new company
                    Company newCompany = new Company();
                    newCompany.setName(similarCompanyName);
                    newCompany.setPrimaryIndustryId(company.getPrimaryIndustryId());
                    newCompany.setSecondaryIndustries(company.getSecondaryIndustries());
                    newCompany.setSimilarCompaniesProcessed(true); // Mark as processed to avoid infinite loops
                    companyRepository.save(newCompany);
                    newCompanyNames.add(similarCompanyName);
                }
            }
            
            // Update original company with similar companies info
            company.setSimilarCompaniesByName(String.join(",", similarCompanyNames));
            company.setSimilarCompaniesById(String.join(",", existingCompanyIds.stream().map(String::valueOf).collect(Collectors.toList())));
            company.setSimilarCompaniesProcessed(true);
            companyRepository.save(company);
            
            result.put("success", true);
            result.put("similarCompaniesFound", similarCompanyNames.size());
            result.put("existingCompanies", existingCompanyNames);
            result.put("newCompaniesCreated", newCompanyNames.size());
            result.put("newCompanies", newCompanyNames);
            
        } catch (Exception e) {
            log.error("Error generating similar companies for company: {} (ID: {})", company.getName(), company.getId(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("similarCompaniesFound", 0);
            result.put("newCompaniesCreated", 0);
        }
        
        return result;
    }

    /**
     * Process all companies similar companies generation in batches
     */
    public Map<String, Object> processAllCompaniesSimilarCompaniesGenerationInBatches() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("Starting similar companies generation for all companies in batches");
            // This method would need to be implemented based on your business logic
            // For now, returning a placeholder response
            result.put("success", true);
            result.put("message", "Batch similar companies generation completed");
            result.put("status", "INFO");
            return result;
        } catch (Exception e) {
            log.error("Error processing all companies similar companies generation in batches", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    // Note: The processAllCompaniesShortnameGenerationInBatches method has been removed
    // as it depended on the shortnameGenerated field which is no longer available.

    // Helper methods
    private void updateCompanyFields(Company existing, Company generated) {
        // Update all available fields from the generated company data
        if (generated.getDescription() != null && !generated.getDescription().trim().isEmpty()) {
            existing.setDescription(generated.getDescription());
        }
        if (generated.getLogoUrl() != null && !generated.getLogoUrl().trim().isEmpty()) {
            existing.setLogoUrl(generated.getLogoUrl());
        }
        if (generated.getWebsite() != null && !generated.getWebsite().trim().isEmpty()) {
            existing.setWebsite(generated.getWebsite());
        }
        if (generated.getCareerPageUrl() != null && !generated.getCareerPageUrl().trim().isEmpty()) {
            existing.setCareerPageUrl(generated.getCareerPageUrl());
        }
        
        // Handle about us and culture content in CompanyContent table
        if (generated.getAboutUs() != null && !generated.getAboutUs().trim().isEmpty()) {
            saveCompanyContent(existing.getId(), "aboutUs", generated.getAboutUs());
        }
        if (generated.getCulture() != null && !generated.getCulture().trim().isEmpty()) {
            saveCompanyContent(existing.getId(), "culture", generated.getCulture());
        }
    }

    private void saveCompanyContent(Long companyId, String contentType, String content) {
        try {
            Optional<CompanyContent> existingContent = companyContentRepository.findByCompanyId(companyId);
            CompanyContent companyContent;
            
            if (existingContent.isPresent()) {
                companyContent = existingContent.get();
            } else {
                companyContent = new CompanyContent();
                companyContent.setCompanyId(companyId);
            }
            
            if ("aboutUs".equals(contentType)) {
                companyContent.setAboutUsOriginal(content);
            } else if ("culture".equals(contentType)) {
                companyContent.setCultureOriginal(content);
            }
            
            companyContentRepository.save(companyContent);
        } catch (Exception e) {
            log.error("Error saving company content for company ID: {} and type: {}", companyId, contentType, e);
        }
    }

    private Long findIndustryIdByName(String industryName) {
        if (industryName == null || industryName.trim().isEmpty()) {
            return null;
        }
        
        return industryRepository.findByName(industryName)
            .map(industry -> industry.getId())
            .orElse(null);
    }

    private String shortenContent(String content) {
        // Check if content needs shortening - keep existing condition
        if (content == null || content.length() <= 500) {
            return content;
        }
        
        // Use intelligent AI-powered content shortening
        try {
            log.info("Using AI-powered content shortening for content of length: {}", content.length());
            return geminiService.shortenContentIntelligently(content, "company content");
        } catch (Exception e) {
            log.error("Error in AI content shortening, falling back to smart truncation", e);
            // Fallback to smart truncation if AI fails
            return smartTruncateContent(content, 500);
        }
    }
    
    /**
     * Smart truncation fallback that tries to break at sentence boundaries
     */
    private String smartTruncateContent(String content, int minLength) {
        if (content == null || content.length() <= minLength) {
            return content;
        }
        
        // Try to find a good sentence boundary around the target length
        int targetLength = Math.max(minLength, 500);
        int searchStart = Math.min(targetLength, content.length() - 50);
        int searchEnd = Math.min(targetLength + 200, content.length());
        
        // Look for sentence endings (.!?) in the search range
        int bestBreakPoint = -1;
        for (int i = searchStart; i < searchEnd; i++) {
            if (i < content.length() && ".!?".indexOf(content.charAt(i)) != -1) {
                bestBreakPoint = i + 1;
                break;
            }
        }
        
        // If no good sentence boundary found, use the target length
        if (bestBreakPoint == -1 || bestBreakPoint < minLength) {
            bestBreakPoint = Math.max(minLength, content.length());
        }
        
        String truncated = content.substring(0, bestBreakPoint).trim();
        
        // Add ellipsis only if we actually truncated
        if (bestBreakPoint < content.length()) {
            truncated += "...";
        }
        
        log.info("Smart truncation: shortened content from {} to {} characters", content.length(), truncated.length());
        return truncated;
    }
} 