package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.entity.Job;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.IndustryCacheService;
import com.tymbl.jobs.constants.ElasticsearchConstants;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

      log.info("Successfully synced job {} to Elasticsearch with result: {}",
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
          new ElasticsearchCompanyIndexingService(elasticsearchClient, companyRepository, dropdownService)
              .updateCompanyJobCount(job.getCompanyId());
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

    document.put("tags", job.getTags());
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
    if (job.getTags() != null) {
      searchableText.append(String.join(" ", job.getTags())).append(" ");
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
  public void reindexAllJobs() {
    List<Job> jobs = jobRepository.findAll();
    log.info("Starting reindex of {} jobs to Elasticsearch", jobs.size());

    int batchSize = 1;
    int totalBatches = (int) Math.ceil((double) jobs.size() / batchSize);
    int totalSuccessCount = 0;
    int totalFailureCount = 0;

    log.info("Processing {} jobs in {} batches of {} each", jobs.size(), totalBatches, batchSize);

    for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
      int startIndex = batchIndex * batchSize;
      int endIndex = Math.min(startIndex + batchSize, jobs.size());
      List<Job> batch = jobs.subList(startIndex, endIndex);

      log.info("Processing batch {}/{}: jobs {} to {}",
          batchIndex + 1, totalBatches, startIndex + 1, endIndex);

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("Building bulk request for batch {} with {} jobs", batchIndex + 1, batch.size());

      for (Job job : batch) {
        try {
          Map<String, Object> jobDocument = buildJobDocument(job);
          bulkRequest.operations(op -> op
              .index(idx -> idx
                  .index(ElasticsearchConstants.JOBS_INDEX)
                  .id(job.getId().toString())
                  .document(jobDocument)
              )
          );
        } catch (Exception e) {
          log.error("Failed to build document for job {}. Error: {}", job.getId(), e.getMessage());
          totalFailureCount++;
        }
      }

      try {
        log.info("Executing bulk request to Elasticsearch for batch {} with {} jobs",
            batchIndex + 1, batch.size());
        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        log.info("Bulk request completed for batch {}. Response received: {} items, Errors: {}",
            batchIndex + 1, response.items().size(), response.errors());

        int batchSuccessCount = 0;
        int batchFailureCount = 0;

        if (response.errors()) {
          batchFailureCount = response.items().size();
          log.error("Failed to index jobs in batch {}: {}",
              batchIndex + 1,
              response.items().stream().map(item -> item.error().reason())
                  .collect(Collectors.joining(", ")));
        } else {
          batchSuccessCount = batch.size();
          log.info("All {} jobs in batch {} indexed successfully", batchSuccessCount,
              batchIndex + 1);
        }

        totalSuccessCount += batchSuccessCount;
        totalFailureCount += batchFailureCount;

        log.info("Batch {} completed - Success: {}, Failures: {}", batchIndex + 1,
            batchSuccessCount, batchFailureCount);

      } catch (Exception e) {
        log.error("Error executing bulk request for batch {}: {}", batchIndex + 1, e.getMessage(),
            e);
        totalFailureCount += batch.size();
      }
    }

    log.info("Reindex completed. Total Success: {}, Total Failures: {}, Batches: {}",
        totalSuccessCount, totalFailureCount, totalBatches);
  }
}
