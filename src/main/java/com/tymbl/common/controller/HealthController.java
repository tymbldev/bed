package com.tymbl.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"status\": \"UP\",\n" +
                          "  \"timestamp\": \"2024-03-20T10:30:00\"\n" +
                          "}"
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
        description = "Returns the liveness state of the application"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Liveness state retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"status\": \"CORRECT\",\n" +
                          "  \"timestamp\": \"2024-03-20T10:30:00\"\n" +
                          "}"
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
        description = "Returns the readiness state of the application"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Readiness state retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"status\": \"ACCEPTING_TRAFFIC\",\n" +
                          "  \"timestamp\": \"2024-03-20T10:30:00\"\n" +
                          "}"
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

    @PostMapping("/state")
    @Operation(
        summary = "Update application state",
        description = "Updates the application state (for testing purposes)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "State updated successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"status\": \"BROKEN\",\n" +
                          "  \"timestamp\": \"2024-03-20T10:30:00\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid state provided")
    })
    public ResponseEntity<Map<String, Object>> updateState(@RequestBody Map<String, String> request) {
        String state = request.get("state");
        Map<String, Object> response = new HashMap<>();
        response.put("status", state);
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
} 