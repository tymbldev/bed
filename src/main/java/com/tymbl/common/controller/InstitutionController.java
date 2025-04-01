package com.tymbl.common.controller;

import com.tymbl.common.entity.Institution;
import com.tymbl.common.service.InstitutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
@Tag(name = "Institutions", description = "APIs for managing educational institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    @GetMapping
    @Operation(summary = "Get all institutions", description = "Returns a list of all educational institutions")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of institutions retrieved successfully",
            content = @Content(schema = @Schema(implementation = Institution.class))
        )
    })
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        return ResponseEntity.ok(institutionService.getAllInstitutions());
    }

    @PostMapping
    @Operation(summary = "Create a new institution", description = "Creates a new educational institution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Institution created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or institution already exists")
    })
    public ResponseEntity<Institution> createInstitution(@Valid @RequestBody Institution institution) {
        return ResponseEntity.ok(institutionService.createInstitution(institution));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get institution by ID", description = "Returns a specific educational institution by its ID")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Institution retrieved successfully",
            content = @Content(schema = @Schema(implementation = Institution.class))
        ),
        @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    public ResponseEntity<Institution> getInstitutionById(@PathVariable Long id) {
        return ResponseEntity.ok(institutionService.getInstitutionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update institution", description = "Updates an existing educational institution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Institution updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or institution already exists"),
        @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    public ResponseEntity<Institution> updateInstitution(
            @PathVariable Long id,
            @Valid @RequestBody Institution institution) {
        return ResponseEntity.ok(institutionService.updateInstitution(id, institution));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete institution", description = "Deletes an educational institution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Institution deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Institution not found")
    })
    public ResponseEntity<Void> deleteInstitution(@PathVariable Long id) {
        institutionService.deleteInstitution(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search institutions", description = "Searches for institutions by name")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = Institution.class))
        )
    })
    public ResponseEntity<List<Institution>> searchInstitutions(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(institutionService.searchInstitutions(keyword));
    }
} 