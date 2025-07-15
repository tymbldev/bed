package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
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
    private final DropdownService dropdownService;
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
        return mapToResponse(company);
    }

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new CompanyNotFoundException(id));
        List<Job> jobs = jobRepository.findByCompanyId(id);
        return mapToResponse(company, jobs);
    }

    public Page<CompanyResponse> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        if (companies.isEmpty()) {
            throw new CompanyNotFoundException("No companies found");
        }
        return companies.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<Company> getAllCompaniesForDropdown() {
        return companyRepository.findAll();
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
        response.setCrawled(company.isCrawled());
        response.setLastCrawledAt(company.getLastCrawledAt());
        response.setCrawledData(company.getCrawledData());
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

    @Transactional
    public List<CompanyIndustryResponse> detectIndustriesForCompanies(boolean useGemini) {
        List<Company> companies = companyRepository.findAll();
        List<CompanyIndustryResponse> results = new ArrayList<>();
        
        for (Company company : companies) {
            CompanyIndustryResponse response = CompanyIndustryResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .processed(false)
                .build();
            
            try {
                if (useGemini) {
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
                        
                        // Update company with detected industries
                        company.setPrimaryIndustryId(primaryIndustryId);
                        company.setSecondaryIndustries(String.join(",", secondaryIndustries != null ? secondaryIndustries : new ArrayList<>()));
                        companyRepository.save(company);
                        
                        response.setPrimaryIndustry(primaryIndustry);
                        response.setPrimaryIndustryId(primaryIndustryId);
                        response.setSecondaryIndustries(secondaryIndustries);
                        response.setProcessed(true);
                    } else {
                        response.setError("Failed to detect industries using Gemini AI");
                    }
                } else {
                    // Manual industry detection based on existing data
                    String primaryIndustry = detectPrimaryIndustryFromExistingData(company);
                    List<String> secondaryIndustries = detectSecondaryIndustriesFromExistingData(company);
                    
                    Long primaryIndustryId = findIndustryIdByName(primaryIndustry);
                    
                    company.setPrimaryIndustryId(primaryIndustryId);
                    company.setSecondaryIndustries(String.join(",", secondaryIndustries));
                    companyRepository.save(company);
                    
                    response.setPrimaryIndustry(primaryIndustry);
                    response.setPrimaryIndustryId(primaryIndustryId);
                    response.setSecondaryIndustries(secondaryIndustries);
                    response.setProcessed(true);
                }
            } catch (Exception e) {
                response.setError("Error processing company: " + e.getMessage());
            }
            
            results.add(response);
        }
        
        return results;
    }
    
    private Long findIndustryIdByName(String industryName) {
        if (industryName == null || industryName.trim().isEmpty()) {
            return null;
        }
        
        return dropdownService.getIndustryIdByName(industryName);
    }
    
    private String detectPrimaryIndustryFromExistingData(Company company) {
        // Simple logic to detect primary industry from existing data
        String description = company.getDescription();
        String aboutUs = company.getAboutUs();
        String specialties = company.getSpecialties();
        String combinedText = (description != null ? description + " " : "") +
                              (aboutUs != null ? aboutUs + " " : "") +
                              (specialties != null ? specialties : "");
        combinedText = combinedText.toLowerCase();
        // Simple keyword-based detection
        if (combinedText.contains("software") || combinedText.contains("technology") || combinedText.contains("tech")) {
            return "Information Technology & Services";
        } else if (combinedText.contains("bank") || combinedText.contains("finance") || combinedText.contains("financial")) {
            return "Financial Services";
        } else if (combinedText.contains("health") || combinedText.contains("medical")) {
            return "Healthcare & HealthTech";
        } else if (combinedText.contains("education") || combinedText.contains("edtech")) {
            return "Education Technology (EdTech)";
        } else if (combinedText.contains("e-commerce") || combinedText.contains("retail")) {
            return "E-commerce & Online Retail";
        } else if (combinedText.contains("travel") || combinedText.contains("hospitality")) {
            return "Travel & Hospitality Technology";
        } else {
            return "Information Technology & Services"; // Default
        }
    }
    
    private List<String> detectSecondaryIndustriesFromExistingData(Company company) {
        List<String> secondaryIndustries = new ArrayList<>();
        String combinedText = (company.getDescription() != null ? company.getDescription() + " " : "") +
                            (company.getAboutUs() != null ? company.getAboutUs() + " " : "") +
                            (company.getSpecialties() != null ? company.getSpecialties() : "");
        
        combinedText = combinedText.toLowerCase();
        
        // Add relevant secondary industries based on keywords
        if (combinedText.contains("saas") || combinedText.contains("software as a service")) {
            secondaryIndustries.add("Software as a Service (SaaS)");
        }
        if (combinedText.contains("cloud") || combinedText.contains("aws") || combinedText.contains("azure")) {
            secondaryIndustries.add("Cloud Computing");
        }
        if (combinedText.contains("ai") || combinedText.contains("machine learning") || combinedText.contains("artificial intelligence")) {
            secondaryIndustries.add("Artificial Intelligence & Machine Learning (AI/ML)");
        }
        if (combinedText.contains("mobile") || combinedText.contains("app")) {
            secondaryIndustries.add("Mobile Applications");
        }
        if (combinedText.contains("product based")) {
            secondaryIndustries.add("Product Based Company");
        }
        
        return secondaryIndustries;
    }
} 