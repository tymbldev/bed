package com.tymbl.common.controller;

import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.entity.Location;
import com.tymbl.common.entity.Currency;
import com.tymbl.common.enums.Degree;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.CurrencyService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.service.CompanyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.tymbl.common.dto.IndustryWiseCompaniesDTO;

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
@Tag(name = "Dropdowns", description = "APIs for managing dropdown data like departments, locations, designations, industries, currencies, and companies")
public class DropdownController {

    private final DropdownService dropdownService;
    private final CurrencyService currencyService;
    private final GeminiService geminiService;
    private final CompanyService companyService;

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

    // Industry endpoints
    @GetMapping("/industries")
    @Operation(summary = "Get all industries", description = "Returns a list of all industries for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of industries retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Industry.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Information Technology & Services\",\n" +
                          "    \"description\": \"Technology and IT services industry\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Software Development\",\n" +
                          "    \"description\": \"Software development and programming\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Financial Services\",\n" +
                          "    \"description\": \"Banking, insurance, and financial services\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Industry>> getAllIndustries() {
        return ResponseEntity.ok(dropdownService.getAllIndustries());
    }

    @PostMapping("/industries")
    @Operation(summary = "Create a new industry", description = "Creates a new industry for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Industry created successfully",
            content = @Content(
                schema = @Schema(implementation = Industry.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"Information Technology & Services\",\n" +
                          "  \"description\": \"Technology and IT services industry\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Industry> createIndustry(@Valid @RequestBody Industry industry) {
        return ResponseEntity.ok(dropdownService.createIndustry(industry));
    }

    @GetMapping("/industries-map")
    @Operation(summary = "Get all industries as a map", description = "Returns industries as a map of value/label pairs for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Industries map retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"value\": \"1\",\n" +
                          "    \"label\": \"Information Technology & Services\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"2\",\n" +
                          "    \"label\": \"Software Development\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"3\",\n" +
                          "    \"label\": \"Financial Services\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Map<String, String>>> getIndustriesAsMap() {
        List<Map<String, String>> industryMaps = dropdownService.getAllIndustries().stream()
            .map(industry -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", industry.getId().toString());
                map.put("label", industry.getName());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(industryMaps);
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
                map.put("label", designation.getName());
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

    // Currency endpoints
    @GetMapping("/currencies")
    @Operation(summary = "Get all currencies", description = "Returns a list of all available currencies")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of currencies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Currency.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"code\": \"USD\",\n" +
                          "    \"name\": \"US Dollar\",\n" +
                          "    \"symbol\": \"$\",\n" +
                          "    \"exchangeRate\": 1.00\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"code\": \"EUR\",\n" +
                          "    \"name\": \"Euro\",\n" +
                          "    \"symbol\": \"€\",\n" +
                          "    \"exchangeRate\": 0.92\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/currencies/{id}")
    @Operation(summary = "Get currency by ID", description = "Returns a currency by its ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Currency retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Currency.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"code\": \"USD\",\n" +
                          "  \"name\": \"US Dollar\",\n" +
                          "  \"symbol\": \"$\",\n" +
                          "  \"exchangeRate\": 1.00\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<Currency> getCurrencyById(@PathVariable Long id) {
        return currencyService.getCurrencyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/currencies/code/{code}")
    @Operation(summary = "Get currency by code", description = "Returns a currency by its code (e.g., USD, EUR)")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Currency retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Currency.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"id\": 1,\n" +
                          "  \"code\": \"USD\",\n" +
                          "  \"name\": \"US Dollar\",\n" +
                          "  \"symbol\": \"$\",\n" +
                          "  \"exchangeRate\": 1.00\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/companies")
    @Operation(summary = "Get all companies for dropdown", description = "Returns a list of all companies with id and name for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of companies retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = Company.class),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"Google\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Microsoft\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Amazon\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompaniesForDropdown());
    }

    @GetMapping("/companies-map")
    @Operation(summary = "Get all companies as a map", description = "Returns companies as a map of value/label pairs for dropdown selection")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Companies map retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"value\": \"1\",\n" +
                          "    \"label\": \"Google\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"2\",\n" +
                          "    \"label\": \"Microsoft\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"value\": \"3\",\n" +
                          "    \"label\": \"Amazon\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<Map<String, String>>> getCompaniesAsMap() {
        List<Map<String, String>> companyMaps = companyService.getAllCompaniesForDropdown().stream()
            .map(company -> {
                Map<String, String> map = new HashMap<>();
                map.put("value", company.getId().toString());
                map.put("label", company.getName());
                return map;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(companyMaps);
    }

    @GetMapping("/companies/by-industry/{industryId}")
    @Operation(summary = "Get companies by industry", description = "Returns a list of companies for the given industryId with detailed attributes")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of companies for the industry",
            content = @Content(
                schema = @Schema(implementation = IndustryWiseCompaniesDTO.TopCompanyDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Industry not found")
    })
    public ResponseEntity<List<IndustryWiseCompaniesDTO.TopCompanyDTO>> getCompaniesByIndustry(@PathVariable Long industryId) {
        List<IndustryWiseCompaniesDTO.TopCompanyDTO> companies = dropdownService.getCompaniesByIndustry(industryId);
        return ResponseEntity.ok(companies);
    }

    // AI-generated designations endpoint
    @GetMapping("/designations/generate/{departmentId}")
    @Operation(summary = "Generate designations for department using AI", description = "Uses Gemini AI to generate relevant designations for a specific department")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Designations generated successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"department\": \"Engineering\",\n" +
                          "  \"designations\": [\n" +
                          "    \"Software Engineer\",\n" +
                          "    \"Senior Software Engineer\",\n" +
                          "    \"Technical Lead\",\n" +
                          "    \"Engineering Manager\",\n" +
                          "    \"Principal Engineer\",\n" +
                          "    \"Staff Engineer\",\n" +
                          "    \"DevOps Engineer\",\n" +
                          "    \"QA Engineer\",\n" +
                          "    \"Frontend Engineer\",\n" +
                          "    \"Backend Engineer\",\n" +
                          "    \"Full Stack Engineer\",\n" +
                          "    \"Mobile Engineer\",\n" +
                          "    \"Data Engineer\",\n" +
                          "    \"Machine Learning Engineer\",\n" +
                          "    \"Site Reliability Engineer\"\n" +
                          "  ]\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "500", description = "AI service error")
    })
    public ResponseEntity<Map<String, Object>> generateDesignationsForDepartment(@PathVariable Long departmentId) {
        try {
            Department department = dropdownService.getDepartmentById(departmentId);
            List<String> designations = geminiService.generateDesignationsForDepartment(department.getName())
                .stream()
                .map(m -> (String) m.get("name"))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("department", department.getName());
            response.put("designations", designations);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/designations/generate-all")
    @Operation(summary = "Generate designations for all departments using AI", description = "Uses Gemini AI to generate relevant designations for all departments")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Designations generated successfully for all departments",
            content = @Content(
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"department\": \"Engineering\",\n" +
                          "    \"designations\": [\n" +
                          "      \"Software Engineer\",\n" +
                          "      \"Senior Software Engineer\",\n" +
                          "      \"Technical Lead\"\n" +
                          "    ]\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"department\": \"Product Management\",\n" +
                          "    \"designations\": [\n" +
                          "      \"Product Manager\",\n" +
                          "      \"Senior Product Manager\",\n" +
                          "      \"Product Director\"\n" +
                          "    ]\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"department\": \"Human Resources\",\n" +
                          "    \"designations\": [\n" +
                          "      \"HR Manager\",\n" +
                          "      \"Recruiter\",\n" +
                          "      \"HR Director\"\n" +
                          "    ]\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "AI service error")
    })
    public ResponseEntity<List<Map<String, Object>>> generateDesignationsForAllDepartments() {
        try {
            List<Department> departments = dropdownService.getAllDepartments();
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            
            for (Department department : departments) {
                try {
                    List<String> designations = geminiService.generateDesignationsForDepartment(department.getName())
                        .stream()
                        .map(m -> (String) m.get("name"))
                        .collect(Collectors.toList());
                    
                    Map<String, Object> departmentResult = new HashMap<>();
                    departmentResult.put("department", department.getName());
                    departmentResult.put("designations", designations);
                    
                    results.add(departmentResult);
                    
                    // Add a small delay to avoid rate limiting
                    Thread.sleep(1000);
                } catch (Exception e) {
                    // Log error but continue with other departments
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("department", department.getName());
                    errorResult.put("designations", Collections.emptyList());
                    errorResult.put("error", "Failed to generate designations for this department");
                    results.add(errorResult);
                }
            }
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/autosuggest")
    @Operation(summary = "Autosuggest for company, designation, and tag", description = "Suggests company names, designation names, or tags matching the query (min 3 chars). Returns a list of {keyword, type}.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Suggestions found")
    })
    public ResponseEntity<List<Map<String, String>>> autosuggest(@RequestParam("query") String query) {
        return ResponseEntity.ok(dropdownService.autosuggest(query));
    }
} 