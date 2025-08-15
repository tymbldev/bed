package com.tymbl.jobs.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobApprovalRequest;
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
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/superadmin")
@RequiredArgsConstructor
@Tag(name = "Super Admin", description = "Super admin job approval endpoints")
public class SuperAdminController {

  private final JobService jobService;
  private final JwtService jwtService;
  private final RegistrationService registrationService;

  private boolean isSuperAdmin(String token) {
    try {
      String email = jwtService.extractUsername(token.substring(7));
      User user = registrationService.getUserByEmail(email);
      return user.getId() == 0L; // Super admin has ID 0
    } catch (Exception e) {
      return false;
    }
  }

  @GetMapping("/jobs/pending")
  @Operation(
      summary = "Get all pending jobs for approval",
      description = "Returns a paginated list of all jobs that are pending approval. Only accessible by super admin (user ID 0)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Retrieved pending jobs successfully",
          content = @Content(
              schema = @Schema(implementation = JobResponse.class)
          )
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Not super admin"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Not super admin"),
      @ApiResponse(responseCode = "500", description = "Server error")
  })
  public ResponseEntity<Page<JobResponse>> getPendingJobs(
      @RequestHeader("Authorization") String token,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10")
      @RequestParam(defaultValue = "10") int size) {

    if (!isSuperAdmin(token)) {
      return ResponseEntity.status(403).build();
    }

    return ResponseEntity.ok(
        jobService.getPendingJobsForSuperAdmin(PageRequest.of(page, size)));
  }

  @PostMapping("/jobs/approve")
  @Operation(
      summary = "Approve or reject a job",
      description = "Approves or rejects a job posting. Only accessible by super admin (user ID 0)."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Job approval status updated successfully",
          content = @Content(
              schema = @Schema(implementation = JobResponse.class)
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Not super admin"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Not super admin"),
      @ApiResponse(responseCode = "404", description = "Job not found"),
      @ApiResponse(responseCode = "500", description = "Server error")
  })
  public ResponseEntity<JobResponse> approveJob(
      @Valid @RequestBody JobApprovalRequest request,
      @RequestHeader("Authorization") String token) {
    if (!isSuperAdmin(token)) {
      return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(
        jobService.approveJob(request.getJobId(), request.getStatus(),
            request.getRejectionReason()));
  }
} 