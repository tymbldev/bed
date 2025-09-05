package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.util.CompanyNameCleaner;
import com.tymbl.jobs.dto.CompanyIndustryResponse;
import com.tymbl.jobs.dto.CompanyRequest;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.exception.CompanyNotFoundException;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

  private final CompanyRepository companyRepository;
  private final JobRepository jobRepository;
  private final GeminiService geminiService;
  private final IndustryRepository industryRepository;
  private final DropdownService dropdownService;
  private final CompanyTransactionService companyTransactionService;
  private final ElasticsearchJobQueryService elasticsearchJobQueryService;

  private static final Long SUPER_ADMIN_ID = 0L;

  @Transactional
  public CompanyResponse createOrUpdateCompany(CompanyRequest request) {
    Company company;
    if (request.getId() == 1000) {
      // New company - clean and validate the name
      String cleanedName = CompanyNameCleaner.cleanAndValidateCompanyName(request.getName());
      if (cleanedName == null) {
        throw new IllegalArgumentException("Invalid company name: " + request.getName());
      }
      company = new Company();
      company.setName(cleanedName);
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
   *
   * @param companyId The company ID
   * @return Company name or null if not found
   */
  public String getCompanyNameById(Long companyId) {
    return dropdownService.getCompanyNameById(companyId);
  }

  /**
   * Get companies by primary industry ID with pagination
   *
   * @param primaryIndustryId The primary industry ID
   * @param pageable Pagination parameters
   * @return Page of companies
   */
  public Page<CompanyResponse> getCompaniesByPrimaryIndustryId(Long primaryIndustryId,
      Pageable pageable) {
    try {
      // Use Elasticsearch to get companies with jobs prioritized and ordered by rank
      return elasticsearchJobQueryService.getCompaniesByPrimaryIndustryId(primaryIndustryId,
          pageable);
    } catch (Exception e) {
      log.error("Error fetching companies by primary industry ID {} from Elasticsearch: {}",
          primaryIndustryId, e.getMessage(), e);
      return null;
    }
  }


  @Transactional(readOnly = true)
  public List<Company> getAllCompaniesForDropdown() {
    // Use cached data from DropdownService instead of direct database call
    return dropdownService.getAllCompanies();
  }

  @Transactional(readOnly = true)
  public List<com.tymbl.common.dto.CompanyDropdownDTO> getAllCompaniesForDropdownDTO() {
    // Use cached data from DropdownService and map to DTO with only id and name
    return dropdownService.getAllCompanies().stream()
        .map(company -> com.tymbl.common.dto.CompanyDropdownDTO.builder()
            .id(company.getId())
            .name(company.getName())
            .build())
        .collect(java.util.stream.Collectors.toList());
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
    if (response.getSecondaryIndustries() == null) {
      response.setSecondaryIndustries("");
    }
    if (response.getCompanySize() == null) {
      response.setCompanySize("");
    }

    // Enrich with dropdown values
    enrichCompanyResponseWithDropdownValues(response);
    if (response.getSpecialties() == null) {
      response.setSpecialties("");
    }
    if (response.getCareerPageUrl() == null) {
      response.setCareerPageUrl("");
    }
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

    // Enrich with dropdown values
    enrichJobResponseWithDropdownValues(jobResponse);

    return jobResponse;
  }

  /**
   * Enrich job response with dropdown values from DropdownService
   */
  private void enrichJobResponseWithDropdownValues(JobResponse response) {
    try {
      // Get city name
      if (response.getCityId() != null) {
        String cityName = dropdownService.getCityNameById(response.getCityId());
        response.setCityName(cityName);
      }

      // Get country name
      if (response.getCountryId() != null) {
        String countryName = dropdownService.getCountryNameById(response.getCountryId());
        response.setCountryName(countryName);
      }

      // Get designation name
      if (response.getDesignationId() != null) {
        String designationName = dropdownService.getDesignationNameById(
            response.getDesignationId());
        response.setDesignationName(designationName);
      }

      // Get currency information
      if (response.getCurrencyId() != null) {
        String currencyName = dropdownService.getCurrencyNameById(response.getCurrencyId());
        String currencySymbol = dropdownService.getCurrencySymbolById(response.getCurrencyId());
        response.setCurrencyName(currencyName);
        response.setCurrencySymbol(currencySymbol);
      }

      // Get company name
      if (response.getCompanyId() != null) {
        String companyName = dropdownService.getCompanyNameById(response.getCompanyId());
        response.setCompanyName(companyName);
      }
    } catch (Exception e) {
      log.warn("Failed to enrich job response with dropdown values for job {}: {}",
          response.getId(), e.getMessage());
    }
  }

  /**
   * Enrich company response with dropdown values from DropdownService
   */
  private void enrichCompanyResponseWithDropdownValues(CompanyResponse response) {
    try {
      // Get primary industry name
      if (response.getPrimaryIndustryId() != null) {
        String industryName = dropdownService.getIndustryNameById(response.getPrimaryIndustryId());
        response.setPrimaryIndustryName(industryName);
      }
    } catch (Exception e) {
      log.warn("Failed to enrich company response with dropdown values for company {}: {}",
          response.getId(), e.getMessage());
    }
  }

  /**
   * Detect industries for companies in batches with individual transactions per batch This ensures
   * that each batch is processed in its own transaction
   */
  public List<CompanyIndustryResponse> detectIndustriesForCompaniesInBatches() {
    log.info("Starting industry detection for companies in batches");

    // Only fetch companies that haven't been processed for industry detection
    List<Company> companies = companyRepository.findByIndustryProcessedFalse();
    List<CompanyIndustryResponse> results = new ArrayList<>();

    int totalProcessed = 0;
    int totalErrors = 0;
    int batchSize = 10; // Process 10 companies at a time

    for (int i = 0; i < companies.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, companies.size());
      List<Company> batch = companies.subList(i, endIndex);

      log.info("Processing industry detection batch {} with {} companies", (i / batchSize) + 1,
          batch.size());

      // Process each company in the batch with its own transaction
      for (Company company : batch) {
        try {
          CompanyIndustryResponse response = companyTransactionService.processCompanyIndustryDetectionInTransaction(
              company);
          results.add(response);
          totalProcessed++;
          log.info("Successfully processed industry detection for company: {} (ID: {})",
              company.getName(), company.getId());
        } catch (Exception e) {
          totalErrors++;
          log.error("Error processing industry detection for company: {} (ID: {})",
              company.getName(), company.getId(), e);

          // Create error response
          CompanyIndustryResponse errorResponse = CompanyIndustryResponse.builder()
              .companyId(company.getId())
              .companyName(company.getName())
              .processed(false)
              .error("Error processing company: " + e.getMessage())
              .build();
          results.add(errorResponse);
        }
      }
    }

    log.info("Completed industry detection in batches. Total processed: {}, Total errors: {}",
        totalProcessed, totalErrors);
    return results;
  }

}