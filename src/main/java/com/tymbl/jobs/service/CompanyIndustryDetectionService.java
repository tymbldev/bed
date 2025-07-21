package com.tymbl.jobs.service;

import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyIndustryDetectionService {

    private final CompanyRepository companyRepository;
    private final IndustryRepository industryRepository;
    private final GeminiService geminiService;
    private final DropdownService dropdownService;

    /**
     * Process industry detection for a single company in its own transaction
     */
    @Transactional
    public CompanyIndustryResponse processCompanyIndustryDetection(Company company) {
        CompanyIndustryResponse response = CompanyIndustryResponse.builder()
            .companyId(company.getId())
            .companyName(company.getName())
            .processed(false)
            .build();
        
        try {
            // Use Gemini AI to detect industries
            Map<String, Object> industryData = geminiService.detectCompanyIndustries(
                company.getName(),
                company.getDescription(),
                company.getSpecialties()
            );
            
            if (!industryData.isEmpty()) {
                String primaryIndustry = (String) industryData.get("primaryIndustry");
                @SuppressWarnings("unchecked")
                List<String> secondaryIndustries = (List<String>) industryData.get("secondaryIndustries");
                
                // Find primary industry ID
                Long primaryIndustryId = findIndustryIdByName(primaryIndustry);
                
                // Update company with detected industries and mark as processed
                company.setPrimaryIndustryId(primaryIndustryId);
                company.setSecondaryIndustries(String.join(",", secondaryIndustries != null ? secondaryIndustries : new ArrayList<>()));
                company.setIndustryProcessed(true);
                companyRepository.save(company);
                
                // Refresh company cache to ensure fresh data
                dropdownService.refreshCompanyList();
                
                response.setPrimaryIndustry(primaryIndustry);
                response.setPrimaryIndustryId(primaryIndustryId);
                response.setSecondaryIndustries(secondaryIndustries);
                response.setProcessed(true);
            } else {
                response.setError("Failed to detect industries using Gemini AI");
            }
        } catch (Exception e) {
            response.setError("Error processing company: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Find industry ID by name
     */
    private Long findIndustryIdByName(String industryName) {
        if (industryName == null || industryName.trim().isEmpty()) {
            return null;
        }
        
        return industryRepository.findByName(industryName)
            .map(industry -> industry.getId())
            .orElse(null);
    }
} 