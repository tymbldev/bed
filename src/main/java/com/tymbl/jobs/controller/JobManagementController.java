package com.tymbl.jobs.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.service.JobService;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/jobmanagement")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "Job management endpoints that require authentication")
public class JobManagementController {

    private final JobService jobService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

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
            @Valid @RequestBody JobRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
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
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        return ResponseEntity.ok(jobService.updateJob(jobId, request, currentUser));
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
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        jobService.deleteJob(jobId, currentUser);
        return ResponseEntity.ok().build();
    }
} 