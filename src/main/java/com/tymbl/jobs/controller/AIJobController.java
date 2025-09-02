package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.ExternalJobSyncService;
import com.tymbl.jobs.service.ExternalJobSyncService.SyncResult;
import com.tymbl.jobs.service.JobContentRefinementService;
import com.tymbl.jobs.service.JobCrawlingService;
import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/ai-jobs")
@Tag(name = "AI Job Sync", description = "Endpoints for syncing external job data to the main Job table")
@Slf4j
public class AIJobController {

  @Autowired
  private ExternalJobSyncService externalJobSyncService;

  @Autowired
  private JobCrawlingService jobCrawlingService;

  @Autowired
  private JobContentRefinementService contentRefinementService;

  @Autowired
  private JobService jobService;

  @Autowired
  private Executor taskExecutor;


  @PostMapping("/crawl-all-active-keywords")
  @Operation(summary = "Crawl all active keywords", description = "Crawls all active keywords for all portals that are ready for crawling (not crawled in last 24 hours)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Crawl process initiated successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Async
  public ResponseEntity<String> crawlAllActiveKeywords() {
    try {
      jobCrawlingService.crawlAllActiveKeywords();
      return ResponseEntity.ok("Crawl process initiated successfully for all active keywords");
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Error during crawl process: " + e.getMessage());
    }
  }

  @PostMapping("/process-pending-raw-responses")
  @Async
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
      return ResponseEntity.internalServerError()
          .body("Error during processing: " + e.getMessage());
    }
  }

  @PostMapping("/refine-unprocessed-content")
  @Operation(summary = "Refine unprocessed job content", description = "Processes all external job records that haven't been refined yet, using AI to clean descriptions and titles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Content refinement process completed successfully"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @Async
  public ResponseEntity<String> refineUnprocessedContent() {
    try {
      int processedCount = contentRefinementService.refineAllUnprocessedContent();
      return ResponseEntity.ok(
          "Content refinement completed successfully. Processed " + processedCount + " records.");
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body("Error during content refinement: " + e.getMessage());
    }
  }

  @PostMapping("/sync-external-raw")
  @Async
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
  @Async
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

  /**
   * PostConstruct method that runs all AI job processing methods in a continuous loop
   * Each method runs independently in its own thread with a 1-hour sleep interval
   */
  //@PostConstruct
  public void startContinuousProcessing() {
    log.info("Starting continuous AI job processing loop");
    
    CompletableFuture.runAsync(() -> {
      while (true) {
        try {
          log.info("Starting new cycle of AI job processing methods");
          
          // Execute all methods independently in parallel
          CompletableFuture<Void> crawlFuture = CompletableFuture.runAsync(() -> {
            try {
              log.info("Executing crawlAllActiveKeywords");
              jobCrawlingService.crawlAllActiveKeywords();
              log.info("Completed crawlAllActiveKeywords");
            } catch (Exception e) {
              log.error("Error in crawlAllActiveKeywords: {}", e.getMessage(), e);
            }
          }, taskExecutor);

          CompletableFuture<Void> processFuture = CompletableFuture.runAsync(() -> {
            try {
              log.info("Executing processPendingRawResponses");
              jobCrawlingService.processPendingRawResponses();
              log.info("Completed processPendingRawResponses");
            } catch (Exception e) {
              log.error("Error in processPendingRawResponses: {}", e.getMessage(), e);
            }
          }, taskExecutor);

          CompletableFuture<Void> refineFuture = CompletableFuture.runAsync(() -> {
            try {
              log.info("Executing refineUnprocessedContent");
              int processedCount = contentRefinementService.refineAllUnprocessedContent();
              log.info("Completed refineUnprocessedContent, processed {} records", processedCount);
            } catch (Exception e) {
              log.error("Error in refineUnprocessedContent: {}", e.getMessage(), e);
            }
          }, taskExecutor);

          CompletableFuture<Void> syncFuture = CompletableFuture.runAsync(() -> {
            try {
              log.info("Executing syncExternalJobs");
              SyncResult result = externalJobSyncService.syncExternalJobsToJobTable();
              log.info("Completed syncExternalJobs: {}", result.getMessage());
            } catch (Exception e) {
              log.error("Error in syncExternalJobs: {}", e.getMessage(), e);
            }
          }, taskExecutor);

          CompletableFuture<Void> reindexFuture = CompletableFuture.runAsync(() -> {
            try {
              log.info("Executing reindexAllJobs");
              jobService.reindexAllJobsToElasticsearch();
              log.info("Completed reindexAllJobs");
            } catch (Exception e) {
              log.error("Error in reindexAllJobs: {}", e.getMessage(), e);
            }
          }, taskExecutor);

          // Wait for all methods to complete (or fail)
          CompletableFuture.allOf(crawlFuture, processFuture, refineFuture, syncFuture, reindexFuture)
              .exceptionally(throwable -> {
                log.error("One or more methods failed in this cycle", throwable);
                return null;
              })
              .join();

          log.info("Completed cycle of AI job processing methods, sleeping for 1 hour");
          
          // Sleep for 1 hour before next cycle
          Thread.sleep(60 * 60 * 1000); // 1 hour in milliseconds
          
        } catch (InterruptedException e) {
          log.warn("Continuous processing loop interrupted: {}", e.getMessage());
          Thread.currentThread().interrupt();
          break;
        } catch (Exception e) {
          log.error("Unexpected error in continuous processing loop: {}", e.getMessage(), e);
          try {
            // Sleep for 1 hour even if there's an error, then continue
            Thread.sleep(60 * 60 * 1000);
          } catch (InterruptedException ie) {
            log.warn("Sleep interrupted after error: {}", ie.getMessage());
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }, taskExecutor);
    
    log.info("Continuous AI job processing loop started successfully");
  }
}
