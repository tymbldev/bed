package com.tymbl.common.controller;

import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Location;
import com.tymbl.common.enums.Degree;
import com.tymbl.common.service.DropdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dropdowns")
@RequiredArgsConstructor
@Tag(name = "Dropdowns", description = "APIs for managing dropdown data like departments, locations, and designations")
public class DropdownController {

    private final DropdownService dropdownService;

    // Department endpoints
    @GetMapping("/departments")
    @Operation(summary = "Get all departments", description = "Returns a list of all departments for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "List of departments retrieved successfully",
            content = @Content(schema = @Schema(implementation = Department.class))
        )
    })
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(dropdownService.getAllDepartments());
    }

    @PostMapping("/departments")
    @Operation(summary = "Create a new department", description = "Creates a new department for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or department already exists")
    })
    public ResponseEntity<Department> createDepartment(@Valid @RequestBody Department department) {
        return ResponseEntity.ok(dropdownService.createDepartment(department));
    }

    // Location endpoints
    @GetMapping("/locations")
    @Operation(summary = "Get all locations", description = "Returns a list of all locations for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "List of locations retrieved successfully",
            content = @Content(schema = @Schema(implementation = Location.class))
        )
    })
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(dropdownService.getAllLocations());
    }

    @PostMapping("/locations")
    @Operation(summary = "Create a new location", description = "Creates a new location for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Location created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        return ResponseEntity.ok(dropdownService.createLocation(location));
    }

    // Designation endpoints
    @GetMapping("/designations")
    @Operation(summary = "Get all designations", description = "Returns a list of all designations/positions for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "List of designations retrieved successfully",
            content = @Content(schema = @Schema(implementation = Designation.class))
        )
    })
    public ResponseEntity<List<Designation>> getAllDesignations() {
        return ResponseEntity.ok(dropdownService.getAllDesignations());
    }

    @PostMapping("/designations")
    @Operation(summary = "Create a new designation", description = "Creates a new designation/position for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Designation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or designation already exists")
    })
    public ResponseEntity<Designation> createDesignation(@Valid @RequestBody Designation designation) {
        return ResponseEntity.ok(dropdownService.createDesignation(designation));
    }

    @GetMapping("/designations-map")
    @Operation(summary = "Get all designations as a map", description = "Returns designations as a map of value/label pairs")
    public ResponseEntity<List<Map<String, String>>> getDesignationsAsMap() {
        List<Map<String, String>> designationMaps = dropdownService.getAllDesignations().stream()
            .map(designation -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", designation.getId().toString());
                map.put("label", designation.getTitle());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(designationMaps);
    }

    @GetMapping("/degrees")
    @Operation(summary = "Get all degrees", description = "Returns all degree types for dropdown selection")
    public ResponseEntity<List<Map<String, String>>> getDegrees() {
        List<Map<String, String>> degrees = Arrays.stream(Degree.values())
            .map(degree -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", degree.name());
                map.put("label", degree.getDisplayName());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(degrees);
    }
} 