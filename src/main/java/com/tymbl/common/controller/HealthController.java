package com.tymbl.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health check endpoints")
public class HealthController {

    @GetMapping
    @Operation(
        summary = "Basic health check",
        description = "Returns basic health status of the application"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application is healthy",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"status\":\"UP\",\"timestamp\":\"2024-03-20T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/liveness")
    @Operation(
        summary = "Liveness probe",
        description = "Indicates whether the application is alive and running"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application is alive",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"status\":\"CORRECT\",\"timestamp\":\"2024-03-20T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", LivenessState.CORRECT);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/readiness")
    @Operation(
        summary = "Readiness probe",
        description = "Indicates whether the application is ready to handle requests"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Application is ready",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"status\":\"ACCEPTING_TRAFFIC\",\"timestamp\":\"2024-03-20T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", ReadinessState.ACCEPTING_TRAFFIC);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-post")
    @Operation(summary = "Test POST endpoint", description = "Test endpoint to verify POST requests are working")
    public ResponseEntity<Map<String, Object>> testPost(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("request", request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-register")
    @Operation(summary = "Test registration endpoint", description = "Test endpoint to verify registration-like POST requests")
    public ResponseEntity<Map<String, Object>> testRegister(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "REGISTERED");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("email", request.get("email"));
        response.put("token", "test-jwt-token-123456789");
        return ResponseEntity.ok(response);
    }
} 