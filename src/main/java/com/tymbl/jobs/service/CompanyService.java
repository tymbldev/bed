package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.dto.CompanyRequest;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.exception.CompanyNotFoundException;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final GeminiService geminiService;
    private final IndustryRepository industryRepository;
    private final DropdownService dropdownService;
    private final CompanyIndustryDetectionService companyIndustryDetectionService;

    private static final Long SUPER_ADMIN_ID = 0L;

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
                .orElseThrow(() -> new CompanyNotFoundException(request.getId()));
        }

        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setLogoUrl(request.getLogoUrl());
        company.setAboutUs(request.getAboutUs());
        company.setVision(request.getVision());
        company.setMission(request.getMission());
        company.setCulture(request.getCulture());

        company = companyRepository.save(company);
        
        // Refresh company cache to ensure fresh data
        dropdownService.refreshCompanyList();
        
        return mapToResponse(company);
    }


    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new CompanyNotFoundException(id));
        List<Job> jobs = jobRepository.findByCompanyId(id);
        return mapToResponse(company, jobs);
    }

    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        // For pagination, we need to use repository directly
        Page<Company> companies = companyRepository.findAll(pageable);
        if (companies.isEmpty()) {
            throw new CompanyNotFoundException("No companies found");
        }
        return companies.map(this::mapToResponse);
    }

    /**
     * Get company name by ID
     * @param companyId The company ID
     * @return Company name or null if not found
     */
    public String getCompanyNameById(Long companyId) {
        return dropdownService.getCompanyNameById(companyId);
    }

    /**
     * Get companies by primary industry ID with pagination
     * @param primaryIndustryId The primary industry ID
     * @param pageable Pagination parameters
     * @return Page of companies
     */
    public Page<CompanyResponse> getCompaniesByPrimaryIndustryId(Long primaryIndustryId, Pageable pageable) {
        // For pagination, we need to use repository directly
        Page<Company> companies = companyRepository.findByPrimaryIndustryId(primaryIndustryId, pageable);
        if (companies.isEmpty()) {
            throw new CompanyNotFoundException("No companies found for industry ID: " + primaryIndustryId);
        }
        return companies.map(this::mapToResponse);
    }

    /**
     * Get companies by primary industry ID without pagination
     * @param primaryIndustryId The primary industry ID
     * @return List of companies
     */
    public List<CompanyResponse> getCompaniesByPrimaryIndustryId(Long primaryIndustryId) {
        List<Company> companies = companyRepository.findByPrimaryIndustryId(primaryIndustryId);
        if (companies.isEmpty()) {
            throw new CompanyNotFoundException("No companies found for industry ID: " + primaryIndustryId);
        }
        return companies.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Company> getAllCompaniesForDropdown() {
        return companyRepository.findAll();
    }

    /**
     * Get all company names from the database
     * @return List of company names
     */
    @Transactional(readOnly = true)
    public List<String> getAllCompanyNames() {
        return companyRepository.findAll().stream()
            .map(Company::getName)
            .collect(Collectors.toList());
    }

    private CompanyResponse mapToResponse(Company company) {
        List<Job> jobs = jobRepository.findByCompanyId(company.getId());
        return mapToResponse(company, jobs);
    }

    private CompanyResponse mapToResponse(Company company, List<Job> jobs) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setDescription(company.getDescription());
        response.setWebsite(company.getWebsite());
        response.setLogoUrl(company.getLogoUrl());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        response.setAboutUs(company.getAboutUs());
        response.setVision(company.getVision());
        response.setMission(company.getMission());
        response.setCulture(company.getCulture());
        response.setCareerPageUrl(company.getCareerPageUrl());
        response.setLinkedinUrl(company.getLinkedinUrl());
        response.setHeadquarters(company.getHeadquarters());
        response.setPrimaryIndustryId(company.getPrimaryIndustryId());
        response.setSecondaryIndustries(company.getSecondaryIndustries());
        response.setCompanySize(company.getCompanySize());
        response.setSpecialties(company.getSpecialties());
        // Ensure all fields are always set
        if (response.getSecondaryIndustries() == null) response.setSecondaryIndustries("");
        if (response.getCompanySize() == null) response.setCompanySize("");
        if (response.getSpecialties() == null) response.setSpecialties("");
        if (response.getCareerPageUrl() == null) response.setCareerPageUrl("");
        if (jobs != null) {
            response.setJobs(jobs.stream().map(this::mapJobToResponse).collect(Collectors.toList()));
        }
        return response;
    }

    private JobResponse mapJobToResponse(Job job) {
        JobResponse jobResponse = new JobResponse();
        jobResponse.setId(job.getId());
        jobResponse.setTitle(job.getTitle());
        jobResponse.setDescription(job.getDescription());
        jobResponse.setCityId(job.getCityId());
        jobResponse.setCountryId(job.getCountryId());
        jobResponse.setDesignationId(job.getDesignationId());
        jobResponse.setDesignation(job.getDesignation());
        jobResponse.setMinSalary(job.getMinSalary());
        jobResponse.setMaxSalary(job.getMaxSalary());
        jobResponse.setMinExperience(job.getMinExperience());
        jobResponse.setMaxExperience(job.getMaxExperience());
        jobResponse.setJobType(job.getJobType());
        jobResponse.setCurrencyId(job.getCurrencyId());
        jobResponse.setCompanyId(job.getCompanyId());
        jobResponse.setCompany(job.getCompany());
        jobResponse.setPostedBy(job.getPostedById());
        jobResponse.setActive(job.isActive());
        jobResponse.setCreatedAt(job.getCreatedAt());
        jobResponse.setUpdatedAt(job.getUpdatedAt());
        jobResponse.setTags(job.getTags());
        jobResponse.setSuperAdminPosted(SUPER_ADMIN_ID.equals(job.getPostedById()));
        return jobResponse;
    }

    public List<CompanyIndustryResponse> detectIndustriesForCompanies() {
        // Only fetch companies that haven't been processed for industry detection
        List<Company> companies = companyRepository.findByIndustryProcessedFalse();
        List<CompanyIndustryResponse> results = new ArrayList<>();
        
        for (Company company : companies) {
            CompanyIndustryResponse response = companyIndustryDetectionService.processCompanyIndustryDetection(company);
            results.add(response);
        }
        return results;
    }
    



    

    
    /**
     * Reset industry processed flag for all companies
     * This allows reprocessing of industry detection for all companies
     */
    @Transactional
    public void resetIndustryProcessedFlag() {
        companyRepository.resetIndustryProcessedFlag();
        // Refresh company cache to ensure fresh data
        dropdownService.refreshCompanyList();
    }
    

} 