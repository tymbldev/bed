package com.tymbl.jobs.controller;


import com.tymbl.common.service.AIJobFetchingService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.service.ProcessedNameService;
import com.tymbl.jobs.service.AIJobService;
import com.tymbl.jobs.service.CompanyCrawlerService;
import com.tymbl.jobs.service.CompanyService;
import com.tymbl.jobs.service.ElasticsearchIndexingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Utilities", description = "APIs for AI-powered data generation and enrichment utilities")
public class AIController {

  private final CompanyCrawlerService companyCrawlerService;
  private final CompanyService companyService;
  private final AIJobService aiJobService;
  private final AIJobFetchingService AIJobFetchingService;
  private final ProcessedNameService processedNameService;
  private final GeminiService geminiService;
  private final ElasticsearchIndexingService elasticsearchIndexingService;

  // ============================================================================
  // COMPANY CRAWLING ENDPOINTS (Legacy - kept for backward compatibility)
  // ============================================================================


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
      java.util.Optional<com.tymbl.common.entity.UrlContent> urlContent = AIJobFetchingService.getUrlContent(
          url);

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

  // ============================================================================
  // PROCESSED NAME GENERATION ENDPOINTS
  // ============================================================================

  @PostMapping("/processed-names/generate-all")
  @Operation(
      summary = "Generate processed names for all unprocessed entities",
      description = "Generates processed names for countries, cities, and designations that haven't been processed yet. Processed names remove special characters and common suffixes to ensure uniqueness and deduplication."
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

  @PostMapping("/processed-names/remove-duplicates")
  @Operation(
      summary = "Remove duplicate processed names from database",
      description = "Removes duplicate entries from the database where entities have the same processed name. Keeps the first occurrence and removes subsequent duplicates."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Duplicate processed names removed successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"success\": true,\n" +
                      "  \"message\": \"Duplicate processed names removal completed\",\n" +
                      "  \"totalDuplicatesRemoved\": 25\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> removeDuplicateProcessedNames() {
    try {
      log.info("Removing duplicate processed names from database");
      Map<String, Object> result = processedNameService.removeDuplicateProcessedNames();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error removing duplicate processed names", e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Error removing duplicate processed names: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/processed-names/reset")
  @Operation(
      summary = "Reset processed name generation flag for all entities",
      description = "Resets the processed_name_generated flag to false for all countries, cities, and designations, allowing reprocessing of processed name generation"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Processed name generation flags reset successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"success\": true,\n" +
                      "  \"message\": \"Processed name generation flags reset successfully for all entities\"\n"
                      +
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
      errorResponse.put("error",
          "Error resetting processed name generation flags: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }


} 