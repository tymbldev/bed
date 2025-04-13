package com.tymbl.common.controller;

import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Location;
import com.tymbl.common.enums.Degree;
import com.tymbl.common.service.DropdownService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

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
            content = @Content(
                schema = @Schema(implementation = Department.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Engineering\",\n" +
                          "    \"description\": \"Software development and engineering\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Product Management\",\n" +
                          "    \"description\": \"Product planning and management\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Human Resources\",\n" +
                          "    \"description\": \"HR and talent management\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(dropdownService.getAllDepartments());
    }

    @PostMapping("/departments")
    @Operation(summary = "Create a new department", description = "Creates a new department for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Department created successfully",
            content = @Content(
                schema = @Schema(implementation = Department.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"Engineering\",\n" +
                          "  \"description\": \"Software development and engineering\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input")
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
            content = @Content(
                schema = @Schema(implementation = Location.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"San Francisco\",\n" +
                          "    \"description\": \"San Francisco Bay Area\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"New York\",\n" +
                          "    \"description\": \"New York City\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Bangalore\",\n" +
                          "    \"description\": \"Bangalore, India\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(dropdownService.getAllLocations());
    }

    @PostMapping("/locations")
    @Operation(summary = "Create a new location", description = "Creates a new location for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Location created successfully",
            content = @Content(
                schema = @Schema(implementation = Location.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"San Francisco\",\n" +
                          "  \"description\": \"San Francisco Bay Area\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        return ResponseEntity.ok(dropdownService.createLocation(location));
    }

    // Designation endpoints
    @GetMapping("/designations")
    @Operation(summary = "Get all designations", description = "Returns a list of all designations for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of designations retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Designation.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Software Engineer\",\n" +
                          "    \"description\": \"Entry-level software development position\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Senior Software Engineer\",\n" +
                          "    \"description\": \"Experienced software development position\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Technical Lead\",\n" +
                          "    \"description\": \"Team leadership position\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Designation>> getAllDesignations() {
        return ResponseEntity.ok(dropdownService.getAllDesignations());
    }

    @PostMapping("/designations")
    @Operation(summary = "Create a new designation", description = "Creates a new designation for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Designation created successfully",
            content = @Content(
                schema = @Schema(implementation = Designation.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"Software Engineer\",\n" +
                          "  \"description\": \"Entry-level software development position\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Designation> createDesignation(@Valid @RequestBody Designation designation) {
        return ResponseEntity.ok(dropdownService.createDesignation(designation));
    }

    @GetMapping("/designations-map")
    @Operation(summary = "Get all designations as a map", description = "Returns designations as a map of value/label pairs")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Designations map retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"value\": \"1\",\n" +
                          "    \"label\": \"Software Engineer\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"2\",\n" +
                          "    \"label\": \"Senior Software Engineer\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"3\",\n" +
                          "    \"label\": \"Tech Lead\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"4\",\n" +
                          "    \"label\": \"Product Manager\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"5\",\n" +
                          "    \"label\": \"UI/UX Designer\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
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
    @Operation(summary = "Get all degree types", description = "Returns a list of all available degree types")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of degree types retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"BACHELOR\": \"Bachelor's Degree\",\n" +
                          "  \"MASTER\": \"Master's Degree\",\n" +
                          "  \"DOCTORATE\": \"Doctorate Degree\",\n" +
                          "  \"ASSOCIATE\": \"Associate Degree\",\n" +
                          "  \"DIPLOMA\": \"Diploma\"\n" +
                          "}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> getAllDegrees() {
        Map<String, String> degreeMap = Arrays.stream(Degree.values())
            .collect(Collectors.toMap(
                Degree::name,
                degree -> degree.name().charAt(0) + degree.name().substring(1).toLowerCase().replace("_", " ")
            ));
        return ResponseEntity.ok(degreeMap);
    }
} 