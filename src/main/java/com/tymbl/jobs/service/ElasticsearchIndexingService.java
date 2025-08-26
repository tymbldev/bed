package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator service for Elasticsearch indexing operations Delegates specific entity indexing to
 * dedicated services
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final ElasticsearchCompanyIndexingService elasticsearchCompanyIndexingService;
  private final ElasticsearchDesignationIndexingService elasticsearchDesignationIndexingService;
  private final ElasticsearchCityIndexingService elasticsearchCityIndexingService;
  private final ElasticsearchSkillIndexingService elasticsearchSkillIndexingService;
  private final ElasticsearchJobIndexingService elasticsearchJobIndexingService;

  private static final String COMPANIES_INDEX = "companies";
  private static final String DESIGNATIONS_INDEX = "designations";
  private static final String CITIES_INDEX = "cities";
  private static final String SKILLS_INDEX = "skills";
  private static final String JOBS_INDEX = "jobs";

  /**
   * Index all companies to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCompanies() {
    return elasticsearchCompanyIndexingService.indexAllCompanies();
  }

  /**
   * Index all designations to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllDesignations() {
    return elasticsearchDesignationIndexingService.indexAllDesignations();
  }

  /**
   * Index all cities to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCities() {
    return elasticsearchCityIndexingService.indexAllCities();
  }

  /**
   * Index all skills to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllSkills() {
    return elasticsearchSkillIndexingService.indexAllSkills();
  }

  /**
   * Index all entities (companies, designations, cities, skills, jobs) to Elasticsearch First
   * deletes all existing documents, then re-indexes everything fresh
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllEntities() {
    log.info("Starting to index all entities to Elasticsearch with cleanup");

    // First, delete all existing documents from all indices
    log.info("Step 1: Deleting all existing documents from Elasticsearch indices");
    Map<String, Object> deleteResult = deleteAllDocumentsFromAllIndices();
    log.info("Cleanup completed - Total documents deleted");
    // Wait a moment for deletion to complete
    log.info("Waiting 10 seconds for deletion operations to complete...");
    try {
      Thread.sleep(10000);
      log.info("Wait period completed, proceeding with re-indexing");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Thread interrupted during deletion wait");
    }

    // Then, re-index all entities using dedicated services
    log.info("Step 2: Re-indexing all entities to Elasticsearch");

    log.info("Starting companies re-indexing...");
    Map<String, Object> companiesResult = elasticsearchCompanyIndexingService.indexAllCompanies();
    log.info("Companies re-indexing completed - Success: {}, Failures: {}",
        companiesResult.get("indexedSuccessfully"), companiesResult.get("failedToIndex"));

    log.info("Starting designations re-indexing...");
    Map<String, Object> designationsResult = elasticsearchDesignationIndexingService.indexAllDesignations();
    log.info("Designations re-indexing completed - Success: {}, Failures: {}",
        designationsResult.get("indexedSuccessfully"), designationsResult.get("failedToIndex"));

    log.info("Starting cities re-indexing...");
    Map<String, Object> citiesResult = elasticsearchCityIndexingService.indexAllCities();
    log.info("Cities re-indexing completed - Success: {}, Failures: {}",
        citiesResult.get("indexedSuccessfully"), citiesResult.get("failedToIndex"));

    log.info("Starting skills re-indexing...");
    Map<String, Object> skillsResult = elasticsearchSkillIndexingService.indexAllSkills();
    log.info("Skills re-indexing completed - Success: {}, Failures: {}",
        skillsResult.get("indexedSuccessfully"), skillsResult.get("failedToIndex"));

    log.info("Starting jobs re-indexing...");
    elasticsearchJobIndexingService.reindexAllJobs();
    Map<String, Object> jobsResult = new HashMap<>();
    jobsResult.put("indexedSuccessfully", 0); // Will be updated by the service
    jobsResult.put("failedToIndex", 0); // Will be updated by the service
    jobsResult.put("totalJobs", 0); // Will be updated by the service
    log.info("Jobs re-indexing completed - delegated to ElasticsearchJobIndexingService");

    Map<String, Object> result = new HashMap<>();
    result.put("cleanup", deleteResult);
    result.put("companies", companiesResult);
    result.put("designations", designationsResult);
    result.put("cities", citiesResult);
    result.put("skills", skillsResult);
    result.put("jobs", jobsResult);
    result.put("message", "All entities re-indexed to Elasticsearch after cleanup");

    int totalIndexed = ((Number) companiesResult.get("indexedSuccessfully")).intValue() +
        ((Number) designationsResult.get("indexedSuccessfully")).intValue() +
        ((Number) citiesResult.get("indexedSuccessfully")).intValue() +
        ((Number) skillsResult.get("indexedSuccessfully")).intValue() +
        ((Number) jobsResult.get("indexedSuccessfully")).intValue();

    int totalFailures = ((Number) companiesResult.get("failedToIndex")).intValue() +
        ((Number) designationsResult.get("failedToIndex")).intValue() +
        ((Number) citiesResult.get("failedToIndex")).intValue() +
        ((Number) skillsResult.get("failedToIndex")).intValue() +
        ((Number) jobsResult.get("failedToIndex")).intValue();

    log.info(
        "All entities re-indexing completed after cleanup - Total indexed: {}, Total failures: {}",
        totalIndexed, totalFailures);

    return result;
  }

  /**
   * Delete all documents from all Elasticsearch indices
   */
  public Map<String, Object> deleteAllDocumentsFromAllIndices() {
    log.info("Starting to delete all documents from all Elasticsearch indices");

    Map<String, Object> result = new HashMap<>();
    int totalDeleted = 0;

    try {
      // Delete from companies index
      log.info("Deleting all documents from companies index");
      DeleteByQueryResponse companiesDeleteResult = elasticsearchClient.deleteByQuery(d -> d
          .index(COMPANIES_INDEX)
          .query(q -> q.matchAll(m -> m))
      );
      Long companiesDeleted = companiesDeleteResult.deleted();
      result.put("companiesDeleted", companiesDeleted);
      totalDeleted += companiesDeleted.intValue();
      log.info("Deleted {} documents from companies index", companiesDeleted);

      // Delete from designations index
      log.info("Deleting all documents from designations index");
      DeleteByQueryResponse designationsDeleteResult = elasticsearchClient.deleteByQuery(d -> d
          .index(DESIGNATIONS_INDEX)
          .query(q -> q.matchAll(m -> m))
      );
      Long designationsDeleted = designationsDeleteResult.deleted();
      result.put("designationsDeleted", designationsDeleted);
      totalDeleted += designationsDeleted.intValue();
      log.info("Deleted {} documents from designations index", designationsDeleted);

      // Delete from cities index
      log.info("Deleting all documents from cities index");
      DeleteByQueryResponse citiesDeleteResult = elasticsearchClient.deleteByQuery(d -> d
          .index(CITIES_INDEX)
          .query(q -> q.matchAll(m -> m))
      );
      Long citiesDeleted = citiesDeleteResult.deleted();
      result.put("citiesDeleted", citiesDeleted);
      totalDeleted += citiesDeleted.intValue();
      log.info("Deleted {} documents from cities index", citiesDeleted);

      // Delete from skills index
      log.info("Deleting all documents from skills index");
      DeleteByQueryResponse skillsDeleteResult = elasticsearchClient.deleteByQuery(d -> d
          .index(SKILLS_INDEX)
          .query(q -> q.matchAll(m -> m))
      );
      Long skillsDeleted = skillsDeleteResult.deleted();
      result.put("skillsDeleted", skillsDeleted);
      totalDeleted += skillsDeleted.intValue();
      log.info("Deleted {} documents from skills index", skillsDeleted);

      // Delete from jobs index
      log.info("Deleting all documents from jobs index");
      DeleteByQueryResponse jobsDeleteResult = elasticsearchClient.deleteByQuery(d -> d
          .index(JOBS_INDEX)
          .query(q -> q.matchAll(m -> m))
      );
      Long jobsDeleted = jobsDeleteResult.deleted();
      result.put("jobsDeleted", jobsDeleted);
      totalDeleted += jobsDeleted.intValue();
      log.info("Deleted {} documents from jobs index", jobsDeleted);

      result.put("totalDeleted", totalDeleted);
      result.put("message", "All documents deleted from all indices");

      log.info("Successfully deleted {} total documents from all indices", totalDeleted);

    } catch (Exception e) {
      log.error("Error deleting documents from Elasticsearch indices", e);
      result.put("error", "Failed to delete documents: " + e.getMessage());
    }

    return result;
  }

  /**
   * Search for autosuggest results
   */
  public List<Map<String, Object>> searchAutosuggest(String keyword, String entityType, int limit) {
    log.info("Searching for autosuggest with keyword: '{}', entityType: '{}', limit: {}", keyword,
        entityType, limit);

    try {
      String indexName = getIndexName(entityType);
      log.info("Resolved entity type '{}' to index name: '{}'", entityType, indexName);

      if (indexName == null) {
        log.error("Invalid entity type: {}", entityType);
        return Collections.emptyList();
      }

      log.info("Building search request for index: {} with keyword: '{}'", indexName, keyword);
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(indexName)
          .query(Query.of(q -> q
              .match(MatchQuery.of(m -> m
                  .field("name")
                  .query(keyword)
                  .fuzziness("AUTO")
              ))
          ))
          .sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)))
          .size(limit)
      );
      log.info("Search request built successfully for index: {} with limit: {}", indexName, limit);

      log.info("Executing search request to Elasticsearch...");
      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      log.info("Search response received - Total hits: {}, Max score: {}",
          response.hits().total().value(), response.hits().maxScore());

      List<Map<String, Object>> results = new ArrayList<>();
      log.info("Processing {} search hits for autosuggest results", response.hits().hits().size());

      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> source = hit.source();
        Map<String, Object> result = new HashMap<>();
        result.put("id", source.get("id"));
        result.put("name", source.get("name"));
        result.put("type", entityType);
        result.put("score", hit.score());

        // Add additional fields based on entity type
        if ("companies".equals(entityType)) {
          result.put("website", source.get("website"));
          result.put("headquarters", source.get("headquarters"));
          result.put("industry", source.get("industry"));
        } else if ("cities".equals(entityType)) {
          result.put("country", source.get("country"));
          result.put("zipCode", source.get("zipCode"));
        }

        results.add(result);
      }

      log.info("Autosuggest search completed - Found {} results", results.size());
      return results;

    } catch (Exception e) {
      log.error("Error searching autosuggest for keyword: '{}', entityType: '{}'", keyword,
          entityType, e);
      return Collections.emptyList();
    }
  }

  /**
   * Get index name for entity type
   */
  private String getIndexName(String entityType) {
    switch (entityType.toLowerCase()) {
      case "companies":
        return COMPANIES_INDEX;
      case "designations":
        return DESIGNATIONS_INDEX;
      case "cities":
        return CITIES_INDEX;
      case "skills":
        return SKILLS_INDEX;
      case "jobs":
        return JOBS_INDEX;
      default:
        return null;
    }
  }


  /**
   * Search companies with filters and pagination This method is kept for backward compatibility
   * with existing controllers
   */
  public Map<String, Object> searchCompanies(String location, String industryName,
      String secondaryIndustryName, int page, int size) {
    // This method is now delegated to the company query service
    // For now, we'll return an empty result and log that this method is deprecated
    log.warn("searchCompanies method is deprecated. Use ElasticsearchJobQueryService instead.");

    Map<String, Object> result = new HashMap<>();
    result.put("companies", new ArrayList<>());
    result.put("total", 0);
    result.put("page", page);
    result.put("size", size);
    result.put("totalPages", 0);
    result.put("message", "This method is deprecated. Use ElasticsearchJobQueryService instead.");

    return result;
  }
} 