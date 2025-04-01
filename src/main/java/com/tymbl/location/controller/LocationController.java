package com.tymbl.location.controller;

import com.tymbl.location.dto.CityDTO;
import com.tymbl.location.dto.CountryDTO;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location API", description = "APIs for managing geographical locations")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Returns a list of all available countries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of countries retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class)),
                            examples = @ExampleObject(
                                value = "[\n" +
                                      "  {\n" +
                                      "    \"id\": 1,\n" +
                                      "    \"name\": \"India\",\n" +
                                      "    \"code\": \"IN\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 2,\n" +
                                      "    \"name\": \"United States\",\n" +
                                      "    \"code\": \"US\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 3,\n" +
                                      "    \"name\": \"United Kingdom\",\n" +
                                      "    \"code\": \"GB\"\n" +
                                      "  }\n" +
                                      "]"
                            )))
    })
    public ResponseEntity<List<CountryDTO>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }

    @GetMapping("/countries/{countryId}/cities")
    @Operation(summary = "Get cities by country", description = "Returns a list of cities for the specified country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of cities retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CityDTO.class)),
                            examples = @ExampleObject(
                                value = "[\n" +
                                      "  {\n" +
                                      "    \"id\": 1,\n" +
                                      "    \"name\": \"Mumbai\",\n" +
                                      "    \"state\": \"Maharashtra\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 2,\n" +
                                      "    \"name\": \"Delhi\",\n" +
                                      "    \"state\": \"Delhi\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 3,\n" +
                                      "    \"name\": \"Bangalore\",\n" +
                                      "    \"state\": \"Karnataka\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  }\n" +
                                      "]"
                            ))),
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
            @ApiResponse(responseCode = "200", description = "List of cities retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CityDTO.class)),
                            examples = @ExampleObject(
                                value = "[\n" +
                                      "  {\n" +
                                      "    \"id\": 1,\n" +
                                      "    \"name\": \"Mumbai\",\n" +
                                      "    \"state\": \"Maharashtra\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 4,\n" +
                                      "    \"name\": \"New York\",\n" +
                                      "    \"state\": \"New York\",\n" +
                                      "    \"countryId\": 2,\n" +
                                      "    \"countryName\": \"United States\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 7,\n" +
                                      "    \"name\": \"London\",\n" +
                                      "    \"state\": \"England\",\n" +
                                      "    \"countryId\": 3,\n" +
                                      "    \"countryName\": \"United Kingdom\"\n" +
                                      "  }\n" +
                                      "]"
                            )))
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
                                      "  \"state\": \"Karnataka\",\n" +
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
                                      "    \"state\": \"Maharashtra\",\n" +
                                      "    \"countryId\": 1,\n" +
                                      "    \"countryName\": \"India\"\n" +
                                      "  },\n" +
                                      "  {\n" +
                                      "    \"id\": 21,\n" +
                                      "    \"name\": \"Mumbai Suburban\",\n" +
                                      "    \"state\": \"Maharashtra\",\n" +
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