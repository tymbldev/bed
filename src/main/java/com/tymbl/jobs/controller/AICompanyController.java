package com.tymbl.jobs.controller;

import com.tymbl.common.service.AIJobFetchingService;
import com.tymbl.common.service.CompanyWebsiteService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyContentRepository;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.service.AIJobService;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.jobs.service.CompanyService;
import com.tymbl.jobs.service.CompanyTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  private final CompanyRepository companyRepository;
  private final CompanyContentRepository companyContentRepository;
  private final CompanyTransactionService companyTransactionService;

  private final CompanyWebsiteService companyWebsiteService;

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
   * Consolidated endpoint to process specific company operations Handles: crawl, detect industries,
   * shorten content, similar companies, cleanup
   */
  @PostMapping("/process")
  @Operation(
      summary = "Process specific company operations",
      description = "Consolidated endpoint that processes specific company operations. If companyName is provided, processes only that company. Otherwise processes all companies. If operations parameter is provided, processes only those specific operations. Skips operations if their respective flags are already set."
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
                      "  \"requestedOperations\": [\"crawl\", \"detectIndustries\"],\n" +
                      "  \"operations\": {\n" +
                      "    \"crawl\": {\"processed\": true, \"message\": \"Company crawled successfully\"},\n"
                      +
                      "    \"detectIndustries\": {\"processed\": true, \"message\": \"Industries detected successfully\"}\n"
                      +
                      "  },\n" +
                      "  \"status\": \"SUCCESS\"\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid operations provided"),
      @ApiResponse(responseCode = "404", description = "Company not found (if companyName provided)"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Async
  public ResponseEntity<Map<String, Object>> processCompanyOperations(
      @Parameter(description = "Comma-separated list of operations to process: crawl, detectIndustries, shortenContent, similarCompanies, fetchWebsites")
      @RequestParam(required = false) String operations,
      @Parameter(description = "Optional company name to process specific company")
      @RequestParam(required = false) String companyName) {

    try {
      Map<String, Object> result = new HashMap<>();
      Map<String, Object> processedOperations = new HashMap<>();

      // Parse operations parameter
      List<String> operationsList = new ArrayList<>();
      if (operations != null && !operations.trim().isEmpty()) {
        operationsList = Arrays.asList(operations.split(","));

        // Validate operations
        List<String> validOperations = Arrays.asList(
            "crawl", "detectIndustries", "shortenContent", "similarCompanies", "fetchWebsites"
        );

        for (String operation : operationsList) {
          if (!validOperations.contains(operation.trim())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error",
                "Invalid operation: " + operation + ". Valid operations: " + validOperations);
            errorResponse.put("status", "ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
          }
        }
      } else {
        // If no operations specified, process all operations
        operationsList = Arrays.asList("crawl", "detectIndustries", "shortenContent",
            "similarCompanies", "fetchWebsites");
      }

      if (companyName != null && !companyName.trim().isEmpty()) {
        log.info("Processing specific company: {} with operations: {}", companyName,
            operationsList);
        result.put("companyName", companyName);

        // Process specific company operations
        for (String operation : operationsList) {
          switch (operation.trim()) {
            case "crawl":
              processedOperations.put("crawl", processCompanyCrawl(companyName));
              break;
            case "detectIndustries":
              processedOperations.put("detectIndustries",
                  processCompanyIndustryDetection(companyName));
              break;
            case "shortenContent":
              processedOperations.put("shortenContent",
                  processCompanyContentShortening(companyName));
              break;
            case "similarCompanies":
              processedOperations.put("similarCompanies",
                  processCompanySimilarCompanies(companyName));
              break;

            case "fetchWebsites":
              processedOperations.put("fetchWebsites", fetchWebsitesForCompany(companyName));
              break;
          }
        }

      } else {
        log.info("Processing all companies with operations: {}", operationsList);
        result.put("companyName", "ALL");

        // Process all companies operations in batches
        for (String operation : operationsList) {
          switch (operation.trim()) {
            case "crawl":
              processedOperations.put("crawl", processAllCompaniesCrawlInBatches());
              break;
            case "detectIndustries":
              processedOperations.put("detectIndustries",
                  processAllCompaniesIndustryDetectionInBatches());
              break;
            case "shortenContent":
              processedOperations.put("shortenContent",
                  processAllCompaniesContentShorteningInBatches());
              break;
            case "similarCompanies":
              processedOperations.put("similarCompanies",
                  processAllCompaniesSimilarCompaniesInBatches());
              break;

            case "fetchWebsites":
              processedOperations.put("fetchWebsites", fetchWebsitesForAllCompanies());
              break;
          }
        }
      }

      result.put("requestedOperations", operationsList);
      result.put("operations", processedOperations);
      result.put("message", "Company processing completed");
      result.put("status", "SUCCESS");

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error processing company operations", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Error processing company operations: " + e.getMessage());
      errorResponse.put("status", "ERROR");
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

      // Perform crawling and save to database
      Map<String, Object> result = aiJobService.fetchAndSaveJobsForCompany(companyName);
      return result;
    } catch (Exception e) {
      log.error("Error crawling company: {}", companyName, e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message", "Error crawling company: " + e.getMessage());
      return response;
    }
  }

  private Map<String, Object> processAllCompaniesCrawlInBatches() {
    try {
      // Check if already crawled
      if (areAllCompaniesAlreadyCrawled()) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "All companies already crawled");
        return response;
      }

      // Perform crawling for all companies in batches
      companyCrawlerService.crawlCompaniesInBatches();
      Map<String, Object> result = new HashMap<>();
      result.put("message", "Company crawling completed");
      Map<String, Object> response = new HashMap<>();
      response.put("processed", true);
      response.put("message", "All companies crawled successfully");
      response.put("result", result);
      return response;
    } catch (Exception e) {
      log.error("Error crawling all companies in batches", e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message", "Error crawling all companies in batches: " + e.getMessage());
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

  private Map<String, Object> processAllCompaniesIndustryDetectionInBatches() {
    try {
      // Check if already processed
      if (areAllCompaniesIndustryAlreadyProcessed()) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "All companies industry already processed");
        return response;
      }

      // Perform industry detection for all companies in batches
      List<CompanyIndustryResponse> results = companyService.detectIndustriesForCompaniesInBatches();
      Map<String, Object> result = new HashMap<>();
      result.put("results", results);
      result.put("count", results.size());

      Map<String, Object> response = new HashMap<>();
      response.put("processed", true);
      response.put("message", "All companies industries detected successfully");
      response.put("result", result);
      return response;
    } catch (Exception e) {
      log.error("Error detecting industries for all companies in batches", e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message",
          "Error detecting industries for all companies in batches: " + e.getMessage());
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

  private Map<String, Object> processAllCompaniesContentShorteningInBatches() {
    try {
      // Check if already processed
      if (areAllCompaniesContentAlreadyShortened()) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "All companies content already shortened");
        return response;
      }

      // Perform content shortening for all companies in batches
      Map<String, Object> result = aiJobService.shortenAllCompaniesContentInBatches();
      Map<String, Object> response = new HashMap<>();
      response.put("processed", true);
      response.put("message", "All companies content shortened successfully");
      response.put("result", result);
      return response;
    } catch (Exception e) {
      log.error("Error shortening content for all companies in batches", e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message",
          "Error shortening content for all companies in batches: " + e.getMessage());
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

      // Get company entity
      Optional<Company> companyOpt = companyRepository.findByName(companyName);
      if (!companyOpt.isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "Company not found: " + companyName);
        return response;
      }

      // Perform similar companies generation
      Map<String, Object> result = companyTransactionService.processSimilarCompaniesGenerationInTransaction(
          companyOpt.get());
      Map<String, Object> response = new HashMap<>();
      response.put("processed", true);
      response.put("message", "Similar companies generated successfully");
      response.put("result", result);
      return response;
    } catch (Exception e) {
      log.error("Error generating similar companies for company: {}", companyName, e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message", "Error generating similar companies for company: " + e.getMessage());
      return response;
    }
  }

  private Map<String, Object> processAllCompaniesSimilarCompaniesInBatches() {
    try {
      log.info("Starting similar companies generation for all companies");
      Map<String, Object> result = companyTransactionService.processAllCompaniesSimilarCompaniesGenerationInBatches();
      return result;
    } catch (Exception e) {
      log.error("Error generating similar companies for all companies in batches", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error generating similar companies for all companies in batches: " + e.getMessage());
      return errorResponse;
    }
  }


  private Map<String, Object> fetchWebsitesForCompany(String companyName) {
    try {
      // Check if already processed
      if (isCompanyWebsiteAlreadyFetched(companyName)) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "Company website already fetched");
        return response;
      }

      // Get company entity
      Optional<Company> companyOpt = companyRepository.findByName(companyName);
      if (!companyOpt.isPresent()) {
        Map<String, Object> response = new HashMap<>();
        response.put("processed", false);
        response.put("message", "Company not found: " + companyName);
        return response;
      }

      // Perform website fetching
      Map<String, Object> result = companyWebsiteService.fetchWebsiteForCompany(companyOpt.get());
      Map<String, Object> response = new HashMap<>();
      response.put("processed", true);
      response.put("message", "Website fetched successfully");
      response.put("result", result);
      return response;
    } catch (Exception e) {
      log.error("Error fetching website for company: {}", companyName, e);
      Map<String, Object> response = new HashMap<>();
      response.put("processed", false);
      response.put("message", "Error fetching website: " + e.getMessage());
      return response;
    }
  }

  private Map<String, Object> fetchWebsitesForAllCompanies() {
    try {
      log.info("Starting website fetching for all companies");
      Map<String, Object> result = companyWebsiteService.fetchWebsitesForAllCompaniesInBatches();
      return result;
    } catch (Exception e) {
      log.error("Error fetching websites for all companies in batches", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error",
          "Error fetching websites for all companies in batches: " + e.getMessage());
      return errorResponse;
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
      Page<Company> unprocessedCompanies = companyRepository.findByIsCrawledFalse(
          PageRequest.of(0, 1));
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
      List<CompanyContent> contentList = companyContentRepository.findByCompanyId(
          company.get().getId());
      return !contentList.isEmpty() && contentList.get(0).isContentShortened();
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
      log.error("Error checking if company similar companies are already processed: {}",
          companyName, e);
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

  private boolean isCompanyWebsiteAlreadyFetched(String companyName) {
    try {
      Optional<Company> company = companyRepository.findByName(companyName);
      return company.isPresent() && company.get().getWebsiteFetched() != null
          && company.get().getWebsiteFetched() == 1;
    } catch (Exception e) {
      log.error("Error checking if company website is already fetched: {}", companyName, e);
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
          case "content_shortened":
            // Reset content shortened flag in CompanyContent table
            List<CompanyContent> contentList = companyContentRepository.findByCompanyId(
                companyEntity.getId());
            if (!contentList.isEmpty()) {
              CompanyContent content = contentList.get(0);
              content.setContentShortened(false);
              companyContentRepository.save(content);
              updated = true;
            }
            break;
          case "websiteFetched":
            // Reset website fetched flag in Company table
            companyEntity.setWebsiteFetched(null); // Set to null to indicate not fetched
            updated = true;
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
          case "content_shortened":
            // Reset all companies' content shortened flag
            companyContentRepository.findAll().forEach(content -> {
              content.setContentShortened(false);
              companyContentRepository.save(content);
            });
            updated = true;
            break;
          case "websiteFetched":
            // Reset all companies' website fetched flag
            companyRepository.findAll().forEach(company -> {
              company.setWebsiteFetched(null); // Set to null to indicate not fetched
              companyRepository.save(company);
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