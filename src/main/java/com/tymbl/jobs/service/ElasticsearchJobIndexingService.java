package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.entity.Job;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.IndustryCacheService;
import com.tymbl.jobs.constants.ElasticsearchConstants;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for indexing jobs to Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchJobIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final DropdownService dropdownService;
  private final IndustryCacheService industryCacheService;
  private final CompanyRepository companyRepository;
  private final JobRepository jobRepository;
  private final SkillRepository skillRepository;
  private final ElasticsearchCompanyIndexingService elasticsearchCompanyIndexingService;

  /**
   * Sync a job to Elasticsearch (save or update) Does not fail the main transaction if ES fails
   */
  public void syncJobToElasticsearch(Job job) {
    try {
      Map<String, Object> jobDocument = buildJobDocument(job);

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(ElasticsearchConstants.JOBS_INDEX)
          .id(job.getId().toString())
          .document(jobDocument)
      );

      IndexResponse response = elasticsearchClient.index(indexRequest);

      log.info("✅ Successfully synced job {} to Elasticsearch with result: {}",
          job.getId(), response.result().name());

      // Flush industry cache to ensure consistency after job data changes
      try {
        industryCacheService.flushCache();
        log.debug("Industry cache flushed after syncing job {}", job.getId());
      } catch (Exception cacheException) {
        log.warn("Failed to flush industry cache after syncing job {}: {}",
            job.getId(), cacheException.getMessage());
        // Don't fail the main operation if cache flush fails
      }

      // Update job count in companies index for this job's company
      try {
        if (job.getCompanyId() != null) {
          elasticsearchCompanyIndexingService.updateCompanyJobCount(job.getCompanyId());
        }
      } catch (Exception e) {
        log.warn("Failed to update company jobCount after syncing job {}: {}", job.getId(), e.getMessage());
      }

    } catch (Exception e) {
      log.error("Failed to sync job {} to Elasticsearch. Error: {}", job.getId(), e.getMessage(),
          e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Safely get skill IDs from job, handling lazy loading
   */
  private Set<Long> getSkillIdsSafely(Job job) {
    try {
      return job.getSkillIds() != null ? job.getSkillIds() : new HashSet<>();
    } catch (Exception e) {
      log.warn("Error accessing skillIds for job {}: {}", job.getId(), e.getMessage());
      return new HashSet<>();
    }
  }

  /**
   * Safely get tags from job, handling lazy loading
   */
  private Set<String> getTagsSafely(Job job) {
    try {
      return job.getTags() != null ? job.getTags() : new HashSet<>();
    } catch (Exception e) {
      log.warn("Error accessing tags for job {}: {}", job.getId(), e.getMessage());
      return new HashSet<>();
    }
  }

  /**
   * Build job document for Elasticsearch
   */
  private Map<String, Object> buildJobDocument(Job job) {
    Map<String, Object> document = new HashMap<>();

    // Basic job fields
    document.put(ElasticsearchConstants.FIELD_ID, job.getId());
    document.put("title", job.getTitle());
    document.put("description", job.getDescription());
    document.put(ElasticsearchConstants.FIELD_CITY_ID, job.getCityId());
    document.put("cityName", job.getCityName());
    document.put(ElasticsearchConstants.FIELD_COUNTRY_ID, job.getCountryId());
    document.put("countryName", job.getCountryName());
    document.put(ElasticsearchConstants.FIELD_DESIGNATION_ID, job.getDesignationId());
    document.put("minSalary", job.getMinSalary());
    document.put("maxSalary", job.getMaxSalary());
    document.put("minExperience", job.getMinExperience());
    document.put("maxExperience", job.getMaxExperience());
    document.put("jobType", job.getJobType() != null ? job.getJobType().name() : null);
    document.put("currencyId", job.getCurrencyId());
    document.put(ElasticsearchConstants.FIELD_COMPANY_ID, job.getCompanyId());
    document.put("postedById", job.getPostedById());
    document.put(ElasticsearchConstants.FIELD_ACTIVE, job.isActive());

    // Convert LocalDateTime to Date to avoid Jackson serialization issues
    if (job.getCreatedAt() != null) {
      document.put(ElasticsearchConstants.FIELD_CREATED_AT,
          java.sql.Timestamp.valueOf(job.getCreatedAt()));
    }
    if (job.getUpdatedAt() != null) {
      document.put(ElasticsearchConstants.FIELD_UPDATED_AT,
          java.sql.Timestamp.valueOf(job.getUpdatedAt()));
    }

    // Add job skills and tags as lists
    List<String> skillNames = new ArrayList<>();
    Set<Long> skillIds = getSkillIdsSafely(job);
    if (!skillIds.isEmpty()) {
      document.put("skillIds", new ArrayList<>(skillIds));
      
      // Fetch skill names for better searchability
      for (Long skillId : skillIds) {
        try {
          Skill skill = skillRepository.findById(skillId).orElse(null);
          if (skill != null) {
            skillNames.add(skill.getName());
          }
        } catch (Exception e) {
          log.warn("Error fetching skill name for ID {}: {}", skillId, e.getMessage());
        }
      }
      document.put("skillNames", skillNames);
    } else {
      document.put("skillIds", new ArrayList<>());
      document.put("skillNames", new ArrayList<>());
    }
    
    // Convert tags from Set to List
    Set<String> tags = getTagsSafely(job);
    if (!tags.isEmpty()) {
      document.put("tags", new ArrayList<>(tags));
    } else {
      document.put("tags", new ArrayList<>());
    }
    
    document.put("openingCount", job.getOpeningCount());
    document.put("uniqueUrl", job.getUniqueUrl());
    document.put("platform", job.getPlatform());

    // Fetch and add company and designation names
    String companyName = null;
    if (job.getCompanyId() != null) {
      companyName = dropdownService.getCompanyNameById(job.getCompanyId());
    }
    document.put(ElasticsearchConstants.FIELD_COMPANY_NAME, companyName);

    String designationName = null;
    if (job.getDesignationId() != null) {
      designationName = dropdownService.getDesignationNameById(job.getDesignationId());
    }
    document.put(ElasticsearchConstants.FIELD_DESIGNATION_NAME, designationName);

    // Fetch and add industry details
    String primaryIndustryName = null;
    String secondaryIndustries = null;
    if (job.getCompanyId() != null) {
      try {
        Company company = companyRepository.findById(job.getCompanyId()).orElse(null);
        if (company != null) {
          if (company.getPrimaryIndustryId() != null) {
            primaryIndustryName = dropdownService.getIndustryNameById(
                company.getPrimaryIndustryId());
          }
          secondaryIndustries = company.getSecondaryIndustries();
        }
      } catch (Exception e) {
        log.warn("Error fetching industry details for company ID {}: {}", job.getCompanyId(),
            e.getMessage());
      }
    }
    document.put(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME, primaryIndustryName);
    document.put(ElasticsearchConstants.FIELD_SECONDARY_INDUSTRIES, secondaryIndustries);

    // Build searchableText (all text fields except description)
    StringBuilder searchableText = new StringBuilder();

    if (job.getTitle() != null) {
      searchableText.append(job.getTitle()).append(" ");
    }
    if (designationName != null) {
      searchableText.append(designationName).append(" ");
    }
    if (companyName != null) {
      searchableText.append(companyName).append(" ");
    }
    if (primaryIndustryName != null) {
      searchableText.append(primaryIndustryName).append(" ");
    }
    if (secondaryIndustries != null) {
      searchableText.append(secondaryIndustries).append(" ");
    }
    // Add skills to searchable text (using already fetched skill names)
    if (!skillNames.isEmpty()) {
      for (String skillName : skillNames) {
        if (skillName != null) {
          searchableText.append(skillName).append(" ");
        }
      }
    }
    
    // Add tags to searchable text (as list)
    Set<String> jobTags = getTagsSafely(job);
    if (!jobTags.isEmpty()) {
      searchableText.append(String.join(" ", jobTags)).append(" ");
    }
    if (job.getPlatform() != null) {
      searchableText.append(job.getPlatform()).append(" ");
    }
    if (job.getJobType() != null) {
      searchableText.append(job.getJobType().name()).append(" ");
    }

    // Add city and country names
    if (job.getCityName() != null) {
      searchableText.append(job.getCityName()).append(" ");
    }
    if (job.getCountryName() != null) {
      searchableText.append(job.getCountryName()).append(" ");
    }

    document.put(ElasticsearchConstants.FIELD_SEARCHABLE_TEXT, searchableText.toString().trim());

    return document;
  }

  /**
   * Reindex all jobs from database to Elasticsearch
   */
  @Transactional(readOnly = true)
  public void reindexAllJobs() {
    long startTime = System.currentTimeMillis();
    log.info("🚀 Starting reindex of all jobs from database to Elasticsearch");
    
    log.info("📊 Querying database for all jobs with collections...");
    List<Job> jobs = jobRepository.findAllWithCollections();
    log.info("📋 Found {} jobs in database for reindexing", jobs.size());

    if (jobs.isEmpty()) {
      log.info("✅ No jobs found in database - reindex process completed immediately");
      return;
    }

    log.info("🔄 Starting reindex of {} jobs to Elasticsearch (centralized sync)", jobs.size());

    int totalSuccessCount = 0;
    int totalFailureCount = 0;
    int currentJobIndex = 0;

    for (Job job : jobs) {
      currentJobIndex++;
      long jobStartTime = System.currentTimeMillis();
      
      log.info("⏳ Processing job {}/{}: ID={}, Title='{}', Company='{}'", 
          currentJobIndex, jobs.size(), job.getId(), 
          job.getTitle(), job.getCompany());

      try {
        log.debug("🔄 Syncing job ID: {} to Elasticsearch", job.getId());
        
        // Sync to Elasticsearch (non-blocking semantics preserved with try/catch)
        syncJobToElasticsearch(job);
        
        long jobProcessingTime = System.currentTimeMillis() - jobStartTime;
        totalSuccessCount++;
        
        log.info("✅ Successfully synced job {}/{}: ID={}, Title='{}', Company='{}' in {}ms", 
            currentJobIndex, jobs.size(), job.getId(), 
            job.getTitle(), job.getCompany(), jobProcessingTime);
            
      } catch (Exception e) {
        long jobProcessingTime = System.currentTimeMillis() - jobStartTime;
        totalFailureCount++;
        log.error("💥 Failed to sync job {}/{}: ID={}, Title='{}', Company='{}' after {}ms: {}", 
            currentJobIndex, jobs.size(), job.getId(), 
            job.getTitle(), job.getCompany(), jobProcessingTime, e.getMessage(), e);
      }
    }

    long totalProcessingTime = System.currentTimeMillis() - startTime;
    log.info("🎉 Reindex completed successfully! 📊 Summary: Total={}, Success={}, Failures={}, TotalTime={}ms, AvgTimePerJob={}ms", 
        jobs.size(), totalSuccessCount, totalFailureCount, totalProcessingTime, 
        jobs.size() > 0 ? totalProcessingTime / jobs.size() : 0);
  }
}
