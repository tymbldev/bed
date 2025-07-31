package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.util.CrawlingService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.CompanyContentRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.CompanyShortnameService;
import com.tymbl.common.service.CompanyShortnameTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyTransactionService {

    private final CrawlingService crawlingService;
    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;
    private final IndustryRepository industryRepository;
    private final RestTemplate restTemplate;
    private final CompanyShortnameService companyShortnameService;
    private final CompanyShortnameTransactionService companyShortnameTransactionService;
    private final GeminiService geminiService;
    private final DropdownService dropdownService;
    
    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

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
     * Process company cleanup in its own transaction
     */
    @Transactional
    public Map<String, Object> processCompanyCleanupInTransaction(Company company) {
        log.info("Processing company cleanup: {}", company.getName());

        String prompt = buildCleanupPrompt(company.getName());
        String aiResponse = callGeminiAPI(prompt);

        Map<String, Object> result = parseCleanupResponse(aiResponse, company.getName());
        if (result == null || result.isEmpty()) {
            log.error("Not processing company.. so returning empty result : " + company.getName());
        }
        if (result.containsKey("delete")) {
            String parentCompanyName = (String) result.get("parentCompany");
            String reason = (String) result.get("reason");
            Boolean parentCompanyExists = (Boolean) result.get("parentCompanyExists");
            Boolean shouldRemove = (Boolean) result.get("shouldRemove");

            if (parentCompanyName != null && Boolean.TRUE.equals(parentCompanyExists)
                && Boolean.TRUE.equals(shouldRemove)) {
                // Product/service entry and parent company exists - REMOVE the product entry
                log.info("Removing product entry '{}' - parent company '{}' already exists in database",
                    company.getName(), parentCompanyName);

                companyRepository.delete(company);

                result.put("removed", true);
                result.put("action", "removed");
                result.put("reason",
                    "Product/service entry removed - parent company already exists: " + parentCompanyName);
                result.put("parentCompany", parentCompanyName);

            } else {
                // Mark as junk (parent company doesn't exist or it's a junk entry)
                log.info("Marking company {} as junk - Parent: {}, Reason: {}",
                    company.getName(), parentCompanyName, reason);

                company.setIsJunk(true);
                company.setJunkReason(reason);
                company.setParentCompanyName(parentCompanyName);
                company.setCleanupProcessed(true);
                company.setCleanupProcessedAt(LocalDateTime.now());
                companyRepository.save(company);

                result.put("junkMarked", true);
                result.put("action", "junk_marked");
                result.put("reason", "Marked as junk - " + reason);
            }
        } else if (result.containsKey("rename")) {
            String newName = (String) result.get("newName");
            String reason = (String) result.get("reason");

            log.info("Renaming company '{}' to '{}' - Reason: {}", company.getName(), newName, reason);

            // Check if a company with the new name already exists
            Optional<Company> existingCompany = companyRepository.findByName(newName);
            if (existingCompany.isPresent()) {
                // If company with new name already exists, delete the current company
                log.info("Company with name '{}' already exists. Deleting current company '{}'", newName,
                    company.getName());
                companyRepository.delete(company);
                result.put("deleted", true);
                result.put("action", "deleted");
                result.put("reason", "Company renamed to existing name - duplicate removed");
            } else {
                // If new name doesn't exist, rename the current company
                company.setName(newName);
                company.setCleanupProcessed(true);
                company.setCleanupProcessedAt(LocalDateTime.now());
                companyRepository.save(company);
                result.put("renamed", true);
                result.put("action", "renamed");
                result.put("reason", "Renamed - " + reason);
                result.put("newName", newName);
            }
        } else {
            // No action needed
            log.info("No action needed for company: {} - Reason: {}", company.getName(), result.get("reason"));

            company.setCleanupProcessed(true);
            company.setCleanupProcessedAt(LocalDateTime.now());
            companyRepository.save(company);

            result.put("noAction", true);
            result.put("action", "no_action");
            result.put("reason", "No action needed - " + result.get("reason"));
        }

        return result;
    }

    /**
     * Process single company for cleanup (alias for processCompanyCleanupInTransaction)
     */
    @Transactional
    public Map<String, Object> processSingleCompany(Company company) {
        return processCompanyCleanupInTransaction(company);
    }

    /**
     * Process a specific company by name
     */
    @Transactional
    public Map<String, Object> processCompanyByName(String companyName) {
        log.info("Processing specific company for cleanup: {}", companyName);

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

        return processCompanyCleanupInTransaction(company);
    }

    /**
     * Reset cleanup processed flag for all companies
     */
    @Transactional
    public Map<String, Object> resetCleanupProcessedFlag() {
        log.info("Resetting cleanup processed flag for all companies");

        List<Company> allCompanies = companyRepository.findAll();
        for (Company company : allCompanies) {
            company.setCleanupProcessed(false);
            company.setCleanupProcessedAt(null);
        }
        companyRepository.saveAll(allCompanies);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Cleanup processed flag reset for all companies");
        result.put("totalCompanies", allCompanies.size());

        log.info("Reset cleanup processed flag for {} companies", allCompanies.size());

        return result;
    }

    /**
     * Get all junk-marked companies for manual review
     */
    public Map<String, Object> getJunkMarkedCompanies() {
        log.info("Retrieving all junk-marked companies for review");

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
        }).collect(Collectors.toList()));
        result.put("message", "Junk-marked companies retrieved for review");

        log.info("Retrieved {} junk-marked companies for review", junkCompanies.size());

        return result;
    }

    /**
     * Clear junk flag for a specific company (undo junk marking)
     */
    @Transactional
    public Map<String, Object> clearJunkFlag(Long companyId) {
        log.info("Clearing junk flag for company ID: {}", companyId);

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

        log.info("Junk flag cleared for company: {}", company.getName());

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
     * Process company shortname generation in its own transaction
     */
    @Transactional
    public Map<String, Object> processCompanyShortnameGenerationInTransaction(Company company) {
        return companyShortnameTransactionService.processCompanyShortnameGenerationAndDeduplicationInTransaction(company);
    }

    /**
     * Process all companies shortname generation in batches
     */
    public Map<String, Object> processAllCompaniesShortnameGenerationInBatches() {
        return companyShortnameService.processAllCompaniesShortnameGenerationAndDeduplicationInBatches();
    }

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
        // Simple content shortening logic - can be enhanced with AI
        if (content == null || content.length() <= 500) {
            return content;
        }
        
        // Truncate to 500 characters and add ellipsis
        return content.substring(0, 497) + "...";
    }

    private String buildCleanupPrompt(String companyName) {
        return String.format(
            "Analyze the company name \"%s\" and determine if it's:\n" +
                "1. A PRODUCT/SERVICE name that should be mapped to its parent company\n" +
                "2. A junk/incorrect entry that should be deleted\n" +
                "3. A valid company name that should remain unchanged\n" +
                "4. A company name with extra descriptive text that should be cleaned up\n" +
                "\n" +
                "IMPORTANT: Focus on identifying products, services, platforms, or tools that belong to larger companies.\n" +
                "\n" +
                "NEW: COMPANY NAME CLEANUP - Remove extra descriptive text that shouldn't be part of the official company name.\n" +
                "\n" +
                "Examples of COMPANY NAMES THAT NEED CLEANUP:\n" +
                "- 'Tata Consultancy Services (TCS) (for IT related roles)' → Clean to: 'Tata Consultancy Services' or 'TCS'\n" +
                "  Reason: The extra phrase '(for IT related roles)' is descriptive text that should not be part of the official company name.\n" +
                "- 'Google (Alphabet Inc.)' → Clean to: 'Google' or 'Alphabet'\n" +
                "  Reason: Parenthetical company structure information should be simplified.\n" +
                "- 'Microsoft Corporation (MSFT)' → Clean to: 'Microsoft'\n" +
                "  Reason: Stock ticker symbols should not be part of the company name.\n" +
                "- 'Apple Inc. (AAPL) - Technology Company' → Clean to: 'Apple'\n" +
                "  Reason: Stock ticker and descriptive phrases should be removed.\n" +
                "- 'Amazon.com, Inc. (AMZN) - E-commerce Giant' → Clean to: 'Amazon'\n" +
                "  Reason: Legal suffixes and descriptive text should be removed.\n" +
                "- 'Meta Platforms, Inc. (formerly Facebook)' → Clean to: 'Meta'\n" +
                "  Reason: Legal suffixes and 'formerly' information should be removed.\n" +
                "- 'Netflix, Inc. (NFLX) - Streaming Service' → Clean to: 'Netflix'\n" +
                "  Reason: Legal suffixes and descriptive phrases should be removed.\n" +
                "- 'Salesforce.com, Inc. (CRM)' → Clean to: 'Salesforce'\n" +
                "  Reason: Legal suffixes and stock tickers should be removed.\n" +
                "- 'Adobe Systems Incorporated (ADBE)' → Clean to: 'Adobe'\n" +
                "  Reason: Legal suffixes and stock tickers should be removed.\n" +
                "- 'Oracle Corporation (ORCL) - Database Company' → Clean to: 'Oracle'\n" +
                "  Reason: Legal suffixes, stock tickers, and descriptive text should be removed.\n" +
                "\n" +
                "CLEANUP RULES:\n" +
                "1. Remove parenthetical descriptive text like '(for IT related roles)', '(technology company)', etc.\n" +
                "2. Remove stock ticker symbols like '(GOOGL)', '(MSFT)', '(AAPL)', etc.\n" +
                "3. Remove legal suffixes like 'Inc.', 'Corp.', 'LLC', 'Ltd.' when they're not essential\n" +
                "4. Remove 'formerly' or 'previously' information\n" +
                "5. Remove descriptive phrases like '- E-commerce Giant', '- Technology Company', etc.\n" +
                "6. Keep the core, recognizable company name\n" +
                "7. Prefer shorter, more commonly used names (e.g., 'Google' over 'Alphabet Inc.')\n" +
                "\n" +
                "Respond in VALID JSON format with double quotes:\n" +
                "{\n" +
                "    \"action\": \"delete|rename|keep\",\n" +
                "    \"reason\": \"detailed explanation\",\n" +
                "    \"parentCompany\": \"parent company name if this is a product/service\",\n" +
                "    \"newName\": \"new name if rename action\"\n" +
                "}\n" +
                "\n" +
                "IMPORTANT JSON RULES:\n" +
                "1. Use double quotes (\") for all JSON keys and string values\n" +
                "2. Escape any internal double quotes with backslash: \\\"\n" +
                "3. Use null for parentCompany and newName when not applicable\n" +
                "4. Ensure the JSON is valid and parseable\n" +
                "\n" +
                "IMPORTANT RULES:\n" +
                "1. If it's a product/service, always provide the parentCompany name\n" +
                "2. Use action 'delete' for products/services that should be mapped to parent companies\n" +
                "3. Use action 'delete' for junk entries\n" +
                "4. Use action 'keep' only for valid standalone companies\n" +
                "5. Use action 'rename' for company names with extra descriptive text that should be cleaned up\n" +
                "6. Be very thorough in identifying products vs companies\n" +
                "7. When in doubt, treat it as a product and provide the most likely parent company\n" +
                "8. For cleanup cases, provide a clear reason explaining what descriptive text was removed\n",
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
            log.error("Error calling Gemini API: {}", e.getMessage());
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
                // Normalize single quotes to double quotes for JSON keys/values
                jsonStr = jsonStr.replace("'", "\"");
                // Use Jackson to parse JSON string to Map
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> aiMap = mapper.readValue(jsonStr, Map.class);
                String action = (aiMap.get("action") != null) ? aiMap.get("action").toString().trim().toLowerCase() : "";
                
                if (action.equals("delete")) {
                    result.put("delete", true);
                    String parentCompany = aiMap.get("parentCompany") != null ? aiMap.get("parentCompany").toString() : null;
                    result.put("parentCompany", parentCompany);
                    boolean parentExists = parentCompany != null && checkIfParentCompanyExists(parentCompany);
                    result.put("parentCompanyExists", parentExists);
                    if (parentExists) {
                        result.put("shouldRemove", true);
                        result.put("reason", "Product/service entry - parent company already exists in database");
                    } else {
                        result.put("shouldRemove", false);
                        result.put("reason", "Product/service entry - parent company not found, will be marked as junk");
                    }
                } else if (action.equals("rename")) {
                    result.put("rename", true);
                    String newName = aiMap.get("newName") != null ? aiMap.get("newName").toString() : null;
                    result.put("newName", newName);
                } else {
                    result.put("keep", true);
                }
                // Extract reason
                String aiReason = aiMap.get("reason") != null ? aiMap.get("reason").toString() : null;
                if (aiReason != null && !result.containsKey("reason")) {
                    result.put("reason", aiReason);
                }
            } else {
                // Fallback parsing
                if (response.toLowerCase().contains("delete")) {
                    result.put("delete", true);
                    result.put("reason", "Parsed from AI response - marked for deletion");
                } else if (response.toLowerCase().contains("rename")) {
                    result.put("rename", true);
                    result.put("reason", "Parsed from AI response - marked for rename");
                } else {
                    result.put("keep", true);
                    result.put("reason", "Parsed from AI response - no action needed");
                }
            }
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            result.put("error", "Failed to parse AI response");
        }
        return result;
    }

    /**
     * Check if parent company exists in database
     */
    private boolean checkIfParentCompanyExists(String parentCompanyName) {
        try {
            // Check for exact match first
            Optional<Company> exactMatch = companyRepository.findByNameIgnoreCase(parentCompanyName);
            if (exactMatch.isPresent()) {
                return true;
            }

            // Check for partial matches (case insensitive)
            List<Company> allCompanies = companyRepository.findAll();
            for (Company company : allCompanies) {
                if (company.getName() != null &&
                    (company.getName().toLowerCase().contains(parentCompanyName.toLowerCase()) ||
                        parentCompanyName.toLowerCase().contains(company.getName().toLowerCase()))) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking if parent company exists: {}", e.getMessage());
            return false;
        }
    }
} 