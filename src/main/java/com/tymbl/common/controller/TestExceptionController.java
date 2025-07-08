package com.tymbl.common.controller;

import com.tymbl.exception.BadRequestException;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.jobs.exception.CompanyNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-exceptions")
@Tag(name = "Test Exceptions", description = "Test endpoints to verify exception handling")
public class TestExceptionController {

    @GetMapping("/bad-request")
    @Operation(summary = "Test BadRequestException", description = "Throws a BadRequestException to test exception handling")
    public ResponseEntity<String> testBadRequest(@RequestParam String message) {
        throw new BadRequestException(message != null ? message : "Test bad request exception");
    }

    @GetMapping("/not-found")
    @Operation(summary = "Test ResourceNotFoundException", description = "Throws a ResourceNotFoundException to test exception handling")
    public ResponseEntity<String> testNotFound(@RequestParam String message) {
        throw new ResourceNotFoundException(message != null ? message : "Test resource not found exception");
    }

    @GetMapping("/company-not-found")
    @Operation(summary = "Test CompanyNotFoundException", description = "Throws a CompanyNotFoundException to test exception handling")
    public ResponseEntity<String> testCompanyNotFound(@RequestParam Long id) {
        throw new CompanyNotFoundException(id != null ? id : 1L);
    }

    @GetMapping("/runtime-exception")
    @Operation(summary = "Test RuntimeException", description = "Throws a RuntimeException to test exception handling")
    public ResponseEntity<String> testRuntimeException(@RequestParam String message) {
        throw new RuntimeException(message != null ? message : "Test runtime exception");
    }

    @GetMapping("/illegal-argument")
    @Operation(summary = "Test IllegalArgumentException", description = "Throws an IllegalArgumentException to test exception handling")
    public ResponseEntity<String> testIllegalArgumentException(@RequestParam String message) {
        throw new IllegalArgumentException(message != null ? message : "Test illegal argument exception");
    }
} 