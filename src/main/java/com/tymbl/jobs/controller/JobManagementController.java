package com.tymbl.jobs.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.dto.JobDetailsWithReferrersResponse;
import com.tymbl.jobs.dto.JobReferrerRegistrationRequest;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.entity.ApplicationStatus;
import com.tymbl.jobs.service.JobService;
import com.tymbl.registration.controller.UserController.ErrorResponse;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobmanagement")
@RequiredArgsConstructor
@Tag(name = "Job Management", description = "Job management endpoints that require authentication")
public class JobManagementController {

    private final JobService jobService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

    private boolean isCompanyAllowed(Long userCompanyId, String userCompany, Long targetCompanyId, String targetCompany) {
        boolean idPresent = userCompanyId != null;
        boolean namePresent = userCompany != null && !userCompany.isEmpty();
        if (idPresent && !namePresent) {
            return targetCompanyId != null && targetCompanyId.equals(userCompanyId);
        } else if (!idPresent && namePresent) {
            return targetCompany != null && targetCompany.equalsIgnoreCase(userCompany);
        } else if (idPresent && namePresent) {
            return (targetCompanyId != null && targetCompanyId.equals(userCompanyId)) ||
                   (targetCompany != null && targetCompany.equalsIgnoreCase(userCompany));
        }
        return false;
    }

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
        Long userCompanyId = jwtService.extractCompanyId(token.substring(7));
        String userCompany = jwtService.extractCompany(token.substring(7));
        if (!isCompanyAllowed(userCompanyId, userCompany, request.getCompanyId(), request.getCompany())) {
            return ResponseEntity.status(403).build();
        }
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
        Long userCompanyId = jwtService.extractCompanyId(token.substring(7));
        String userCompany = jwtService.extractCompany(token.substring(7));
        JobResponse job = jobService.getJobById(jobId);
        if (!isCompanyAllowed(userCompanyId, userCompany, job.getCompanyId(), job.getCompany())) {
            return ResponseEntity.status(403).build();
        }
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
        Long userCompanyId = jwtService.extractCompanyId(token.substring(7));
        String userCompany = jwtService.extractCompany(token.substring(7));
        JobResponse job = jobService.getJobById(jobId);
        if (!isCompanyAllowed(userCompanyId, userCompany, job.getCompanyId(), job.getCompany())) {
            return ResponseEntity.status(403).build();
        }
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        jobService.deleteJob(jobId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-posts")
    @Operation(
        summary = "Get all job postings by the current user and jobs where user is a referrer",
        description = "Returns a paginated list of all job postings created by the authenticated user and jobs where the user is registered as a referrer."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved job postings successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<JobResponse>> getMyJobs(
        @RequestHeader("Authorization") String token,
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "10")
        @RequestParam(defaultValue = "10") int size) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        return ResponseEntity.ok(
            jobService.getMyPosts(currentUser, PageRequest.of(page, size)));
    }

