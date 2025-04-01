package com.tymbl.jobs.controller;

import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobApplicationRequest;
import com.tymbl.jobs.dto.JobApplicationResponse;
import com.tymbl.jobs.service.JobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/job-applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications", description = "APIs for job applications")
@SecurityRequirement(name = "bearerAuth")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping
    @Operation(
        summary = "Apply for a job",
        description = "Submit an application for a specific job posting. The applicant is determined by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Application submitted successfully",
            content = @Content(
                schema = @Schema(implementation = JobApplicationResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"jobId\": 5,\n" +
                          "  \"jobTitle\": \"Senior Java Developer\",\n" +
                          "  \"companyName\": \"Example Corp\",\n" +
                          "  \"applicantId\": 10,\n" +
                          "  \"applicantName\": \"John Doe\",\n" +
                          "  \"applicantEmail\": \"john.doe@example.com\",\n" +
                          "  \"status\": \"PENDING\",\n" +
                          "  \"applicationDate\": \"2023-09-15T14:30:00\",\n" +
                          "  \"coverLetter\": \"I am excited to apply for this position...\",\n" +
                          "  \"resumeUrl\": \"https://example.com/resumes/johndoe.pdf\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input or already applied"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<JobApplicationResponse> applyForJob(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Job application details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "JobApplicationRequest",
                        summary = "Standard job application request",
                        value = "{\n" +
                              "  \"jobId\": 5,\n" +
                              "  \"coverLetter\": \"I am excited to apply for this position...\",\n" +
                              "  \"resumeUrl\": \"https://example.com/resumes/johndoe.pdf\"\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal User applicant) {
        return ResponseEntity.ok(jobApplicationService.applyForJob(request, applicant));
    }

    @GetMapping("/job/{jobId}")
    @Operation(
        summary = "Get all applications for a job",
        description = "Retrieves all applications submitted for a specific job posting. This operation is typically for recruiters or job posters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Applications retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = JobApplicationResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"jobId\": 5,\n" +
                          "    \"jobTitle\": \"Senior Java Developer\",\n" +
                          "    \"companyName\": \"Example Corp\",\n" +
                          "    \"applicantId\": 10,\n" +
                          "    \"applicantName\": \"John Doe\",\n" +
                          "    \"applicantEmail\": \"john.doe@example.com\",\n" +
                          "    \"status\": \"PENDING\",\n" +
                          "    \"applicationDate\": \"2023-09-15T14:30:00\",\n" +
                          "    \"coverLetter\": \"I am excited to apply for this position...\",\n" +
                          "    \"resumeUrl\": \"https://example.com/resumes/johndoe.pdf\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"jobId\": 5,\n" +
                          "    \"jobTitle\": \"Senior Java Developer\",\n" +
                          "    \"companyName\": \"Example Corp\",\n" +
                          "    \"applicantId\": 11,\n" +
                          "    \"applicantName\": \"Jane Smith\",\n" +
                          "    \"applicantEmail\": \"jane.smith@example.com\",\n" +
                          "    \"status\": \"PENDING\",\n" +
                          "    \"applicationDate\": \"2023-09-16T09:45:00\",\n" +
                          "    \"coverLetter\": \"Please consider my application...\",\n" +
                          "    \"resumeUrl\": \"https://example.com/resumes/janesmith.pdf\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view these applications"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsByJob(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByJob(jobId));
    }

    @GetMapping("/my-applications")
    @Operation(
        summary = "Get current user's job applications",
        description = "Retrieves all job applications submitted by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Applications retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = JobApplicationResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"jobId\": 5,\n" +
                          "    \"jobTitle\": \"Senior Java Developer\",\n" +
                          "    \"companyName\": \"Example Corp\",\n" +
                          "    \"applicantId\": 10,\n" +
                          "    \"applicantName\": \"John Doe\",\n" +
                          "    \"applicantEmail\": \"john.doe@example.com\",\n" +
                          "    \"status\": \"PENDING\",\n" +
                          "    \"applicationDate\": \"2023-09-15T14:30:00\",\n" +
                          "    \"coverLetter\": \"I am excited to apply for this position...\",\n" +
                          "    \"resumeUrl\": \"https://example.com/resumes/johndoe.pdf\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"jobId\": 8,\n" +
                          "    \"jobTitle\": \"DevOps Engineer\",\n" +
                          "    \"companyName\": \"Tech Solutions Inc\",\n" +
                          "    \"applicantId\": 10,\n" +
                          "    \"applicantName\": \"John Doe\",\n" +
                          "    \"applicantEmail\": \"john.doe@example.com\",\n" +
                          "    \"status\": \"UNDER_REVIEW\",\n" +
                          "    \"applicationDate\": \"2023-09-18T11:20:00\",\n" +
                          "    \"coverLetter\": \"I believe my DevOps experience makes me ideal...\",\n" +
                          "    \"resumeUrl\": \"https://example.com/resumes/johndoe.pdf\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal User applicant) {
        return ResponseEntity.ok(jobApplicationService.getApplicationsByApplicant(applicant));
    }
} 