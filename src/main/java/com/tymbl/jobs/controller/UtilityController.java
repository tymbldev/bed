package com.tymbl.jobs.controller;

import com.tymbl.common.service.CompanyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * UtilityController: Internal/utility endpoints for admin/data operations (non-AI). This controller
 * merges all utility endpoints except AI-related ones.
 */
@RestController
@RequestMapping("/api/v1/utility")
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
@RequiredArgsConstructor
@Tag(name = "Utility", description = "Internal/utility endpoints for admin/data operations (non-AI)")
public class UtilityController {

  private final CompanyDataService companyDataService;

  // --- Company Data Endpoints ---
  @PostMapping("/company-data/load-basic")
  public ResponseEntity<List<String>> loadBasicCompanyData() {
    List<String> results = companyDataService.loadBasicCompanyData();
    return ResponseEntity.ok(results);
  }

  @PostMapping("/company-data/update-detailed")
  public ResponseEntity<List<String>> updateDetailedCompanyData() {
    List<String> results = companyDataService.updateDetailedCompanyData();
    return ResponseEntity.ok(results);
  }

  @PostMapping("/company-data/cleanup-duplicates")
  public ResponseEntity<List<String>> cleanupDuplicateCompanies() {
    List<String> results = companyDataService.cleanupDuplicateCompanies();
    return ResponseEntity.ok(results);
  }
} 