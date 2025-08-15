package com.tymbl.jobs.controller;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.dto.JobCrawlResponse;
import com.tymbl.jobs.service.JobCrawlingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-jobs")
@Tag(name = "AI Job Crawling", description = "Endpoints for crawling job data from various portals")
public class AIJobController {

  @Autowired
  private JobCrawlingService jobCrawlingService;

  @PostMapping("/crawl")
  @Operation(summary = "Crawl jobs for specific keyword and portal", description = "Crawls job data from the specified portal using the given keyword")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Jobs crawled successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> crawlJobs(@RequestBody JobCrawlRequest request) {
    try {
      JobCrawlResponse response = jobCrawlingService.crawlJobs(request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error during crawling: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/crawl/{keyword}/{portalName}")
  @Operation(summary = "Crawl jobs for specific keyword and portal", description = "Crawls job data from the specified portal using the given keyword")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Jobs crawled successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> manualCrawl(
      @PathVariable String keyword,
      @PathVariable String portalName) {
    try {
      JobCrawlResponse response = jobCrawlingService.manualCrawl(keyword, portalName);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error during crawling: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/crawl-all")
  @Operation(summary = "Crawl all active keywords for all portals", description = "Automatically crawls all active keywords for all configured portals")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Crawling process started successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> crawlAllActiveKeywords() {
    try {
      jobCrawlingService.crawlAllActiveKeywords();
      JobCrawlResponse response = new JobCrawlResponse();
      response.setStatus("SUCCESS");
      response.setMessage("Crawling process started for all active keywords");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error starting crawling process: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/process-pending")
  @Operation(summary = "Process pending raw responses", description = "Processes any pending raw API responses and extracts job details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Processing completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> processPendingResponses() {
    try {
      jobCrawlingService.processPendingRawResponses();
      JobCrawlResponse response = new JobCrawlResponse();
      response.setStatus("SUCCESS");
      response.setMessage("Pending responses processed successfully");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error processing pending responses: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @GetMapping("/stats")
  @Operation(summary = "Get crawling statistics", description = "Returns statistics about the crawling process")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> getCrawlingStats() {
    try {
      JobCrawlResponse response = jobCrawlingService.getCrawlingStats();
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error retrieving statistics: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @GetMapping("/keywords")
  @Operation(summary = "Get all configured keywords", description = "Returns all configured keywords and portal configurations")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Keywords retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<List<Object>> getAllKeywords() {
    try {
      // This would need a service method to return keyword configurations
      // For now, returning a placeholder response
      return ResponseEntity.ok(
          java.util.Arrays.asList("Keywords endpoint - implementation needed"));
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping("/keywords/add")
  @Operation(summary = "Add new keyword configuration", description = "Adds a new keyword and portal configuration for crawling")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Keyword added successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid keyword configuration"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<JobCrawlResponse> addKeyword(@RequestBody JobCrawlRequest request) {
    try {
      // This would need a service method to add keywords
      // For now, returning a placeholder response
      JobCrawlResponse response = new JobCrawlResponse();
      response.setStatus("SUCCESS");
      response.setMessage("Keyword addition endpoint - implementation needed");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      JobCrawlResponse errorResponse = new JobCrawlResponse();
      errorResponse.setStatus("ERROR");
      errorResponse.setMessage("Error adding keyword: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }


}
