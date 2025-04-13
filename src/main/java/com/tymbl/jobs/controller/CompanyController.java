package com.tymbl.jobs.controller;

import com.tymbl.jobs.dto.CompanyRequest;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                          "  \"industry\": \"Technology\",\n" +
                          "  \"size\": \"1000-5000\",\n" +
                          "  \"foundedYear\": 2010,\n" +
                          "  \"headquarters\": \"San Francisco, CA\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<CompanyResponse> createOrUpdateCompany(@RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.createOrUpdateCompany(request));
    }

    @GetMapping
    @Operation(
        summary = "Get all companies",
        description = "Returns a list of all registered companies."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Example Corp\",\n" +
                          "    \"description\": \"A leading technology company\",\n" +
                          "    \"website\": \"https://example.com\",\n" +
                          "    \"industry\": \"Technology\",\n" +
                          "    \"size\": \"1000-5000\",\n" +
                          "    \"foundedYear\": 2010,\n" +
                          "    \"headquarters\": \"San Francisco, CA\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Tech Solutions Inc\",\n" +
                          "    \"description\": \"Innovative software solutions\",\n" +
                          "    \"website\": \"https://techsolutions.com\",\n" +
                          "    \"industry\": \"Software\",\n" +
                          "    \"size\": \"500-1000\",\n" +
                          "    \"foundedYear\": 2015,\n" +
                          "    \"headquarters\": \"New York, NY\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get company by ID",
        description = "Returns the details of a specific company by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Company retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"Example Corp\",\n" +
                          "  \"description\": \"A leading technology company\",\n" +
                          "  \"website\": \"https://example.com\",\n" +
                          "  \"industry\": \"Technology\",\n" +
                          "  \"size\": \"1000-5000\",\n" +
                          "  \"foundedYear\": 2010,\n" +
                          "  \"headquarters\": \"San Francisco, CA\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }
} 