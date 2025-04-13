package com.tymbl.location.controller;

import com.tymbl.common.dto.CityDTO;
import com.tymbl.common.dto.CountryDTO;
import com.tymbl.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location API", description = "APIs for managing geographical locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Returns a list of all available countries")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of countries retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class)),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"United States\",\n" +
                          "    \"code\": \"US\",\n" +
                          "    \"phoneCode\": \"+1\"\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"India\",\n" +
                          "    \"code\": \"IN\",\n" +
                          "    \"phoneCode\": \"+91\"\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }

    @GetMapping("/countries/{countryId}/cities")
    @Operation(summary = "Get cities by country", description = "Returns a list of cities for a specific country")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of cities retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CityDTO.class)),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"New York\",\n" +
                          "    \"state\": \"New York\",\n" +
                          "    \"countryId\": 1\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Los Angeles\",\n" +
                          "    \"state\": \"California\",\n" +
                          "    \"countryId\": 1\n" +
                          "  }\n" +
                          "]"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<List<CityDTO>> getCitiesByCountry(
            @Parameter(description = "Country ID", required = true)
            @PathVariable Long countryId) {
        return ResponseEntity.ok(locationService.getCitiesByCountry(countryId));
    }

    @GetMapping("/cities")
    @Operation(summary = "Get all cities", description = "Returns a list of all available cities")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of cities retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = CityDTO.class)),
                examples = @ExampleObject(
                    value = "[\n" +
                          "  {\n" +
                          "    \"id\": 1,\n" +
                          "    \"name\": \"New York\",\n" +
                          "    \"state\": \"New York\",\n" +
                          "    \"countryId\": 1\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 2,\n" +
                          "    \"name\": \"Los Angeles\",\n" +
                          "    \"state\": \"California\",\n" +
                          "    \"countryId\": 1\n" +
                          "  },\n" +
                          "  {\n" +
                          "    \"id\": 3,\n" +
                          "    \"name\": \"Mumbai\",\n" +
                          "    \"state\": \"Maharashtra\",\n" +
                          "    \"countryId\": 2\n" +
                          "  }\n" +
                          "]"
                )
            )
        )
    })
    public ResponseEntity<List<CityDTO>> getAllCities() {
        return ResponseEntity.ok(locationService.getAllCities());
    }

    @GetMapping("/cities/{cityId}")
    @Operation(summary = "Get city by ID", description = "Returns a city by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "City retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityDTO.class),
                            examples = @ExampleObject(
                                value = "{\n" +
                                      "  \"id\": 3,\n" +
                                      "  \"name\": \"Bangalore\",\n" +
                                      "  \"zipCode\": \"560001\",\n" +
                                      "  \"countryId\": 1,\n" +
                                      "  \"countryName\": \"India\"\n" +
                                      "}"
                            ))),
            @ApiResponse(responseCode = "404", description = "City not found")
    })
    public ResponseEntity<CityDTO> getCityById(
            @Parameter(description = "City ID", required = true)
            @PathVariable Long cityId) {
        return ResponseEntity.ok(locationService.getCityById(cityId));
    }

    @GetMapping("/cities/search")
    @Operation(summary = "Search cities", description = "Search cities by name or partial name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cities matching search criteria",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CityDTO.class)),
                            examples = @ExampleObject(
                                value = "[\n" +
                                      "  {\n" +
                                      "    \"id\": 1,\n" +
                                      "    \"name\": \"Mumbai\",\n" +
                                      "    \"zipCode\": \"400001\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 21,\n" +
                                      "    \"name\": \"Mumbai Suburban\",\n" +
                                      "    \"zipCode\": \"400051\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  }\n" +
                                      "]"
                            )))
    })
    public ResponseEntity<List<CityDTO>> searchCities(
            @Parameter(description = "Search term for city name", required = true)
            @RequestParam String query) {
        return ResponseEntity.ok(locationService.searchCities(query));
    }
} 