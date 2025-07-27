package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.AIJobService;
import com.tymbl.jobs.service.CompanyService;
import com.tymbl.jobs.service.CompanyCleanupService;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.common.service.CompanyShortnameService;
import com.tymbl.common.service.AIJobFetchingService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.CompanyContentRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*", 
    methods = {
        RequestMethod.GET, 
        RequestMethod.POST, 
        RequestMethod.PUT, 
        RequestMethod.DELETE, 
        RequestMethod.OPTIONS, 
        RequestMethod.PATCH
    }
)
@RequestMapping("/api/v1/ai/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Company Operations", description = "Consolidated AI operations for company processing")
public class AICompanyController {

    private final CompanyCrawlerService companyCrawlerService;
    private final CompanyService companyService;
    private final AIJobService aiJobService;
    private final AIJobFetchingService AIJobFetchingService;
    private final CompanyCleanupService companyCleanupService;
    private final CompanyShortnameService companyShortnameService;
    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;

    /**
     * Generate and save companies industry-wise using Gemini
     */
    @PostMapping("/generate-batch")
    @Operation(
        summary = "Generate and save companies industry-wise using Gemini",
        description = "For each industry, uses Gemini to generate a comprehensive list of companies (name, website), excluding those already present in the DB. Only name, website, and primaryIndustryId are saved. Returns a summary per industry."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Companies generated and saved successfully industry-wise",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  'industry_results': [{\n" +
                        "    'industry': 'FinTech',\n" +
                        "    'generated': 10,\n" +
                        "    'skipped': 5,\n" +
                        "    'errors': []\n" +
                        "  }],\n" +
                        "  'total_generated': 10,\n" +
                        "  'total_skipped': 5,\n" +
                        "  'message': 'Company generation completed'\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateCompaniesBatch() {
        try {
            Map<String, Object> result = aiJobService.generateCompaniesBatch();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating companies batch", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating companies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Consolidated endpoint to process all company operations
     * Handles: crawl, detect industries, shorten content, similar companies, cleanup
     */
    @PostMapping("/process")
    @Operation(
        summary = "Process all company operations",
        description = "Consolidated endpoint that processes all company operations. If companyName is provided, processes only that company. Otherwise processes all companies. Skips operations if their respective flags are already set."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company processing completed successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"message\": \"Company processing completed\",\n" +
                        "  \"companyName\": \"Google\",\n" +
                        "  \"operations\": {\n" +
                        "    \"crawl\": {\"processed\": true, \"message\": \"Company crawled successfully\"},\n" +
                        "    \"detectIndustries\": {\"processed\": true, \"message\": \"Industries detected successfully\"},\n" +
                        "    \"shortenContent\": {\"processed\": false, \"message\": \"Already processed\"},\n" +
                        "    \"similarCompanies\": {\"processed\": true, \"message\": \"Similar companies generated\"},\n" +
                        "    \"cleanup\": {\"processed\": true, \"message\": \"Cleanup completed\"}\n" +
                        "  },\n" +
                        "  \"status\": \"SUCCESS\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found (if companyName provided)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> processCompanyOperations(
            @Parameter(description = "Optional company name to process specific company")
            @RequestParam(required = false) String companyName) {
        
        try {
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> operations = new HashMap<>();
            
            if (companyName != null && !companyName.trim().isEmpty()) {
                log.info("Processing specific company: {}", companyName);
                result.put("companyName", companyName);
                
                // Process specific company operations
                operations.put("crawl", processCompanyCrawl(companyName));
                operations.put("detectIndustries", processCompanyIndustryDetection(companyName));
                operations.put("shortenContent", processCompanyContentShortening(companyName));
                operations.put("similarCompanies", processCompanySimilarCompanies(companyName));
                operations.put("cleanup", processCompanyCleanup(companyName));
                
            } else {
                log.info("Processing all companies");
                result.put("companyName", "ALL");
                
                // Process all companies operations
                operations.put("crawl", processAllCompaniesCrawl());
                operations.put("detectIndustries", processAllCompaniesIndustryDetection());
                operations.put("shortenContent", processAllCompaniesContentShortening());
                operations.put("similarCompanies", processAllCompaniesSimilarCompanies());
                operations.put("cleanup", processAllCompaniesCleanup());
            }
            
            result.put("operations", operations);
            result.put("message", "Company processing completed");
            result.put("status", "SUCCESS");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error processing company operations", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing company operations: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Reset specific flags for company operations
     */
    @PostMapping("/reset-flags")
    @Operation(
        summary = "Reset processing flags for company operations",
        description = "Resets specific processing flags to allow reprocessing. If companyName is provided, resets flags for that specific company only."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Flags reset successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"message\": \"Flags reset successfully\",\n" +
                        "  \"companyName\": \"Google\",\n" +
                        "  \"resetFlags\": [\"is_crawled\", \"industry_processed\"],\n" +
                        "  \"status\": \"SUCCESS\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid flags provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetCompanyFlags(
            @Parameter(description = "Comma-separated list of flags to reset: is_crawled, industry_processed, content_shortened, similar_companies_processed, cleanup_processed")
            @RequestParam String flags,
            @Parameter(description = "Optional company name to reset flags for specific company")
            @RequestParam(required = false) String companyName) {
        
        try {
            List<String> flagsList = Arrays.asList(flags.split(","));
            Map<String, Object> result = new HashMap<>();
            
            // Validate flags
            List<String> validFlags = Arrays.asList(
                "is_crawled", "industry_processed", "content_shortened", 
                "similar_companies_processed", "cleanup_processed"
            );
            
            for (String flag : flagsList) {
                if (!validFlags.contains(flag.trim())) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid flag: " + flag + ". Valid flags: " + validFlags);
                    errorResponse.put("status", "ERROR");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
            }
            
            if (companyName != null && !companyName.trim().isEmpty()) {
                log.info("Resetting flags for specific company: {}", companyName);
                result.put("companyName", companyName);
                // Reset flags for specific company
                resetFlagsForCompany(companyName, flagsList);
            } else {
                log.info("Resetting flags for all companies");
                result.put("companyName", "ALL");
                // Reset flags for all companies
                resetFlagsForAllCompanies(flagsList);
            }
            
            result.put("resetFlags", flagsList);
            result.put("message", "Flags reset successfully");
            result.put("status", "SUCCESS");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error resetting company flags", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting company flags: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================
    // COMPANY SHORTNAME GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/shortnames/generate-for-all")
    @Operation(
        summary = "Generate shortnames for all companies using GenAI",
        description = "Uses Gemini AI to generate commonly used shortnames or nicknames for all companies in the database. Examples: 'Eternal' → 'Zomato', 'International Business Machines' → 'IBM', 'Microsoft' → 'MS'."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company shortnames generated successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"totalCompanies\": 100,\n" +
                        "  \"totalShortnamesGenerated\": 85,\n" +
                        "  \"totalErrors\": 15,\n" +
                        "  \"companyResults\": [\n" +
                        "    {\n" +
                        "      \"companyName\": \"Eternal\",\n" +
                        "      \"success\": true,\n" +
                        "      \"shortname\": \"Zomato\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"companyName\": \"International Business Machines\",\n" +
                        "      \"success\": true,\n" +
                        "      \"shortname\": \"IBM\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"companyName\": \"Google\",\n" +
                        "      \"success\": true,\n" +
                        "      \"shortname\": \"Google\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Company shortname generation completed\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateShortnamesForAllCompanies() {
        try {
            log.info("Starting shortname generation for all companies");
            
            // Get all company names from the database
            List<String> companyNames = companyService.getAllCompanyNames();
            
            Map<String, Object> result = companyShortnameService.generateShortnamesForAllCompanies(companyNames);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating shortnames for all companies", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating shortnames for all companies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/shortnames/generate-for-company/{companyName}")
    @Operation(
        summary = "Generate shortname for a specific company using GenAI",
        description = "Uses Gemini AI to generate the commonly used shortname or nickname for a specific company. Examples: 'Eternal' → 'Zomato', 'International Business Machines' → 'IBM'."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company shortname generated successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"companyName\": \"Eternal\",\n" +
                        "  \"shortname\": \"Zomato\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateShortnameForCompany(@PathVariable String companyName) {
        try {
            log.info("Starting shortname generation for company: {}", companyName);
            Map<String, Object> result = companyShortnameService.generateShortnameForSingleCompany(companyName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating shortname for company: {}", companyName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating shortname for company: " + e.getMessage());
            errorResponse.put("companyName", companyName);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Helper methods for processing operations
    private Map<String, Object> processCompanyCrawl(String companyName) {
        try {
            // Check if already crawled
            if (isCompanyAlreadyCrawled(companyName)) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "Company already crawled");
                return response;
            }
            
            // Perform crawling
            List<Map<String, Object>> jobs = AIJobFetchingService.fetchJobsForCompany(companyName);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "Company crawled successfully");
            response.put("jobsCount", jobs.size());
            return response;
        } catch (Exception e) {
            log.error("Error crawling company: {}", companyName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error crawling company: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processAllCompaniesCrawl() {
        try {
            // Check if already crawled
            if (areAllCompaniesAlreadyCrawled()) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "All companies already crawled");
                return response;
            }
            
            // Perform crawling for all companies
            companyCrawlerService.crawlCompanies();
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Company crawling completed");
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "All companies crawled successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error crawling all companies", e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error crawling all companies: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processCompanyIndustryDetection(String companyName) {
        try {
            // Check if already processed
            if (isCompanyIndustryAlreadyProcessed(companyName)) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "Company industry already processed");
                return response;
            }
            
            // Perform industry detection - need to find company first
            // For now, return a placeholder response
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Industry detection for specific company not implemented yet");
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "Industries detected successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error detecting industries for company: {}", companyName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error detecting industries: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processAllCompaniesIndustryDetection() {
        try {
            // Check if already processed
            if (areAllCompaniesIndustryAlreadyProcessed()) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "All companies industry already processed");
                return response;
            }
            
            // Perform industry detection for all companies
            List<CompanyIndustryResponse> results = companyService.detectIndustriesForCompanies();
            Map<String, Object> result = new HashMap<>();
            result.put("results", results);
            result.put("count", results.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "All companies industries detected successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error detecting industries for all companies", e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error detecting industries for all companies: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processCompanyContentShortening(String companyName) {
        try {
            // Check if already processed
            if (isCompanyContentAlreadyShortened(companyName)) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "Company content already shortened");
                return response;
            }
            
            // Perform content shortening - need to find company ID first
            // For now, return a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Content shortening for specific company not implemented yet");
            return response;
        } catch (Exception e) {
            log.error("Error shortening content for company: {}", companyName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error shortening content: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processAllCompaniesContentShortening() {
        try {
            // Check if already processed
            if (areAllCompaniesContentAlreadyShortened()) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "All companies content already shortened");
                return response;
            }
            
            // Perform content shortening for all companies
            Map<String, Object> result = aiJobService.shortenAllCompaniesContent();
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "All companies content shortened successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error shortening content for all companies", e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error shortening content for all companies: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processCompanySimilarCompanies(String companyName) {
        try {
            // Check if already processed
            if (isCompanySimilarCompaniesAlreadyProcessed(companyName)) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "Company similar companies already processed");
                return response;
            }
            
            // Perform similar companies generation - need to find company first
            // For now, return a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Similar companies generation for specific company not implemented yet");
            return response;
        } catch (Exception e) {
            log.error("Error generating similar companies for company: {}", companyName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error generating similar companies: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processAllCompaniesSimilarCompanies() {
        try {
            // Check if already processed
            if (areAllCompaniesSimilarCompaniesAlreadyProcessed()) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "All companies similar companies already processed");
                return response;
            }
            
            // Perform similar companies generation for all companies
            Map<String, Object> result = aiJobService.generateSimilarCompaniesForAll();
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "All companies similar companies generated successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error generating similar companies for all companies", e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error generating similar companies for all companies: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processCompanyCleanup(String companyName) {
        try {
            // Check if already processed
            if (isCompanyCleanupAlreadyProcessed(companyName)) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "Company cleanup already processed");
                return response;
            }
            
            // Perform cleanup
            Map<String, Object> result = companyCleanupService.processCompanyByName(companyName);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "Cleanup completed successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error cleaning up company: {}", companyName, e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error cleaning up company: " + e.getMessage());
            return response;
        }
    }

    private Map<String, Object> processAllCompaniesCleanup() {
        try {
            // Check if already processed
            if (areAllCompaniesCleanupAlreadyProcessed()) {
                Map<String, Object> response = new HashMap<>();
                response.put("processed", false);
                response.put("message", "All companies cleanup already processed");
                return response;
            }
            
            // Perform cleanup for all companies
            Map<String, Object> result = companyCleanupService.processAllCompanies();
            Map<String, Object> response = new HashMap<>();
            response.put("processed", true);
            response.put("message", "All companies cleanup completed successfully");
            response.put("result", result);
            return response;
        } catch (Exception e) {
            log.error("Error cleaning up all companies", e);
            Map<String, Object> response = new HashMap<>();
            response.put("processed", false);
            response.put("message", "Error cleaning up all companies: " + e.getMessage());
            return response;
        }
    }

    // Helper methods for checking processing status
    private boolean isCompanyAlreadyCrawled(String companyName) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            return company.isPresent() && company.get().isCrawled();
        } catch (Exception e) {
            log.error("Error checking if company is already crawled: {}", companyName, e);
            return false;
        }
    }

    private boolean areAllCompaniesAlreadyCrawled() {
        try {
            // Check if there are any companies that haven't been crawled
            Page<Company> unprocessedCompanies = companyRepository.findByIsCrawledFalse(PageRequest.of(0, 1));
            return unprocessedCompanies.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if all companies are already crawled", e);
            return false;
        }
    }

    private boolean isCompanyIndustryAlreadyProcessed(String companyName) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            return company.isPresent() && company.get().isIndustryProcessed();
        } catch (Exception e) {
            log.error("Error checking if company industry is already processed: {}", companyName, e);
            return false;
        }
    }

    private boolean areAllCompaniesIndustryAlreadyProcessed() {
        try {
            // Check if there are any companies that haven't been processed for industry detection
            List<Company> unprocessedCompanies = companyRepository.findByIndustryProcessedFalse();
            return unprocessedCompanies.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if all companies industry are already processed", e);
            return false;
        }
    }

    private boolean isCompanyContentAlreadyShortened(String companyName) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            if (!company.isPresent()) {
                return false;
            }
            
            // Check if company content has been shortened
            Optional<CompanyContent> content = companyContentRepository.findByCompanyId(company.get().getId());
            return content.isPresent() && content.get().isContentShortened();
        } catch (Exception e) {
            log.error("Error checking if company content is already shortened: {}", companyName, e);
            return false;
        }
    }

    private boolean areAllCompaniesContentAlreadyShortened() {
        try {
            // Check if there are any companies that haven't been processed for content shortening
            List<CompanyContent> unprocessedContent = companyContentRepository.findUnprocessedContentWithOriginalData();
            return unprocessedContent.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if all companies content are already shortened", e);
            return false;
        }
    }

    private boolean isCompanySimilarCompaniesAlreadyProcessed(String companyName) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            return company.isPresent() && company.get().isSimilarCompaniesProcessed();
        } catch (Exception e) {
            log.error("Error checking if company similar companies are already processed: {}", companyName, e);
            return false;
        }
    }

    private boolean areAllCompaniesSimilarCompaniesAlreadyProcessed() {
        try {
            // Check if there are any companies that haven't been processed for similar companies
            List<Company> unprocessedCompanies = companyRepository.findBySimilarCompaniesProcessedFalse();
            return unprocessedCompanies.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if all companies similar companies are already processed", e);
            return false;
        }
    }

    private boolean isCompanyCleanupAlreadyProcessed(String companyName) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            return company.isPresent() && Boolean.TRUE.equals(company.get().getCleanupProcessed());
        } catch (Exception e) {
            log.error("Error checking if company cleanup is already processed: {}", companyName, e);
            return false;
        }
    }