    @GetMapping("/company/{companyId}/super-admin-jobs")
    @Operation(
        summary = "Get jobs posted in a company by super admin",
        description = "Returns a list of jobs posted in the specified company by super admin only."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retrieved jobs successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<List<JobResponse>> getJobsByCompanyPostedBySuperAdmin(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long companyId) {
        return ResponseEntity.ok(jobService.getJobsByCompanyPostedBySuperAdmin(companyId));
    }

    @PostMapping("/register-referrer")
    @Operation(
        summary = "Register as a JobReferrer for a job",
        description = "Register yourself as a referrer for a job from your company. You can only register for jobs from the same company as yours."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered as referrer"),
        @ApiResponse(responseCode = "400", description = "Invalid input or already registered"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Different company"),
        @ApiResponse(responseCode = "404", description = "Job not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<Void> registerAsJobReferrer(
            @Valid @RequestBody JobReferrerRegistrationRequest request,
            @RequestHeader("Authorization") String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User currentUser = registrationService.getUserByEmail(email);
        jobService.registerAsJobReferrer(request.getJobId(), currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept/{applicationId}")
    @Operation(
        summary = "Accept referral application as referrer",
        description = "Allows the job poster or any user from the same company to accept a referral application and set its status to SHORTLISTED or REJECTED."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referral application status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or not allowed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized")
    })
    public ResponseEntity<?> acceptReferralAsReferrer(
        @PathVariable Long applicationId,
        @RequestParam ApplicationStatus status,
        @RequestHeader("Authorization") String token) {
        try {
            // Validate status parameter
            if (status != ApplicationStatus.SHORTLISTED && status != ApplicationStatus.REJECTED) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Status must be either SHORTLISTED or REJECTED"));
            }
            
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            jobService.acceptReferralAsReferrer(applicationId, currentUser, status);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{jobId}/details")
    @Operation(
        summary = "Get job details with referrer profiles",
        description = "Get comprehensive job details along with profile information of all users who have registered as referrers for this job. Only accessible by job poster or users from the same company."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Job details with referrer profiles retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = JobDetailsWithReferrersResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"title\": \"Software Engineer\",\n" +
                          "  \"description\": \"We are looking for a talented software engineer...\",\n" +
                          "  \"cityId\": 1,\n" +
                          "  \"cityName\": \"Mountain View\",\n" +
                          "  \"countryId\": 1,\n" +
                          "  \"countryName\": \"United States\",\n" +
                          "  \"designationId\": 1,\n" +
                          "  \"designation\": \"Software Engineer\",\n" +
                          "  \"designationName\": \"Software Engineer\",\n" +
                          "  \"minSalary\": 100000,\n" +
                          "  \"maxSalary\": 150000,\n" +
                          "  \"minExperience\": 2,\n" +
                          "  \"maxExperience\": 5,\n" +
                          "  \"jobType\": \"HYBRID\",\n" +
                          "  \"currencyId\": 1,\n" +
                          "  \"currencyName\": \"US Dollar\",\n" +
                          "  \"currencySymbol\": \"$\",\n" +
                          "  \"companyId\": 1,\n" +
                          "  \"company\": \"Google\",\n" +
                          "  \"companyName\": \"Google\",\n" +
                          "  \"postedBy\": 1,\n" +
                          "  \"active\": true,\n" +
                          "  \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"tags\": [\"Java\", \"Spring\", \"Microservices\"],\n" +
                          "  \"openingCount\": 5,\n" +
                          "  \"uniqueUrl\": \"https://careers.google.com/jobs/123\",\n" +
                          "  \"platform\": \"Google Careers\",\n" +
                          "  \"approved\": 1,\n" +
                          "  \"approvalStatus\": \"APPROVED\",\n" +
                          "  \"referrerCount\": 2,\n" +
                          "  \"referrers\": [\n" +
                          "    {\n" +
                          "      \"userId\": 123,\n" +
                          "      \"userName\": \"Alice Smith\",\n" +
                          "      \"email\": \"alice@google.com\",\n" +
                          "      \"designation\": \"Senior Engineer\",\n" +
                          "      \"company\": \"Google\",\n" +
                          "      \"companyId\": 1,\n" +
                          "      \"companyName\": \"Google\",\n" +
                          "      \"yearsOfExperience\": \"5\",\n" +
                          "      \"monthsOfExperience\": \"6\",\n" +
                          "      \"education\": \"MS Computer Science from Stanford University\",\n" +
                          "      \"portfolioWebsite\": \"https://alice.dev\",\n" +
                          "      \"linkedInProfile\": \"https://linkedin.com/in/alice-smith\",\n" +
                          "      \"githubProfile\": \"https://github.com/alice-smith\",\n" +
                          "      \"numApplicationsAccepted\": 5,\n" +
                          "      \"feedbackScore\": 4.5,\n" +
                          "      \"overallScore\": 7.2,\n" +
                          "      \"registeredAt\": \"2024-01-01T10:00:00\"\n" +
                          "    }\n" +
                          "  ]\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized to view this job"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    public ResponseEntity<JobDetailsWithReferrersResponse> getJobDetailsWithReferrers(
            @Parameter(description = "Job ID", required = true)
            @PathVariable Long jobId,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User currentUser = registrationService.getUserByEmail(email);
            
            // Get job details to check authorization
            JobDetailsWithReferrersResponse jobDetails = jobService.getJobDetailsWithReferrers(jobId);
            
            // Check if user is authorized to view this job (job poster or same company)
            if (!jobDetails.getPostedBy().equals(currentUser.getId()) &&
                (currentUser.getCompanyId() == null || !currentUser.getCompanyId().equals(jobDetails.getCompanyId()))) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(jobDetails);
        } catch (RuntimeException e) {
            throw new RuntimeException("Job not found");
        }
    }
} 