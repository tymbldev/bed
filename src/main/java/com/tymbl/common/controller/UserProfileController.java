package com.tymbl.common.controller;

import com.tymbl.common.dto.ProfileCompletionResponse;
import com.tymbl.common.dto.ProfileCompletionResponse.PendingField;
import com.tymbl.common.entity.User;
import com.tymbl.registration.dto.ProfileUpdateRequest;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Authenticated user profile management endpoints")
public class UserProfileController {

  private static final Logger logger = LoggerFactory.getLogger("com.tymbl");

  private final RegistrationService registrationService;

  @PutMapping
  @Operation(
      summary = "Update user profile",
      description = "Updates the profile of the authenticated user. All fields are optional. The user's identity is verified using the JWT token."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Profile updated successfully",
          content = @Content(
              schema = @Schema(implementation = User.class),
              examples = @ExampleObject(
                  name = "ProfileUpdateRequest",
                  summary = "Complete profile update request",
                  value = "{\n" +
                      "  \"firstName\": \"John\",\n" +
                      "  \"lastName\": \"Doe\",\n" +
                      "  \"role\": \"USER\",\n" +
                      "  \"phoneNumber\": \"+1234567890\",\n" +
                      "  \"company\": \"Example Corp\",\n" +
                      "  \"departmentId\": 1,\n" +
                      "  \"designationId\": 1,\n" +
                      "  \"cityId\": 1,\n" +
                      "  \"countryId\": 1,\n" +
                      "  \"zipCode\": \"10001\",\n" +
                      "  \"linkedInProfile\": \"https://linkedin.com/in/johndoe\",\n" +
                      "  \"githubProfile\": \"https://github.com/johndoe\",\n" +
                      "  \"portfolioWebsite\": \"https://johndoe.com\",\n" +
                      "  \"resume\": \"base64_encoded_resume\",\n" +
                      "  \"yearsOfExperience\": 5,\n" +
                      "  \"monthsOfExperience\": 6,\n" +
                      "  \"currentSalary\": 100000,\n" +
                      "  \"expectedSalary\": 120000,\n" +
                      "  \"noticePeriod\": 30,\n" +
                      "  \"skillIds\": [1, 2, 3],\n" +
                      "  \"education\": [\n" +
                      "    {\n" +
                      "      \"degree\": \"Bachelor of Science\",\n" +
                      "      \"fieldOfStudy\": \"Computer Science\",\n" +
                      "      \"institution\": \"Example University\",\n" +
                      "      \"startYear\": 2015,\n" +
                      "      \"endYear\": 2019,\n" +
                      "      \"grade\": \"3.8\"\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
      @ApiResponse(responseCode = "500", description = "Server error")
  })
  public ResponseEntity<User> updateProfile(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User profile update details. All fields are optional. User identity is verified using the JWT token.",
          required = true,
          content = @Content(
              examples = @ExampleObject(
                  name = "ProfileUpdateRequest",
                  summary = "Complete profile update request",
                  value = "{\n" +
                      "  \"firstName\": \"John\",\n" +
                      "  \"lastName\": \"Doe\",\n" +
                      "  \"role\": \"USER\",\n" +
                      "  \"phoneNumber\": \"+1234567890\",\n" +
                      "  \"company\": \"Example Corp\",\n" +
                      "  \"departmentId\": 1,\n" +
                      "  \"designationId\": 1,\n" +
                      "  \"cityId\": 1,\n" +
                      "  \"countryId\": 1,\n" +
                      "  \"zipCode\": \"10001\",\n" +
                      "  \"linkedInProfile\": \"https://linkedin.com/in/johndoe\",\n" +
                      "  \"githubProfile\": \"https://github.com/johndoe\",\n" +
                      "  \"portfolioWebsite\": \"https://johndoe.com\",\n" +
                      "  \"resume\": \"base64_encoded_resume\",\n" +
                      "  \"yearsOfExperience\": 5,\n" +
                      "  \"monthsOfExperience\": 6,\n" +
                      "  \"currentSalary\": 100000,\n" +
                      "  \"expectedSalary\": 120000,\n" +
                      "  \"noticePeriod\": 30,\n" +
                      "  \"skillIds\": [1, 2, 3],\n" +
                      "  \"education\": [\n" +
                      "    {\n" +
                      "      \"degree\": \"Bachelor of Science\",\n" +
                      "      \"fieldOfStudy\": \"Computer Science\",\n" +
                      "      \"institution\": \"Example University\",\n" +
                      "      \"startYear\": 2015,\n" +
                      "      \"endYear\": 2019,\n" +
                      "      \"grade\": \"3.8\"\n" +
                      "    }\n" +
                      "  ]\n" +
                      "}"
              )
          )
      )
      @RequestBody ProfileUpdateRequest request,
      @AuthenticationPrincipal User currentUser) throws Exception {
    logger.info("Updating profile for user: {}", currentUser.getEmail());
    try {
      User updatedUser = registrationService.updateUserProfile(currentUser.getId(), request);
      logger.info("Successfully updated profile for user: {}", currentUser.getEmail());
      return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
      logger.error("Failed to update profile for user: {}. Error: {}", currentUser.getEmail(),
          e.getMessage());
      throw e;
    }
  }

  @GetMapping("/completion")
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
    logger.debug("Calculating profile completion for user: {}", currentUser.getEmail());
    ProfileCompletionResponse response = calculateProfileCompletion(currentUser);
    logger.debug("Profile completion for user {}: {}%", currentUser.getEmail(),
        response.getCompletionPercentage());
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(
      summary = "Get user profile",
      description = "Retrieves the profile details of the authenticated user."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Profile retrieved successfully",
          content = @Content(schema = @Schema(implementation = User.class))
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
      @ApiResponse(responseCode = "500", description = "Server error")
  })
  public ResponseEntity<User> getProfile(@AuthenticationPrincipal User currentUser) {
    logger.info("Retrieving profile for user: {}", currentUser.getEmail());
    try {
      User user = registrationService.getUserById(currentUser.getId());
      logger.info("Successfully retrieved profile for user: {}", currentUser.getEmail());
      return ResponseEntity.ok(user);
    } catch (Exception e) {
      logger.error("Failed to retrieve profile for user: {}. Error: {}", currentUser.getEmail(), e.getMessage());
      throw e;
    }
  }

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
      pendingFields.add(
          createPendingField("firstName", "First Name", "Your first name is required"));
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
      pendingFields.add(
          createPendingField("phoneNumber", "Phone Number", "Your phone number is required"));
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

    // Location Information (Optional)
    totalFields += 2;
    if (user.getCityId() == null || user.getCityId() == 0) {
      pendingFields.add(createPendingField("cityId", "City", "Add your city"));
    } else {
      completedFields++;
    }

    if (user.getCountryId() == null || user.getCountryId() == 0) {
      pendingFields.add(createPendingField("countryId", "Country", "Add your country"));
    } else {
      completedFields++;
    }

    // Calculate percentage
    float percentage = (totalFields > 0) ? ((float) completedFields / totalFields) * 100 : 0;

    response.setCompletionPercentage(Math.round(percentage));
    response.setPendingFields(pendingFields);
    response.setMissingMandatoryFields(missingMandatoryFields);
    response.setCanApply(missingMandatoryFields.isEmpty());

    return response;
  }

  private PendingField createPendingField(String field, String label, String message) {
    PendingField pendingField = new PendingField();
    pendingField.setFieldName(field);
    pendingField.setFieldLabel(label);
    pendingField.setDescription(message);
    return pendingField;
  }
} 