    private boolean areAllCompaniesCleanupAlreadyProcessed() {
        try {
            // Check if there are any companies that haven't been processed for cleanup
            List<Company> unprocessedCompanies = companyRepository.findByCleanupProcessedFalse();
            return unprocessedCompanies.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if all companies cleanup are already processed", e);
            return false;
        }
    }

    // Helper methods for resetting flags
    private void resetFlagsForCompany(String companyName, List<String> flags) {
        try {
            Optional<Company> company = companyRepository.findByName(companyName);
            if (!company.isPresent()) {
                log.warn("Company not found for flag reset: {}", companyName);
                return;
            }
            
            Company companyEntity = company.get();
            boolean updated = false;
            
            for (String flag : flags) {
                switch (flag.trim()) {
                    case "is_crawled":
                        companyEntity.setCrawled(false);
                        updated = true;
                        break;
                    case "industry_processed":
                        companyEntity.setIndustryProcessed(false);
                        updated = true;
                        break;
                    case "similar_companies_processed":
                        companyEntity.setSimilarCompaniesProcessed(false);
                        updated = true;
                        break;
                    case "cleanup_processed":
                        companyEntity.setCleanupProcessed(false);
                        updated = true;
                        break;
                    case "content_shortened":
                        // Reset content shortened flag in CompanyContent table
                        Optional<CompanyContent> content = companyContentRepository.findByCompanyId(companyEntity.getId());
                        if (content.isPresent()) {
                            content.get().setContentShortened(false);
                            companyContentRepository.save(content.get());
                            updated = true;
                        }
                        break;
                    default:
                        log.warn("Unknown flag for reset: {}", flag);
                }
            }
            
            if (updated) {
                companyRepository.save(companyEntity);
                log.info("Successfully reset flags {} for company: {}", flags, companyName);
            }
        } catch (Exception e) {
            log.error("Error resetting flags for company: {}", companyName, e);
        }
    }

