package com.tymbl.jobs.controller;

import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.service.ElasticsearchSEOService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Similar Content", description = "APIs for similar designations or skills with job counts")
public class SimilarContentController {

  private final ElasticsearchSEOService elasticsearchSEOService;
  private final DropdownService dropdownService;

  @GetMapping("/similarcontent")
  @Operation(
      summary = "Get similar content (designation or skill) with job counts",
      description = "Detects if the query is a designation or a skill using dropdown cache and returns similar items with job counts"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Similar content retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"type\": \"designation\",\n" +
                      "  \"input\": \"Software Engineer\",\n" +
                      "  \"items\": [{\"name\": \"Senior Software Engineer\", \"jobCount\": 45}]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<Map<String, Object>> getSimilarContent(
      @Parameter(description = "Designation or skill name", example = "Software Engineer")
      @RequestParam String query) {
    try {
      if (query == null || query.trim().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Query parameter is required");
        return ResponseEntity.badRequest().body(error);
      }

      String lowered = query.trim().toLowerCase();
      boolean isDesignation = false;
      try {
        List<com.tymbl.common.entity.Designation> designations = dropdownService.getAllDesignations();
        isDesignation = designations.stream().anyMatch(d -> d.getName() != null && d.getName().trim().equalsIgnoreCase(lowered));
      } catch (Exception ignore) {
        isDesignation = true; // fallback
      }
      Map<String, Object> result;
      if (isDesignation) {
        result = elasticsearchSEOService.getSimilarDesignationsWithJobCounts(query.trim());
      } else {
        result = elasticsearchSEOService.getSimilarSkillsWithJobCounts(query.trim());
      }
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      log.error("Error getting similar content for: {}", query, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting similar content: " + e.getMessage());
      return ResponseEntity.internalServerError().body(error);
    }
  }
}



