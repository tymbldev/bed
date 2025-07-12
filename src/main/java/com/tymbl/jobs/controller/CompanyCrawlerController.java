package com.tymbl.jobs.controller;

import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.jobs.service.CompanyService;
import com.tymbl.jobs.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

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
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Crawler", description = "APIs for crawling company information and jobs from LinkedIn")
public class CompanyCrawlerController {

    private final CompanyCrawlerService companyCrawlerService;
    private final CompanyService companyService;

    @PostMapping("/companies/crawl")
    @Operation(summary = "Crawl all companies", description = "Triggers the crawling process for all companies from the companies.txt file")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company crawling process started successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Company crawling process started successfully\",\n" +
                          "  \"status\": \"SUCCESS\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error during crawling process")
    })
    public ResponseEntity<Object> crawlCompanies() {
        try {
            log.info("Manual company crawling triggered via API");
            companyCrawlerService.crawlCompanies();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Company crawling process completed successfully");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during company crawling process", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error during company crawling process: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/companies/{companyId}/jobs/crawl")
    @Operation(summary = "Crawl jobs for specific company", description = "Triggers job crawling for a specific company by ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Job crawling process started successfully for the company",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Job crawling process started successfully for company: Google\",\n" +
                          "  \"companyId\": 1,\n" +
                          "  \"companyName\": \"Google\",\n" +
                          "  \"status\": \"SUCCESS\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error during crawling process")
    })
    public ResponseEntity<Object> crawlJobsForCompany(@PathVariable Long companyId) {
        try {
            // Get company to verify it exists and get name for response
            CompanyResponse companyResponse = companyService.getCompanyById(companyId);
            
            log.info("Manual job crawling triggered for company: {} (ID: {})", companyResponse.getName(), companyId);
            
            // Get the company entity for the crawler service
            Company company = new Company();
            company.setId(companyId);
            company.setName(companyResponse.getName());
            

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job crawling process completed successfully for company: " + companyResponse.getName());
            response.put("companyId", companyId);
            response.put("companyName", companyResponse.getName());
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during job crawling process for company ID: {}", companyId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Error during job crawling process: " + e.getMessage());
            response.put("companyId", companyId);
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/companies/{companyName}/jobs/crawl-by-name")
    @Operation(summary = "Crawl jobs for company by name", description = "Triggers job crawling for a company by name")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Job crawling process started successfully for the company",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Job crawling process started successfully for company: Google\",\n" +
                          "  \"companyName\": \"Google\",\n" +
                          "  \"status\": \"SUCCESS\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error during crawling process")
    })
    public ResponseEntity<Object> crawlJobsForCompanyByName(@PathVariable String companyName) {
        try {
            log.info("Manual job crawling triggered for company by name: {}", companyName);
            
            // Create a company object with just the name for the crawler service
            Company company = new Company();
            company.setName(companyName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Job crawling process completed successfully for company: " + companyName);
            response.put("companyName", companyName);
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during job crawling process for company: {}", companyName, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error during job crawling process: " + e.getMessage());
            response.put("companyName", companyName);
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/detect-industries")
    @Operation(summary = "Detect industries for all companies", description = "Detects primary and secondary industries for all companies using AI or manual detection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Industry detection completed successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyIndustryResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"companyId\": 1,\n" +
                        "    \"companyName\": \"Yatra\",\n" +
                        "    \"primaryIndustry\": \"Travel & Hospitality Technology\",\n" +
                        "    \"primaryIndustryId\": 15,\n" +
                        "    \"secondaryIndustries\": [\"OTA\", \"Travel Tech\", \"Product Based Company\"],\n" +
                        "    \"processed\": true,\n" +
                        "    \"error\": null\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"companyId\": 2,\n" +
                        "    \"companyName\": \"Google\",\n" +
                        "    \"primaryIndustry\": \"Information Technology & Services\",\n" +
                        "    \"primaryIndustryId\": 1,\n" +
                        "    \"secondaryIndustries\": [\"Cloud Computing\", \"AI/ML\", \"Product Based Company\"],\n" +
                        "    \"processed\": true,\n" +
                        "    \"error\": null\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CompanyIndustryResponse>> detectIndustriesForCompanies(
        @RequestParam(defaultValue = "false") boolean useGemini) {
        return ResponseEntity.ok(companyService.detectIndustriesForCompanies(useGemini));
    }
} 