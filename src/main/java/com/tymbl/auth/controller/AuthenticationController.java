package com.tymbl.auth.controller;

import com.tymbl.auth.dto.AuthResponse;
import com.tymbl.auth.dto.LoginRequest;
import com.tymbl.auth.service.impl.AuthenticationService;
import com.tymbl.common.dto.LinkedInLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate user and return JWT token",
      description = "Authenticates a user with their email and password, and returns a JWT token that can be used for protected API endpoints."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Authentication successful",
          content = @Content(
              schema = @Schema(implementation = AuthResponse.class),
              examples = @ExampleObject(
                  value = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"emailVerified\":true}"
              )
          )
      ),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "400", description = "Invalid input")
  })
  public ResponseEntity<AuthResponse> login(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Login credentials",
          required = true,
          content = @Content(
              examples = @ExampleObject(
                  name = "LoginRequest",
                  summary = "Standard login request",
                  value = "{\"email\":\"user@example.com\",\"password\":\"Password123\"}"
              )
          )
      )
      @Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authenticationService.login(request));
  }

  @GetMapping("/verify-email")
  @Operation(
      summary = "Verify user's email address",
      description = "Verifies a user's email address using the token sent to their email."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Email verified successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid token or email already verified"),
      @ApiResponse(responseCode = "404", description = "Token not found")
  })
  public ResponseEntity<String> verifyEmail(
      @Parameter(description = "Email verification token", required = true)
      @RequestParam String token) {
    authenticationService.verifyEmailToken(token);
    return ResponseEntity.ok("Email verified successfully");
  }

  @PostMapping("/forgot-password")
  @Operation(
      summary = "Request password reset",
      description = "Initiates the password reset process by sending a reset token to the user's email."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password reset instructions sent"),
      @ApiResponse(responseCode = "404", description = "Email not found")
  })
  public ResponseEntity<String> forgotPassword(
      @Parameter(description = "User's email address", required = true)
      @RequestParam String email) throws Exception {
    authenticationService.initiatePasswordReset(email);
    return ResponseEntity.ok("Password reset instructions sent to your email");
  }

  @PostMapping("/reset-password")
  @Operation(
      summary = "Reset password using token",
      description = "Resets a user's password using the token sent to their email."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password reset successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
      @ApiResponse(responseCode = "404", description = "Token not found")
  })
  public ResponseEntity<String> resetPassword(
      @Parameter(description = "Password reset token", required = true)
      @RequestParam String token,
      @Parameter(description = "New password", required = true)
      @RequestParam String newPassword) {
    authenticationService.resetPassword(token, newPassword);
    return ResponseEntity.ok("Password reset successfully");
  }

  @PostMapping("/resend-verification")
  @Operation(
      summary = "Resend email verification",
      description = "Resends the email verification token to the user's email."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
      @ApiResponse(responseCode = "400", description = "Email already verified"),
      @ApiResponse(responseCode = "404", description = "Email not found")
  })
  public ResponseEntity<String> resendVerificationEmail(
      @Parameter(description = "User's email address", required = true)
      @RequestParam String email)
      throws Exception {
    authenticationService.resendVerificationEmail(email);
    return ResponseEntity.ok("Verification email sent successfully");
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
          content = @Content(
              schema = @Schema(implementation = AuthResponse.class),
              examples = @ExampleObject(
                  value = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"email\":\"user@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"USER\",\"emailVerified\":true}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid LinkedIn access token")
  })
  public ResponseEntity<AuthResponse> loginWithLinkedIn(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "LinkedIn login credentials",
          required = true,
          content = @Content(
              examples = @ExampleObject(
                  name = "LinkedInLoginRequest",
                  summary = "LinkedIn login request",
                  value = "{\n" +
                      "  \"accessToken\": \"AQV...\"\n" +
                      "}"
              )
          )
      )
      @Valid @RequestBody LinkedInLoginRequest request) {
    return ResponseEntity.ok(authenticationService.loginWithLinkedIn(request.getAccessToken()));
  }
} 