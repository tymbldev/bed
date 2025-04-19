package com.tymbl.jobs.controller;

import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobsearch")
@RequiredArgsConstructor
@Tag(name = "Job Search", description = "Job search and retrieval endpoints")
public class JobSearchController {

    private final JobService jobService;

    @GetMapping
    @Operation(
        summary = "Get all active job postings with pagination",
        description = "Returns a paginated list of all active job postings, sorted by the specified field and direction."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved job postings successfully")
    })
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC", schema = @Schema(allowableValues = {"ASC", "DESC"}))
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        PageRequest pageRequest = PageRequest.of(
                page, size,
                Sort.Direction.valueOf(sortDirection),
                sortBy);
        return ResponseEntity.ok(jobService.getAllActiveJobs(pageRequest));
    }

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

    @GetMapping("/search")
    @Operation(
        summary = "Search job postings by keyword",
        description = "Returns a paginated list of job postings matching the specified keyword in the title, description, or company name."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @Parameter(description = "Search keyword", required = true, example = "Java")
            @RequestParam String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.searchJobs(keyword, PageRequest.of(page, size)));
    }

    @GetMapping("/search/skills")
    @Operation(
        summary = "Search job postings by required skills",
        description = "Returns a paginated list of job postings requiring the specified skills."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<JobResponse>> searchBySkills(
            @Parameter(description = "Required skills", required = true, example = "[\"Java\", \"Spring\"]")
            @RequestParam List<String> skills,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.searchBySkills(skills, PageRequest.of(page, size)));
    }

    @GetMapping("/company/{companyId}")
    @Operation(
        summary = "Get jobs by company ID",
        description = "Returns all jobs posted by a specific company."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved jobs successfully")
    })
    public ResponseEntity<List<JobResponse>> getJobsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId));
    }

    @GetMapping("/company/{companyId}/search")
    @Operation(
        summary = "Search jobs by company ID and title",
        description = "Returns jobs posted by a specific company matching the given title."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<JobResponse>> getJobsByCompanyAndTitle(
            @PathVariable Long companyId,
            @RequestParam String title) {
        return ResponseEntity.ok(jobService.getJobsByCompanyAndTitle(companyId, title));
    }

    @GetMapping("/company/search")
    @Operation(
        summary = "Search jobs by company name",
        description = "Returns jobs posted by companies matching the given name."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<JobResponse>> getJobsByCompanyName(
            @RequestParam String companyName) {
        return ResponseEntity.ok(jobService.getJobsByCompanyName(companyName));
    }

    @GetMapping("/company/search/title")
    @Operation(
        summary = "Search jobs by company name and title",
        description = "Returns jobs posted by companies matching the given name and title."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<List<JobResponse>> getJobsByCompanyNameAndTitle(
            @RequestParam String companyName,
            @RequestParam String title) {
        return ResponseEntity.ok(jobService.getJobsByCompanyNameAndTitle(companyName, title));
    }
} 