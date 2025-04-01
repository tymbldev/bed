package com.tymbl.registration.controller;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.common.dto.ProfileCompletionResponse;
import com.tymbl.common.dto.ProfileCompletionResponse.PendingField;
import com.tymbl.common.entity.User;
import com.tymbl.registration.dto.LinkedInRegisterRequest;
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
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "User registration and profile management endpoints")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @Operation(
        summary = "Register a new user",
        description = "Registers a new user with email and password. Only email and password are mandatory. After registration, a verification email will be sent to the user's email address."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User registered successfully",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9...\",\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"emailVerified\":false}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Email already registered"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "RegisterRequest",
                        summary = "Minimal registration request",
                        value = "{\n" +
                              "  \"email\": \"user@example.com\",\n" +
                              "  \"password\": \"Password123\"\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody RegisterRequest request) throws Exception {
        return ResponseEntity.ok(registrationService.registerUserWithToken(request));
    }
    
    @PutMapping("/profile")
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
                    value = "{\"id\":1,\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"phoneNumber\":\"+1234567890\",\"company\":\"Example Corp\",\"position\":\"Software Engineer\",\"emailVerified\":true}"
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
                        summary = "Standard profile update request",
                        value = "{\n" +
                              "  \"firstName\": \"John\",\n" +
                              "  \"lastName\": \"Doe\",\n" +
                              "  \"phoneNumber\": \"+1234567890\",\n" +
                              "  \"company\": \"Example Corp\",\n" +
                              "  \"designationId\": 1,\n" +
                              "  \"departmentId\": 1,\n" +
                              "  \"cityId\": 1,\n" +
                              "  \"countryId\": 1,\n" +
                              "  \"yearsOfExperience\": 5,\n" +
                              "  \"skills\": [\"Java\", \"Spring Boot\", \"Microservices\"]\n" +
                              "}"
                    )
                )
            )
            @RequestBody ProfileUpdateRequest request,
            @AuthenticationPrincipal User currentUser) throws Exception {
        return ResponseEntity.ok(registrationService.updateUserProfile(currentUser.getId(), request));
    }

    @PostMapping("/linkedin")
    @Operation(
        summary = "Register a new user with LinkedIn",
        description = "Registers a new user using their LinkedIn profile data. The frontend should provide the LinkedIn access token obtained from the LinkedIn OAuth flow."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User registered successfully with LinkedIn",
            content = @Content(
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = "{\"token\":\"eyJhbGciOiJIUzI1NiJ9...\",\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"emailVerified\":false}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already registered"),
        @ApiResponse(responseCode = "401", description = "Invalid LinkedIn access token"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<AuthResponse> registerWithLinkedIn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "LinkedIn registration details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "LinkedInRegisterRequest",
                        summary = "LinkedIn registration request",
                        value = "{\n" +
                              "  \"accessToken\": \"AQV...\",\n" +
                              "  \"linkedInProfileUrl\": \"https://www.linkedin.com/in/johndoe\"\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody LinkedInRegisterRequest request) throws Exception {
        return ResponseEntity.ok(registrationService.registerWithLinkedIn(request));
    }

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