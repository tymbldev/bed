package com.tymbl.jobs.controller;

import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.service.ElasticsearchIndexingService;
import com.tymbl.jobs.service.ElasticsearchSEOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/seo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SEO Interlinking", description = "APIs for SEO interlinking with job location combinations, similar designations, and top designations")
public class SEOInterlinkingController {

  private final ElasticsearchSEOService elasticsearchSEOService;
  private final ElasticsearchIndexingService elasticsearchIndexingService;
  private final DropdownService dropdownService;

  // ============================================================================
  // JOB LOCATION COMBINATIONS ENDPOINTS
  // ============================================================================

  @GetMapping("/jobs/location-combinations")
  @Operation(
      summary = "Get job location combinations for designation or skill",
      description =
          "Returns all location combinations with job counts for a given designation or skill. " +
              "For example: 'Software Engineer' returns 'Software Engineer jobs in Bangalore (50)', 'Software Engineer jobs in Delhi (30)', etc. "
              +
              "For skills like 'Java', returns 'Java jobs in Bangalore (25)', 'Java jobs in Mumbai (15)', etc."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Location combinations retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"query\": \"Software Engineer\",\n" +
                      "  \"totalJobs\": 150,\n" +
                      "  \"locationCombinations\": [\n" +
                      "    {\n" +
                      "      \"location\": \"Bangalore\",\n" +
                      "      \"jobCount\": 50,\n" +
                      "      \"seoText\": \"Software Engineer jobs in Bangalore\"\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"location\": \"Delhi\",\n" +
                      "      \"jobCount\": 30,\n" +
                      "      \"seoText\": \"Software Engineer jobs in Delhi\"\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getJobLocationCombinations(
      @Parameter(description = "Designation name or skill name", example = "Software Engineer")
      @RequestParam String query) {

    try {
      log.info("Getting job location combinations for: {}", query);

      if (query == null || query.trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Query parameter is required");
        return ResponseEntity.badRequest().body(error);
      }

      Map<String, Object> result = elasticsearchSEOService.getJobLocationCombinations(query.trim());
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error getting job location combinations for: {}", query, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting job location combinations: " + e.getMessage());
      error.put("query", query);
      return ResponseEntity.internalServerError().body(error);
    }
  }

  // ============================================================================
  // DESIGNATION/SKILL + LOCATION COMBINATIONS ENDPOINTS
  // ============================================================================

  @GetMapping("/jobs/designation-skill-location")
  @Operation(
      summary = "Get job counts and similar combinations for designation/skill + location",
      description =
          "Takes a combination of (designation or skill) + location and returns job counts with location combinations and similar designations/skills. "
              +
              "For example: 'Software Engineer + Delhi' returns job counts and similar designation+location combinations. "
              +
              "'Java + Delhi' returns job counts and similar skill+location combinations."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Job counts and similar combinations retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"query\": \"Software Engineer + Delhi\",\n" +
                      "  \"type\": \"designation\",\n" +
                      "  \"location\": \"Delhi\",\n" +
                      "  \"jobCount\": 45,\n" +
                      "  \"similarCombinations\": [\n" +
                      "    {\n" +
                      "      \"designationName\": \"Senior Software Engineer\",\n" +
                      "      \"location\": \"Delhi\",\n" +
                      "      \"jobCount\": 32,\n" +
                      "      \"seoText\": \"Senior Software Engineer jobs in Delhi\"\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"designationName\": \"Tech Lead\",\n" +
                      "      \"location\": \"Delhi\",\n" +
                      "      \"jobCount\": 18,\n" +
                      "      \"seoText\": \"Tech Lead jobs in Delhi\"\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"totalSimilarCombinations\": 2\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getDesignationSkillLocationCombinations(
      @Parameter(description = "Designation name or skill name", example = "Software Engineer")
      @RequestParam String query,
      @Parameter(description = "Location name", example = "Delhi")
      @RequestParam String location,
      @Parameter(description = "Type of query: 'designation' or 'skill'", example = "designation")
      @RequestParam(defaultValue = "designation") String type) {

    try {
      log.info("Getting designation/skill + location combinations for {}: {} in {}", type, query,
          location);

      if (query == null || query.trim().isEmpty() || location == null || location.trim()
          .isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Both query and location parameters are required");
        return ResponseEntity.badRequest().body(error);
      }

      Map<String, Object> result = elasticsearchSEOService.getDesignationSkillLocationCombinations(
          query.trim(), location.trim(), type);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error getting designation/skill + location combinations for {}: {} in {}", type,
          query, location, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error",
          "Error getting designation/skill + location combinations: " + e.getMessage());
      error.put("query", query);
      error.put("location", location);
      error.put("type", type);
      return ResponseEntity.internalServerError().body(error);
    }
  }

  // ============================================================================
  // TOP DESIGNATIONS ENDPOINTS
  // ============================================================================

  @GetMapping("/designations/top")
  @Operation(
      summary = "Get top designations by job count",
      description = "Returns the top designations ordered by job count. " +
          "Useful for SEO to show most popular job titles."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Top designations retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"topDesignations\": [\n" +
                      "    {\n" +
                      "      \"designationName\": \"Software Engineer\",\n" +
                      "      \"jobCount\": 150\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"designationName\": \"Sales Representative\",\n" +
                      "      \"jobCount\": 120\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"designationName\": \"Product Manager\",\n" +
                      "      \"jobCount\": 95\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"totalDesignations\": 50,\n" +
                      "  \"totalJobs\": 2500\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getTopDesignations(
      @Parameter(description = "Number of top designations to return", example = "20")
      @RequestParam(defaultValue = "20") int limit) {

    try {
      log.info("Getting top {} designations by job count", limit);

      if (limit <= 0 || limit > 100) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Limit must be between 1 and 100");
        return ResponseEntity.badRequest().body(error);
      }

      Map<String, Object> result = elasticsearchSEOService.getTopDesignationsByJobCount(limit);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error getting top designations", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting top designations: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }

  // ============================================================================
  // TOP SKILLS ENDPOINTS
  // ============================================================================

  @GetMapping("/skills/top")
  @Operation(
      summary = "Get top skills by job count",
      description = "Returns the top skills ordered by job count. Useful for SEO to show most popular skills."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Top skills retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"topSkills\": [\n" +
                      "    {\n" +
                      "      \"skillName\": \"Java\",\n" +
                      "      \"jobCount\": 220\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"skillName\": \"React\",\n" +
                      "      \"jobCount\": 180\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"totalSkills\": 50,\n" +
                      "  \"totalJobs\": 2500\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getTopSkills(
      @Parameter(description = "Number of top skills to return", example = "20")
      @RequestParam(defaultValue = "20") int limit) {

    try {
      log.info("Getting top {} skills by job count", limit);

      if (limit <= 0 || limit > 100) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Limit must be between 1 and 100");
        return ResponseEntity.badRequest().body(error);
      }

      Map<String, Object> result = elasticsearchSEOService.getTopSkillsByJobCount(limit);
      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error getting top skills", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting top skills: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }


}
