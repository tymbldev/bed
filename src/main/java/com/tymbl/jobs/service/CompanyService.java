package com.tymbl.jobs.service;

import com.tymbl.jobs.dto.CompanyRequest;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Transactional
    public CompanyResponse createOrUpdateCompany(CompanyRequest request) {
        Company company;
        if (request.getId() == 1000) {
            // New company
            company = new Company();
            company.setName(request.getName());
        } else {
            // Existing company
            company = companyRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Company not found"));
        }

        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setLogoUrl(request.getLogoUrl());

        company = companyRepository.save(company);
        return mapToResponse(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Company not found"));
        return mapToResponse(company);
    }

    private CompanyResponse mapToResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setDescription(company.getDescription());
        response.setWebsite(company.getWebsite());
        response.setLogoUrl(company.getLogoUrl());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
} 