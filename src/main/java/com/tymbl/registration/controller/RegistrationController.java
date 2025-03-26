package com.tymbl.registration.controller;

import com.tymbl.common.entity.User;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registration")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "User registration endpoints")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @Operation(
        summary = "Register a new user",
        description = "Registers a new user with the given details. After registration, a verification email will be sent to the user's email address."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User registered successfully",
            content = @Content(
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\":1,\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"phoneNumber\":\"+1234567890\",\"company\":\"Example Corp\",\"position\":\"Software Engineer\",\"emailVerified\":false}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input or email already registered"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<User> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User registration details",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "RegisterRequest",
                        summary = "Standard registration request",
                        value = "{\n" +
                              "  \"email\": \"user@example.com\",\n" +
                              "  \"password\": \"Password123\",\n" +
                              "  \"firstName\": \"John\",\n" +
                              "  \"lastName\": \"Doe\",\n" +
                              "  \"role\": \"USER\",\n" +
                              "  \"phoneNumber\": \"+1234567890\",\n" +
                              "  \"company\": \"Example Corp\",\n" +
                              "  \"position\": \"Software Engineer\",\n" +
                              "  \"department\": \"Engineering\",\n" +
                              "  \"location\": \"San Francisco, CA\",\n" +
                              "  \"yearsOfExperience\": 5,\n" +
                              "  \"skills\": [\"Java\", \"Spring Boot\", \"Microservices\"]\n" +
                              "}"
                    )
                )
            )
            @Valid @RequestBody RegisterRequest request) throws Exception {
        return ResponseEntity.ok(registrationService.registerUser(request));
    }
} 