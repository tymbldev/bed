package com.tymbl.jobs.controller;

import com.tymbl.common.service.CityGenerationService;
import com.tymbl.common.service.AIJobFetchingService;
import com.tymbl.common.service.ProcessedNameService;
import com.tymbl.common.service.DesignationGenerationService;
import com.tymbl.common.service.CompanyShortnameService;
import com.tymbl.common.service.SecondaryIndustryMappingService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.service.CompanyCleanupService;
import com.tymbl.jobs.service.ElasticsearchIndexingService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.service.AIJobService;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.jobs.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
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
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Utilities", description = "APIs for AI-powered data generation and enrichment utilities")
public class AIController {

    private final CompanyCrawlerService companyCrawlerService;
    private final CompanyService companyService;
    private final AIJobService aiJobService;
    private final AIJobFetchingService AIJobFetchingService;
    private final CityGenerationService cityGenerationService;
    private final ProcessedNameService processedNameService;
    private final DesignationGenerationService designationGenerationService;
    private final CompanyShortnameService companyShortnameService;
    private final SecondaryIndustryMappingService secondaryIndustryMappingService;
    private final GeminiService geminiService;
    private final CompanyCleanupService companyCleanupService;
    private final ElasticsearchIndexingService elasticsearchIndexingService;

    // ============================================================================
    // COMPANY CRAWLING ENDPOINTS (Legacy - kept for backward compatibility)
    // ============================================================================

    @PostMapping("/companies/generate-batch")
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
                          "  \"jobs\": [\n" +
                          "    {\n" +
                          "      \"title\": \"Software Engineer\",\n" +
                          "      \"designation\": \"Software Engineer\",\n" +
                          "      \"description\": \"Join our team...\",\n" +
                          "      \"location\": {\"city\": \"Mountain View\", \"country\": \"USA\"},\n" +
                          "      \"job_type\": \"Hybrid\",\n" +
                          "      \"salary\": {\"min\": 120000, \"max\": 180000, \"currency\": \"USD\"},\n" +
                          "      \"experience\": {\"min\": 2, \"max\": 5},\n" +
                          "      \"skills\": [\"Java\", \"Spring\"],\n" +
                          "      \"tags\": [\"backend\", \"fullstack\"],\n" +
                          "      \"openings\": 5,\n" +
                          "      \"posted\": \"2025-01-15\",\n" +
                          "      \"platform\": \"LinkedIn\",\n" +
                          "      \"apply_url\": \"https://careers.google.com/jobs/123\"\n" +
                          "    }\n" +
                          "  ],\n" +
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
            log.info("Manual job crawling triggered for company ID: {}", companyId);
            
