package com.tymbl.common.controller;

import com.tymbl.common.service.CompanyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/company-data")
public class CompanyDataController {

    @Autowired
    private CompanyDataService companyDataService;

    @PostMapping("/load-basic")
    public ResponseEntity<List<String>> loadBasicCompanyData() {
        List<String> results = companyDataService.loadBasicCompanyData();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/update-detailed")
    public ResponseEntity<List<String>> updateDetailedCompanyData() {
        List<String> results = companyDataService.updateDetailedCompanyData();
        return ResponseEntity.ok(results);
    }

    @PostMapping("/cleanup-duplicates")
    public ResponseEntity<List<String>> cleanupDuplicateCompanies() {
        List<String> results = companyDataService.cleanupDuplicateCompanies();
        return ResponseEntity.ok(results);
    }
} 