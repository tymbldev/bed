package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.ElasticsearchIndexingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies List", description = "APIs for searching and listing companies with filters")
@Slf4j
public class ListOfCompaniesController {

  private final ElasticsearchIndexingService elasticsearchIndexingService;

  @GetMapping("/search")
  @Operation(
      summary = "Search companies with filters",
      description = "Search companies by location (headquarters), industry name, and secondary industry name. Results are sorted by job count (descending) and then by company name (ascending)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Companies found successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"companies\": [\n" +
                      "    {\n" +
                      "      \"id\": 1,\n" +
                      "      \"name\": \"Google\",\n" +
                      "      \"description\": \"A technology company\",\n" +
                      "      \"website\": \"https://google.com\",\n" +
                      "      \"logoUrl\": \"https://google.com/logo.png\",\n" +
                      "      \"headquarters\": \"Mountain View, CA\",\n" +
                      "      \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                      "      \"secondaryIndustries\": \"Software,Cloud,AI\",\n" +
                      "      \"companySize\": \"100000+\",\n" +
                      "      \"specialties\": \"AI,ML,Search\",\n" +
                      "      \"jobCount\": 25,\n" +
                      "      \"score\": 8.5\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"id\": 2,\n" +
                      "      \"name\": \"Microsoft\",\n" +
                      "      \"description\": \"A technology company\",\n" +
                      "      \"website\": \"https://microsoft.com\",\n" +
                      "      \"logoUrl\": \"https://microsoft.com/logo.png\",\n" +
                      "      \"headquarters\": \"Redmond, WA\",\n" +
                      "      \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                      "      \"secondaryIndustries\": \"Software,Cloud\",\n" +
                      "      \"companySize\": \"100000+\",\n" +
                      "      \"specialties\": \"Windows,Office,Azure\",\n" +
                      "      \"jobCount\": 18,\n" +
                      "      \"score\": 7.2\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"total\": 2,\n" +
                      "  \"page\": 0,\n" +
                      "  \"size\": 10,\n" +
                      "  \"totalPages\": 1\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> searchCompanies(
      @RequestParam(value = "location", required = false) String location,
      @RequestParam(value = "industryName", required = false) String industryName,
      @RequestParam(value = "secondaryIndustryName", required = false) String secondaryIndustryName,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    try {
      // Validate parameters
      if (page < 0) {
        return ResponseEntity.badRequest().body(createErrorResponse("Page must be non-negative"));
      }

      if (size <= 0 || size > 100) {
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Size must be between 1 and 100"));
      }

      // All filters are optional, so we can proceed with the search
      Map<String, Object> result = elasticsearchIndexingService.searchCompanies(
          location, industryName, secondaryIndustryName, page, size);

      if (result.containsKey("error")) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
      }

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("Error searching companies", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to search companies: " + e.getMessage()));
    }
  }

  @PostMapping("/{companyId}/update-job-count")
  @Operation(
      summary = "Update job count for a company",
      description = "Updates the job count for a specific company in Elasticsearch. This should be called when jobs are posted or updated."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Job count updated successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"message\": \"Job count updated successfully for company 1\",\n" +
                      "  \"companyId\": 1\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "404", description = "Company not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> updateCompanyJobCount(@PathVariable Long companyId) {
    try {
      elasticsearchIndexingService.updateCompanyJobCount(companyId);

      Map<String, Object> response = new HashMap<>();
      response.put("message", "Job count updated successfully for company " + companyId);
      response.put("companyId", companyId);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error updating job count for company {}", companyId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Failed to update job count: " + e.getMessage()));
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", message);
    return error;
  }
} 