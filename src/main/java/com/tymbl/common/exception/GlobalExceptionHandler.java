package com.tymbl.common.exception;

import com.tymbl.exception.BadRequestException;
import com.tymbl.exception.ConflictException;
import com.tymbl.exception.EmailAlreadyExistsException;
import com.tymbl.exception.ForbiddenException;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.exception.UnauthorizedException;
import com.tymbl.jobs.exception.CompanyNotFoundException;
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

import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
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
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            
            logger.error("Validation error on field '{}': {}", fieldName, errorMessage);
        });
        
        logger.error("Request validation failed with {} errors: {}", errors.size(), errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request body format: " + ex.getMessage();
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null) {
            errorMessage += ". Root cause: " + rootCause.getMessage();
        }
        
        logger.error("Request body parsing error: {}", errorMessage);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid request body format");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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