            // Get company name from ID
            String companyName = companyService.getCompanyNameById(companyId);
            if (companyName == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Company not found with ID: " + companyId);
                response.put("status", "ERROR");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Fetch jobs using JobFetchingService
            List<Map<String, Object>> jobs = AIJobFetchingService.fetchJobsForCompany(companyName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job crawling process completed successfully for company: " + companyName);
            response.put("companyId", companyId);
            response.put("companyName", companyName);
            response.put("jobs", jobs);
            response.put("jobsCount", jobs.size());
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during job crawling process for company ID: {}", companyId, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error during job crawling process: " + e.getMessage());
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
                          "  \"message\": \"Job crawling process completed successfully for company: Google\",\n" +
                          "  \"companyName\": \"Google\",\n" +
                          "  \"jobs\": [\n" +
                          "    {\n" +
                          "      \"title\": \"Software Engineer\",\n" +
                          "      \"designation\": \"Software Engineer\",\n" +
                          "      \"description\": \"Join our team...\",\n" +
                          "      \"location\": {\"city\": \"Mountain View\", \"country\": \"USA\"},\n" +
                          "      \"job_type\": \"Hybrid\",\n" +
                          "      \"salary\": {\"min\": 120000, \"max\": 180000, \"currency\": \"USD\"},\n" +
                          "      \"experience\": {\"min\": 2, \"max\": 5},\n" +
                          "      \"skills\": [\"Java\", \"Spring\"],\n" +
                          "      \"tags\": [\"backend\", \"fullstack\"],\n" +
                          "      \"openings\": 5,\n" +
                          "      \"posted\": \"2025-01-15\",\n" +
                          "      \"platform\": \"LinkedIn\",\n" +
                          "      \"apply_url\": \"https://careers.google.com/jobs/123\"\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"status\": \"SUCCESS\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error during crawling process")
    })
    public ResponseEntity<Object> crawlJobsForCompanyByName(@PathVariable String companyName) {
        try {
            log.info("Manual job crawling triggered for company: {}", companyName);
            
            // Fetch jobs using JobFetchingService
            List<Map<String, Object>> jobs = AIJobFetchingService.fetchJobsForCompany(companyName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job crawling process completed successfully for company: " + companyName);
            response.put("companyName", companyName);
            response.put("jobs", jobs);
            response.put("jobsCount", jobs.size());
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during job crawling process for company: {}", companyName, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error during job crawling process: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/detect-industries")
    @Operation(summary = "Detect industries for all unprocessed companies", description = "Detects primary and secondary industries for companies that haven't been processed yet using AI. Only processes companies where industry_processed = false.")
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
    public ResponseEntity<List<CompanyIndustryResponse>> detectIndustriesForCompanies() {
        try {
            List<CompanyIndustryResponse> results = companyService.detectIndustriesForCompanies();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error detecting industries for companies", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/detect-industries/reset")
    @Operation(summary = "Reset industry processed flag for all companies", description = "Resets the industry_processed flag to false for all companies, allowing reprocessing of industry detection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Industry processed flag reset successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"message\": \"Industry processed flag reset successfully for all companies\",\n" +
                        "  \"status\": \"SUCCESS\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetIndustryProcessedFlag() {
        try {
            companyService.resetIndustryProcessedFlag();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Industry processed flag reset successfully for all companies");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting industry processed flag", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting industry processed flag: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/companies/{companyId}/shorten-content")
    @Operation(
        summary = "Shorten company about us and culture content using AI",
        description = "Takes the original about us and culture content from a company, uses Gemini AI to shorten them to 5 key points each with proper hyphen formatting (e.g., '- First point\\n- Second point'), and saves the shortened versions to the aboutUs and culture fields. Only processes companies that haven't been processed before (content_shortened = false)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Content shortened and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"companyId\": 1,\n" +
                        "  \"companyName\": \"Google\",\n" +
                        "  \"aboutUsShortened\": true,\n" +
                        "  \"cultureShortened\": true,\n" +
                        "  \"message\": \"Content shortened and saved successfully\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(
            responseCode = "409", 
            description = "Company already processed", 
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"companyId\": 1,\n" +
                        "  \"companyName\": \"Google\",\n" +
                        "  \"aboutUsShortened\": false,\n" +
                        "  \"cultureShortened\": false,\n" +
                        "  \"message\": \"Company content has already been shortened\",\n" +
                        "  \"alreadyProcessed\": true\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> shortenCompanyContent(@PathVariable Long companyId) {
        try {
            Map<String, Object> result = aiJobService.shortenCompanyContent(companyId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Company not found for content shortening: {}", companyId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error shortening content for company ID: {}", companyId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error shortening content: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/companies/shorten-content-all")
    @Operation(
        summary = "Shorten about us and culture content for all unprocessed companies using AI",
        description = "Loops through all companies that haven't been processed for content shortening (content_shortened = false) and have original content, takes their original about us and culture content, uses Gemini AI to shorten them to 5 key points each with proper hyphen formatting (e.g., '- First point\\n- Second point'), and saves the shortened versions to the aboutUs and culture fields."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Content shortened and saved for all companies successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_companies_processed\": 50,\n" +
                        "  \"total_about_us_shortened\": 45,\n" +
                        "  \"total_culture_shortened\": 42,\n" +
                        "  \"total_errors\": 3,\n" +
                        "  \"total_skipped\": 0,\n" +
                        "  \"company_results\": [\n" +
                        "    {\n" +
                        "      \"companyId\": 1,\n" +
                        "      \"companyName\": \"Google\",\n" +
                        "      \"aboutUsShortened\": true,\n" +
                        "      \"cultureShortened\": true,\n" +
                        "      \"message\": \"Content shortened and saved successfully\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Content shortening completed for unprocessed companies\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> shortenAllCompaniesContent() {
        try {
            Map<String, Object> result = aiJobService.shortenAllCompaniesContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error shortening content for all companies", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error shortening content for all companies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // INTERVIEW QUESTION GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/interview-questions/generate-comprehensive")
    @Operation(summary = "Generate comprehensive interview questions for all skills", description = "Generates detailed interview questions for all skills in the system using AI")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comprehensive interview questions generated successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_skills_processed\": 15,\n" +
                        "  \"total_questions_generated\": 450,\n" +
                        "  \"message\": \"Comprehensive question generation completed\",\n" +
                        "  \"skill_results\": [\n" +
                        "    {\n" +
                        "      \"skill_name\": \"Java\",\n" +
                        "      \"skill_id\": 1,\n" +
                        "      \"questions_generated\": 30,\n" +
                        "      \"mappings_created\": 90,\n" +
                        "      \"status\": \"success\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}" 
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateComprehensiveInterviewQuestions() {
            try {
            Map<String, Object> result = aiJobService.generateComprehensiveInterviewQuestions();
            return ResponseEntity.ok(result);
            } catch (Exception e) {
            log.error("Error generating comprehensive interview questions", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
            }
    }

    @PostMapping("/interview-questions/generate-for-skill/{skillName}")
    @Operation(summary = "Generate comprehensive interview questions for specific skill", description = "Generates detailed interview questions for a specific skill using AI")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Comprehensive interview questions generated successfully for the skill",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"skill_name\": \"Java\",\n" +
                        "  \"skill_id\": 1,\n" +
                        "  \"questions_generated\": 30,\n" +
                        "  \"mappings_created\": 90,\n" +
                        "  \"status\": \"success\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Skill not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateComprehensiveInterviewQuestionsForSkill(
            @PathVariable String skillName) {
        try {
            Map<String, Object> result = aiJobService.generateComprehensiveInterviewQuestionsForSkill(skillName);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Skill not found for question generation: {}", skillName);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating questions for skill: {}", skillName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/generate-and-save")
    @Operation(summary = "Generate and save more tech skills using AI", description = "Uses Gemini to generate a comprehensive list of tech skills and saves new ones to the Skill table.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Skills generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"new_skills_added\": 10,\n" +
                        "  \"total_skills_generated\": 50,\n" +
                        "  \"added_skills\": [\n" +
                        "    {\"name\": \"Rust\", \"category\": \"Programming Language\", \"description\": \"A fast, safe systems programming language.\"}\n" +
                        "  ]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveTechSkills() {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveTechSkills();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving tech skills", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/{skillName}/topics/generate-and-save")
    @Operation(summary = "Generate and save topics for a technical skill", description = "Uses Gemini to generate topics for a given technical skill and saves them to the SkillTopic table. Excludes interpersonal/soft skills.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"skill_name\": \"Java\",\n" +
                        "  \"topics_added\": 8,\n" +
                        "  \"topics\": [\n" +
                        "    {\"topic\": \"Collections\", \"description\": \"Data structures in Java\"}\n" +
                        "  ]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Skill not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForSkill(@PathVariable String skillName) {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveTopicsForSkill(skillName);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Skill not found for topic generation: {}", skillName);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating topics for skill: {}", skillName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/topics/generate-and-save-all")
    @Operation(
        summary = "Generate and save topics for all technical skills",
        description = "Uses Gemini to generate topics for all enabled technical skills and saves them to the SkillTopic table. Excludes interpersonal/soft skills."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics generated and saved for all skills successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_skills\": 15,\n" +
                        "  \"results\": [\n" +
                        "    {\"skill_name\": \"Java\", \"topics_added\": 8, \"message\": \"Topics generated and saved successfully\"},\n" +
                        "    {\"skill_name\": \"Python\", \"topics_added\": 10, \"message\": \"Topics generated and saved successfully\"}\n" +
                        "  ],\n" +
                        "  \"message\": \"Topics generated and saved for all skills\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForAllSkills() {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveTopicsForAllSkills();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating topics for all skills", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/{skillName}/topics/{topicName}/questions/generate-and-save")
    @Operation(summary = "Generate and save interview questions for a skill and topic", description = "Uses Gemini to generate summary and detailed, HTML-rich interview questions for a given skill and topic, and saves them to the InterviewQuestion table. Coding questions get code in Java, Python, and C++.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"skill_name\": \"Java\",\n" +
                        "  \"topic_name\": \"Collections\",\n" +
                        "  \"questions_added\": 10,\n" +
                        "  \"questions\": [\n" +
                        "    {\"question\": \"Explain Java Collections Framework\", \"answer\": \"<div>...</div>\", \"coding\": true, \"javaCode\": \"...\", \"pythonCode\": \"...\", \"cppCode\": \"...\"}\n" +
                        "  ]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Skill or topic not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForSkillAndTopic(
            @PathVariable String skillName,
            @PathVariable String topicName,
            @RequestParam(defaultValue = "10") int numQuestions) {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveQuestionsForSkillAndTopic(skillName, topicName, numQuestions);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Skill or topic not found for question generation: {} - {}", skillName, topicName);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating questions for skill and topic: {} - {}", skillName, topicName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/{skillName}/topics/questions/generate-and-save")
    @Operation(summary = "Generate and save interview questions for all topics of a skill", description = "Loops through all topics for a skill and generates (and saves) questions for each topic using Gemini. Uses the same logic as the single-topic endpoint.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions generated and saved for all topics successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"skill_name\": \"Java\",\n" +
                        "  \"results\": [\n" +
                        "    {\"topic_name\": \"Collections\", \"questions_added\": 10, \"questions\": [...]},\n" +
                        "    {\"topic_name\": \"Multithreading\", \"questions_added\": 8, \"questions\": [...]}\n" +
                        "  ],\n" +
                        "  \"message\": \"Questions generated and (optionally) saved for all topics\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Skill not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForAllTopicsOfSkill(
            @PathVariable String skillName,
            @RequestParam(defaultValue = "10") int numQuestions) {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveQuestionsForAllTopicsOfSkill(skillName, numQuestions);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Skill not found for all topics generation: {}", skillName);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            log.error("Error generating questions for all topics of skill: {}", skillName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/skills/topics/questions/generate-and-save-all")
    @Operation(
        summary = "Generate and save interview questions for all skills and topics",
        description = "Loops through all enabled skills and all their topics, generates (and saves) questions for each topic using Gemini. Uses the same logic as the single-skill endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions generated and saved for all skills and topics successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_skills\": 15,\n" +
                        "  \"results\": [\n" +
                        "    {\"skill_name\": \"Java\", \"topics_processed\": 8, \"total_questions_added\": 80, \"message\": \"Questions generated and saved for all topics\"},\n" +
                        "    {\"skill_name\": \"Python\", \"topics_processed\": 10, \"total_questions_added\": 100, \"message\": \"Questions generated and saved for all topics\"}\n" +
                        "  ],\n" +
                        "  \"message\": \"Questions generated and saved for all skills and topics\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateAndSaveQuestionsForAllSkillsAndTopics(@RequestParam(defaultValue = "10") int numQuestions) {
        try {
            Map<String, Object> result = aiJobService.generateAndSaveQuestionsForAllSkillsAndTopics(numQuestions);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating questions for all skills and topics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/designations/generate-similar")
    @Operation(
        summary = "Generate similar designations for all unprocessed designations",
        description = "Loops through all designations that haven't been processed for similar designation generation, uses AI to find similar designations that a person can switch to, creates new designations if they don't exist, and stores the results in the designation table."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Similar designations generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_designations_processed\": 50,\n" +
                        "  \"total_similar_designations_found\": 400,\n" +
                        "  \"total_new_designations_created\": 25,\n" +
                        "  \"total_errors\": 2,\n" +
                        "  \"designation_results\": [\n" +
                        "    {\n" +
                        "      \"designationId\": 1,\n" +
                        "      \"designationName\": \"Software Engineer\",\n" +
                        "      \"success\": true,\n" +
                        "      \"similarDesignationsFound\": 8,\n" +
                        "      \"newDesignationsCreated\": 2,\n" +
                        "      \"similarDesignationNames\": [\"Full Stack Engineer\", \"Backend Engineer\", \"DevOps Engineer\"],\n" +
                        "      \"newDesignationNames\": [\"Platform Engineer\", \"Cloud Engineer\"],\n" +
                        "      \"existingDesignationNames\": [\"Full Stack Engineer\", \"Backend Engineer\", \"DevOps Engineer\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Similar designation generation completed\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateSimilarDesignationsForAll() {
        try {
            Map<String, Object> result = aiJobService.generateSimilarDesignationsForAll();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating similar designations", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating similar designations: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/companies/generate-similar")
    @Operation(
        summary = "Generate similar companies for all unprocessed companies",
        description = "Loops through all companies that haven't been processed for similar company generation and have industry info, uses AI to find similar companies that a person can switch to based on industry, company size, business model, etc., creates new companies if they don't exist, and stores the results in the company table."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Similar companies generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total_companies_processed\": 50,\n" +
                        "  \"total_similar_companies_found\": 400,\n" +
                        "  \"total_new_companies_created\": 25,\n" +
                        "  \"total_errors\": 2,\n" +
                        "  \"company_results\": [\n" +
                        "    {\n" +
                        "      \"companyId\": 1,\n" +
                        "      \"companyName\": \"Google\",\n" +
                        "      \"success\": true,\n" +
                        "      \"similarCompaniesFound\": 8,\n" +
                        "      \"newCompaniesCreated\": 2,\n" +
                        "      \"similarCompanyNames\": [\"Microsoft\", \"Amazon\", \"Apple\"],\n" +
                        "      \"newCompanyNames\": [\"OpenAI\", \"Anthropic\"],\n" +
                        "      \"existingCompanyNames\": [\"Microsoft\", \"Amazon\", \"Apple\"],\n" +
                        "      \"industry\": \"Information Technology & Services\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Similar company generation completed\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateSimilarCompaniesForAll() {
        try {
            log.info("Starting similar company generation for all companies");
            Map<String, Object> result = aiJobService.generateSimilarCompaniesForAll();
            log.info("Similar company generation completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating similar companies for all", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate similar companies: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================
    // URL CONTENT MANAGEMENT ENDPOINTS
    // ============================================================================

    @GetMapping("/url-content")
    @Operation(summary = "Get all URL content", description = "Retrieves all URL content from the database")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "URL content retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"url\": \"https://careers.google.com/jobs/123\",\n" +
                        "    \"extractedText\": \"Job description content...\",\n" +
                        "    \"extractionStatus\": \"SUCCESS\",\n" +
                        "    \"extractedAt\": \"2025-01-15T10:30:00\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> getAllUrlContent() {
        try {
            List<Map<String, Object>> urlContents = AIJobFetchingService.getAllUrlContents().stream()
                .map(content -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", content.getId());
                    map.put("url", content.getUrl());
                    map.put("extractedText", content.getExtractedText());
                    map.put("extractionStatus", content.getExtractionStatus());
                    map.put("errorMessage", content.getErrorMessage());
                    map.put("extractedAt", content.getExtractedAt());
                    map.put("createdAt", content.getCreatedAt());
                    map.put("updatedAt", content.getUpdatedAt());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(urlContents);
        } catch (Exception e) {
            log.error("Error retrieving URL content", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error retrieving URL content: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/url-content/{url}")
    @Operation(summary = "Get URL content by URL", description = "Retrieves URL content for a specific URL")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "URL content retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"url\": \"https://careers.google.com/jobs/123\",\n" +
                        "  \"extractedText\": \"Job description content...\",\n" +
                        "  \"extractionStatus\": \"SUCCESS\",\n" +
                        "  \"extractedAt\": \"2025-01-15T10:30:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "URL content not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> getUrlContent(@PathVariable String url) {
        try {
            java.util.Optional<com.tymbl.common.entity.UrlContent> urlContent = AIJobFetchingService.getUrlContent(url);
            
            if (urlContent.isPresent()) {
                com.tymbl.common.entity.UrlContent content = urlContent.get();
                Map<String, Object> response = new HashMap<>();
                response.put("id", content.getId());
                response.put("url", content.getUrl());
                response.put("extractedText", content.getExtractedText());
                response.put("extractionStatus", content.getExtractionStatus());
                response.put("errorMessage", content.getErrorMessage());
                response.put("extractedAt", content.getExtractedAt());
                response.put("createdAt", content.getCreatedAt());
                response.put("updatedAt", content.getUpdatedAt());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "URL content not found for: " + url);
                response.put("status", "NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            log.error("Error retrieving URL content for: {}", url, e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error retrieving URL content: " + e.getMessage());
            response.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ============================================================================
    // CITY GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/cities/generate-for-countries")
    @Operation(
        summary = "Generate cities for all unprocessed countries using GenAI",
        description = "Uses Gemini AI to generate major cities for countries that haven't been processed yet. Only processes countries where cities_processed = false. Generates 15-25 major cities per country focusing on business, technology, and employment opportunities. Each country is processed in its own transaction for data persistence."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cities generated and saved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"totalCountries\": 10,\n" +
                        "  \"totalCitiesGenerated\": 200,\n" +
                        "  \"totalErrors\": 0,\n" +
                        "  \"countryResults\": [\n" +
                        "    {\n" +
                        "      \"countryId\": 1,\n" +
                        "      \"countryName\": \"United States\",\n" +
                        "      \"countryCode\": \"US\",\n" +
                        "      \"success\": true,\n" +
                        "      \"citiesGenerated\": 20,\n" +
                        "      \"citiesSaved\": 18,\n" +
                        "      \"cities\": [\"New York\", \"San Francisco\", \"Los Angeles\", \"Chicago\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"City generation completed for unprocessed countries\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateCitiesForCountries() {
        try {
            Map<String, Object> result = cityGenerationService.generateCitiesForAllCountries();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating cities for countries", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating cities for countries: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/cities/generate-for-country/{countryId}")
    @Operation(
        summary = "Generate cities for a specific country using GenAI",
        description = "Uses Gemini AI to generate major cities for a specific country. Only processes if the country hasn't been processed yet (cities_processed = false). Generates 15-25 major cities focusing on business, technology, and employment opportunities."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cities generated and saved successfully for the country",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"countryId\": 1,\n" +
                        "  \"countryName\": \"United States\",\n" +
                        "  \"citiesGenerated\": 20,\n" +
                        "  \"citiesSaved\": 18,\n" +
                        "  \"cities\": [\"New York\", \"San Francisco\", \"Los Angeles\", \"Chicago\"]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Country not found"),
        @ApiResponse(responseCode = "409", description = "Country already processed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateCitiesForCountry(@PathVariable Long countryId) {
        try {
            Map<String, Object> result = cityGenerationService.generateCitiesForSingleCountry(countryId);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                String error = (String) result.get("error");
                if (error.contains("Country not found")) {
                    return ResponseEntity.notFound().build();
                } else if (error.contains("already been processed")) {
                    return ResponseEntity.status(409).body(result);
                } else {
                    return ResponseEntity.internalServerError().body(result);
                }
            }
        } catch (Exception e) {
            log.error("Error generating cities for country ID: {}", countryId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating cities for country: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/cities/reset-processed-flag")
    @Operation(
        summary = "Reset cities processed flag for all countries",
        description = "Resets the cities_processed flag to false for all countries, allowing reprocessing of city generation"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cities processed flag reset successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"message\": \"Cities processed flag reset successfully for all countries\",\n" +
                        "  \"status\": \"SUCCESS\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetCitiesProcessedFlag() {
        try {
            cityGenerationService.resetCitiesProcessedFlag();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cities processed flag reset successfully for all countries");
            response.put("status", "SUCCESS");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting cities processed flag", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting cities processed flag: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // PROCESSED NAME GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/processed-names/generate-all")
    @Operation(
        summary = "Generate processed names for all unprocessed entities",
        description = "Generates processed names for countries, cities, companies, and designations that haven't been processed yet. Processed names remove special characters and common suffixes to ensure uniqueness and deduplication."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed names generated successfully for all entities",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Processed name generation completed\",\n" +
                        "  \"countries\": {\n" +
                        "    \"total\": 50,\n" +
                        "    \"processed\": 48,\n" +
                        "    \"errors\": 2,\n" +
                        "    \"success\": true\n" +
                        "  },\n" +
                        "  \"cities\": {\n" +
                        "    \"total\": 200,\n" +
                        "    \"processed\": 195,\n" +
                        "    \"errors\": 5,\n" +
                        "    \"success\": true\n" +
                        "  },\n" +
                        "  \"companies\": {\n" +
                        "    \"total\": 100,\n" +
                        "    \"processed\": 95,\n" +
                        "    \"errors\": 5,\n" +
                        "    \"success\": true\n" +
                        "  },\n" +
                        "  \"designations\": {\n" +
                        "    \"total\": 80,\n" +
                        "    \"processed\": 78,\n" +
                        "    \"errors\": 2,\n" +
                        "    \"success\": true\n" +
                        "  }\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateProcessedNamesForAllEntities() {
        try {
            log.info("Starting processed name generation for all entities");
            Map<String, Object> result = processedNameService.generateProcessedNamesForAllEntities();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating processed names for all entities", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating processed names: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-countries")
    @Operation(
        summary = "Generate processed names for unprocessed countries",
        description = "Generates processed names for countries that haven't been processed yet. Processed names remove special characters and common suffixes to ensure uniqueness."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed names generated successfully for countries",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total\": 50,\n" +
                        "  \"processed\": 48,\n" +
                        "  \"errors\": 2,\n" +
                        "  \"success\": true\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateProcessedNamesForCountries() {
        try {
            log.info("Starting processed name generation for countries");
            Map<String, Object> result = processedNameService.generateProcessedNamesForCountries();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating processed names for countries", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating processed names for countries: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-cities")
    @Operation(
        summary = "Generate processed names for unprocessed cities",
        description = "Generates processed names for cities that haven't been processed yet. Processed names remove special characters and common suffixes to ensure uniqueness."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed names generated successfully for cities",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total\": 200,\n" +
                        "  \"processed\": 195,\n" +
                        "  \"errors\": 5,\n" +
                        "  \"success\": true\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateProcessedNamesForCities() {
        try {
            log.info("Starting processed name generation for cities");
            Map<String, Object> result = processedNameService.generateProcessedNamesForCities();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating processed names for cities", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating processed names for cities: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-companies")
    @Operation(
        summary = "Generate processed names for unprocessed companies",
        description = "Generates processed names for companies that haven't been processed yet. Processed names remove special characters and common suffixes like .com, pvt ltd, etc. to ensure uniqueness."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed names generated successfully for companies",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total\": 100,\n" +
                        "  \"processed\": 95,\n" +
                        "  \"errors\": 5,\n" +
                        "  \"success\": true\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateProcessedNamesForCompanies() {
        try {
            log.info("Starting processed name generation for companies");
            Map<String, Object> result = processedNameService.generateProcessedNamesForCompanies();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating processed names for companies", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating processed names for companies: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-designations")
    @Operation(
        summary = "Generate processed names for unprocessed designations",
        description = "Generates processed names for designations that haven't been processed yet. Processed names remove special characters and common suffixes to ensure uniqueness."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed names generated successfully for designations",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"total\": 80,\n" +
                        "  \"processed\": 78,\n" +
                        "  \"errors\": 2,\n" +
                        "  \"success\": true\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateProcessedNamesForDesignations() {
        try {
            log.info("Starting processed name generation for designations");
            Map<String, Object> result = processedNameService.generateProcessedNamesForDesignations();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating processed names for designations", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating processed names for designations: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/processed-names/reset")
    @Operation(
        summary = "Reset processed name generation flag for all entities",
        description = "Resets the processed_name_generated flag to false for all countries, cities, companies, and designations, allowing reprocessing of processed name generation"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Processed name generation flags reset successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Processed name generation flags reset successfully for all entities\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetProcessedNameGenerationFlag() {
        try {
            log.info("Resetting processed name generation flags for all entities");
            Map<String, Object> result = processedNameService.resetProcessedNameGenerationFlag();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error resetting processed name generation flags", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting processed name generation flags: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // DESIGNATION GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/designations/generate-for-departments")
    @Operation(
        summary = "Generate designations for all departments using GenAI",
        description = "Uses Gemini AI to generate comprehensive job designations for all departments in the database. Generates 20-35 valid designations per department covering entry-level, mid-level, senior-level, and executive positions across different specializations and career paths."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Designations generated and saved successfully for all departments",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"totalDepartments\": 35,\n" +
                        "  \"totalDesignationsGenerated\": 875,\n" +
                        "  \"totalErrors\": 0,\n" +
                        "  \"departmentResults\": [\n" +
                        "    {\n" +
                        "      \"departmentId\": 1,\n" +
                        "      \"departmentName\": \"Engineering\",\n" +
                        "      \"departmentDescription\": \"Software development and engineering teams\",\n" +
                        "      \"success\": true,\n" +
                        "      \"designationsGenerated\": 25,\n" +
                        "      \"designationsSaved\": 23,\n" +
                        "      \"designations\": [\"Software Engineer\", \"Senior Software Engineer\", \"Lead Engineer\", \"Engineering Manager\", \"CTO\"]\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"departmentId\": 2,\n" +
                        "      \"departmentName\": \"Product\",\n" +
                        "      \"departmentDescription\": \"Product management and product strategy\",\n" +
                        "      \"success\": true,\n" +
                        "      \"designationsGenerated\": 28,\n" +
                        "      \"designationsSaved\": 26,\n" +
                        "      \"designations\": [\"Product Manager\", \"Senior Product Manager\", \"Product Director\", \"VP of Product\", \"CPO\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Designation generation completed for all departments\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateDesignationsForDepartments() {
        try {
            log.info("Starting designation generation for all departments");
            Map<String, Object> result = designationGenerationService.generateDesignationsForAllDepartments();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating designations for departments", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating designations for departments: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/designations/generate-for-department/{departmentId}")
    @Operation(
        summary = "Generate designations for a specific department using GenAI",
        description = "Uses Gemini AI to generate comprehensive job designations for a specific department. Generates 20-35 valid designations covering entry-level, mid-level, senior-level, and executive positions across different specializations."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Designations generated and saved successfully for the department",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"departmentId\": 1,\n" +
                        "  \"departmentName\": \"Engineering\",\n" +
                        "  \"designationsGenerated\": 25,\n" +
                        "  \"designationsSaved\": 23,\n" +
                        "  \"designations\": [\"Software Engineer\", \"Senior Software Engineer\", \"Lead Engineer\", \"Engineering Manager\", \"CTO\"]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateDesignationsForDepartment(@PathVariable Long departmentId) {
        try {
            log.info("Starting designation generation for department ID: {}", departmentId);
            Map<String, Object> result = designationGenerationService.generateDesignationsForSingleDepartment(departmentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating designations for department ID: {}", departmentId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating designations for department: " + e.getMessage());
            errorResponse.put("departmentId", departmentId);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ============================================================================
    // COMPANY SHORTNAME GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/companies/shortnames/generate-for-all")
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

    @PostMapping("/companies/shortnames/generate-for-company/{companyName}")
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

    // ============================================================================
    // SECONDARY INDUSTRY MAPPING ENDPOINTS
    // ============================================================================

    @PostMapping("/secondary-industries/map-all")
    @Operation(
        summary = "Process and map all secondary industries using GenAI",
        description = "Extracts all unique secondary industries from companies table, uses Gemini AI to create standardized mappings, and stores them in a separate mapping table. Groups similar variations (e.g., 'Fortune 500', 'Fortune500', 'Fortune 500 Top') under the same parent category."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Secondary industry mapping completed successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"totalIndustries\": 150,\n" +
                        "  \"totalProcessed\": 120,\n" +
                        "  \"totalErrors\": 5,\n" +
                        "  \"totalSkipped\": 25,\n" +
                        "  \"industryResults\": [\n" +
                        "    {\n" +
                        "      \"industryName\": \"Fortune 500\",\n" +
                        "      \"success\": true,\n" +
                        "      \"mappedName\": \"Fortune 500\",\n" +
                        "      \"mappedId\": 1\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"industryName\": \"Fortune500\",\n" +
                        "      \"success\": true,\n" +
                        "      \"mappedName\": \"Fortune 500\",\n" +
                        "      \"mappedId\": 1\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"industryName\": \"Tech\",\n" +
                        "      \"success\": true,\n" +
                        "      \"mappedName\": \"Technology\",\n" +
                        "      \"mappedId\": 2\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Secondary industry processing completed\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> processSecondaryIndustries() {
        try {
            log.info("Starting secondary industry mapping for all industries");
            Map<String, Object> result = secondaryIndustryMappingService.processSecondaryIndustries();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error processing secondary industries", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing secondary industries: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/secondary-industries/map-industry/{industryName}")
    @Operation(
        summary = "Process and map a specific secondary industry using GenAI",
        description = "Uses Gemini AI to create a standardized mapping for a specific secondary industry. Groups similar variations under the same parent category."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Secondary industry mapping completed successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"industryName\": \"Fortune 500\",\n" +
                        "  \"mappedName\": \"Fortune 500\",\n" +
                        "  \"mappedId\": 1\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "409", description = "Industry already processed"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> processSingleSecondaryIndustry(@PathVariable String industryName) {
        try {
            log.info("Starting secondary industry mapping for industry: {}", industryName);
            Map<String, Object> result = secondaryIndustryMappingService.processSingleSecondaryIndustry(industryName);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else if (result.containsKey("existingMapping")) {
                return ResponseEntity.status(409).body(result);
            } else {
                return ResponseEntity.internalServerError().body(result);
            }
        } catch (Exception e) {
            log.error("Error processing secondary industry: {}", industryName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing secondary industry: " + e.getMessage());
            errorResponse.put("industryName", industryName);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/secondary-industries/reset-processed-flag")
    @Operation(
        summary = "Reset processed flag for all secondary industry mappings",
        description = "Resets the processed flag for all secondary industry mappings to allow reprocessing"
    )
    public ResponseEntity<Map<String, Object>> resetSecondaryIndustryProcessedFlag() {
        log.info("Resetting secondary industry processed flag");
        Map<String, Object> result = secondaryIndustryMappingService.resetProcessedFlag();
        return ResponseEntity.ok(result);
    }

    /**
     * Process all companies for cleanup (remove junk/product companies)
     */
    @PostMapping("/companies/cleanup-all")
    @Operation(summary = "Process all companies for cleanup",
            description = "Processes all unprocessed companies to identify and clean up junk/product entries using GenAI")
    public ResponseEntity<Map<String, Object>> processAllCompaniesForCleanup() {
        log.info("Starting company cleanup process for all companies");
        Map<String, Object> result = companyCleanupService.processAllCompanies();
        return ResponseEntity.ok(result);
    }

    /**
     * Process a specific company for cleanup
     */
    @PostMapping("/companies/cleanup/{companyName}")
    @Operation(summary = "Process specific company for cleanup",
            description = "Processes a specific company to identify if it's a junk/product entry and clean it up using GenAI")
    public ResponseEntity<Map<String, Object>> processCompanyForCleanup(
            @PathVariable String companyName) {
        log.info("Processing company for cleanup: {}", companyName);
        Map<String, Object> result = companyCleanupService.processCompanyByName(companyName);
        return ResponseEntity.ok(result);
    }

    /**
     * Reset cleanup processed flag for all companies
     */
    @PostMapping("/companies/cleanup/reset-processed-flag")
    @Operation(summary = "Reset cleanup processed flag for all companies",
            description = "Resets the cleanup processed flag for all companies to allow reprocessing")
    public ResponseEntity<Map<String, Object>> resetCompanyCleanupProcessedFlag() {
        log.info("Resetting company cleanup processed flag");
        Map<String, Object> result = companyCleanupService.resetCleanupProcessedFlag();
        return ResponseEntity.ok(result);
    }

    /**
     * Get all junk-marked companies for manual review
     */
    @GetMapping("/companies/cleanup/junk-marked")
    @Operation(summary = "Get all junk-marked companies for review",
            description = "Retrieves all companies marked as junk for manual review before deletion")
    public ResponseEntity<Map<String, Object>> getJunkMarkedCompanies() {
        log.info("Retrieving junk-marked companies for review");
        Map<String, Object> result = companyCleanupService.getJunkMarkedCompanies();
        return ResponseEntity.ok(result);
    }

    /**
     * Clear junk flag for a specific company
     */
    @PostMapping("/companies/cleanup/clear-junk-flag/{companyId}")
    @Operation(summary = "Clear junk flag for a company",
            description = "Removes the junk flag from a specific company, effectively undoing the junk marking")
    public ResponseEntity<Map<String, Object>> clearJunkFlag(@PathVariable Long companyId) {
        log.info("Clearing junk flag for company ID: {}", companyId);
        Map<String, Object> result = companyCleanupService.clearJunkFlag(companyId);
        return ResponseEntity.ok(result);
    }
} 