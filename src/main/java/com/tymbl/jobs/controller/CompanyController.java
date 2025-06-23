package com.tymbl.jobs.controller;

import com.tymbl.jobs.dto.CompanyRequest;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.exception.CompanyNotFoundException;
import com.tymbl.jobs.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company management endpoints")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @Operation(
        summary = "Create or update a company",
        description = "Creates a new company or updates an existing one if the company name already exists."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company created/updated successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"Example Corp\",\n" +
                          "  \"description\": \"A leading technology company\",\n" +
                          "  \"website\": \"https://example.com\",\n" +
                          "  \"logoUrl\": \"https://logo.example.com\",\n" +
                          "  \"aboutUs\": \"About Example Corp\",\n" +
                          "  \"vision\": \"To innovate\",\n" +
                          "  \"mission\": \"Empower people\",\n" +
                          "  \"culture\": \"Inclusive\",\n" +
                          "  \"createdAt\": \"2024-06-01T12:00:00\",\n" +
                          "  \"updatedAt\": \"2024-06-01T12:00:00\",\n" +
                          "  \"jobs\": [\n" +
                          "    {\n" +
                          "      \"id\": 101,\n" +
                          "      \"title\": \"Software Engineer\",\n" +
                          "      \"description\": \"Develop software\",\n" +
                          "      \"cityId\": 1,\n" +
                          "      \"countryId\": 1,\n" +
                          "      \"designationId\": 1,\n" +
                          "      \"designation\": \"Engineer\",\n" +
                          "      \"salary\": 100000,\n" +
                          "      \"currencyId\": 1,\n" +
                          "      \"companyId\": 1,\n" +
                          "      \"company\": \"Example Corp\",\n" +
                          "      \"postedBy\": 1,\n" +
                          "      \"active\": true,\n" +
                          "      \"createdAt\": \"2024-06-01T12:00:00\",\n" +
                          "      \"updatedAt\": \"2024-06-01T12:00:00\",\n" +
                          "      \"tags\": [\"java\", \"spring\"]\n" +
                          "    }\n" +
                          "  ]\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<CompanyResponse> createOrUpdateCompany(@RequestBody CompanyRequest request) {
        try {
            return ResponseEntity.ok(companyService.createOrUpdateCompany(request));
        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating/updating company: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(
        summary = "Get all companies",
        description = "Returns a paginated list of all registered companies, each with their jobs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Paginated list of companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"content\": [\n" +
                          "    {\n" +
                          "      \"id\": 1,\n" +
                          "      \"name\": \"Example Corp\",\n" +
                          "      \"description\": \"A leading technology company\",\n" +
                          "      \"website\": \"https://example.com\",\n" +
                          "      \"logoUrl\": \"https://logo.example.com\",\n" +
                          "      \"aboutUs\": \"About Example Corp\",\n" +
                          "      \"vision\": \"To innovate\",\n" +
                          "      \"mission\": \"Empower people\",\n" +
                          "      \"culture\": \"Inclusive\",\n" +
                          "      \"createdAt\": \"2024-06-01T12:00:00\",\n" +
                          "      \"updatedAt\": \"2024-06-01T12:00:00\",\n" +
                          "      \"jobs\": [\n" +
                          "        {\n" +
                          "          \"id\": 101,\n" +
                          "          \"title\": \"Software Engineer\",\n" +
                          "          \"description\": \"Develop software\",\n" +
                          "          \"cityId\": 1,\n" +
                          "          \"countryId\": 1,\n" +
                          "          \"designationId\": 1,\n" +
                          "          \"designation\": \"Engineer\",\n" +
                          "          \"salary\": 100000,\n" +
                          "          \"currencyId\": 1,\n" +
                          "          \"companyId\": 1,\n" +
                          "          \"company\": \"Example Corp\",\n" +
                          "          \"postedBy\": 1,\n" +
                          "          \"active\": true,\n" +
                          "          \"createdAt\": \"2024-06-01T12:00:00\",\n" +
                          "          \"updatedAt\": \"2024-06-01T12:00:00\",\n" +
                          "          \"tags\": [\"java\", \"spring\"]\n" +
                          "        }\n" +
                          "      ]\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"pageable\": {\n" +
                          "    \"pageNumber\": 0,\n" +
                          "    \"pageSize\": 100\n" +
                          "  },\n" +
                          "  \"totalElements\": 1,\n" +
                          "  \"totalPages\": 1,\n" +
                          "  \"last\": true\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "No companies found")
    })
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(companyService.getAllCompanies(pageable));
        } catch (CompanyNotFoundException e) {
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get company by ID",
        description = "Returns the details of a specific company by its ID, including all jobs."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(companyService.getCompanyById(id));
        } catch (CompanyNotFoundException e) {
            throw e;
        }
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFoundException(CompanyNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
} 