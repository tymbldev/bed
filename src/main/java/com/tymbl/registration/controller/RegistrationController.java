package com.tymbl.registration.controller;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.registration.dto.RegisterRequest;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(
    origins = "*", 
    allowedHeaders = "*", 
    methods = {
        RequestMethod.GET, 
        RequestMethod.POST, 
        RequestMethod.PUT, 
        RequestMethod.DELETE, 
        RequestMethod.OPTIONS, 
        RequestMethod.PATCH
    }
)
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "User registration endpoints")
public class RegistrationController {

    private static final Logger logger = LoggerFactory.getLogger("com.tymbl.access");
    
    private final RegistrationService registrationService;

    @PostMapping
    @Operation(
        summary = "Register a new user",
        description = "Registers a new user with email and password. Only email and password are mandatory."
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
                        summary = "Complete registration request",
                        value = "{\n" +
                              "  \"email\": \"user@example.com\",\n" +
                              "  \"password\": \"Password123\",\n" +
                              "  \"firstName\": \"John\",\n" +
                              "  \"lastName\": \"Doe\",\n" +
                              "  \"role\": \"USER\",\n" +
                              "  \"phoneNumber\": \"+1234567890\",\n" +
                              "  \"company\": \"Example Corp\",\n" +
                              "  \"designationId\": 1,\n" +
                              "  \"departmentId\": 1,\n" +
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
            @Valid @RequestBody RegisterRequest request) throws Exception {
        logger.info("Received registration request for email: {}", request.getEmail());
        try {
            AuthResponse response = registrationService.registerUserWithToken(request);
            logger.info("Successfully registered user with email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to register user with email: {}. Error: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }
} 