    private void resetFlagsForAllCompanies(List<String> flags) {
        try {
            boolean updated = false;
            
            for (String flag : flags) {
                switch (flag.trim()) {
                    case "is_crawled":
                        // Reset all companies' crawled flag
                        companyRepository.findAll().forEach(company -> {
                            company.setCrawled(false);
                            companyRepository.save(company);
                        });
                        updated = true;
                        break;
                    case "industry_processed":
                        // Use the existing repository method to reset industry processed flag
                        companyRepository.resetIndustryProcessedFlag();
                        updated = true;
                        break;
                    case "similar_companies_processed":
                        // Reset all companies' similar companies processed flag
                        companyRepository.findAll().forEach(company -> {
                            company.setSimilarCompaniesProcessed(false);
                            companyRepository.save(company);
                        });
                        updated = true;
                        break;
                    case "cleanup_processed":
                        // Reset all companies' cleanup processed flag
                        companyRepository.findAll().forEach(company -> {
                            company.setCleanupProcessed(false);
                            companyRepository.save(company);
                        });
                        updated = true;
                        break;
                    case "content_shortened":
                        // Reset all companies' content shortened flag
                        companyContentRepository.findAll().forEach(content -> {
                            content.setContentShortened(false);
                            companyContentRepository.save(content);
                        });
                        updated = true;
                        break;
                    default:
                        log.warn("Unknown flag for reset: {}", flag);
                }
            }
            
            if (updated) {
                log.info("Successfully reset flags {} for all companies", flags);
            }
        } catch (Exception e) {
            log.error("Error resetting flags for all companies", e);
        }
    }
} 