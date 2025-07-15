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
import com.tymbl.common.entity.SkillTopic;
import com.tymbl.common.repository.SkillTopicRepository;
import com.tymbl.interview.entity.InterviewQuestion;
import com.tymbl.interview.repository.InterviewQuestionRepository;
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
import java.util.concurrent.ExecutorService;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.Optional;
import com.tymbl.common.repository.IndustryRepository;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final SkillTopicRepository skillTopicRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final CompanyRepository companyRepository;
    private final IndustryRepository industryRepository;

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
            log.info("Manual job crawling triggered for company ID: {}", companyId);
            // Get company to verify it exists and get name for response
            CompanyResponse companyResponse = companyService.getCompanyById(companyId);
            
            log.info("Company found for job crawling: {} (ID: {})", companyResponse.getName(), companyId);
            
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
        List<Map<String, Object>> industryResults = new ArrayList<>();
        int totalGenerated = 0;
        int totalSkipped = 0;
        List<String> globalErrors = new ArrayList<>();
        try {
            List<com.tymbl.common.entity.Industry> industries = industryRepository.findAll();
            for (com.tymbl.common.entity.Industry industry : industries) {
                String industryName = industry.getName();
                Long industryId = industry.getId();
                List<Company> existingCompanies = companyRepository.findByPrimaryIndustryId(industryId);
                Set<String> existingNames = existingCompanies.stream()
                    .map(c -> c.getName().trim().toLowerCase())
                    .collect(Collectors.toSet());
                Set<String> existingWebsites = existingCompanies.stream()
                    .map(c -> normalizeWebsite(c.getWebsite()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
                List<Map<String, String>> generatedCompanies = geminiService.generateCompanyListForIndustry(industryName, new ArrayList<>(existingNames));
                int generated = 0, skipped = 0;
                List<String> errors = new ArrayList<>();
                for (Map<String, String> companyMap : generatedCompanies) {
                    String name = companyMap.getOrDefault("name", "").trim();
                    String website = normalizeWebsite(companyMap.get("website"));
                    if (name.isEmpty() || website == null || website.isEmpty()) {
                        skipped++;
                        continue;
                    }
                    if (existingNames.contains(name.toLowerCase()) || existingWebsites.contains(website)) {
                        skipped++;
                        continue;
                    }
                    // Save new company
                    Company company = new Company();
                    company.setName(name);
                    company.setWebsite(website);
                    company.setPrimaryIndustryId(industryId);
                    try {
                        companyRepository.save(company);
                        existingNames.add(name.toLowerCase());
                        existingWebsites.add(website);
                        generated++;
                    } catch (Exception e) {
                        errors.add("Failed to save: " + name + " (" + website + ") - " + e.getMessage());
                        skipped++;
                    }
                }
                totalSkipped += skipped;
                Map<String, Object> result = new HashMap<>();
                result.put("industry", industryName);
                result.put("generated", generated);
                result.put("skipped", skipped);
                result.put("errors", errors);
                industryResults.add(result);
            }
        } catch (Exception e) {
            globalErrors.add("Fatal error: " + e.getMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("industry_results", industryResults);
        response.put("total_generated", totalGenerated);
        response.put("total_skipped", totalSkipped);
        response.put("errors", globalErrors);
        response.put("message", "Company generation completed");
        return ResponseEntity.ok(response);
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
            log.info("Industry detection completed. Total companies processed: {}", results.size());
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
        log.info("[BG] Starting background comprehensive interview question generation");
        new Thread(() -> {
            try {
                log.info("[BG] Starting background comprehensive interview question generation");
                comprehensiveQuestionService.generateQuestionsForAllSkills();
                log.info("[BG] Comprehensive interview question generation completed successfully");
            } catch (Exception e) {
                log.error("[BG] Error in background comprehensive interview question generation", e);
            }
        }).start();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comprehensive interview question generation started in background. Check logs or status endpoint for results.");
        return ResponseEntity.accepted().body(response);
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
                log.error("Error during comprehensive interview question generation for skill: {}", skillName, result);
                return ResponseEntity.badRequest().body(result);
            }
            
            log.info("Comprehensive interview questions generated successfully for skill: {}", skillName);
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
            int newSkillsAdded = 0;
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
                    newSkillsAdded++;
                }
            }
            log.info("Skills generated and saved successfully. New skills added: {}, total generated: {}", newSkillsAdded, skills.size());
            Map<String, Object> result = new HashMap<>();
            result.put("new_skills_added", newSkillsAdded);
            result.put("total_skills_generated", skills.size());
            result.put("message", "Skills generated and saved successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving tech skills", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
            log.info("Generating and saving topics for skill: {}", skillName);
            Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim()).orElse(null);
            if (skill == null) {
                log.error("Skill not found for topic generation: {}", skillName);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Skill not found: " + skillName);
                return ResponseEntity.status(404).body(error);
            }
            List<Map<String, Object>> topics = geminiService.generateTopicsForSkill(skill.getName());
            List<SkillTopic> savedTopics = new ArrayList<>();
            for (Map<String, Object> topicData : topics) {
                String topic = (String) topicData.get("topic");
                if (topic == null || topic.trim().isEmpty()) continue;
                boolean exists = false;
                for (SkillTopic st : savedTopics) {
                    if (st.getTopic().equalsIgnoreCase(topic.trim())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    SkillTopic skillTopic = new SkillTopic();
                    skillTopic.setSkill(skill);
                    skillTopic.setTopic(topic.trim());
                    skillTopic.setDescription((String) topicData.get("description"));
                    savedTopics.add(skillTopic);
                }
            }
            skillTopicRepository.deleteBySkill(skill);
            skillTopicRepository.saveAll(savedTopics);
            log.info("Topics generated and saved successfully for skill: {}. Topics added: {}", skillName, savedTopics.size());
            Map<String, Object> result = new HashMap<>();
            result.put("skill_name", skill.getName());
            result.put("topics_added", savedTopics.size());
            result.put("message", "Topics generated and saved successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving topics for skill", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
        log.info("Generating and saving topics for all enabled skills");
        List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
        List<Map<String, Object>> results = new ArrayList<>();
        int totalSkills = 0;
        for (Skill skill : skills) {
            totalSkills++;
            ResponseEntity<Map<String, Object>> response = generateAndSaveTopicsForSkill(skill.getName());
            Map<String, Object> result = response.getBody();
            if (result != null) {
                results.add(result);
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_skills", totalSkills);
        summary.put("results", results);
        summary.put("message", "Topics generated and saved for all skills");
        return ResponseEntity.ok(summary);
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
            log.info("Generating and saving questions for skill: {}, topic: {}", skillName, topicName);
            Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim()).orElse(null);
            if (skill == null) {
                log.error("Skill not found for question generation: {}", skillName);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Skill not found: " + skillName);
                return ResponseEntity.status(404).body(error);
            }
            SkillTopic skillTopic = skillTopicRepository.findBySkill(skill).stream()
                .filter(t -> t.getTopic().equalsIgnoreCase(topicName.trim()))
                .findFirst().orElse(null);
            if (skillTopic == null) {
                log.error("Topic not found for skill: {}", skillName);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Topic not found for skill: " + topicName);
                return ResponseEntity.status(404).body(error);
            }
            Map<String, Object> topicSummary = generateQuestionsForSkillAndTopicInternal(skill, skillTopic, numQuestions);
            log.info("Questions generated and (optionally) saved successfully for skill: {}, topic: {}", skillName, topicName);
            Map<String, Object> result = new HashMap<>();
            result.put("skill_name", skill.getName());
            result.put("topic_name", skillTopic.getTopic());
            result.put("questions_added", topicSummary.get("questions_added"));
            result.put("message", "Questions generated and (optionally) saved successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving questions for skill and topic", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
            log.info("Generating and saving questions for all topics of skill: {}", skillName);
            Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim()).orElse(null);
            if (skill == null) {
                log.error("Skill not found for all topics generation: {}", skillName);
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Skill not found: " + skillName);
                return ResponseEntity.status(404).body(error);
            }
            List<SkillTopic> topics = skillTopicRepository.findBySkill(skill);
            List<Map<String, Object>> results = new ArrayList<>();
            int totalQuestions = 0;
            // Multithreading: process up to 10 topics in parallel
            ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(10);
            List<java.util.concurrent.Future<Map<String, Object>>> futures = new ArrayList<>();
            for (SkillTopic topic : topics) {
                futures.add(executor.submit(() -> generateQuestionsForSkillAndTopicInternal(skill, topic, numQuestions)));
            }
            for (java.util.concurrent.Future<Map<String, Object>> future : futures) {
                try {
                    Map<String, Object> topicSummary = future.get();
                    results.add(topicSummary);
                    totalQuestions += (int) topicSummary.getOrDefault("questions_added", 0);
                } catch (Exception e) {
                    log.error("Error processing topic in parallel", e);
                }
            }
            executor.shutdown();
            log.info("Questions generated and (optionally) saved for all topics of skill: {}. Total questions added: {}", skillName, totalQuestions);
            Map<String, Object> result = new HashMap<>();
            result.put("skill_name", skill.getName());
            result.put("topics_processed", results.size());
            result.put("total_questions_added", totalQuestions);
            result.put("topic_summaries", results);
            result.put("message", "Questions generated and (optionally) saved for all topics");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving questions for all topics of skill", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
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
        log.info("Generating and saving questions for all enabled skills and their topics");
        List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
        List<Map<String, Object>> results = new ArrayList<>();
        int totalSkills = 0;
        for (Skill skill : skills) {
            totalSkills++;
            ResponseEntity<Map<String, Object>> response = generateAndSaveQuestionsForAllTopicsOfSkill(skill.getName(), numQuestions);
            Map<String, Object> result = response.getBody();
            if (result != null) {
                result.put("skill_name", skill.getName());
                results.add(result);
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_skills", totalSkills);
        summary.put("results", results);
        summary.put("message", "Questions generated and saved for all skills and topics");
        return ResponseEntity.ok(summary);
    }

    // Internal helper to reuse the logic for a single topic
    private Map<String, Object> generateQuestionsForSkillAndTopicInternal(Skill skill, SkillTopic skillTopic, int numQuestions) {
        Long existingCount = interviewQuestionRepository.countBySkillIdAndTopicId(skill.getId(), skillTopic.getId());
        if (existingCount != null && existingCount >= 20) {
            log.info("[AI] Skipping question generation for skill='{}', topic='{}' as {} questions already exist", skill.getName(), skillTopic.getTopic(), existingCount);
            Map<String, Object> topicSummary = new HashMap<>();
            topicSummary.put("topic_name", skillTopic.getTopic());
            topicSummary.put("questions_added", 0);
            topicSummary.put("skipped", true);
            topicSummary.put("message", "Skipped: already has " + existingCount + " questions");
            return topicSummary;
        }
        log.info("[AI] Starting question generation for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
        List<Map<String, Object>> summaryQuestions = geminiService.generateQuestionsForSkillAndTopic(skill.getName(), skillTopic.getTopic(), numQuestions);
        log.info("[AI] Generated {} summary questions for skill='{}', topic='{}'", summaryQuestions.size(), skill.getName(), skillTopic.getTopic());
        int questionsAdded = 0;
        for (Map<String, Object> q : summaryQuestions) {
            String questionText = (String) q.get("question");
            if (questionText == null || questionText.trim().isEmpty()) {
                log.info("[AI] Skipping empty question for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
                continue;
            }
            log.info("[AI] Generating detailed content for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
            List<Map<String, Object>> detailedContentList = geminiService.generateDetailedQuestionContent(skill.getName(), questionText);
            Map<String, Object> detailedContent = detailedContentList.isEmpty() ? new HashMap<>() : detailedContentList.get(0);
            String answer = (String) detailedContent.getOrDefault("detailed_answer", "");
            String htmlContent = (String) detailedContent.getOrDefault("html_content", answer);
            String codeExamples = (String) detailedContent.getOrDefault("code_examples", "");
            boolean isCoding = false;
            String questionType = (String) q.get("question_type");
            String tags = (String) q.get("tags");
            if ((questionType != null && questionType.toLowerCase().contains("coding")) ||
                (tags != null && tags.toLowerCase().contains("coding")) ||
                (questionText.toLowerCase().contains("code") || questionText.toLowerCase().contains("implement") || questionText.toLowerCase().contains("write a function"))) {
                isCoding = true;
            }
            String javaCode = null, pythonCode = null, cppCode = null;
            if (isCoding && skill.getName().equalsIgnoreCase("dsa")) {
                log.info("[AI] Detected coding question. Generating code for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
                javaCode = generateCodeWithGemini(skill.getName(), questionText, "Java");
                pythonCode = generateCodeWithGemini(skill.getName(), questionText, "Python");
                cppCode = generateCodeWithGemini(skill.getName(), questionText, "C++");
            }
            log.info("[AI] Going to save question for for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
            InterviewQuestion iq = InterviewQuestion.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .topicId(skillTopic.getId())
                .topicName(skillTopic.getTopic())
                .question(questionText)
                .answer(answer)
                .difficultyLevel((String) q.get("difficulty_level"))
                .questionType(questionType)
                .tags(tags)
                .htmlContent(htmlContent)
                .codeExamples(codeExamples)
                .javaCode(javaCode)
                .pythonCode(pythonCode)
                .cppCode(cppCode)
                .coding(isCoding)
                .build();
            interviewQuestionRepository.save(iq); // Save one by one to avoid OOM
            questionsAdded++;
            log.info("[AI] Finished processing and saved question for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
        }
        log.info("[AI] Finished generating questions for skill='{}', topic='{}'. Total questions added: {}", skill.getName(), skillTopic.getTopic(), questionsAdded);
        Map<String, Object> topicSummary = new HashMap<>();
        topicSummary.put("topic_name", skillTopic.getTopic());
        topicSummary.put("questions_added", questionsAdded);
        return topicSummary;
    }

    // Helper for code generation
    private String generateCodeWithGemini(String skillName, String questionText, String language) {
        try {
            String prompt = "Write a working, well-commented solution in " + language + " for the following coding interview question. Only return the code, no explanation.\n\n" +
                    "SKILL: " + skillName + "\nQUESTION: " + questionText;
            List<Map<String, Object>> codeResp = geminiService.generateDetailedQuestionContent(skillName, prompt);
            if (!codeResp.isEmpty()) {
                String code = (String) codeResp.get(0).getOrDefault("detailed_answer", "");
                if (code.isEmpty()) code = (String) codeResp.get(0).getOrDefault("html_content", "");
                return code;
            }
        } catch (Exception e) {
            log.warn("Error generating code for {} in {}: {}", questionText, language, e.getMessage());
        }
        return null;
    }

    // Add this utility method for website normalization
    private String normalizeWebsite(String website) {
        if (website == null) return null;
        website = website.trim();
        if (website.endsWith("/")) {
            website = website.substring(0, website.length() - 1);
        }
        return website.toLowerCase();
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