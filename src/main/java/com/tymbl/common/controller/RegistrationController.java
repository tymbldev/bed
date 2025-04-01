package com.tymbl.common.controller;

import com.tymbl.common.dto.LoginResponse;
import com.tymbl.common.dto.LinkedInLoginRequest;
import com.tymbl.common.dto.ProfileCompletionResponse;
import com.tymbl.common.entity.User;
import com.tymbl.common.service.LinkedInService;
import com.tymbl.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "APIs for user registration and profile management")
public class RegistrationController {

    private final UserService userService;
    private final LinkedInService linkedInService;

    // ... existing endpoints ...

    @GetMapping("/profile/completion")
    @Operation(summary = "Get profile completion status", description = "Returns the user's profile completion percentage and pending fields")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile completion status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProfileCompletionResponse.class))
        )
    })
    public ResponseEntity<ProfileCompletionResponse> getProfileCompletionStatus(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(calculateProfileCompletion(currentUser));
    }

    @PostMapping("/login/linkedin")
    @Operation(
        summary = "Authenticate user with LinkedIn",
        description = "Authenticates a user using their LinkedIn account and returns a JWT token that can be used for protected API endpoints."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated with LinkedIn",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid LinkedIn access token"
        )
    })
    public ResponseEntity<LoginResponse> loginWithLinkedIn(@Valid @RequestBody LinkedInLoginRequest request) {
        String token = linkedInService.validateAndLogin(request.getAccessToken());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private ProfileCompletionResponse calculateProfileCompletion(User user) {
        ProfileCompletionResponse response = new ProfileCompletionResponse();
        List<ProfileCompletionResponse.PendingField> pendingFields = new ArrayList<>();
        List<String> missingMandatoryFields = new ArrayList<>();
        int totalFields = 0;
        int completedFields = 0;

        // Basic Information (Mandatory)
        totalFields += 4;
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            missingMandatoryFields.add("First Name");
            pendingFields.add(createPendingField("firstName", "First Name", "Your first name is required"));
        } else {
            completedFields++;
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            missingMandatoryFields.add("Last Name");
            pendingFields.add(createPendingField("lastName", "Last Name", "Your last name is required"));
        } else {
            completedFields++;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            missingMandatoryFields.add("Email");
            pendingFields.add(createPendingField("email", "Email", "Your email address is required"));
        } else {
            completedFields++;
        }

        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            missingMandatoryFields.add("Phone Number");
            pendingFields.add(createPendingField("phoneNumber", "Phone Number", "Your phone number is required"));
        } else {
            completedFields++;
        }

        // Professional Information (Mandatory)
        totalFields += 3;
        if (user.getDesignation() == null) {
            missingMandatoryFields.add("Designation");
            pendingFields.add(createPendingField("designation", "Designation", "Your current designation is required"));
        } else {
            completedFields++;
        }

        if (user.getDepartment() == null) {
            missingMandatoryFields.add("Department");
            pendingFields.add(createPendingField("department", "Department", "Your department is required"));
        } else {
            completedFields++;
        }

        if (user.getYearsOfExperience() == null) {
            missingMandatoryFields.add("Years of Experience");
            pendingFields.add(createPendingField("yearsOfExperience", "Years of Experience", "Your years of experience is required"));
        } else {
            completedFields++;
        }

        // Location Information (Mandatory)
        totalFields += 2;
        if (user.getCity() == null) {
            missingMandatoryFields.add("City");
            pendingFields.add(createPendingField("city", "City", "Your city is required"));
        } else {
            completedFields++;
        }

        if (user.getCountry() == null) {
            missingMandatoryFields.add("Country");
            pendingFields.add(createPendingField("country", "Country", "Your country is required"));
        } else {
            completedFields++;
        }

        // Skills (Mandatory)
        totalFields++;
        if (user.getSkills() == null || user.getSkills().isEmpty()) {
            missingMandatoryFields.add("Skills");
            pendingFields.add(createPendingField("skills", "Skills", "At least one skill is required"));
        } else {
            completedFields++;
        }

        // Education (Mandatory)
        totalFields++;
        if (user.getEducation() == null || user.getEducation().isEmpty()) {
            missingMandatoryFields.add("Education");
            pendingFields.add(createPendingField("education", "Education", "At least one education entry is required"));
        } else {
            completedFields++;
        }

        // Optional fields (not counted in mandatory)
        if (user.getLinkedInProfile() == null || user.getLinkedInProfile().trim().isEmpty()) {
            pendingFields.add(createPendingField("linkedInProfile", "LinkedIn Profile", "Add your LinkedIn profile for better visibility"));
        }

        if (user.getPortfolioUrl() == null || user.getPortfolioUrl().trim().isEmpty()) {
            pendingFields.add(createPendingField("portfolioUrl", "Portfolio URL", "Add your portfolio URL to showcase your work"));
        }

        if (user.getResumeUrl() == null || user.getResumeUrl().trim().isEmpty()) {
            pendingFields.add(createPendingField("resumeUrl", "Resume URL", "Upload your resume to apply for jobs"));
        }

        // Calculate completion percentage
        response.setCompletionPercentage((completedFields * 100) / totalFields);
        response.setPendingFields(pendingFields);
        response.setMissingMandatoryFields(missingMandatoryFields);
        response.setCanApply(missingMandatoryFields.isEmpty());

        return response;
    }

    private ProfileCompletionResponse.PendingField createPendingField(String fieldName, String fieldLabel, String description) {
        ProfileCompletionResponse.PendingField field = new ProfileCompletionResponse.PendingField();
        field.setFieldName(fieldName);
        field.setFieldLabel(fieldLabel);
        field.setDescription(description);
        return field;
    }
} 