package com.tymbl.common.exception;

import com.tymbl.exception.BadRequestException;
import com.tymbl.exception.ConflictException;
import com.tymbl.exception.EmailAlreadyExistsException;
import com.tymbl.exception.ForbiddenException;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.exception.UnauthorizedException;
import com.tymbl.jobs.exception.CompanyNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  // Add a test method to verify the exception handler is working
  public void testExceptionHandler() {
    logger.info("GlobalExceptionHandler is properly initialized");
  }

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Object> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    logger.error("Registration error: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
    logger.error("Resource not found: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Object> handleUnauthorized(UnauthorizedException ex) {
    logger.error("Unauthorized access: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Object> handleForbidden(ForbiddenException ex) {
    logger.error("Forbidden access: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Object> handleBadRequest(BadRequestException ex) {
    logger.error("Bad request: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Object> handleConflict(ConflictException ex) {
    logger.error("Conflict: {}", ex.getMessage());
    Map<String, Object> error = new HashMap<>();
    error.put("error", ex.getMessage());
    if (ex.getJobId() != null) {
      error.put("jobId", ex.getJobId());
    }
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(CompanyNotFoundException.class)
  public ResponseEntity<Object> handleCompanyNotFound(CompanyNotFoundException ex) {
    logger.error("Company not found: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
    logger.error("Authentication failed: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "Invalid credentials");
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex) {
    logger.error("User not found: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "User not found");
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(OAuth2AuthenticationException.class)
  public ResponseEntity<Object> handleOAuth2Authentication(OAuth2AuthenticationException ex) {
    logger.error("OAuth2 authentication error: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", "OAuth2 authentication failed");
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
    logger.error("Invalid argument: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
    logger.error("Runtime exception: {}", ex.getMessage());
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, Object> response = new HashMap<>();
    Map<String, String> fieldErrors = new HashMap<>();
    Map<String, Object> detailedErrors = new HashMap<>();

    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      String rejectedValue = ((FieldError) error).getRejectedValue() != null ?
          ((FieldError) error).getRejectedValue().toString() : null;

      // Simple field errors map
      fieldErrors.put(fieldName, errorMessage);

      // Detailed error information
      Map<String, Object> fieldDetail = new HashMap<>();
      fieldDetail.put("message", errorMessage);
      fieldDetail.put("rejectedValue", rejectedValue);
      fieldDetail.put("code", error.getCode());
      detailedErrors.put(fieldName, fieldDetail);

      logger.error("Validation error on field '{}': {} (rejected value: {})",
          fieldName, errorMessage, rejectedValue);
    });

    response.put("error", "Validation failed");
    response.put("fieldErrors", fieldErrors);
    response.put("detailedErrors", detailedErrors);
    response.put("totalErrors", fieldErrors.size());

    logger.error("Request validation failed with {} errors: {}", fieldErrors.size(), fieldErrors);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("error", "Invalid request body format");

    // Extract detailed error information
    String detailedMessage = ex.getMessage();
    error.put("message", detailedMessage);

    // Try to extract field information from the error message
    String fieldInfo = extractFieldInfo(detailedMessage);
    if (fieldInfo != null) {
      error.put("field", fieldInfo);
    }

    // Get root cause for additional context
    Throwable rootCause = ex.getRootCause();
    if (rootCause != null) {
      error.put("rootCause", rootCause.getMessage());

      // Try to extract field info from root cause as well
      String rootCauseFieldInfo = extractFieldInfo(rootCause.getMessage());
      if (rootCauseFieldInfo != null && fieldInfo == null) {
        error.put("field", rootCauseFieldInfo);
      }
    }

    logger.error("Request body parsing error: {}", detailedMessage);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  /**
   * Extracts field information from error messages
   */
  private String extractFieldInfo(String message) {
    if (message == null) {
      return null;
    }

    // Common patterns for field extraction
    String[] patterns = {
        "Unrecognized field \"([^\"]+)\"",
        "Field \"([^\"]+)\" is not recognized",
        "Cannot deserialize value of type .* from String \"([^\"]+)\"",
        "Unexpected character .* at line .* column .*",
        "Missing required creator property '([^']+)'",
        "Required property '([^']+)' is missing",
        "Invalid value for ([^:]+):",
        "Cannot construct instance of .* from String value '([^']+)'",
        "Expected BEGIN_OBJECT but was ([^\\s]+)",
        "Expected BEGIN_ARRAY but was ([^\\s]+)",
        "Expected ([^\\s]+) but was ([^\\s]+)"
    };

    for (String pattern : patterns) {
      java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern,
          java.util.regex.Pattern.CASE_INSENSITIVE);
      java.util.regex.Matcher m = p.matcher(message);
      if (m.find()) {
        if (m.groupCount() >= 1) {
          return m.group(1);
        } else {
          return "unknown";
        }
      }
    }

    // If no specific pattern matches, try to extract any quoted field name
    java.util.regex.Pattern fieldPattern = java.util.regex.Pattern.compile(
        "\"([a-zA-Z_][a-zA-Z0-9_]*)\"");
    java.util.regex.Matcher fieldMatcher = fieldPattern.matcher(message);
    if (fieldMatcher.find()) {
      return fieldMatcher.group(1);
    }

    return null;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGlobalException(Exception ex) {
    logger.error("Unhandled exception occurred: ", ex);
    Map<String, Object> error = new HashMap<>();
    error.put("error", "An unexpected error occurred");
    error.put("message", ex.getMessage());
    error.put("type", ex.getClass().getSimpleName());

    // In development, you might want to include stack trace
    if (logger.isDebugEnabled()) {
      error.put("stackTrace", ex.getStackTrace());
    }

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
} 