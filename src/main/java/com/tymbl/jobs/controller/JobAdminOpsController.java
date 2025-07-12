package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/job-admin")
@RequiredArgsConstructor
@Tag(name = "Job Admin Operations", description = "Admin/on-demand operations for jobs, such as Elasticsearch reindexing.")
public class JobAdminOpsController {
    private final JobService jobService;

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