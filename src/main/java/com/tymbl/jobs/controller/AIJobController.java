package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.ExternalJobSyncService;
import com.tymbl.jobs.service.ExternalJobSyncService.SyncResult;
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



  @PostMapping("/sync-external-job")
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

}
