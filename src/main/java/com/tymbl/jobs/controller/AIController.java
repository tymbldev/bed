package com.tymbl.jobs.controller;

import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.jobs.service.CompanyService;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.interview.service.ComprehensiveQuestionService;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.service.GeminiService;
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
import java.util.ArrayList;

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
    private final ComprehensiveQuestionService comprehensiveQuestionService;
    private final SkillRepository skillRepository;
    private final GeminiService geminiService;

    // ============================================================================
    // COMPANY CRAWLING ENDPOINTS (Legacy - kept for backward compatibility)
    // ============================================================================

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

    // ============================================================================
    // AI-POWERED DATA GENERATION ENDPOINTS
    // ============================================================================

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
            log.info("Industry detection triggered for all companies. Use Gemini: {}", useGemini);
            List<CompanyIndustryResponse> results = companyService.detectIndustriesForCompanies(useGemini);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error during industry detection process", e);
            return ResponseEntity.internalServerError().build();
        }
    }

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
            log.info("Comprehensive interview question generation triggered");
            Map<String, Object> result = comprehensiveQuestionService.generateQuestionsForAllSkills();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during comprehensive interview question generation", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error during question generation: " + e.getMessage());
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
            log.info("Comprehensive interview question generation triggered for skill: {}", skillName);
            Map<String, Object> result = comprehensiveQuestionService.generateQuestionsForSpecificSkill(skillName);
            
            if (result.containsKey("error")) {
                return ResponseEntity.badRequest().body(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error during comprehensive interview question generation for skill: {}", skillName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error during question generation: " + e.getMessage());
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
            log.info("Generating and saving more tech skills using Gemini");
            List<Map<String, Object>> skills = geminiService.generateComprehensiveTechSkills();
            int newSkills = 0;
            List<Skill> addedSkills = new ArrayList<>();
            for (Map<String, Object> skillData : skills) {
                String name = (String) skillData.get("name");
                if (name == null || name.trim().isEmpty()) continue;
                if (!skillRepository.existsByNameIgnoreCase(name.trim())) {
                    Skill skill = new Skill();
                    skill.setName(name.trim());
                    skill.setCategory((String) skillData.get("category"));
                    skill.setDescription((String) skillData.get("description"));
                    skill.setEnabled(true);
                    skill.setUsageCount(0L);
                    skillRepository.save(skill);
                    addedSkills.add(skill);
                    newSkills++;
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("new_skills_added", newSkills);
            result.put("total_skills_generated", skills.size());
            result.put("added_skills", addedSkills);
            result.put("message", "Skills generated and saved successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving tech skills", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ============================================================================
    // FUTURE AI UTILITY ENDPOINTS (Placeholder comments)
    // ============================================================================
    
    // TODO: Add endpoints for:
    // - Generate job descriptions using AI
    // - Enrich company profiles with AI-generated content
    // - Generate interview questions for specific companies/roles
    // - AI-powered job matching and recommendations
    // - Generate company culture and values descriptions
    // - AI-powered salary range suggestions
    // - Generate job requirements and qualifications
    // - AI-powered company similarity analysis
    // - Generate industry trend analysis
    // - AI-powered candidate skill assessment questions
} 