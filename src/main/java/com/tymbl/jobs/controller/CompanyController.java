package com.tymbl.jobs.controller;

import com.tymbl.common.dto.IndustryWiseCompaniesDTO;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
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

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
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
@Tag(name = "Companies", description = "APIs for managing companies")
public class CompanyController {

    private final CompanyService companyService;
    private final DropdownService dropdownService;

    @PostMapping
    @Operation(summary = "Create or update a company", description = "Creates a new company or updates an existing one")
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
                          "  \"description\": \"A technology company\",\n" +
                          "  \"website\": \"https://example.com\",\n" +
                          "  \"logoUrl\": \"https://example.com/logo.png\",\n" +
                          "  \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"aboutUs\": \"About Example Corp\",\n" +
                          "  \"vision\": \"To innovate\",\n" +
                          "  \"mission\": \"Empower developers\",\n" +
                          "  \"culture\": \"Open and inclusive\",\n" +
                          "  \"jobs\": [],\n" +
                          "  \"careerPageUrl\": \"https://example.com/careers\",\n" +
                          "  \"linkedinUrl\": \"https://linkedin.com/company/example-corp\",\n" +
                          "  \"headquarters\": \"San Francisco, CA\",\n" +
                          "  \"primaryIndustryId\": 10,\n" +
                          "  \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                          "  \"secondaryIndustries\": \"Software,Cloud\",\n" +
                          "  \"companySize\": \"1000+\",\n" +
                          "  \"specialties\": \"AI,ML,Cloud\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Company not found")
    })
    public ResponseEntity<CompanyResponse> createOrUpdateCompany(@Valid @RequestBody CompanyRequest request) {
        try {
            return ResponseEntity.ok(companyService.createOrUpdateCompany(request));
        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error creating/updating company: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Returns a paginated list of all companies")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class)
            )
        )
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
    @Operation(summary = "Get company by ID", description = "Returns a company by its ID")
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
                          "  \"description\": \"A technology company\",\n" +
                          "  \"website\": \"https://example.com\",\n" +
                          "  \"logoUrl\": \"https://example.com/logo.png\",\n" +
                          "  \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "  \"aboutUs\": \"About Example Corp\",\n" +
                          "  \"vision\": \"To innovate\",\n" +
                          "  \"mission\": \"Empower developers\",\n" +
                          "  \"culture\": \"Open and inclusive\",\n" +
                          "  \"jobs\": [],\n" +
                          "  \"careerPageUrl\": \"https://example.com/careers\",\n" +
                          "  \"linkedinUrl\": \"https://linkedin.com/company/example-corp\",\n" +
                          "  \"headquarters\": \"San Francisco, CA\",\n" +
                          "  \"primaryIndustryId\": 10,\n" +
                          "  \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                          "  \"secondaryIndustries\": \"Software,Cloud\",\n" +
                          "  \"companySize\": \"1000+\",\n" +
                          "  \"specialties\": \"AI,ML,Cloud\"\n" +
                          "}"
                )
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

    @GetMapping("/industry-wise-companies")
    @Operation(summary = "Get industry-wise companies with statistics", description = "Returns all industries with company counts and top 5 companies in each industry based on active job count")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Industry-wise companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = IndustryWiseCompaniesDTO.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"industryId\": 1,\n" +
                          "    \"industryName\": \"Information Technology & Services\",\n" +
                          "    \"industryDescription\": \"Technology and IT services industry\",\n" +
                          "    \"companyCount\": 25,\n" +
                          "    \"topCompanies\": [\n" +
                          "      {\n" +
                          "        \"companyId\": 1,\n" +
                          "        \"companyName\": \"Google\",\n" +
                          "        \"logoUrl\": \"https://example.com/google-logo.png\",\n" +
                          "        \"website\": \"https://google.com\",\n" +
                          "        \"headquarters\": \"Mountain View, CA\",\n" +
                          "        \"activeJobCount\": 15\n" +
                          "      },\n" +
                          "      {\n" +
                          "        \"companyId\": 2,\n" +
                          "        \"companyName\": \"Microsoft\",\n" +
                          "        \"logoUrl\": \"https://example.com/microsoft-logo.png\",\n" +
                          "        \"website\": \"https://microsoft.com\",\n" +
                          "        \"headquarters\": \"Redmond, WA\",\n" +
                          "        \"activeJobCount\": 12\n" +
                          "      }\n" +
                          "    ]\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"industryId\": 2,\n" +
                          "    \"industryName\": \"Financial Services\",\n" +
                          "    \"industryDescription\": \"Banking, insurance, and financial services\",\n" +
                          "    \"companyCount\": 15,\n" +
                          "    \"topCompanies\": [\n" +
                          "      {\n" +
                          "        \"companyId\": 3,\n" +
                          "        \"companyName\": \"Goldman Sachs\",\n" +
                          "        \"logoUrl\": \"https://example.com/goldman-logo.png\",\n" +
                          "        \"website\": \"https://goldmansachs.com\",\n" +
                          "        \"headquarters\": \"New York, NY\",\n" +
                          "        \"activeJobCount\": 8\n" +
                          "      }\n" +
                          "    ]\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<IndustryWiseCompaniesDTO>> getIndustryWiseCompanies() {
        return ResponseEntity.ok(dropdownService.getIndustryStatistics());
    }

    @GetMapping("/by-industry/{primaryIndustryId}")
    @Operation(summary = "Get companies by primary industry ID", description = "Returns a paginated list of companies filtered by primary industry ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = CompanyResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"content\": [\n" +
                          "    {\n" +
                          "      \"id\": 1,\n" +
                          "      \"name\": \"Google\",\n" +
                          "      \"description\": \"A technology company\",\n" +
                          "      \"website\": \"https://google.com\",\n" +
                          "      \"logoUrl\": \"https://google.com/logo.png\",\n" +
                          "      \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"aboutUs\": \"About Google\",\n" +
                          "      \"vision\": \"To organize the world's information\",\n" +
                          "      \"mission\": \"Make information universally accessible\",\n" +
                          "      \"culture\": \"Innovative and inclusive\",\n" +
                          "      \"jobs\": [],\n" +
                          "      \"careerPageUrl\": \"https://careers.google.com\",\n" +
                          "      \"linkedinUrl\": \"https://linkedin.com/company/google\",\n" +
                          "      \"headquarters\": \"Mountain View, CA\",\n" +
                          "      \"primaryIndustryId\": 1,\n" +
                          "      \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                          "      \"secondaryIndustries\": \"Software,Cloud\",\n" +
                          "      \"companySize\": \"100000+\",\n" +
                          "      \"specialties\": \"AI,ML,Search\"\n" +
                          "    },\n" +
                          "    {\n" +
                          "      \"id\": 2,\n" +
                          "      \"name\": \"Microsoft\",\n" +
                          "      \"description\": \"A technology company\",\n" +
                          "      \"website\": \"https://microsoft.com\",\n" +
                          "      \"logoUrl\": \"https://microsoft.com/logo.png\",\n" +
                          "      \"createdAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"updatedAt\": \"2024-01-01T10:00:00\",\n" +
                          "      \"aboutUs\": \"About Microsoft\",\n" +
                          "      \"vision\": \"To empower every person and organization\",\n" +
                          "      \"mission\": \"Empower every person and organization to achieve more\",\n" +
                          "      \"culture\": \"Growth mindset\",\n" +
                          "      \"jobs\": [],\n" +
                          "      \"careerPageUrl\": \"https://careers.microsoft.com\",\n" +
                          "      \"linkedinUrl\": \"https://linkedin.com/company/microsoft\",\n" +
                          "      \"headquarters\": \"Redmond, WA\",\n" +
                          "      \"primaryIndustryId\": 1,\n" +
                          "      \"primaryIndustryName\": \"Information Technology & Services\",\n" +
                          "      \"secondaryIndustries\": \"Software,Cloud\",\n" +
                          "      \"companySize\": \"100000+\",\n" +
                          "      \"specialties\": \"Windows,Office,Azure\"\n" +
                          "    }\n" +
                          "  ],\n" +
                          "  \"pageable\": {\n" +
                          "    \"sort\": {\n" +
                          "      \"empty\": false,\n" +
                          "      \"sorted\": true,\n" +
                          "      \"unsorted\": false\n" +
                          "    },\n" +
                          "    \"offset\": 0,\n" +
                          "    \"pageNumber\": 0,\n" +
                          "    \"pageSize\": 100,\n" +
                          "    \"paged\": true,\n" +
                          "    \"unpaged\": false\n" +
                          "  },\n" +
                          "  \"last\": false,\n" +
                          "  \"totalElements\": 25,\n" +
                          "  \"totalPages\": 1,\n" +
                          "  \"size\": 100,\n" +
                          "  \"number\": 0,\n" +
                          "  \"sort\": {\n" +
                          "    \"empty\": false,\n" +
                          "    \"sorted\": true,\n" +
                          "    \"unsorted\": false\n" +
                          "  },\n" +
                          "  \"first\": true,\n" +
                          "  \"numberOfElements\": 25,\n" +
                          "  \"empty\": false\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "No companies found for the given industry ID")
    })
    public ResponseEntity<Page<CompanyResponse>> getCompaniesByPrimaryIndustryId(
            @PathVariable Long primaryIndustryId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "100") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(companyService.getCompaniesByPrimaryIndustryId(primaryIndustryId, pageable));
        } catch (CompanyNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving companies by industry ID: " + e.getMessage());
        }
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFoundException(CompanyNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
} 