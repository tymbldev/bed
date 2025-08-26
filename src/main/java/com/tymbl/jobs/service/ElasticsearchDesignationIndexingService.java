package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.service.DropdownService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for indexing designations to Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchDesignationIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final DesignationRepository designationRepository;
  private final DropdownService dropdownService;

  private static final String DESIGNATIONS_INDEX = "designations";

  /**
   * Index all designations to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllDesignations() {
    log.info("Starting to index all designations to Elasticsearch");

    try {
      List<Designation> designations = designationRepository.findAll();
      log.info("Found {} designations to index", designations.size());

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("Building bulk request for {} designations", designations.size());

      for (Designation designation : designations) {
        log.info("Building document for designation: {} (ID: {})", designation.getName(),
            designation.getId());
        Map<String, Object> designationDoc = buildDesignationDocument(designation);
        log.info(
            "Designation document built successfully for: {} - Document size: {} fields, Department: {}",
            designation.getName(), designationDoc.size(), designationDoc.get("departmentName"));

        bulkRequest.operations(op -> op
            .index(idx -> idx
                .index(DESIGNATIONS_INDEX)
                .id(designation.getId().toString())
                .document(designationDoc)
            )
        );
      }

      log.info("Executing bulk request to Elasticsearch for {} designations", designations.size());
      BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
      log.info("Bulk request completed. Response received: {} items, Errors: {}",
          response.items().size(), response.errors());

      int successCount = 0;
      int failureCount = 0;

      if (response.errors()) {
        failureCount = response.items().size();
        log.error("Failed to index designations: {}",
            response.items().stream().map(item -> item.error().reason())
                .collect(Collectors.joining(", ")));
      } else {
        successCount = designations.size();
        log.info("All {} designations indexed successfully", successCount);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("totalDesignations", designations.size());
      result.put("indexedSuccessfully", successCount);
      result.put("failedToIndex", failureCount);
      result.put("message", "Designation indexing completed");

      log.info("Designation indexing completed - Success: {}, Failures: {}", successCount,
          failureCount);

      return result;

    } catch (Exception e) {
      log.error("Error indexing designations to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index designations: " + e.getMessage());
      return error;
    }
  }

  /**
   * Sync a designation to Elasticsearch (save or update)
   */
  public void syncDesignationToElasticsearch(Designation designation) {
    try {
      log.info("Starting to sync designation to Elasticsearch: {} (ID: {})", designation.getName(),
          designation.getId());

      Map<String, Object> designationDocument = buildDesignationDocument(designation);
      log.info("Designation document built successfully for: {} - Document size: {} fields",
          designation.getName(), designationDocument.size());

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(DESIGNATIONS_INDEX)
          .id(designation.getId().toString())
          .document(designationDocument)
      );
      log.info("Index request prepared for designation: {} to index: {}", designation.getName(),
          DESIGNATIONS_INDEX);

      log.info("Executing index request to Elasticsearch for designation: {}",
          designation.getName());
      IndexResponse response = elasticsearchClient.index(indexRequest);
      log.info("Index request completed for designation: {} - Result: {}, Document ID: {}",
          designation.getName(), response.result().name(), response.id());

      log.info("Successfully synced designation {} to Elasticsearch with result: {}",
          designation.getId(), response.result().name());

    } catch (Exception e) {
      log.error("Failed to sync designation {} to Elasticsearch. Error: {}", designation.getId(),
          e.getMessage(), e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Build designation document for Elasticsearch
   */
  private Map<String, Object> buildDesignationDocument(Designation designation) {
    log.info("Building Elasticsearch document for designation: {} (ID: {})", designation.getName(),
        designation.getId());

    Map<String, Object> doc = new HashMap<>();
    doc.put("id", designation.getId());
    doc.put("name", designation.getName());
    doc.put("level", designation.getLevel());
    doc.put("enabled", designation.isEnabled());
    doc.put("similarDesignationsByName", designation.getSimilarDesignationsByName());
    doc.put("similarDesignationsById", designation.getSimilarDesignationsById());
    doc.put("similarDesignationsProcessed", designation.isSimilarDesignationsProcessed());
    doc.put("processedName", designation.getProcessedName());
    doc.put("processedNameGenerated", designation.isProcessedNameGenerated());
    doc.put("departmentId", designation.getDepartmentId());
    doc.put("department", designation.getDepartment());
    doc.put("departmentAssigned", designation.isDepartmentAssigned());

    log.info("Basic designation fields added for: {} - Fields: {}", designation.getName(),
        doc.size());

    // Add department name if available
    if (designation.getDepartmentId() != null) {
      try {
        String departmentName = dropdownService.getDepartmentNameById(
            designation.getDepartmentId());
        doc.put("departmentName", departmentName);
        log.info("Department name added for designation: {} - Department: {}",
            designation.getName(), departmentName);
      } catch (Exception e) {
        log.warn("Error fetching department name for designation ID {}: {}", designation.getId(),
            e.getMessage());
      }
    }

    // Build searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for designation: {}", designation.getName());

    if (designation.getName() != null) {
      searchableText.append(designation.getName()).append(" ");
    }
    if (designation.getProcessedName() != null) {
      searchableText.append(designation.getProcessedName()).append(" ");
    }
    if (designation.getDepartment() != null) {
      searchableText.append(designation.getDepartment()).append(" ");
    }
    if (designation.getDepartmentId() != null) {
      try {
        String departmentName = dropdownService.getDepartmentNameById(
            designation.getDepartmentId());
        if (departmentName != null) {
          searchableText.append(departmentName).append(" ");
        }
      } catch (Exception e) {
        log.warn("Error adding department name to searchable text for designation {}: {}",
            designation.getName(), e.getMessage());
      }
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for designation: {} - Length: {} characters",
        designation.getName(), searchableText.length());

    log.info("Designation document built successfully for: {} - Total fields: {}",
        designation.getName(), doc.size());
    return doc;
  }
}
