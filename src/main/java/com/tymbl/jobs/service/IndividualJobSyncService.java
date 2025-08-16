package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.jobs.service.ExternalJobTagger.TaggingResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndividualJobSyncService {

  private final JobRepository jobRepository;
  private final ExternalJobDetailRepository externalJobDetailRepository;
  private final ExternalJobTagger externalJobTagger;

  /**
   * Sync a single external job to the main Job table This method is transactional to ensure data
   * consistency for each individual job
   */
  @Transactional
  public SyncResult syncIndividualJob(ExternalJobDetail externalJob) {
    SyncResult result = new SyncResult();

    try {
      // Check if job already exists in Job table by portal job ID
      Optional<Job> existingJob = jobRepository.findByPortalJobId(externalJob.getPortalJobId());

      if (existingJob.isPresent()) {
        log.info("Job with portal ID {} already exists in Job table, skipping",
            externalJob.getPortalJobId());
        // Mark as synced even if it already exists
        externalJob.setIsSyncedToJobTable(true);
        externalJobDetailRepository.save(externalJob);
        result.setSuccess(true);
        result.setMessage("Job already exists, marked as synced");
        return result;
      }

      // Tag the external job with company and designation
      TaggingResult taggingResult = externalJobTagger.tagExternalJob(externalJob);

      if (taggingResult.getError() != null) {
        log.warn("Failed to tag external job {}: {}", externalJob.getId(),
            taggingResult.getError());
        result.setSuccess(false);
        result.setMessage("Tagging failed: " + taggingResult.getError());
        return result;
      }
      if (taggingResult.getCompanyId() == null || taggingResult.getDesignationId() == null) {
        log.warn("Tagging result missing company or designation for external job {}",
            externalJob.getId());
        result.setSuccess(false);
        result.setMessage("Tagging result incomplete");
        return result;
      }

      // Create new Job entity
      Job job = createJobFromExternalJob(externalJob, taggingResult);

      // Save the job
      Job savedJob = jobRepository.save(job);

      // Mark external job as synced
      externalJob.setIsSyncedToJobTable(true);
      externalJobDetailRepository.save(externalJob);

      log.info("Successfully synced external job {} to Job table with ID {}",
          externalJob.getId(), savedJob.getId());

      result.setSuccess(true);
      result.setMessage("Job synced successfully");
      result.setJobId(savedJob.getId());

    } catch (Exception e) {
      log.error("Error syncing external job {}: {}", externalJob.getId(), e.getMessage(), e);
      result.setSuccess(false);
      result.setMessage("Error during sync: " + e.getMessage());
    }

    return result;
  }

  /**
   * Create a Job entity from ExternalJobDetail
   */
  private Job createJobFromExternalJob(ExternalJobDetail externalJob, TaggingResult taggingResult) {
    Job job = new Job();

    // Basic job information
    job.setTitle(externalJob.getJobTitle());
    job.setDescription(externalJob.getJobDescription());
    job.setPortalJobId(externalJob.getPortalJobId());
    job.setIsSyncedFromExternal(true);

    // Location information
    job.setCityId(externalJob.getCityId());
    job.setCityName(externalJob.getCityName());
    job.setCountryId(externalJob.getCountryId());
    job.setCountryName(externalJob.getCountryName());

    // Company and designation information
    if (taggingResult.getCompanyId() != null) {
      job.setCompanyId(taggingResult.getCompanyId());
      job.setCompany(taggingResult.getCompanyName());
    } else {
      // Fallback to external company name if tagging failed
      job.setCompany(externalJob.getCompanyName());
      // Set a default company ID (you might want to create a default company)
      job.setCompanyId(1L); // Default company ID
    }

    if (taggingResult.getDesignationId() != null) {
      job.setDesignationId(taggingResult.getDesignationId());
      job.setDesignation(taggingResult.getDesignationName());
    } else {
      // Fallback to external job title if tagging failed
      job.setDesignation(externalJob.getJobTitle());
      // Set a default designation ID (you might want to create a default designation)
      job.setDesignationId(1L); // Default designation ID
    }

    // Salary information
    if (externalJob.getMinimumSalary() != null) {
      job.setMinSalary(externalJob.getMinimumSalary());
    } else {
      job.setMinSalary(BigDecimal.ZERO);
    }

    if (externalJob.getMaximumSalary() != null) {
      job.setMaxSalary(externalJob.getMaximumSalary());
    } else {
      job.setMaxSalary(BigDecimal.ZERO);
    }

    // Experience information
    job.setMinExperience(externalJob.getMinimumExperience());
    job.setMaxExperience(externalJob.getMaximumExperience());

    // Job type (try to map from external data)
    job.setJobType(mapJobType(externalJob.getJobTypes()));

    // Currency (default to USD for now)
    job.setCurrencyId(1L); // Default to USD

    // Posted by (default user ID)
    job.setPostedById(1L); // Default user ID

    // Job status
    job.setActive(true);
    job.setApprovalStatus(com.tymbl.common.entity.JobApprovalStatus.PENDING);

    // Platform information
    job.setPlatform(externalJob.getPortalName());

    // Opening count
    job.setOpeningCount(1);

    // Set timestamps
    job.setCreatedAt(LocalDateTime.now());
    job.setUpdatedAt(LocalDateTime.now());

    return job;
  }

  /**
   * Map external job types to internal JobType enum
   */
  private com.tymbl.common.entity.Job.JobType mapJobType(String externalJobTypes) {
    if (externalJobTypes == null || externalJobTypes.trim().isEmpty()) {
      return com.tymbl.common.entity.Job.JobType.ONSITE; // Default to onsite
    }

    String jobTypes = externalJobTypes.toLowerCase();

    if (jobTypes.contains("remote") || jobTypes.contains("work from home")) {
      return com.tymbl.common.entity.Job.JobType.REMOTE_ONLY;
    } else if (jobTypes.contains("hybrid")) {
      return com.tymbl.common.entity.Job.JobType.HYBRID;
    } else if (jobTypes.contains("onsite") || jobTypes.contains("work from office")) {
      return com.tymbl.common.entity.Job.JobType.ONSITE;
    } else {
      return com.tymbl.common.entity.Job.JobType.ONSITE; // Default
    }
  }

  // Result class for individual job sync
  public static class SyncResult {

    private boolean success;
    private String message;
    private Long jobId;

    // Getters and setters
    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public Long getJobId() {
      return jobId;
    }

    public void setJobId(Long jobId) {
      this.jobId = jobId;
    }
  }
}
