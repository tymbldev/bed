package com.tymbl.jobs.controller;

import com.tymbl.common.service.CompanyDataService;
import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UtilityController: Internal/utility endpoints for admin/data operations (non-AI).
 * This controller merges all utility endpoints except AI-related ones.
 */
@RestController
@RequestMapping("/api/v1/utility")
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
@RequiredArgsConstructor
@Tag(name = "Utility", description = "Internal/utility endpoints for admin/data operations (non-AI)")
public class UtilityController {
    private final CompanyDataService companyDataService;
    private final JobService jobService;

    // --- Company Data Endpoints ---
    @PostMapping("/company-data/load-basic")
    public ResponseEntity<List<String>> loadBasicCompanyData() {
        List<String> results = companyDataService.loadBasicCompanyData();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/company-data/update-detailed")
    public ResponseEntity<List<String>> updateDetailedCompanyData() {
        List<String> results = companyDataService.updateDetailedCompanyData();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/company-data/cleanup-duplicates")
    public ResponseEntity<List<String>> cleanupDuplicateCompanies() {
        List<String> results = companyDataService.cleanupDuplicateCompanies();
        return ResponseEntity.ok(results);
    }

    // --- Job Admin Operations ---
    @PostMapping("/job-admin/reindex")
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