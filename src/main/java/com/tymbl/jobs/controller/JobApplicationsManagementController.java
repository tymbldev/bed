package com.tymbl.jobs.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobApplicationResponse;
import com.tymbl.jobs.dto.JobApplicationResponseExtendedDetails;
import com.tymbl.jobs.service.JobApplicationService;
import com.tymbl.registration.controller.UserController.ErrorResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-applications")
@RequiredArgsConstructor
@Tag(name = "Job Applications Management", description = "Endpoints for managing job applications received for jobs posted by the current user")
public class JobApplicationsManagementController {

    private final JobApplicationService jobApplicationService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

    @GetMapping("/my-jobs")
    @Operation(
        summary = "Get applications for my posted jobs",
        description = "Retrieves all applications for jobs posted by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Applications retrieved successfully",
            content = @Content(schema = @Schema(implementation = JobApplicationResponseExtendedDetails.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<JobApplicationResponseExtendedDetails>> getApplicationsForMyJobs(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        return ResponseEntity.ok(jobApplicationService.getApplicationsForJobsPostedByUser(currentUser));
    }

    @GetMapping("/job/{jobId}")
    @Operation(
        summary = "Get applications for a specific job",
        description = "Retrieves all applications for a specific job posting. Only accessible by the job poster."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Applications retrieved successfully",
            content = @Content(schema = @Schema(implementation = JobApplicationResponseExtendedDetails.class))
        ),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view these applications"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<?> getApplicationsByJob(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            List<JobApplicationResponseExtendedDetails> response = jobApplicationService.getApplicationsByJob(jobId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Job not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            }
        }
    }

    @GetMapping("/{applicationId}")
    @Operation(
        summary = "Get application details",
        description = "Retrieves detailed information about a specific job application. Only accessible by the job poster."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Application details retrieved successfully",
            content = @Content(schema = @Schema(implementation = JobApplicationResponseExtendedDetails.class))
        ),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view this application"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<?> getApplicationDetails(
            @Parameter(description = "Application ID", required = true)
            @PathVariable Long applicationId,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            JobApplicationResponseExtendedDetails response = jobApplicationService.getApplicationDetails(applicationId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(403).body(new ErrorResponse(e.getMessage()));
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            }
        }
    }
} 