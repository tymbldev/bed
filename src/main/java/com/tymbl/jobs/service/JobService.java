package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.entity.JobReferrer;
import com.tymbl.common.entity.ReferrerFeedback;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.JobReferrerRepository;
import com.tymbl.common.repository.ReferrerFeedbackRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.util.UserEnrichmentUtil;
import com.tymbl.exception.BadRequestException;
import com.tymbl.exception.ConflictException;
import com.tymbl.exception.ForbiddenException;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.exception.UnauthorizedException;
import com.tymbl.jobs.dto.JobDetailsWithReferrersResponse;
import com.tymbl.jobs.dto.JobReferrerResponse;
import com.tymbl.jobs.dto.JobRequest;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import com.tymbl.jobs.dto.ReferrerFeedbackRequest;
import com.tymbl.jobs.entity.ApplicationStatus;
import com.tymbl.jobs.entity.JobApplication;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobApplicationRepository;
import com.tymbl.jobs.repository.JobRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

  private static final Logger logger = LoggerFactory.getLogger(JobService.class);

  private final JobRepository jobRepository;
  private final SkillRepository skillRepository;
  private final JobReferrerRepository jobReferrerRepository;
  private final ReferrerFeedbackRepository referrerFeedbackRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final UserRepository userRepository;
  private final CompanyRepository companyRepository;
  private final CompanyService companyService;
  private final UserEnrichmentUtil userEnrichmentUtil;
  private final ElasticsearchJobIndexingService elasticsearchJobIndexingService;
  private final ElasticsearchJobQueryService elasticsearchJobQueryService;
  private final ElasticsearchIndexingService elasticsearchIndexingService;
  private final ElasticsearchCompanyIndexingService elasticsearchCompanyIndexingService;
  private final DropdownService dropdownService;

  @Value("${referrer.sort.weight.designation:0.3}")
  private double designationWeight;
  @Value("${referrer.sort.weight.accepted:0.3}")
  private double acceptedWeight;
  @Value("${referrer.sort.weight.feedback:0.4}")
  private double feedbackWeight;

  /**
   * Helper method to enrich user data with all names (company, designation, department, country,
   * city)
   */
  private User enrichUserWithAllNames(User user) {
    return userEnrichmentUtil.enrichUserWithAllNames(user);
  }

  @Transactional
  public JobResponse createJob(JobRequest request, User postedBy) {
    if (postedBy == null) {
      throw new UnauthorizedException("User must be authenticated to create a job");
    }
    if (postedBy.getId() == null) {
      throw new BadRequestException("Invalid user ID");
    }

    if (request.getPlatform() == null || request.getPlatform().isEmpty()) {
      request.setPlatform("NA");
    }

    // Handle uniqueUrl based on platform
    String finalUniqueUrl = request.getUniqueUrl();
    if (request.getPlatform() != null && "other".equalsIgnoreCase(request.getPlatform())) {
      finalUniqueUrl = generateRandomUniqueUrl();
    } else if (request.getUniqueUrl() != null && request.getPlatform() != null) {
      // For non-"other" platforms, check for existing job
      Job existingJob = jobRepository.findByUniqueUrlAndPlatform(request.getUniqueUrl(),
          request.getPlatform());
      if (existingJob != null) {
        // Add user as referrer if not already present
        if (jobReferrerRepository.findByJobIdAndUserId(existingJob.getId(), postedBy.getId())
            == null) {
          JobReferrer ref = new JobReferrer();
          ref.setJob(existingJob);
          ref.setUser(postedBy);
          jobReferrerRepository.save(ref);
        }
        throw new ConflictException("Job with this URL and platform already exists.",
            existingJob.getId());
      }
    }
    Job job = new Job();
    job.setTitle(request.getTitle());
    job.setDescription(request.getDescription());
    job.setCityId(request.getCityId());
    job.setCountryId(request.getCountryId());
    job.setDesignationId(request.getDesignationId());
    job.setDesignation(request.getDesignation());
    job.setMinSalary(request.getMinSalary());
    job.setMaxSalary(request.getMaxSalary());
    job.setMinExperience(request.getMinExperience());
    job.setMaxExperience(request.getMaxExperience());
    job.setJobType(request.getJobType());
    job.setCurrencyId(request.getCurrencyId());
    job.setCompanyId(request.getCompanyId());
    job.setCompany(request.getCompany());
    job.setPostedById(postedBy.getId());
    job.setOpeningCount(request.getOpeningCount() != null ? request.getOpeningCount() : 1);
    job.setUniqueUrl(finalUniqueUrl);
    job.setPlatform(request.getPlatform());

    // Set approval status based on unique URL and platform
    if (request.getUniqueUrl() != null && request.getPlatform() != null) {
      job.setApprovalStatus(JobApprovalStatus.APPROVED);
    } else {
      job.setApprovalStatus(JobApprovalStatus.PENDING);
    }

    // Set tags if provided
    if (request.getTags() != null && !request.getTags().isEmpty()) {
      job.setTags(request.getTags());
    }

    job = jobRepository.save(job);
    // Add poster as referrer
    JobReferrer ref = new JobReferrer();
    ref.setJob(job);
    ref.setUser(postedBy);
    jobReferrerRepository.save(ref);

    // Sync to Elasticsearch (non-blocking)
    try {
      elasticsearchJobIndexingService.syncJobToElasticsearch(job);
    } catch (Exception e) {
      logger.error("Failed to sync job {} to Elasticsearch: {}", job.getId(), e.getMessage());
      // Don't fail the main transaction
    }

    return mapToResponse(job);
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> getAllActiveJobs(Pageable pageable) {
    Page<Job> jobs = jobRepository.findByActiveTrueAndApproved(JobApprovalStatus.APPROVED.getValue(),
            pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> getJobsByUser(User user, Pageable pageable) {
    Page<Job> jobs = jobRepository.findByPostedByIdAndActiveTrue(user.getId(), pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> getMyPosts(User user, Pageable pageable) {
    // Get jobs posted by the user
    Page<Job> postedJobs = jobRepository.findByPostedByIdAndActiveTrue(user.getId(), pageable);

    // Get jobs where user is a referrer
    List<JobReferrer> userReferrers = jobReferrerRepository.findByUserId(user.getId());
    List<Long> referrerJobIds = userReferrers.stream()
        .map(ref -> ref.getJob().getId())
        .filter(jobId -> !jobId.equals(
            user.getId())) // Exclude jobs posted by the user (already included above)
        .collect(Collectors.toList());

    List<Job> referrerJobs = new ArrayList<>();
    if (!referrerJobIds.isEmpty()) {
      referrerJobs = jobRepository.findByIdInAndActiveTrue(referrerJobIds);
    }

    // Combine both lists
    List<Job> allJobs = new ArrayList<>();
    allJobs.addAll(postedJobs.getContent());
    allJobs.addAll(referrerJobs);

    // Remove duplicates (in case user is both poster and referrer)
    allJobs = allJobs.stream()
        .collect(Collectors.toMap(Job::getId, job -> job, (existing, replacement) -> existing))
        .values()
        .stream()
        .collect(Collectors.toList());

    // Sort by creation date (newest first)
    allJobs.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

    // Apply pagination manually
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allJobs.size());

    List<Job> paginatedJobs = allJobs.subList(start, end);

    // Convert to JobResponse with role information
    List<JobResponse> jobResponses = paginatedJobs.stream()
        .map(job -> mapToResponseWithRole(job, user))
        .collect(Collectors.toList());

    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);

    // Create a new Page with the combined results
    return new PageImpl<>(jobResponses, pageable, allJobs.size());
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> getPendingJobsForSuperAdmin(Pageable pageable) {
    return jobRepository.findByActiveTrueAndApproved(JobApprovalStatus.PENDING.getValue(), pageable)
        .map(this::mapToResponse);
  }

  @Transactional(readOnly = true)
  public JobResponse getJobById(Long jobId) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    return mapToResponse(job);
  }

  @Transactional
  public void deleteJob(Long jobId, User currentUser) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

    if (!job.getPostedById().equals(currentUser.getId())) {
      throw new ForbiddenException("You are not authorized to delete this job");
    }

    // Delete all JobReferrer entries for this job
    jobReferrerRepository.deleteByJobId(jobId);

    // Set job as inactive
    job.setActive(false);
    jobRepository.save(job);

    // Update company job count in Elasticsearch (non-blocking)
    try {
      if (job.getCompanyId() != null) {
        elasticsearchCompanyIndexingService.updateCompanyJobCount(job.getCompanyId());
      }
    } catch (Exception e) {
      logger.error("Failed to update job count for company {} in Elasticsearch: {}",
          job.getCompanyId(), e.getMessage());
      // Don't fail the main transaction
    }
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> searchJobs(String keyword, Pageable pageable) {
    Page<Job> jobs = jobRepository.searchJobs(keyword, pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  /**
   * Search jobs using Elasticsearch
   */
  public JobSearchResponse searchJobsWithElasticsearch(JobSearchRequest request, User currentUser) {
    Long userDesignationId = null;
    if (currentUser != null && currentUser.getDesignationId() != null) {
      userDesignationId = currentUser.getDesignationId();
    }

    // Map city and country names to IDs if provided
    JobSearchRequest mappedRequest = mapCityAndCountryNamesToIds(request);

    JobSearchResponse response = elasticsearchJobQueryService.searchJobs(mappedRequest,
        userDesignationId);

    // Populate referrer user IDs for all jobs in a single query
    if (response.getJobs() != null && !response.getJobs().isEmpty()) {
      populateReferrerUserIdsForJobs(response.getJobs());
    }

    // Populate companyMetaData
    if (response.getJobs() != null && !response.getJobs().isEmpty()) {
      java.util.Set<Long> companyIds = response.getJobs().stream()
          .map(com.tymbl.jobs.dto.JobResponse::getCompanyId)
          .filter(java.util.Objects::nonNull)
          .collect(java.util.stream.Collectors.toSet());
      java.util.Map<Long, JobSearchResponse.CompanyMetaData> companyMetaData = new java.util.HashMap<>();
      for (Long companyId : companyIds) {
        com.tymbl.jobs.entity.Company company = null;
        try {
          company = companyRepository.findById(companyId).orElse(null);
        } catch (Exception ignored) {
        }
        if (company != null) {
          JobSearchResponse.CompanyMetaData meta = JobSearchResponse.CompanyMetaData.builder()
              .companyName(company.getName())
              .logoUrl(company.getLogoUrl())
              .website(company.getWebsite())
              .headquarters(company.getHeadquarters())
              .activeJobCount(jobRepository.countByCompanyIdAndActiveTrue(companyId))
              .secondaryIndustry(company.getSecondaryIndustries())
              .companySize(company.getCompanySize())
              .specialties(company.getSpecialties())
              .careerPageUrl(company.getCareerPageUrl())
              .build();
          companyMetaData.put(companyId, meta);
        }
      }
      response.setCompanyMetaData(companyMetaData);
    }
    return response;
  }

  /**
   * Map city and country names to their respective IDs using DropdownService
   */
  private JobSearchRequest mapCityAndCountryNamesToIds(JobSearchRequest request) {
    JobSearchRequest mappedRequest = JobSearchRequest.builder()
        .keywords(request.getKeywords())
        .cityId(request.getCityId())
        .countryId(request.getCountryId())
        .cityName(request.getCityName())
        .countryName(request.getCountryName())
        .companyId(request.getCompanyId())
        .designationId(request.getDesignationId())
        .minExperience(request.getMinExperience())
        .maxExperience(request.getMaxExperience())
        .page(request.getPage())
        .size(request.getSize())
        .build();

    // Map city name to city ID if provided and cityId is not already set
    if (request.getCityName() != null && !request.getCityName().trim().isEmpty()
        && request.getCityId() == null) {
      Long cityId = dropdownService.getCityIdByName(request.getCityName().trim());
      if (cityId != null) {
        mappedRequest.setCityId(cityId);
        logger.debug("Mapped city name '{}' to city ID: {}", request.getCityName(), cityId);
      } else {
        logger.warn("Could not find city ID for city name: {}", request.getCityName());
      }
    }

    // Map country name to country ID if provided and countryId is not already set
    if (request.getCountryName() != null && !request.getCountryName().trim().isEmpty()
        && request.getCountryId() == null) {
      Long countryId = dropdownService.getCountryIdByName(request.getCountryName().trim());
      if (countryId != null) {
        mappedRequest.setCountryId(countryId);
        logger.debug("Mapped country name '{}' to country ID: {}", request.getCountryName(),
            countryId);
      } else {
        logger.warn("Could not find country ID for country name: {}", request.getCountryName());
      }
    }

    return mappedRequest;
  }

  /**
   * Reindex all jobs to Elasticsearch
   */
  public void reindexAllJobsToElasticsearch() {
    elasticsearchJobIndexingService.reindexAllJobs();
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> searchBySkills(List<String> skills, Pageable pageable) {
    if (skills == null || skills.isEmpty()) {
      return getAllActiveJobs(pageable);
    }

    // Convert skill names to skill IDs
    Set<Long> skillIds = skills.stream()
        .map(skillName -> skillRepository.findByNameContainingIgnoreCase(skillName))
        .filter(skillsList -> !skillsList.isEmpty())
        .map(skillsList -> skillsList.get(0).getId())
        .collect(Collectors.toSet());

    if (skillIds.isEmpty()) {
      return Page.empty(pageable);
    }

    Page<Job> jobs = jobRepository.findBySkillsInAndApproved(skillIds, JobApprovalStatus.APPROVED.getValue(),
            pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  public List<JobResponse> getJobsByCompany(Long companyId) {
    List<Job> jobs = jobRepository.findByCompanyIdAndApproved(companyId,
            JobApprovalStatus.APPROVED.getValue());
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return jobResponses;
  }

  public List<JobResponse> getJobsByCompanyAndTitle(Long companyId, String title) {
    List<Job> jobs = jobRepository.findActiveJobsByCompanyIdAndTitleAndApproved(companyId, title,
            JobApprovalStatus.APPROVED.getValue());
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return jobResponses;
  }

  public List<JobResponse> getJobsByCompanyName(String companyName) {
    List<Job> jobs = jobRepository.findByCompanyContainingIgnoreCaseAndApproved(companyName,
            JobApprovalStatus.APPROVED.getValue());
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return jobResponses;
  }

  public List<JobResponse> getJobsByCompanyNameAndTitle(String companyName, String title) {
    List<Job> jobs = jobRepository.findActiveJobsByCompanyAndTitleAndApproved(companyName, title,
            JobApprovalStatus.APPROVED.getValue());
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return jobResponses;
  }

  @Transactional
  public JobResponse updateJob(Long jobId, JobRequest request, User currentUser) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

    // Check if user is authorized to update the job
    if (!job.getPostedById().equals(currentUser.getId())) {
      logger.warn("User {} is not authorized to update job {}", currentUser.getId(), jobId);
      throw new ForbiddenException("You are not authorized to update this job");
    }

    // Handle uniqueUrl based on platform
    String finalUniqueUrl = request.getUniqueUrl();
    if (request.getPlatform() != null && "other".equalsIgnoreCase(request.getPlatform())) {
      // For "other" platform, generate a random uniqueUrl
      finalUniqueUrl = generateRandomUniqueUrl();
    } else if (request.getUniqueUrl() != null && request.getPlatform() != null) {
      // For non-"other" platforms, check for duplicate uniqueUrl and platform combination
      Job existingJob = jobRepository.findByUniqueUrlAndPlatform(request.getUniqueUrl(),
          request.getPlatform());
      if (existingJob != null && !existingJob.getId().equals(jobId)) {
        logger.warn(
            "User {} attempted to update job {} with duplicate uniqueUrl '{}' and platform '{}' (existing job: {})",
            currentUser.getId(), jobId, request.getUniqueUrl(), request.getPlatform(),
            existingJob.getId());
        throw new ConflictException("Job with this URL and platform already exists.",
            existingJob.getId());
      }
    }

    // Prevent updating company and designation after job is posted
    if (request.getCompanyId() != null && !request.getCompanyId().equals(job.getCompanyId())) {
      logger.warn("User {} attempted to change companyId from {} to {} for job {}",
          currentUser.getId(), job.getCompanyId(), request.getCompanyId(), jobId);
      throw new BadRequestException("Cannot update company after job is posted");
    }

    if (request.getCompany() != null && !request.getCompany().equals(job.getCompany())) {
      logger.warn("User {} attempted to change company from '{}' to '{}' for job {}",
          currentUser.getId(), job.getCompany(), request.getCompany(), jobId);
      throw new BadRequestException("Cannot update company after job is posted");
    }

    if (request.getDesignationId() != null && !request.getDesignationId()
        .equals(job.getDesignationId())) {
      logger.warn("User {} attempted to change designationId from {} to {} for job {}",
          currentUser.getId(), job.getDesignationId(), request.getDesignationId(), jobId);
      throw new BadRequestException("Cannot update designation after job is posted");
    }

    if (request.getDesignation() != null && !request.getDesignation()
        .equals(job.getDesignation())) {
      logger.warn("User {} attempted to change designation from '{}' to '{}' for job {}",
          currentUser.getId(), job.getDesignation(), request.getDesignation(), jobId);
      throw new BadRequestException("Cannot update designation after job is posted");
    }

    // Update job details (excluding company and designation)
    job.setTitle(request.getTitle());
    job.setDescription(request.getDescription());
    job.setCityId(request.getCityId());
    job.setCountryId(request.getCountryId());
    job.setMinSalary(request.getMinSalary());
    job.setMaxSalary(request.getMaxSalary());
    job.setMinExperience(request.getMinExperience());
    job.setMaxExperience(request.getMaxExperience());
    job.setJobType(request.getJobType());
    job.setCurrencyId(request.getCurrencyId());
    job.setOpeningCount(
        request.getOpeningCount() != null ? request.getOpeningCount() : job.getOpeningCount());

    // Update uniqueUrl and platform from request
    job.setUniqueUrl(finalUniqueUrl);
    job.setPlatform(request.getPlatform());

    // Update tags
    if (request.getTags() != null) {
      job.setTags(request.getTags());
    }

    job = jobRepository.save(job);

    // Sync to Elasticsearch (non-blocking)
    try {
      elasticsearchJobIndexingService.syncJobToElasticsearch(job);
    } catch (Exception e) {
      logger.error("Failed to sync job {} to Elasticsearch: {}", job.getId(), e.getMessage());
      // Don't fail the main transaction
    }

    return mapToResponse(job);
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> searchJobsByTags(Set<String> tags, Pageable pageable) {
    if (tags == null || tags.isEmpty()) {
      return getAllActiveJobs(pageable);
    }

    Page<Job> jobs = jobRepository.findByTagsIn(tags, pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  @Transactional(readOnly = true)
  public Page<JobResponse> searchJobsByTagKeyword(String tagKeyword, Pageable pageable) {
    if (tagKeyword == null || tagKeyword.isEmpty()) {
      return getAllActiveJobs(pageable);
    }

    Page<Job> jobs = jobRepository.findByTagsContaining(tagKeyword, pageable);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.getContent().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return new PageImpl<>(jobResponses, pageable, jobs.getTotalElements());
  }

  @Transactional(readOnly = true)
  public List<String> getAllTags() {
    return jobRepository.findAllTags();
  }

  public List<JobResponse> getJobsByCompanyPostedBySuperAdmin(Long companyId) {
    Long superAdminId = 0L; // Convention for super admin
    List<Job> jobs = jobRepository.findByCompanyIdAndPostedByIdAndActiveTrue(companyId, superAdminId);
    
    // Convert to JobResponse list
    List<JobResponse> jobResponses = jobs.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
    
    // Populate referrer user IDs for all jobs in a single query
    populateReferrerUserIdsForJobs(jobResponses);
    
    return jobResponses;
  }

  public List<JobReferrerResponse> getReferrersForJob(Long jobId) {
    List<JobReferrer> referrers = jobReferrerRepository.findByJobId(jobId);
    List<JobReferrerResponse> responses = new java.util.ArrayList<>();
    for (JobReferrer ref : referrers) {
      User user = ref.getUser();
      JobReferrerResponse dto = new JobReferrerResponse();
      dto.setUserId(user.getId());
      dto.setUserName(
          user.getFirstName() + (user.getLastName() != null ? (" " + user.getLastName()) : ""));
      dto.setDesignation(user.getDesignation());
      // Applications accepted: count of job applications for this job where referrer is this user
      int numApplicationsAccepted = (int) jobApplicationRepository.findByJobId(jobId).stream()
          .filter(app -> app.getJobId().equals(jobId) && app.getJobReferrerId() != null
              && app.getJobReferrerId().equals(ref.getId())
              && app.getStatus() == JobApplication.ApplicationStatus.SHORTLISTED)
          .count();
      dto.setNumApplicationsAccepted(numApplicationsAccepted);
      // Feedback score: average score from feedbacks for this referrer
      double feedbackScore = referrerFeedbackRepository.findByJobReferrerId(ref.getId()).stream()
          .filter(fb -> fb.getScore() != null)
          .mapToInt(ReferrerFeedback::getScore)
          .average().orElse(0.0);
      dto.setFeedbackScore(feedbackScore);
      // Designation score: simple mapping (e.g., higher for 'Manager', 'Lead', etc.)
      double designationScore = getDesignationScore(user.getDesignation());
      // Overall score (configurable weights)
      double overallScore =
          designationWeight * designationScore + acceptedWeight * numApplicationsAccepted
              + feedbackWeight * feedbackScore;
      dto.setOverallScore(overallScore);
      responses.add(dto);
    }
    // Sort by overallScore descending
    responses.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
    return responses;
  }

  private double getDesignationScore(String designation) {
    if (designation == null) {
      return 0.0;
    }
    String d = designation.toLowerCase();
    if (d.contains("lead")) {
      return 3.0;
    }
    if (d.contains("manager")) {
      return 4.0;
    }
    if (d.contains("director")) {
      return 5.0;
    }
    if (d.contains("head")) {
      return 4.5;
    }
    if (d.contains("principal")) {
      return 4.2;
    }
    if (d.contains("senior")) {
      return 2.0;
    }
    if (d.contains("junior")) {
      return 1.0;
    }
    return 1.5;
  }

  public void submitReferrerFeedback(ReferrerFeedbackRequest request, User applicant) {
    JobReferrer referrer = jobReferrerRepository.findByJobIdAndUserId(request.getJobId(),
        request.getReferrerUserId());
    if (referrer == null) {
      throw new ResourceNotFoundException("Referrer not found for this job");
    }
    ReferrerFeedback feedback = referrerFeedbackRepository.findByJobReferrerIdAndApplicantId(
        referrer.getId(), applicant.getId());
    if (feedback == null) {
      feedback = new ReferrerFeedback();
    }
    feedback.setJobReferrer(referrer);
    feedback.setApplicant(applicant);
    feedback.setFeedbackText(request.getFeedbackText());
    feedback.setGotCall(request.getGotCall());
    feedback.setScore(request.getScore());
    referrerFeedbackRepository.save(feedback);
  }

  @Transactional
  public void acceptReferralAsReferrer(Long applicationId, User currentUser,
      ApplicationStatus status) {
    JobApplication application = jobApplicationRepository.findById(applicationId)
        .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    Job job = jobRepository.findById(application.getJobId())
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    // Only allow if user is job poster or from same company
    if (!job.getPostedById().equals(currentUser.getId()) &&
        (currentUser.getCompanyId() == null || !currentUser.getCompanyId()
            .equals(job.getCompanyId()))) {
      throw new ForbiddenException("You are not authorized to accept this referral application");
    }
    // Add as referrer if not already
    JobReferrer ref = jobReferrerRepository.findByJobIdAndUserId(job.getId(), currentUser.getId());
    if (ref == null) {
      ref = new JobReferrer();
      ref.setJob(job);
      ref.setUser(currentUser);
      jobReferrerRepository.save(ref);
    }
    // Set this user as the referrer for the application
    application.setJobReferrerId(ref.getId());

    // Convert ApplicationStatus to JobApplication.ApplicationStatus
    JobApplication.ApplicationStatus jobApplicationStatus;
    switch (status) {
      case SHORTLISTED:
        jobApplicationStatus = JobApplication.ApplicationStatus.SHORTLISTED;
        break;
      case REJECTED:
        jobApplicationStatus = JobApplication.ApplicationStatus.REJECTED;
        break;
      default:
        throw new BadRequestException("Invalid status. Only SHORTLISTED or REJECTED allowed.");
    }

    application.setStatus(jobApplicationStatus);
    jobApplicationRepository.save(application);
  }

  @Transactional
  public JobResponse approveJob(Long jobId, JobApprovalStatus status, String rejectionReason) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

    job.setApprovalStatus(status);
    jobRepository.save(job);

    return mapToResponse(job);
  }

  @Transactional
  public void registerAsJobReferrer(Long jobId, User currentUser) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

    // Check if job is active and approved
    if (!job.isActive()) {
      throw new BadRequestException("Cannot register as referrer for inactive job");
    }

    if (!job.isApproved()) {
      throw new BadRequestException("Cannot register as referrer for unapproved job");
    }

    // Check if user is already a referrer for this job
    JobReferrer existingReferrer = jobReferrerRepository.findByJobIdAndUserId(jobId,
        currentUser.getId());
    if (existingReferrer != null) {
      throw new ConflictException("You are already registered as a referrer for this job");
    }

    // Check if user is from the same company as the job
    if (currentUser.getCompanyId() == null || !currentUser.getCompanyId()
        .equals(job.getCompanyId())) {
      throw new ForbiddenException(
          "You can only register as a referrer for jobs from your company");
    }

    // Create new JobReferrer entry
    JobReferrer jobReferrer = new JobReferrer();
    jobReferrer.setJob(job);
    jobReferrer.setUser(currentUser);
    jobReferrerRepository.save(jobReferrer);
  }

  @Transactional
  public com.tymbl.jobs.dto.MultipleJobReferrerRegistrationResponse registerAsJobReferrerForMultipleJobs(
      List<Long> jobIds, User currentUser) {
    
    List<com.tymbl.jobs.dto.JobReferrerRegistrationResult> results = new java.util.ArrayList<>();
    int successfulRegistrations = 0;
    int failedRegistrations = 0;

    for (Long jobId : jobIds) {
      try {
        registerAsJobReferrer(jobId, currentUser);
        
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(true)
            .message("Successfully registered as referrer")
            .build());
        successfulRegistrations++;
        
      } catch (ResourceNotFoundException e) {
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(false)
            .message("Job not found")
            .errorCode("JOB_NOT_FOUND")
            .build());
        failedRegistrations++;
        
      } catch (BadRequestException e) {
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(false)
            .message(e.getMessage())
            .errorCode("INVALID_JOB_STATE")
            .build());
        failedRegistrations++;
        
      } catch (ConflictException e) {
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(false)
            .message("Already registered as referrer for this job")
            .errorCode("ALREADY_REGISTERED")
            .build());
        failedRegistrations++;
        
      } catch (ForbiddenException e) {
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(false)
            .message("Cannot register for jobs from different company")
            .errorCode("DIFFERENT_COMPANY")
            .build());
        failedRegistrations++;
        
      } catch (Exception e) {
        results.add(com.tymbl.jobs.dto.JobReferrerRegistrationResult.builder()
            .jobId(jobId)
            .success(false)
            .message("Unexpected error occurred")
            .errorCode("UNKNOWN_ERROR")
            .build());
        failedRegistrations++;
        logger.error("Unexpected error registering as referrer for job {}: {}", jobId, e.getMessage(), e);
      }
    }

    String overallMessage = String.format("Registration completed: %d successful, %d failed out of %d jobs", 
        successfulRegistrations, failedRegistrations, jobIds.size());

    return com.tymbl.jobs.dto.MultipleJobReferrerRegistrationResponse.builder()
        .results(results)
        .totalJobs(jobIds.size())
        .successfulRegistrations(successfulRegistrations)
        .failedRegistrations(failedRegistrations)
        .overallMessage(overallMessage)
        .build();
  }

  private JobResponse mapToResponse(Job job) {
    JobResponse response = new JobResponse();
    response.setId(job.getId());
    response.setTitle(job.getTitle());
    response.setDescription(job.getDescription());
    response.setCityId(job.getCityId());
    response.setCountryId(job.getCountryId());
    response.setDesignationId(job.getDesignationId());
    response.setDesignation(job.getDesignation());
    response.setMinSalary(job.getMinSalary());
    response.setMaxSalary(job.getMaxSalary());
    response.setMinExperience(job.getMinExperience());
    response.setMaxExperience(job.getMaxExperience());
    response.setJobType(job.getJobType());
    response.setCurrencyId(job.getCurrencyId());
    response.setCompanyId(job.getCompanyId());
    response.setCompany(job.getCompany());
    response.setPostedBy(job.getPostedById());
    response.setActive(job.isActive());
    response.setCreatedAt(job.getCreatedAt());
    response.setUpdatedAt(job.getUpdatedAt());

    // Map tags to response
    if (job.getTags() != null) {
      response.setTags(job.getTags());
    }

    // Map openingCount to response
    response.setOpeningCount(job.getOpeningCount());

    // Map new fields
    response.setUniqueUrl(job.getUniqueUrl());
    response.setPlatform(job.getPlatform());
    response.setApproved(job.getApproved());

    // Initialize referrer data (will be populated by bulk method for multiple jobs)
    response.setReferrerUserIds(new ArrayList<>());
    response.setReferrerCount(0);

    // Enrich with dropdown values
    enrichJobResponseWithDropdownValues(response);

    return response;
  }


  /**
   * Populate referrer user IDs for multiple jobs in a single query
   */
  private void populateReferrerUserIdsForJobs(List<JobResponse> jobResponses) {
    if (jobResponses == null || jobResponses.isEmpty()) {
      return;
    }

    // Extract job IDs
    List<Long> jobIds = jobResponses.stream()
        .map(JobResponse::getId)
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

    if (jobIds.isEmpty()) {
      return;
    }

    try {
      // Fetch all referrer user IDs for these jobs in a single query
      List<Map<String, Object>> referrerData = jobReferrerRepository.findReferrerUserIdsByJobIds(jobIds);
      
      // Group by job ID
      Map<Long, List<Long>> referrersByJobId = referrerData.stream()
          .collect(Collectors.groupingBy(
              data -> (Long) data.get("jobId"),
              Collectors.mapping(data -> (Long) data.get("userId"), Collectors.toList())
          ));

      // Populate referrer user IDs for each job
      for (JobResponse jobResponse : jobResponses) {
        if (jobResponse.getId() != null) {
          List<Long> referrerUserIds = referrersByJobId.getOrDefault(jobResponse.getId(), new ArrayList<>());
          jobResponse.setReferrerUserIds(referrerUserIds);
          jobResponse.setReferrerCount(referrerUserIds.size());
        } else {
          jobResponse.setReferrerUserIds(new ArrayList<>());
          jobResponse.setReferrerCount(0);
        }
      }
    } catch (Exception e) {
      logger.warn("Failed to fetch referrer user IDs for jobs: {}", e.getMessage());
      // Set empty lists for all jobs on error
      for (JobResponse jobResponse : jobResponses) {
        jobResponse.setReferrerUserIds(new ArrayList<>());
        jobResponse.setReferrerCount(0);
      }
    }
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
      logger.warn("Failed to enrich job response with dropdown values for job {}: {}",
          response.getId(), e.getMessage());
    }
  }

  private JobResponse mapToResponseWithRole(Job job, User user) {
    JobResponse response = mapToResponse(job);

    // Set user role based on whether the user is the poster or a referrer
    if (job.getPostedById().equals(user.getId())) {
      response.setUserRole("POSTER");
      response.setActualPostedBy(job.getPostedById()); // Same as postedBy
    } else {
      response.setUserRole("REFERRER");
      response.setActualPostedBy(job.getPostedById()); // The actual poster's ID
    }

    return response;
  }

  @Transactional(readOnly = true)
  public JobDetailsWithReferrersResponse getJobDetailsWithReferrers(Long jobId) {
    Job job = jobRepository.findById(jobId)
        .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

    JobDetailsWithReferrersResponse response = new JobDetailsWithReferrersResponse();

    // Use mapToResponse to get job details with dropdown enrichment
    JobResponse jobResponse = mapToResponse(job);

    // Map job details from enriched JobResponse
    response.setId(jobResponse.getId());
    response.setTitle(jobResponse.getTitle());
    response.setDescription(jobResponse.getDescription());
    response.setCityId(jobResponse.getCityId());
    response.setCityName(jobResponse.getCityName()); // Dropdown value
    response.setCountryId(jobResponse.getCountryId());
    response.setCountryName(jobResponse.getCountryName()); // Dropdown value
    response.setDesignationId(jobResponse.getDesignationId());
    response.setDesignation(jobResponse.getDesignation());
    response.setDesignationName(jobResponse.getDesignationName()); // Dropdown value
    response.setMinSalary(jobResponse.getMinSalary());
    response.setMaxSalary(jobResponse.getMaxSalary());
    response.setMinExperience(jobResponse.getMinExperience());
    response.setMaxExperience(jobResponse.getMaxExperience());
    response.setJobType(jobResponse.getJobType());
    response.setCurrencyId(jobResponse.getCurrencyId());
    response.setCurrencyName(jobResponse.getCurrencyName()); // Dropdown value
    response.setCurrencySymbol(jobResponse.getCurrencySymbol()); // Dropdown value
    response.setCompanyId(jobResponse.getCompanyId());
    response.setCompany(jobResponse.getCompany());
    response.setCompanyName(jobResponse.getCompanyName()); // Dropdown value
    response.setPostedBy(jobResponse.getPostedBy());
    response.setActive(jobResponse.isActive());
    response.setCreatedAt(jobResponse.getCreatedAt());
    response.setUpdatedAt(jobResponse.getUpdatedAt());
    response.setTags(jobResponse.getTags());
    response.setOpeningCount(jobResponse.getOpeningCount());
    response.setUniqueUrl(jobResponse.getUniqueUrl());
    response.setPlatform(jobResponse.getPlatform());
    response.setApproved(jobResponse.getApproved());
    response.setApprovalStatus(jobResponse.getApprovalStatus());

    // Get referrers with detailed profiles
    List<JobReferrer> referrers = jobReferrerRepository.findByJobId(jobId);
    List<JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse> referrerResponses = new java.util.ArrayList<>();

    for (JobReferrer ref : referrers) {
      User user = ref.getUser();
      // Enrich user data with all names (company, designation, department, country, city)
      user = enrichUserWithAllNames(user);

      JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse referrerResponse = new JobDetailsWithReferrersResponse.JobReferrerWithProfileResponse();

      // Basic user info
      referrerResponse.setUserId(user.getId());
      referrerResponse.setUserName(
          user.getFirstName() + (user.getLastName() != null ? (" " + user.getLastName()) : ""));
      referrerResponse.setEmail(user.getEmail());
      referrerResponse.setDesignation(user.getDesignation());
      referrerResponse.setCompany(user.getCompany());
      referrerResponse.setCompanyId(user.getCompanyId());

      // Enrich referrer dropdown values
      if (user.getCompanyId() != null) {
        String companyName = dropdownService.getCompanyNameById(user.getCompanyId());
        referrerResponse.setCompanyName(companyName);
      }

      // Experience
      referrerResponse.setYearsOfExperience(String.valueOf(user.getYearsOfExperience()));
      referrerResponse.setMonthsOfExperience(String.valueOf(user.getMonthsOfExperience()));

      // Education
      if (user.getEducation() != null && !user.getEducation().isEmpty()) {
        String education = user.getEducation().stream()
            .map(edu -> edu.getDegree() + " from " + edu.getInstitution())
            .collect(Collectors.joining(", "));
        referrerResponse.setEducation(education);
      }

      // Social profiles
      referrerResponse.setPortfolioWebsite(user.getPortfolioWebsite());
      referrerResponse.setLinkedInProfile(user.getLinkedInProfile());
      referrerResponse.setGithubProfile(user.getGithubProfile());

      // Referrer metrics
      int numApplicationsAccepted = (int) jobApplicationRepository.findByJobId(jobId).stream()
          .filter(app -> app.getJobId().equals(jobId) && app.getJobReferrerId() != null
              && app.getJobReferrerId().equals(ref.getId())
              && app.getStatus() == JobApplication.ApplicationStatus.SHORTLISTED)
          .count();
      referrerResponse.setNumApplicationsAccepted(numApplicationsAccepted);

      double feedbackScore = referrerFeedbackRepository.findByJobReferrerId(ref.getId()).stream()
          .filter(fb -> fb.getScore() != null)
          .mapToInt(ReferrerFeedback::getScore)
          .average().orElse(0.0);
      referrerResponse.setFeedbackScore(feedbackScore);

      double designationScore = getDesignationScore(user.getDesignation());
      double overallScore =
          designationWeight * designationScore + acceptedWeight * numApplicationsAccepted
              + feedbackWeight * feedbackScore;
      referrerResponse.setOverallScore(overallScore);

      // Registration date (using created timestamp from JobReferrer if available)
      referrerResponse.setRegisteredAt(
          ref.getCreatedAt() != null ? ref.getCreatedAt() : job.getCreatedAt());

      referrerResponses.add(referrerResponse);
    }

    // Sort referrers by overall score descending
    referrerResponses.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));

    response.setReferrers(referrerResponses);
    response.setReferrerCount(referrerResponses.size());

    return response;
  }

  /**
   * Generates a random unique URL for jobs with "other" platform
   *
   * @return A random unique URL string
   */
  private String generateRandomUniqueUrl() {
    return "other_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString()
        .substring(0, 8);
  }
}