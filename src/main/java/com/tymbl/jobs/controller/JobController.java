package com.tymbl.jobs.controller;

import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job posting and management APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @Operation(
        summary = "Create a new job posting",
        description = "Creates a new job posting with the given details. The job posting will be associated with the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Job posting created successfully",
            content = @Content(
                schema = @Schema(implementation = JobResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<JobResponse> createJob(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Job posting details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "JobRequest",
                        summary = "Complete job posting request",
                        value = "{\n" +
                              "  \"title\": \"Senior Java Developer\",\n" +
                              "  \"description\": \"We are looking for an experienced Java Developer...\",\n" +
                              "  \"location\": \"San Francisco, CA\",\n" +
                              "  \"employmentType\": \"FULL_TIME\",\n" +
                              "  \"experienceLevel\": \"SENIOR\",\n" +
                              "  \"salary\": 120000.0,\n" +
                              "  \"currency\": \"USD\",\n" +
                              "  \"companyId\": 1,\n" +
                              "  \"companyName\": \"Example Corp\"\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobService.createJob(request, currentUser));
    }

    @PutMapping("/{jobId}")
    @Operation(
        summary = "Update an existing job posting",
        description = "Updates an existing job posting with the given details. Only the user who posted the job can update it."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job posting updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not the job owner"),
        @ApiResponse(responseCode = "404", description = "Job posting not found")
    })
    public ResponseEntity<JobResponse> updateJob(
            @Parameter(description = "Job ID", required = true) 
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobService.updateJob(jobId, request, currentUser));
    }

    @GetMapping
    @Operation(
        summary = "Get all active job postings with pagination",
        description = "Returns a paginated list of all active job postings, sorted by the specified field and direction."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved job postings successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "postedDate")
            @RequestParam(defaultValue = "postedDate") String sortBy,
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
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Job posting not found")
    })
    public ResponseEntity<JobResponse> getJobById(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId) {
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping("/my-posts")
    @Operation(
        summary = "Get all job postings by the current user",
        description = "Returns a paginated list of all job postings created by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved job postings successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                jobService.getJobsByUser(currentUser, PageRequest.of(page, size)));
    }

    @DeleteMapping("/{jobId}")
    @Operation(
        summary = "Delete (deactivate) a job posting",
        description = "Deactivates a job posting with the specified ID. Only the user who posted the job can delete it."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Job posting deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not the job owner"),
        @ApiResponse(responseCode = "404", description = "Job posting not found")
    })
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId,
            @AuthenticationPrincipal User currentUser) {
        jobService.deleteJob(jobId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search job postings by keyword",
        description = "Returns a paginated list of job postings matching the specified keyword in the title, description, or company name."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
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
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
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
    public ResponseEntity<List<JobResponse>> getJobsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobService.getJobsByCompany(companyId));
    }

    @GetMapping("/company/{companyId}/search")
    public ResponseEntity<List<JobResponse>> getJobsByCompanyAndTitle(
            @PathVariable Long companyId,
            @RequestParam String title) {
        return ResponseEntity.ok(jobService.getJobsByCompanyAndTitle(companyId, title));
    }

    @GetMapping("/company/search")
    public ResponseEntity<List<JobResponse>> getJobsByCompanyName(
            @RequestParam String companyName) {
        return ResponseEntity.ok(jobService.getJobsByCompanyName(companyName));
    }

    @GetMapping("/company/search/title")
    public ResponseEntity<List<JobResponse>> getJobsByCompanyNameAndTitle(
            @RequestParam String companyName,
            @RequestParam String title) {
        return ResponseEntity.ok(jobService.getJobsByCompanyNameAndTitle(companyName, title));
    }
} 