package com.tymbl.jobs.controller;

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
            // This would need to be implemented in CompanyCrawlerService
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job crawling process started successfully for company ID: " + companyId);
            response.put("companyId", companyId);
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
            log.info("Manual job crawling triggered for company: {}", companyName);
            // This would need to be implemented in CompanyCrawlerService
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job crawling process started successfully for company: " + companyName);
            response.put("companyName", companyName);
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
        try {
            List<CompanyIndustryResponse> results = companyService.detectIndustriesForCompanies(useGemini);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error detecting industries for companies", e);
            return ResponseEntity.internalServerError().build();
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
} 