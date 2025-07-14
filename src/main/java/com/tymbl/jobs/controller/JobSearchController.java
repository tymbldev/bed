package com.tymbl.jobs.controller;

import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import com.tymbl.jobs.dto.JobReferrerResponse;

@RestController
@RequestMapping("/api/v1/jobsearch")
@RequiredArgsConstructor
@Tag(name = "Job Search", description = "Job search and retrieval endpoints")
public class JobSearchController {

    private final JobService jobService;

    @GetMapping("/{jobId}")
    @Operation(
        summary = "Get job posting details by ID",
        description = "Returns the details of a job posting with the specified ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved job posting successfully"),
        @ApiResponse(responseCode = "404", description = "Job posting not found")
    })
    public ResponseEntity<JobResponse> getJobById(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @PostMapping("/search")
    @Operation(
        summary = "Search jobs using Elasticsearch",
        description = "Unified search endpoint that searches jobs using Elasticsearch. Supports keyword search, location filtering, experience filtering, company, designation, and pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully",
            content = @Content(
                schema = @Schema(implementation = JobSearchResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"jobs\": [\n" +
                          "    {\n" +
                          "      \"id\": 1,\n" +
                          "      \"title\": \"Software Engineer\",\n" +
                          "      \"description\": \"We are looking for a talented software engineer...\",\n" +
                          "      \"company\": \"Google\",\n" +
                          "      \"designation\": \"Software Engineer\",\n" +
                          "      \"minSalary\": 100000,\n" +
                          "      \"maxSalary\": 150000,\n" +
                          "      \"minExperience\": 2,\n" +
                          "      \"maxExperience\": 5,\n" +
                          "      \"jobType\": \"HYBRID\",\n" +
                          "      \"cityId\": 1,\n" +
                          "      \"countryId\": 1,\n" +
                          "      \"companyId\": 1,\n" +
                          "      \"designationId\": 1,\n" +
                          "      \"currencyId\": 1,\n" +
                          "      \"postedBy\": 1,\n" +
                          "      \"active\": true,\n" +
                          "      \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"tags\": [\"Java\", \"Spring\", \"Microservices\"],\n" +
                          "      \"openingCount\": 5,\n" +
                          "      \"uniqueUrl\": \"https://careers.google.com/jobs/123\",\n" +
                          "      \"platform\": \"Google Careers\",\n" +
                          "      \"approved\": 1,\n" +
                          "      \"referrerCount\": 2,\n" +
                          "      \"userRole\": \"VIEWER\",\n" +
                          "      \"actualPostedBy\": 1,\n" +
                          "      \"isSuperAdminPosted\": false\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"total\": 1,\n" +
                          "  \"page\": 0,\n" +
                          "  \"size\": 20,\n" +
                          "  \"totalPages\": 1\n" +
                          "}"
                )
            )
        )
    })
    public ResponseEntity<JobSearchResponse> searchJobs(
            @Valid @RequestBody JobSearchRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        JobSearchResponse response = jobService.searchJobsWithElasticsearch(request, currentUser);
        response.setPage(request.getPage());
        response.setSize(request.getSize());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}/referrers")
    @Operation(
        summary = "Get all referrers for a job",
        description = "Returns a list of all referrers for the specified job, sorted by overall score."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of referrers for the job",
            content = @Content(
                schema = @Schema(implementation = JobReferrerResponse.class),
                examples = @ExampleObject(
                    value = "[\n  {\n    \"userId\": 123,\n    \"userName\": \"Alice Smith\",\n    \"designation\": \"Senior Engineer\",\n    \"numApplicationsAccepted\": 5,\n    \"feedbackScore\": 4.5,\n    \"overallScore\": 7.2\n  },\n  {\n    \"userId\": 456,\n    \"userName\": \"Bob Lee\",\n    \"designation\": \"Manager\",\n    \"numApplicationsAccepted\": 2,\n    \"feedbackScore\": 4.0,\n    \"overallScore\": 6.1\n  }\n]"
                )
            )
        )
    })
    public ResponseEntity<java.util.List<JobReferrerResponse>> getReferrersForJob(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getReferrersForJob(jobId));
    }
} 