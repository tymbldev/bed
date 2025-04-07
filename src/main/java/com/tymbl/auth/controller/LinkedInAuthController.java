package com.tymbl.auth.controller;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.registration.dto.LinkedInRegisterRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/linkedin")
@RequiredArgsConstructor
@Tag(name = "LinkedIn Authentication", description = "LinkedIn authentication endpoints")
public class LinkedInAuthController {

    private static final Logger logger = LoggerFactory.getLogger("com.tymbl.access");
    
    private final RegistrationService registrationService;

    @PostMapping
    @Operation(
        summary = "Authenticate with LinkedIn",
        description = "Authenticates a user using their LinkedIn profile data. The frontend should provide the LinkedIn access token obtained from the LinkedIn OAuth flow."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User authenticated successfully with LinkedIn",
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
    public ResponseEntity<AuthResponse> authenticateWithLinkedIn(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "LinkedIn authentication details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "LinkedInRegisterRequest",
                        summary = "LinkedIn authentication request",
                        value = "{\n" +
                              "  \"accessToken\": \"AQV...\",\n" +
                              "  \"linkedInProfileUrl\": \"https://www.linkedin.com/in/johndoe\"\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody LinkedInRegisterRequest request) throws Exception {
        logger.info("Received LinkedIn authentication request");
        try {
            AuthResponse response = registrationService.registerWithLinkedIn(request);
            logger.info("Successfully authenticated user with LinkedIn");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to authenticate user with LinkedIn. Error: {}", e.getMessage());
            throw e;
        }
    }
} 