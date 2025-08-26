package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for indexing skills to Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchSkillIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final SkillRepository skillRepository;

  private static final String SKILLS_INDEX = "skills";

  /**
   * Index all skills to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllSkills() {
    log.info("Starting to index all skills to Elasticsearch");

    try {
      List<Skill> skills = skillRepository.findAll();
      log.info("Found {} skills to index", skills.size());

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("Building bulk request for {} skills", skills.size());

      for (Skill skill : skills) {
        log.info("Building document for skill: {} (ID: {})", skill.getName(), skill.getId());
        Map<String, Object> skillDoc = buildSkillDocument(skill);
        log.info(
            "Skill document built successfully for: {} - Document size: {} fields, Category: {}, Similar Skills: {}",
            skill.getName(), skillDoc.size(), skillDoc.get("category"),
            skillDoc.get("similarSkillsByName"));

        bulkRequest.operations(op -> op
            .index(idx -> idx
                .index(SKILLS_INDEX)
                .id(skill.getId().toString())
                .document(skillDoc)
            )
        );
      }

      log.info("Executing bulk request to Elasticsearch for {} skills", skills.size());
      BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
      log.info("Bulk request completed. Response received: {} items, Errors: {}",
          response.items().size(), response.errors());

      int successCount = 0;
      int failureCount = 0;

      if (response.errors()) {
        failureCount = response.items().size();
        log.error("Failed to index skills: {}",
            response.items().stream().map(item -> item.error().reason())
                .collect(Collectors.joining(", ")));
      } else {
        successCount = skills.size();
        log.info("All {} skills indexed successfully", successCount);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("totalSkills", skills.size());
      result.put("indexedSuccessfully", successCount);
      result.put("failedToIndex", failureCount);
      result.put("message", "Skill indexing completed");

      log.info("Skill indexing completed - Success: {}, Failures: {}", successCount, failureCount);

      return result;

    } catch (Exception e) {
      log.error("Error indexing skills to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index skills: " + e.getMessage());
      return error;
    }
  }

  /**
   * Sync a skill to Elasticsearch (save or update)
   */
  public void syncSkillToElasticsearch(Skill skill) {
    try {
      log.info("Starting to sync skill to Elasticsearch: {} (ID: {})", skill.getName(),
          skill.getId());

      Map<String, Object> skillDocument = buildSkillDocument(skill);
      log.info("Skill document built successfully for: {} - Document size: {} fields",
          skill.getName(), skillDocument.size());

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(SKILLS_INDEX)
          .id(skill.getId().toString())
          .document(skillDocument)
      );
      log.info("Index request prepared for skill: {} to index: {}", skill.getName(), SKILLS_INDEX);

      log.info("Executing index request to Elasticsearch for skill: {}", skill.getName());
      IndexResponse response = elasticsearchClient.index(indexRequest);
      log.info("Index request completed for skill: {} - Result: {}, Document ID: {}",
          skill.getName(), response.result().name(), response.id());

      log.info("Successfully synced skill {} to Elasticsearch with result: {}",
          skill.getId(), response.result().name());

    } catch (Exception e) {
      log.error("Failed to sync skill {} to Elasticsearch. Error: {}", skill.getId(),
          e.getMessage(), e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Build skill document for Elasticsearch
   */
  private Map<String, Object> buildSkillDocument(Skill skill) {
    log.info("Building Elasticsearch document for skill: {} (ID: {})", skill.getName(),
        skill.getId());

    Map<String, Object> doc = new HashMap<>();
    doc.put("id", skill.getId());
    doc.put("name", skill.getName());
    doc.put("category", skill.getCategory());
    doc.put("description", skill.getDescription());
    doc.put("usageCount", skill.getUsageCount());
    doc.put("similarSkillsByName", skill.getSimilarSkillsByName());
    doc.put("similarSkillsById", skill.getSimilarSkillsById());
    doc.put("similarSkillsProcessed", skill.isSimilarSkillsProcessed());

    log.info("Basic skill fields added for: {} - Fields: {}", skill.getName(), doc.size());

    // Build searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for skill: {}", skill.getName());

    if (skill.getName() != null) {
      searchableText.append(skill.getName()).append(" ");
    }
    if (skill.getCategory() != null) {
      searchableText.append(skill.getCategory()).append(" ");
    }
    if (skill.getDescription() != null) {
      searchableText.append(skill.getDescription()).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for skill: {} - Length: {} characters",
        skill.getName(), searchableText.length());

    log.info("Skill document built successfully for: {} - Total fields: {}", skill.getName(),
        doc.size());
    return doc;
  }
}
