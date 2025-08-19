package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.ExternalJobSyncService;
import com.tymbl.jobs.service.ExternalJobSyncService.SyncResult;
import com.tymbl.jobs.service.JobCrawlingService;
import com.tymbl.jobs.service.JobContentRefinementService;
import com.tymbl.jobs.service.JobService;
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
@Tag(name = "AI Job Sync", description = "Endpoints for syncing external job data to the main Job table")
public class AIJobController {

  @Autowired
  private ExternalJobSyncService externalJobSyncService;

  @Autowired
  private JobCrawlingService jobCrawlingService;

  @Autowired
  private JobContentRefinementService contentRefinementService;

  @Autowired
  private JobService jobService;


  @PostMapping("/crawl-all-active-keywords")
  @Operation(summary = "Crawl all active keywords", description = "Crawls all active keywords for all portals that are ready for crawling (not crawled in last 24 hours)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Crawl process initiated successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<String> crawlAllActiveKeywords() {
    try {
      jobCrawlingService.crawlAllActiveKeywords();
      return ResponseEntity.ok("Crawl process initiated successfully for all active keywords");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Error during crawl process: " + e.getMessage());
    }
  }

  @PostMapping("/process-pending-raw-responses")
  @Operation(summary = "Process pending raw responses", description = "Processes all pending raw responses from job crawling and converts them to job details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Processing completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<String> processPendingRawResponses() {
    try {
      jobCrawlingService.processPendingRawResponses();
      return ResponseEntity.ok("Pending raw responses processing completed successfully");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Error during processing: " + e.getMessage());
    }
  }

  @PostMapping("/refine-unprocessed-content")
  @Operation(summary = "Refine unprocessed job content", description = "Processes all external job records that haven't been refined yet, using AI to clean descriptions and titles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Content refinement process completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<String> refineUnprocessedContent() {
    try {
      int processedCount = contentRefinementService.refineAllUnprocessedContent();
      return ResponseEntity.ok("Content refinement completed successfully. Processed " + processedCount + " records.");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Error during content refinement: " + e.getMessage());
    }
  }

  @PostMapping("/sync-external-raw")
  @Operation(summary = "Sync external jobs to Job table", description = "Synchronizes external job data to the main Job table using AI tagging")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Sync completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<SyncResult> syncExternalJobs() {
    try {
      SyncResult result = externalJobSyncService.syncExternalJobsToJobTable();
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      SyncResult errorResponse = new SyncResult();
      errorResponse.setSuccess(false);
      errorResponse.setMessage("Error during sync: " + e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  @PostMapping("/reindex")
  @Operation(
      summary = "Reindex all jobs to Elasticsearch",
      description = "Replaces all existing data in Elasticsearch with current job data from the database. This is an admin/on-demand operation."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Reindex completed successfully"),
      @ApiResponse(responseCode = "500", description = "Reindex failed")
  })
  public ResponseEntity<String> reindexAllJobs() {
    try {
      jobService.reindexAllJobsToElasticsearch();
      return ResponseEntity.ok("Reindex completed successfully");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("Reindex failed: " + e.getMessage());
    }
  }
}
