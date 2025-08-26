package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for indexing companies to Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchCompanyIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final CompanyRepository companyRepository;
  private final DropdownService dropdownService;

  private static final String COMPANIES_INDEX = "companies";

  /**
   * Index all companies to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCompanies() {
    log.info("Starting to index all companies to Elasticsearch");

    try {
      int batchSize = 500;
      int totalSuccessCount = 0;
      int totalFailureCount = 0;
      int totalCompanies = 0;
      int batchIndex = 0;

      log.info("Processing companies in batches of {} each", batchSize);

      while (true) {
        Pageable pageable = PageRequest.of(batchIndex, batchSize);
        Page<Company> companyPage = companyRepository.findAll(pageable);

        List<Company> batch = companyPage.getContent();

        if (batch.isEmpty()) {
          break; // No more companies to process
        }

        totalCompanies += batch.size();
        log.info("Processing batch {}: companies {} to {} (total processed: {})",
            batchIndex + 1, (batchIndex * batchSize) + 1, (batchIndex * batchSize) + batch.size(),
            totalCompanies);

        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
        log.info("Building bulk request for batch {} with {} companies", batchIndex + 1,
            batch.size());

        for (Company company : batch) {
          log.info("Building document for company: {} (ID: {})", company.getName(),
              company.getId());
          Map<String, Object> companyDoc = buildCompanyDocument(company);
          log.info("Company document built successfully for: {} - Document size: {} fields",
              company.getName(), companyDoc.size());

          bulkRequest.operations(op -> op
              .index(idx -> idx
                  .index(COMPANIES_INDEX)
                  .id(company.getId().toString())
                  .document(companyDoc)
              )
          );
        }

        log.info("Executing bulk request to Elasticsearch for batch {} with {} companies",
            batchIndex + 1, batch.size());
        BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
        log.info("Bulk request completed for batch {}. Response received: {} items, Errors: {}",
            batchIndex + 1, response.items().size(), response.errors());
        Thread.sleep(10000); // Brief pause to avoid overwhelming ES

        int batchSuccessCount = 0;
        int batchFailureCount = 0;

        if (response.errors()) {
          batchFailureCount = response.items().size();
          log.error("Failed to index companies in batch {}: {}",
              batchIndex + 1,
              response.items().stream().map(item -> item.error().reason())
                  .collect(Collectors.joining(", ")));
        } else {
          batchSuccessCount = batch.size();
          log.info("All {} companies in batch {} indexed successfully", batchSuccessCount,
              batchIndex + 1);
        }

        totalSuccessCount += batchSuccessCount;
        totalFailureCount += batchFailureCount;

        log.info("Batch {} completed - Success: {}, Failures: {}", batchIndex + 1,
            batchSuccessCount, batchFailureCount);

        // Check if this was the last page
        if (!companyPage.hasNext()) {
          break;
        }

        batchIndex++;
      }

      int totalBatches = batchIndex + 1;

      Map<String, Object> result = new HashMap<>();
      result.put("totalCompanies", totalCompanies);
      result.put("indexedSuccessfully", totalSuccessCount);
      result.put("failedToIndex", totalFailureCount);
      result.put("totalBatches", totalBatches);
      result.put("batchSize", batchSize);
      result.put("message", "Company indexing completed");

      log.info("Company indexing completed - Total Success: {}, Total Failures: {}, Batches: {}",
          totalSuccessCount, totalFailureCount, totalBatches);

      return result;

    } catch (Exception e) {
      log.error("Error indexing companies to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index companies: " + e.getMessage());
      return error;
    }
  }

  /**
   * Sync a company to Elasticsearch (save or update)
   */
  public void syncCompanyToElasticsearch(Company company) {
    try {
      log.info("Starting to sync company to Elasticsearch: {} (ID: {})", company.getName(),
          company.getId());

      Map<String, Object> companyDocument = buildCompanyDocument(company);
      log.info("Company document built successfully for: {} - Document size: {} fields",
          company.getName(), companyDocument.size());

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(COMPANIES_INDEX)
          .id(company.getId().toString())
          .document(companyDocument)
      );
      log.info("Index request prepared for company: {} to index: {}", company.getName(),
          COMPANIES_INDEX);

      log.info("Executing index request to Elasticsearch for company: {}", company.getName());
      IndexResponse response = elasticsearchClient.index(indexRequest);
      log.info("Index request completed for company: {} - Result: {}, Document ID: {}",
          company.getName(), response.result().name(), response.id());

      log.info("Successfully synced company {} to Elasticsearch with result: {}",
          company.getId(), response.result().name());

    } catch (Exception e) {
      log.error("Failed to sync company {} to Elasticsearch. Error: {}", company.getId(),
          e.getMessage(), e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Build company document for Elasticsearch
   */
  private Map<String, Object> buildCompanyDocument(Company company) {
    log.info("Building Elasticsearch document for company: {} (ID: {})", company.getName(),
        company.getId());

    Map<String, Object> doc = new HashMap<>();
    doc.put("id", company.getId());
    doc.put("name", company.getName());
    doc.put("description", company.getDescription());
    doc.put("website", company.getWebsite());
    doc.put("logoUrl", company.getLogoUrl());
    doc.put("careerPageUrl", company.getCareerPageUrl());
    doc.put("linkedinUrl", company.getLinkedinUrl());
    doc.put("headquarters", company.getHeadquarters());
    doc.put("primaryIndustryId", company.getPrimaryIndustryId());
    doc.put("secondaryIndustries", company.getSecondaryIndustries());
    doc.put("companySize", company.getCompanySize());
    doc.put("specialties", company.getSpecialties());
    doc.put("aboutUs", company.getAboutUs());
    doc.put("vision", company.getVision());
    doc.put("mission", company.getMission());
    doc.put("culture", company.getCulture());
    doc.put("isCrawled", company.isCrawled());
    doc.put("similarCompaniesProcessed", company.isSimilarCompaniesProcessed());
    doc.put("industryProcessed", company.isIndustryProcessed());
    doc.put("websiteFetched", company.getWebsiteFetched());
    doc.put("shortname", company.getShortname());

    log.info("Basic company fields added for: {} - Fields: {}", company.getName(), doc.size());

    // Add industry name if available
    if (company.getPrimaryIndustryId() != null) {
      try {
        String industryName = dropdownService.getIndustryNameById(company.getPrimaryIndustryId());
        doc.put("primaryIndustryName", industryName);
        log.info("Industry name added for company: {} - Industry: {}", company.getName(),
            industryName);
      } catch (Exception e) {
        log.warn("Error fetching industry name for company ID {}: {}", company.getId(),
            e.getMessage());
      }
    }

    // Add timestamps (converted to avoid Jackson issues)
    if (company.getCreatedAt() != null) {
      doc.put("createdAt", java.sql.Timestamp.valueOf(company.getCreatedAt()));
    }
    if (company.getUpdatedAt() != null) {
      doc.put("updatedAt", java.sql.Timestamp.valueOf(company.getUpdatedAt()));
    }

    // Build searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for company: {}", company.getName());

    if (company.getName() != null) {
      searchableText.append(company.getName()).append(" ");
    }
    if (company.getDescription() != null) {
      searchableText.append(company.getDescription()).append(" ");
    }
    if (company.getHeadquarters() != null) {
      searchableText.append(company.getHeadquarters()).append(" ");
    }
    if (company.getSpecialties() != null) {
      searchableText.append(company.getSpecialties()).append(" ");
    }
    if (company.getAboutUs() != null) {
      searchableText.append(company.getAboutUs()).append(" ");
    }
    if (company.getVision() != null) {
      searchableText.append(company.getVision()).append(" ");
    }
    if (company.getMission() != null) {
      searchableText.append(company.getMission()).append(" ");
    }
    if (company.getCulture() != null) {
      searchableText.append(company.getCulture()).append(" ");
    }
    if (company.getShortname() != null) {
      searchableText.append(company.getShortname()).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for company: {} - Length: {} characters",
        company.getName(), searchableText.length());

    log.info("Company document built successfully for: {} - Total fields: {}", company.getName(),
        doc.size());
    return doc;
  }

  /**
   * Update job count for a specific company in Elasticsearch
   */
  public void updateCompanyJobCount(Long companyId) {
    try {
      log.info("Starting job count update for company ID: {}", companyId);

      Company company = companyRepository.findById(companyId).orElse(null);
      if (company == null) {
        log.warn("Company not found for job count update: {}", companyId);
        return;
      }
      log.info("Found company: {} (ID: {}) for job count update", company.getName(), companyId);

      // Count active jobs for the company
      long jobCount = companyRepository.countActiveJobsByCompanyId(companyId);
      log.info("Found {} active jobs for company: {}", jobCount, company.getName());

      Map<String, Object> updateDoc = new HashMap<>();
      updateDoc.put("jobCount", jobCount);
      log.info("Prepared update document for company: {} - Job count: {}", company.getName(),
          jobCount);

      log.info("Executing update request to Elasticsearch for company: {} in index: {}",
          company.getName(), COMPANIES_INDEX);
      elasticsearchClient.update(u -> u
              .index(COMPANIES_INDEX)
              .id(companyId.toString())
              .doc(updateDoc)
          , Map.class);

      log.info("Successfully updated job count for company {} (ID: {}) to {} in Elasticsearch",
          company.getName(), companyId, jobCount);
        Thread.sleep(1000);
    } catch (Exception e) {
      log.error("Error updating job count for company {} in Elasticsearch", companyId, e);
    }
  }
}
