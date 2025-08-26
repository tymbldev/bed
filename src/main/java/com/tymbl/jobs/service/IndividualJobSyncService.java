package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.entity.PendingContent;
import com.tymbl.common.repository.PendingContentRepository;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.jobs.service.ExternalJobTagger.TaggingResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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
  private final PendingContentRepository pendingContentRepository;

  /**
   * Sync a single external job to the main Job table This method is transactional to ensure data
   * consistency for each individual job
   */
  @Transactional
  public SyncResult syncIndividualJob(ExternalJobDetail externalJob) {
    SyncResult result = new SyncResult();

    try {
      // Reload the entity within the transaction to ensure it's properly managed
      ExternalJobDetail managedExternalJob = externalJobDetailRepository.findById(
              externalJob.getId())
          .orElseThrow(
              () -> new RuntimeException("External job not found: " + externalJob.getId()));

      // Check if job already exists in Job table by portal job ID
      Optional<Job> existingJob = jobRepository.findByPortalJobId(
          managedExternalJob.getPortalJobId());

      if (existingJob.isPresent()) {
        log.info("Job with portal ID {} already exists in Job table, skipping",
            managedExternalJob.getPortalJobId());
        // Mark as synced even if it already exists
        managedExternalJob.setIsSyncedToJobTable(true);
        externalJobDetailRepository.save(managedExternalJob);
        result.setSuccess(true);
        result.setMessage("Job already exists, marked as synced");
        return result;
      }

      // Tag the external job with company and designation
      TaggingResult taggingResult = externalJobTagger.tagExternalJob(managedExternalJob);

      if (taggingResult.getError() != null) {
        log.warn("Failed to tag external job {}: {}", managedExternalJob.getId(),
            taggingResult.getError());

        // Record tagging failure in pending_content table
        try {
          PendingContent pendingContent = PendingContent.builder()
              .entityName("External Job ID: " + managedExternalJob.getId())
              .entityType(PendingContent.EntityType.COMPANY)
              .sourceTable("external_job_details")
              .sourceId(managedExternalJob.getId())
              .portalName(managedExternalJob.getPortalName())
              .notes("Tagging failed with error: " + taggingResult.getError())
              .createdAt(java.time.LocalDateTime.now())
              .attemptCount(0)
              .build();
          pendingContentRepository.save(pendingContent);
          log.info("Recorded tagging failure in pending_content table for external job {}",
              managedExternalJob.getId());
        } catch (Exception e) {
          log.warn("Failed to record tagging failure in pending_content table for external job {}",
              managedExternalJob.getId(), e);
        }

        result.setSuccess(false);
        result.setMessage("Tagging failed: " + taggingResult.getError());
        return result;
      }

      if (taggingResult.getCompanyId() == null || taggingResult.getDesignationId() == null) {
        log.warn("Tagging result missing company or designation for external job {}",
            managedExternalJob.getId());

        // Record incomplete tagging result in pending_content table
        try {
          String missingFields = "";
          if (taggingResult.getCompanyId() == null) {
            missingFields += "company, ";
          }
          if (taggingResult.getDesignationId() == null) {
            missingFields += "designation, ";
          }
          missingFields = missingFields.replaceAll(", $", ""); // Remove trailing comma

          PendingContent pendingContent = PendingContent.builder()
              .entityName("External Job ID: " + managedExternalJob.getId())
              .entityType(PendingContent.EntityType.COMPANY)
              .sourceTable("external_job_details")
              .sourceId(managedExternalJob.getId())
              .portalName(managedExternalJob.getPortalName())
              .notes("Tagging result incomplete - missing: " + missingFields + ". Company: " +
                  (taggingResult.getCompanyName() != null ? taggingResult.getCompanyName() : "null")
                  +
                  ", Designation: " + (taggingResult.getDesignationName() != null
                  ? taggingResult.getDesignationName() : "null"))
              .createdAt(java.time.LocalDateTime.now())
              .attemptCount(0)
              .build();
          pendingContentRepository.save(pendingContent);
          log.info(
              "Recorded incomplete tagging result in pending_content table for external job {}",
              managedExternalJob.getId());
        } catch (Exception e) {
          log.warn(
              "Failed to record incomplete tagging result in pending_content table for external job {}",
              managedExternalJob.getId(), e);
        }

        result.setSuccess(false);
        result.setMessage("Tagging result incomplete");
        return result;
      }

      // Create new Job entity
      Job job = createJobFromExternalJob(managedExternalJob, taggingResult);

      // Save the job
      Job savedJob = jobRepository.save(job);

      // Mark external job as synced
      managedExternalJob.setIsSyncedToJobTable(true);
      externalJobDetailRepository.save(managedExternalJob);

      log.info("Successfully synced external job {} to Job table with ID {}",
          managedExternalJob.getId(), savedJob.getId());

      result.setSuccess(true);
      result.setMessage("Job synced successfully");
      result.setJobId(savedJob.getId());

    } catch (Exception e) {
      log.error("Error syncing external job {}: {}", externalJob.getId(), e.getMessage(), e);

      // Record sync failure in pending_content table
      try {
        PendingContent pendingContent = PendingContent.builder()
            .entityName("External Job ID: " + externalJob.getId())
            .entityType(PendingContent.EntityType.COMPANY)
            .sourceTable("external_job_details")
            .sourceId(externalJob.getId())
            .portalName(externalJob.getPortalName())
            .notes("Sync failed with exception: " + e.getMessage())
            .createdAt(java.time.LocalDateTime.now())
            .attemptCount(0)
            .build();
        pendingContentRepository.save(pendingContent);
        log.info("Recorded sync failure in pending_content table for external job {}",
            externalJob.getId());
      } catch (Exception saveException) {
        log.warn("Failed to record sync failure in pending_content table for external job {}",
            externalJob.getId(), saveException);
      }

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
    job.setTitle(externalJob.getRefinedTitle() != null ? externalJob.getRefinedTitle()
        : externalJob.getJobTitle());
    job.setDescription(
        externalJob.getRefinedDescription() != null ? externalJob.getRefinedDescription()
            : externalJob.getJobDescription());
    job.setPortalJobId(externalJob.getPortalJobId());
    job.setIsSyncedFromExternal(true);

    // Location information - use tagged city and country if available
    if (taggingResult.getCityId() != null) {
      job.setCityId(taggingResult.getCityId());
      job.setCityName(taggingResult.getCityName());
    }

    if (taggingResult.getCountryId() != null) {
      job.setCountryId(taggingResult.getCountryId());
      job.setCountryName(taggingResult.getCountryName());
    }

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
    job.setApprovalStatus(JobApprovalStatus.APPROVED);

    // Platform information
    job.setPlatform(externalJob.getPortalName());

    // Opening count
    job.setOpeningCount(1);

    // Skills and tags from tagging result
    if (taggingResult.getSkillIds() != null && !taggingResult.getSkillIds().isEmpty()) {
      job.setSkillIds(new HashSet<>(taggingResult.getSkillIds()));
    }

    if (taggingResult.getJobTags() != null && !taggingResult.getJobTags().isEmpty()) {
      job.setTags(new HashSet<>(taggingResult.getJobTags()));
    }

    // Set timestamps from external job data
    if (externalJob.getPostedDate() != null) {
      job.setPostedAt(externalJob.getPostedDate());
    } else {
      job.setPostedAt(LocalDateTime.now()); // Fallback to current time
    }

    if (externalJob.getCreatedDate() != null) {
      job.setCreatedAt(externalJob.getCreatedDate());
    } else {
      job.setCreatedAt(LocalDateTime.now()); // Fallback to current time
    }

    if (externalJob.getUpdatedDate() != null) {
      job.setUpdatedAt(externalJob.getUpdatedDate());
    } else {
      job.setUpdatedAt(LocalDateTime.now()); // Fallback to current time
    }

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
