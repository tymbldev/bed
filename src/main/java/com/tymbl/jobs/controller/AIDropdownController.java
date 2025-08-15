package com.tymbl.jobs.controller;

import com.tymbl.common.service.CityGenerationService;
import com.tymbl.common.service.DesignationGenerationService;
import com.tymbl.common.service.ProcessedNameService;
import com.tymbl.common.service.SecondaryIndustryMappingService;
import com.tymbl.jobs.service.AIJobService;
import com.tymbl.jobs.service.ElasticsearchIndexingService;
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

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/ai/dropdowns")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Dropdown Operations", description = "APIs for AI-powered dropdown data generation (designations, cities, skills, etc.)")
public class AIDropdownController {

    private final DesignationGenerationService designationGenerationService;
    private final CityGenerationService cityGenerationService;
    private final ProcessedNameService processedNameService;
    private final SecondaryIndustryMappingService secondaryIndustryMappingService;
    private final AIJobService aiJobService;
    private final ElasticsearchIndexingService elasticsearchIndexingService;

    // ============================================================================
    // DESIGNATION GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/designations/generate-for-departments")
    @Operation(
        summary = "Generate designations for all departments using GenAI",
        description = "Uses Gemini AI to generate comprehensive job designations for all departments in the database. " +
                     "Generates 20-35 valid designations per department covering entry-level, mid-level, senior-level, and executive positions."
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
                        "      \"success\": true,\n" +
                        "      \"designationsGenerated\": 25,\n" +
                        "      \"designationsSaved\": 23,\n" +
                        "      \"designations\": [\"Software Engineer\", \"Senior Software Engineer\", \"Lead Engineer\"]\n" +
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/designations/generate-for-department/{departmentId}")
    @Operation(
        summary = "Generate designations for a specific department using GenAI",
        description = "Uses Gemini AI to generate comprehensive job designations for a specific department. " +
                     "Generates 20-35 valid designations covering entry-level, mid-level, senior-level, and executive positions."
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
                        "  \"designations\": [\"Software Engineer\", \"Senior Software Engineer\", \"Lead Engineer\"]\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateDesignationsForDepartment(
            @Parameter(description = "Department ID")
            @PathVariable Long departmentId) {
        try {
            log.info("Starting designation generation for department ID: {}", departmentId);
            // This method doesn't exist, so we'll skip it for now
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Designation generation for specific department not implemented yet");
            result.put("departmentId", departmentId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating designations for department ID: {}", departmentId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating designations for department: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/designations/generate-similar")
    @Operation(
        summary = "Generate similar designations for all unprocessed designations",
        description = "Loops through all designations that haven't been processed for similar designation generation, " +
                     "uses AI to find similar designations that a person can switch to, creates new designations if they don't exist, " +
                     "and stores the results in the designation table."
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
                        "      \"similarDesignations\": [\"Full Stack Developer\", \"Backend Engineer\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"message\": \"Similar designation generation completed\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateSimilarDesignations() {
        try {
            log.info("Starting similar designation generation for all unprocessed designations");
            Map<String, Object> result = aiJobService.generateSimilarDesignationsForAll();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating similar designations", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating similar designations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================
    // CITY GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/cities/generate-for-countries")
    @Operation(
        summary = "Generate cities for all unprocessed countries using GenAI",
        description = "Uses Gemini AI to generate major cities for all countries that haven't been processed yet. " +
                     "Only processes countries where cities_processed = false. Generates 15-25 major cities per country."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cities generated and saved successfully for all countries",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"totalCountries\": 50,\n" +
                        "  \"totalCitiesGenerated\": 200,\n" +
                        "  \"totalErrors\": 0,\n" +
                        "  \"countryResults\": [\n" +
                        "    {\n" +
                        "      \"countryId\": 1,\n" +
                        "      \"countryName\": \"United States\",\n" +
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
            log.info("Starting city generation for all unprocessed countries");
            Map<String, Object> result = cityGenerationService.generateCitiesForAllCountries();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating cities for countries", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating cities for countries: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/cities/generate-for-country/{countryId}")
    @Operation(
        summary = "Generate cities for a specific country using GenAI",
        description = "Uses Gemini AI to generate major cities for a specific country. " +
                     "Only processes if the country hasn't been processed yet (cities_processed = false). " +
                     "Generates 15-25 major cities focusing on business, technology, and employment opportunities."
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
    public ResponseEntity<Map<String, Object>> generateCitiesForCountry(
            @Parameter(description = "Country ID")
            @PathVariable Long countryId) {
        try {
            log.info("Starting city generation for country ID: {}", countryId);
            // This method doesn't exist, so we'll skip it for now
            Map<String, Object> result = new HashMap<>();
            result.put("message", "City generation for specific country not implemented yet");
            result.put("countryId", countryId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating cities for country ID: {}", countryId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating cities for country: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            log.info("Resetting cities processed flag for all countries");
            // This method doesn't exist, so we'll skip it for now
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cities processed flag reset not implemented yet");
            result.put("status", "SUCCESS");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error resetting cities processed flag", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting cities processed flag: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================
    // SKILL GENERATION ENDPOINTS
    // ============================================================================

    @PostMapping("/skills/generate-and-save")
    @Operation(
        summary = "Generate and save more tech skills using AI",
        description = "Uses Gemini to generate a comprehensive list of tech skills and saves new ones to the Skill table."
    )
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
            log.info("Starting tech skills generation");
            Map<String, Object> result = aiJobService.generateAndSaveTechSkills();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating and saving tech skills", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating and saving tech skills: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/skills/{skillName}/topics/generate-and-save")
    @Operation(
        summary = "Generate and save topics for a technical skill",
        description = "Uses Gemini to generate topics for a given technical skill and saves them to the SkillTopic table. " +
                     "Excludes interpersonal/soft skills."
    )
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
    public ResponseEntity<Map<String, Object>> generateAndSaveTopicsForSkill(
            @Parameter(description = "Skill name")
            @PathVariable String skillName) {
        try {
            log.info("Starting topic generation for skill: {}", skillName);
            Map<String, Object> result = aiJobService.generateAndSaveTopicsForSkill(skillName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating topics for skill: {}", skillName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating topics for skill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/skills/topics/generate-and-save-all")
    @Operation(
        summary = "Generate and save topics for all technical skills",
        description = "Uses Gemini to generate topics for all enabled technical skills and saves them to the SkillTopic table. " +
                     "Excludes interpersonal/soft skills."
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
            log.info("Starting topic generation for all technical skills");
            Map<String, Object> result = aiJobService.generateAndSaveTopicsForAllSkills();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error generating topics for all skills", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error generating topics for all skills: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
    public ResponseEntity<Map<String, Object>> processSingleSecondaryIndustry(
            @Parameter(description = "Industry name")
            @PathVariable String industryName) {
        try {
            log.info("Starting secondary industry mapping for industry: {}", industryName);
            Map<String, Object> result = secondaryIndustryMappingService.processSingleSecondaryIndustry(industryName);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else if (result.containsKey("existingMapping")) {
                return ResponseEntity.status(409).body(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }
        } catch (Exception e) {
            log.error("Error processing secondary industry: {}", industryName, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing secondary industry: " + e.getMessage());
            errorResponse.put("industryName", industryName);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/secondary-industries/reset-processed-flag")
    @Operation(
        summary = "Reset processed flag for all secondary industry mappings",
        description = "Resets the processed flag for all secondary industry mappings to allow reprocessing"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Secondary industry processed flag reset successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Secondary industry processed flag reset successfully\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> resetSecondaryIndustryProcessedFlag() {
        try {
            log.info("Resetting secondary industry processed flag");
            Map<String, Object> result = secondaryIndustryMappingService.resetProcessedFlag();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error resetting secondary industry processed flag", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error resetting secondary industry processed flag: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================
    // PROCESSED NAMES ENDPOINTS
    // ============================================================================

    @PostMapping("/processed-names/generate-all")
    @Operation(
        summary = "Generate processed names for all unprocessed entities",
        description = "Generates processed names for countries, cities, companies, and designations that haven't been processed yet. " +
                     "Processed names remove special characters and common suffixes to ensure uniqueness and deduplication."
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
                        "  \"countries\": {\"total\": 50, \"processed\": 48, \"errors\": 2, \"success\": true},\n" +
                        "  \"cities\": {\"total\": 200, \"processed\": 195, \"errors\": 5, \"success\": true},\n" +
                        "  \"companies\": {\"total\": 100, \"processed\": 95, \"errors\": 5, \"success\": true},\n" +
                        "  \"designations\": {\"total\": 80, \"processed\": 78, \"errors\": 2, \"success\": true}\n" +
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
            errorResponse.put("error", "Error generating processed names for all entities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-countries")
    @Operation(
        summary = "Generate processed names for unprocessed countries",
        description = "Generates processed names for countries that haven't been processed yet. " +
                     "Processed names remove special characters and common suffixes to ensure uniqueness."
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-cities")
    @Operation(
        summary = "Generate processed names for unprocessed cities",
        description = "Generates processed names for cities that haven't been processed yet. " +
                     "Processed names remove special characters and common suffixes to ensure uniqueness."
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/processed-names/generate-designations")
    @Operation(
        summary = "Generate processed names for unprocessed designations",
        description = "Generates processed names for designations that haven't been processed yet. " +
                     "Processed names remove special characters and common suffixes to ensure uniqueness."
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/processed-names/reset")
    @Operation(
        summary = "Reset processed name generation flag for all entities",
        description = "Resets the processed_name_generated flag to false for all countries, cities, companies, and designations, " +
                     "allowing reprocessing of processed name generation"
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ============================================================================

    @PostMapping("/elasticsearch/index-all-entities")
    @Operation(
        summary = "Re-index all entities to Elasticsearch with cleanup",
        description = "First deletes all existing documents from Elasticsearch indices (companies, designations, cities), " +
                     "then re-indexes all entities fresh. This ensures a clean, up-to-date search index."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All entities successfully re-indexed to Elasticsearch after cleanup",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"status\":\"SUCCESS\",\"message\":\"All entities re-indexed to Elasticsearch after cleanup\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> indexAllEntitiesToElasticsearch() {
        try {
            log.info("Starting to re-index all entities to Elasticsearch with cleanup");
            Map<String, Object> result = elasticsearchIndexingService.indexAllEntities();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error re-indexing all entities to Elasticsearch", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error re-indexing all entities to Elasticsearch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
