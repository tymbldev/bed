package com.tymbl.jobs.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobApplicationRequest;
import com.tymbl.jobs.dto.JobApplicationResponse;
import com.tymbl.jobs.dto.ReferrerFeedbackRequest;
import com.tymbl.jobs.service.JobApplicationService;
import com.tymbl.jobs.service.JobService;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/my-applications")
@RequiredArgsConstructor
@Tag(name = "My Job Applications", description = "Endpoints for managing job applications made by the current user")
public class MyJobApplicationsController {

    private final JobApplicationService jobApplicationService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;
    private final JobService jobService;

    @PostMapping
    @Operation(
        summary = "Apply for a referral",
        description = "Submit an application for a specific job via a selected referrer. The applicant is determined by the authenticated user. The resume URL is automatically fetched from the user's profile."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Application submitted successfully",
            content = @Content(schema = @Schema(implementation = JobApplicationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input, already applied, or resume URL not found in profile"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot apply to your own job posting"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<?> applyForReferral(
            @Valid @RequestBody JobApplicationRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            JobApplicationResponse response = jobApplicationService.applyForJob(request, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(
        summary = "Get my job applications",
        description = "Retrieves all job applications submitted by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Applications retrieved successfully",
            content = @Content(schema = @Schema(implementation = JobApplicationResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        return ResponseEntity.ok(jobApplicationService.getApplicationsByApplicant(currentUser));
    }

    @PostMapping("/feedback")
    @Operation(
        summary = "Submit feedback for a referrer",
        description = "Allows an applicant to submit feedback about a referrer for a job."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feedback submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or not allowed")
    })
    public ResponseEntity<?> submitReferrerFeedback(
            @Valid @RequestBody ReferrerFeedbackRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            jobService.submitReferrerFeedback(request, currentUser);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/switch-referrer")
    @Operation(
        summary = "Switch referrer for an application",
        description = "Allows an applicant to transfer their application to another referrer if not yet accepted."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referrer switched successfully", content = @Content(schema = @Schema(implementation = JobApplicationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or not allowed")
    })
    public ResponseEntity<?> switchReferrer(
            @RequestParam Long applicationId,
            @RequestParam Long newJobReferrerId,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            JobApplicationResponse response = jobApplicationService.switchReferrer(applicationId, newJobReferrerId, currentUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }



} 