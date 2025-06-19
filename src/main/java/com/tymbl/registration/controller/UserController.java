package com.tymbl.registration.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.dto.ProfileCompletionResponse;
import com.tymbl.common.dto.ProfileCompletionResponse.PendingField;
import com.tymbl.common.entity.User;
import com.tymbl.exception.EmailAlreadyExistsException;
import com.tymbl.registration.dto.ProfileUpdateRequest;
import com.tymbl.registration.dto.RegisterRequest;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger("com.tymbl");
    
    private final RegistrationService registrationService;
    private final JwtService jwtService;


    /**
     * Update user profile using token
     */
    @PutMapping("/profile")
    @Operation(
        summary = "Update user profile",
        description = "Updates the profile of the authenticated user. All fields are optional. The user's identity is verified using the JWT token."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<Object> updateProfile(
        @RequestBody ProfileUpdateRequest request, 
        @RequestHeader("Authorization") String token
    ) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Updating profile for user: {}", user.getEmail());
            User updatedUser = registrationService.updateUserProfile(user.getId(), request);
            logger.info("Successfully updated profile for user: {}", user.getEmail());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Failed to update profile. Error: {}", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get user profile",
        description = "Retrieves the profile details of the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<Object> getProfile(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Retrieving profile for user: {}", user.getEmail());
            User userProfile = registrationService.getUserById(user.getId());
            logger.info("Successfully retrieved profile for user: {}", user.getEmail());
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException e) {
            logger.error("Failed to get profile. Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Get profile completion status
     */
    @GetMapping("/profile/completion")
    @Operation(
        summary = "Get profile completion status", 
        description = "Returns the user's profile completion percentage and pending fields"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile completion status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<Object> getProfileCompletionStatus(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.debug("Calculating profile completion for user: {}", user.getEmail());
            ProfileCompletionResponse response = calculateProfileCompletion(user);
            logger.debug("Profile completion for user {}: {}%", user.getEmail(), response.getCompletionPercentage());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to get profile completion. Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Helper method to calculate profile completion
     */
    private ProfileCompletionResponse calculateProfileCompletion(User user) {
        ProfileCompletionResponse response = new ProfileCompletionResponse();
        List<PendingField> pendingFields = new ArrayList<>();
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

        // Professional Information (Optional but important)
        totalFields += 3;
        if (user.getDesignationId() == null || user.getDesignationId() == 0) {
            pendingFields.add(createPendingField("designationId", "Job Title", "Add your job title"));
        } else {
            completedFields++;
        }

        if (user.getDepartmentId() == null || user.getDepartmentId() == 0) {
            pendingFields.add(createPendingField("departmentId", "Department", "Add your department"));
        } else {
            completedFields++;
        }

        if (user.getCompany() == null || user.getCompany().trim().isEmpty()) {
            pendingFields.add(createPendingField("company", "Company", "Add your company"));
        } else {
            completedFields++;
        }

        // Location Information (Optional but important)
        totalFields += 3;
        if (user.getCountryId() == null || user.getCountryId() == 0) {
            pendingFields.add(createPendingField("countryId", "Country", "Add your country"));
        } else {
            completedFields++;
        }

        if (user.getCityId() == null || user.getCityId() == 0) {
            pendingFields.add(createPendingField("cityId", "City", "Add your city"));
        } else {
            completedFields++;
        }

        if (user.getZipCode() == null || user.getZipCode().trim().isEmpty()) {
            pendingFields.add(createPendingField("zipCode", "Zip Code", "Add your zip code"));
        } else {
            completedFields++;
        }

        // Experience Information (Optional but important for job matching)
        totalFields += 5;
        if (user.getYearsOfExperience() == null) {
            pendingFields.add(createPendingField("yearsOfExperience", "Years of Experience", "Add your years of experience"));
        } else {
            completedFields++;
        }

        if (user.getMonthsOfExperience() == null) {
            pendingFields.add(createPendingField("monthsOfExperience", "Months of Experience", "Add your months of experience"));
        } else {
            completedFields++;
        }

        if (user.getCurrentSalary() == null) {
            pendingFields.add(createPendingField("currentSalary", "Current Salary", "Add your current salary"));
        } else {
            completedFields++;
        }

        if (user.getExpectedSalary() == null) {
            pendingFields.add(createPendingField("expectedSalary", "Expected Salary", "Add your expected salary"));
        } else {
            completedFields++;
        }

        if (user.getNoticePeriod() == null) {
            pendingFields.add(createPendingField("noticePeriod", "Notice Period", "Add your notice period"));
        } else {
            completedFields++;
        }

        // Skills (Optional but important for job matching)
        totalFields += 1;
        if (user.getSkillIds() == null || user.getSkillIds().isEmpty()) {
            pendingFields.add(createPendingField("skillIds", "Skills", "Add your skills"));
        } else {
            completedFields++;
        }

        // Profile Links (Optional)
        totalFields += 3;
        if (user.getLinkedInProfile() == null || user.getLinkedInProfile().trim().isEmpty()) {
            pendingFields.add(createPendingField("linkedInProfile", "LinkedIn Profile", "Add your LinkedIn profile URL"));
        } else {
            completedFields++;
        }

        if (user.getGithubProfile() == null || user.getGithubProfile().trim().isEmpty()) {
            pendingFields.add(createPendingField("githubProfile", "GitHub Profile", "Add your GitHub profile URL"));
        } else {
            completedFields++;
        }

        if (user.getPortfolioWebsite() == null || user.getPortfolioWebsite().trim().isEmpty()) {
            pendingFields.add(createPendingField("portfolioWebsite", "Portfolio Website", "Add your portfolio website URL"));
        } else {
            completedFields++;
        }

        // Resume (Optional but highly recommended)
        totalFields += 1;
        if (user.getResume() == null || user.getResume().trim().isEmpty()) {
            pendingFields.add(createPendingField("resume", "Resume", "Upload your resume"));
        } else {
            completedFields++;
        }

        // Education (Optional but important)
        totalFields += 1;
        if (user.getEducation() == null || user.getEducation().isEmpty()) {
            pendingFields.add(createPendingField("education", "Education", "Add your education details"));
        } else {
            completedFields++;
        }

        int completionPercentage = (completedFields * 100) / totalFields;
        response.setCompletionPercentage(completionPercentage);
        response.setPendingFields(pendingFields);
        response.setMissingMandatoryFields(missingMandatoryFields);

        return response;
    }

    private PendingField createPendingField(String field, String label, String message) {
        PendingField pendingField = new PendingField();
        pendingField.setFieldName(field);
        pendingField.setFieldLabel(label);
        pendingField.setDescription(message);
        return pendingField;
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
} 