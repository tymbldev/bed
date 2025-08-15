package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final CompanyRepository companyRepository;
  private final DesignationRepository designationRepository;
  private final CityRepository cityRepository;
  private final IndustryRepository industryRepository;
  private final JobRepository jobRepository;
  private final DepartmentRepository departmentRepository;
  private final SkillRepository skillRepository;

  private static final String COMPANIES_INDEX = "companies";
  private static final String DESIGNATIONS_INDEX = "designations";
  private static final String CITIES_INDEX = "cities";
  private static final String SKILLS_INDEX = "skills";

  /**
   * Index all companies to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCompanies() {
    log.info("Starting to index all companies to Elasticsearch");

    try {
      List<Company> companies = companyRepository.findAll();
      log.info("Found {} companies to index", companies.size());

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("Building bulk request for {} companies", companies.size());

      for (Company company : companies) {
        log.info("Building document for company: {} (ID: {})", company.getName(), company.getId());
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

      log.info("Executing bulk request to Elasticsearch for {} companies", companies.size());
      BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
      log.info("Bulk request completed. Response received: {} items, Errors: {}", 
          response.items().size(), response.errors());

      int successCount = 0;
      int failureCount = 0;

      if (response.errors()) {
        failureCount = response.items().size();
        log.error("Failed to index companies: {}",
            response.items().stream().map(item -> item.error().reason())
                .collect(Collectors.joining(", ")));
      } else {
        successCount = companies.size();
        log.info("All {} companies indexed successfully", successCount);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("totalCompanies", companies.size());
      result.put("indexedSuccessfully", successCount);
      result.put("failedToIndex", failureCount);
      result.put("message", "Company indexing completed");

      log.info("Company indexing completed - Success: {}, Failures: {}", successCount,
          failureCount);

      return result;

    } catch (Exception e) {
      log.error("Error indexing companies to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index companies: " + e.getMessage());
      return error;
    }
  }

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
        log.info("Building document for designation: {} (ID: {})", designation.getName(), designation.getId());
        Map<String, Object> designationDoc = buildDesignationDocument(designation);
        log.info("Designation document built successfully for: {} - Document size: {} fields, Department: {}", 
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
   * Index all cities to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCities() {
    log.info("Starting to index all cities to Elasticsearch");

    try {
      List<City> cities = cityRepository.findAll();
      log.info("Found {} cities to index", cities.size());

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("Building bulk request for {} cities", cities.size());

      for (City city : cities) {
        log.info("Building document for city: {} (ID: {})", city.getName(), city.getId());
        Map<String, Object> cityDoc = buildCityDocument(city);
        log.info("City document built successfully for: {} - Document size: {} fields, Country ID: {}", 
            city.getName(), cityDoc.size(), cityDoc.get("countryId"));
        
        bulkRequest.operations(op -> op
            .index(idx -> idx
                .index(CITIES_INDEX)
                .id(city.getId().toString())
                .document(cityDoc)
            )
        );
      }

      log.info("Executing bulk request to Elasticsearch for {} cities", cities.size());
      BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
      log.info("Bulk request completed. Response received: {} items, Errors: {}", 
          response.items().size(), response.errors());

      int successCount = 0;
      int failureCount = 0;

      if (response.errors()) {
        failureCount = response.items().size();
        log.error("Failed to index cities: {}",
            response.items().stream().map(item -> item.error().reason())
                .collect(Collectors.joining(", ")));
      } else {
        successCount = cities.size();
        log.info("All {} cities indexed successfully", successCount);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("totalCities", cities.size());
      result.put("indexedSuccessfully", successCount);
      result.put("failedToIndex", failureCount);
      result.put("message", "City indexing completed");

      log.info("City indexing completed - Success: {}, Failures: {}", successCount, failureCount);

      return result;

    } catch (Exception e) {
      log.error("Error indexing cities to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index cities: " + e.getMessage());
      return error;
    }
  }

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
        log.info("Skill document built successfully for: {} - Document size: {} fields, Category: {}, Similar Skills: {}", 
            skill.getName(), skillDoc.size(), skillDoc.get("category"), skillDoc.get("similarSkillsByName"));
        
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
   * Sync a skill to Elasticsearch (save or update) Does not fail the main transaction if ES fails
   */
  public void syncSkillToElasticsearch(Skill skill) {
    try {
      log.info("Starting to sync skill to Elasticsearch: {} (ID: {})", skill.getName(), skill.getId());
      
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
   * Delete all documents from a specific index
   */
  public Map<String, Object> deleteAllDocumentsFromIndex(String indexName) {
    log.info("Starting to delete all documents from index: {}", indexName);

    try {
      // Check if index exists
      log.info("Checking if index {} exists", indexName);
      boolean indexExists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
      log.info("Index {} exists: {}", indexName, indexExists);
      
      if (!indexExists) {
        log.info("Index {} does not exist, nothing to delete", indexName);
        Map<String, Object> result = new HashMap<>();
        result.put("indexName", indexName);
        result.put("documentsDeleted", 0);
        result.put("message", "Index does not exist, nothing to delete");
        return result;
      }

      // Delete all documents using match_all query
      log.info("Executing delete by query for all documents in index: {}", indexName);
      DeleteByQueryResponse deleteResponse = elasticsearchClient.deleteByQuery(d -> d
          .index(indexName)
          .query(q -> q.matchAll(m -> m))
      );
      log.info("Delete by query completed for index: {} - Documents deleted: {}", 
          indexName, deleteResponse.deleted());

      Map<String, Object> result = new HashMap<>();
      result.put("indexName", indexName);
      result.put("documentsDeleted", deleteResponse.deleted());
      result.put("message", "All documents deleted from index: " + indexName);

      log.info("Successfully deleted {} documents from index: {}", deleteResponse.deleted(),
          indexName);
      return result;

    } catch (Exception e) {
      log.error("Error deleting documents from index: {}", indexName, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error",
          "Failed to delete documents from index " + indexName + ": " + e.getMessage());
      return error;
    }
  }

  /**
   * Delete all documents from all indices before re-indexing
   */
  public Map<String, Object> deleteAllDocumentsFromAllIndices() {
    log.info("Starting to delete all documents from all indices");

    log.info("Deleting documents from companies index: {}", COMPANIES_INDEX);
    Map<String, Object> companiesDeleteResult = deleteAllDocumentsFromIndex(COMPANIES_INDEX);
    
    log.info("Deleting documents from designations index: {}", DESIGNATIONS_INDEX);
    Map<String, Object> designationsDeleteResult = deleteAllDocumentsFromIndex(DESIGNATIONS_INDEX);
    
    log.info("Deleting documents from cities index: {}", CITIES_INDEX);
    Map<String, Object> citiesDeleteResult = deleteAllDocumentsFromIndex(CITIES_INDEX);
    
    log.info("Deleting documents from skills index: {}", SKILLS_INDEX);
    Map<String, Object> skillsDeleteResult = deleteAllDocumentsFromIndex(SKILLS_INDEX);

    Map<String, Object> result = new HashMap<>();
    result.put("companies", companiesDeleteResult);
    result.put("designations", designationsDeleteResult);
    result.put("cities", citiesDeleteResult);
    result.put("skills", skillsDeleteResult);
    result.put("message", "All documents deleted from all indices");

    log.info("All documents deletion completed - Companies: {}, Designations: {}, Cities: {}, Skills: {}", 
        companiesDeleteResult.get("documentsDeleted"), 
        designationsDeleteResult.get("documentsDeleted"),
        citiesDeleteResult.get("documentsDeleted"), 
        skillsDeleteResult.get("documentsDeleted"));
    return result;
  }

  /**
   * Index all entities (companies, designations, cities) to Elasticsearch First deletes all
   * existing documents, then re-indexes everything fresh
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

    // Then, re-index all entities
    log.info("Step 2: Re-indexing all entities to Elasticsearch");
    
    log.info("Starting companies re-indexing...");
    Map<String, Object> companiesResult = indexAllCompanies();
    log.info("Companies re-indexing completed - Success: {}, Failures: {}", 
        companiesResult.get("indexedSuccessfully"), companiesResult.get("failedToIndex"));
    
    log.info("Starting designations re-indexing...");
    Map<String, Object> designationsResult = indexAllDesignations();
    log.info("Designations re-indexing completed - Success: {}, Failures: {}", 
        designationsResult.get("indexedSuccessfully"), designationsResult.get("failedToIndex"));
    
    log.info("Starting cities re-indexing...");
    Map<String, Object> citiesResult = indexAllCities();
    log.info("Cities re-indexing completed - Success: {}, Failures: {}", 
        citiesResult.get("indexedSuccessfully"), citiesResult.get("failedToIndex"));
    
    log.info("Starting skills re-indexing...");
    Map<String, Object> skillsResult = indexAllSkills();
    log.info("Skills re-indexing completed - Success: {}, Failures: {}", 
        skillsResult.get("indexedSuccessfully"), skillsResult.get("failedToIndex"));

    Map<String, Object> result = new HashMap<>();
    result.put("cleanup", deleteResult);
    result.put("companies", companiesResult);
    result.put("designations", designationsResult);
    result.put("cities", citiesResult);
    result.put("skills", skillsResult);
    result.put("message", "All entities re-indexed to Elasticsearch after cleanup");

    int totalIndexed = ((Number) companiesResult.get("indexedSuccessfully")).intValue() + 
                       ((Number) designationsResult.get("indexedSuccessfully")).intValue() + 
                       ((Number) citiesResult.get("indexedSuccessfully")).intValue() + 
                       ((Number) skillsResult.get("indexedSuccessfully")).intValue();
    
    int totalFailures = ((Number) companiesResult.get("failedToIndex")).intValue() + 
                        ((Number) designationsResult.get("failedToIndex")).intValue() + 
                        ((Number) citiesResult.get("failedToIndex")).intValue() + 
                        ((Number) skillsResult.get("failedToIndex")).intValue();
    
    log.info("All entities re-indexing completed after cleanup - Total indexed: {}, Total failures: {}", 
        totalIndexed, totalFailures);

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
        } else if ("designations".equals(entityType)) {
          result.put("level", source.get("level"));
          result.put("enabled", source.get("enabled"));
        } else if ("skills".equals(entityType)) {
          result.put("category", source.get("category"));
          result.put("enabled", source.get("enabled"));
          result.put("usageCount", source.get("usageCount"));
        }

        results.add(result);
      }

      log.info("Found {} autosuggest results for keyword: '{}' in index: '{}'", 
          results.size(), keyword, indexName);
      return results;

    } catch (Exception e) {
      log.error("Error searching autosuggest for keyword: '{}', entityType: '{}'", keyword,
          entityType, e);
      return Collections.emptyList();
    }
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

      log.info("Counting active jobs for company: {}", company.getName());
      long jobCount = jobRepository.countByCompanyIdAndActiveTrue(companyId);
      log.info("Found {} active jobs for company: {}", jobCount, company.getName());

      Map<String, Object> updateDoc = new HashMap<>();
      updateDoc.put("jobCount", jobCount);
      log.info("Prepared update document for company: {} - Job count: {}", company.getName(), jobCount);

      log.info("Executing update request to Elasticsearch for company: {} in index: {}", 
          company.getName(), COMPANIES_INDEX);
      elasticsearchClient.update(u -> u
              .index(COMPANIES_INDEX)
              .id(companyId.toString())
              .doc(updateDoc)
          , Map.class);

      log.info("Successfully updated job count for company {} (ID: {}) to {} in Elasticsearch", 
          company.getName(), companyId, jobCount);

    } catch (Exception e) {
      log.error("Error updating job count for company {} in Elasticsearch", companyId, e);
    }
  }

  /**
   * Search companies with filters and pagination
   */
  public Map<String, Object> searchCompanies(String location, String industryName,
      String secondaryIndustryName,
      int page, int size) {
    try {
      log.info("Starting company search with filters - Location: '{}', Industry: '{}', Secondary Industry: '{}', Page: {}, Size: {}", 
          location, industryName, secondaryIndustryName, page, size);
      
      // Build the query
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      log.info("Building boolean query for company search");

      // Location filter (headquarters)
      if (location != null && !location.trim().isEmpty()) {
        log.info("Adding location filter: '{}'", location.trim());
        boolQueryBuilder.must(Query.of(q -> q
            .match(MatchQuery.of(m -> m
                .field("headquarters")
                .query(location.trim())
                .fuzziness("AUTO")
            ))
        ));
      }

      // Industry name filter
      if (industryName != null && !industryName.trim().isEmpty()) {
        log.info("Adding primary industry filter: '{}'", industryName.trim());
        boolQueryBuilder.must(Query.of(q -> q
            .match(MatchQuery.of(m -> m
                .field("primaryIndustryName")
                .query(industryName.trim())
                .fuzziness("AUTO")
            ))
        ));
      }

      // Secondary industry filter
      if (secondaryIndustryName != null && !secondaryIndustryName.trim().isEmpty()) {
        log.info("Adding secondary industry filter: '{}'", secondaryIndustryName.trim());
        boolQueryBuilder.must(Query.of(q -> q
            .match(MatchQuery.of(m -> m
                .field("secondaryIndustries")
                .query(secondaryIndustryName.trim())
                .fuzziness("AUTO")
            ))
        ));
      }

      log.info("Building search request for companies index with pagination - Page: {}, Size: {}", page, size);
      // Build search request with sorting by job count
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(COMPANIES_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .sort(sort -> sort.field(f -> f.field("jobCount").order(SortOrder.Desc)))
          .sort(sort -> sort.field(f -> f.field("name").order(SortOrder.Asc)))
          .from(page * size)
          .size(size)
      );
      log.info("Search request built successfully for companies index");

      log.info("Executing search request to Elasticsearch...");
      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      log.info("Search response received - Total hits: {}, Max score: {}, Current page results: {}", 
          response.hits().total().value(), response.hits().maxScore(), response.hits().hits().size());

      List<Map<String, Object>> companies = new ArrayList<>();
      log.info("Processing {} search hits for company results", response.hits().hits().size());
      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> source = hit.source();
        Map<String, Object> company = new HashMap<>(source);
        company.put("score", hit.score());
        companies.add(company);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("companies", companies);
      result.put("total", response.hits().total().value());
      result.put("page", page);
      result.put("size", size);
      result.put("totalPages", (int) Math.ceil((double) response.hits().total().value() / size));

      log.info("Company search completed successfully - Found {} companies, Total: {}, Page: {}/{}, Size: {}", 
          companies.size(), response.hits().total().value(), page + 1, result.get("totalPages"), size);

      return result;

    } catch (Exception e) {
      log.error("Error searching companies with filters", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to search companies: " + e.getMessage());
      return error;
    }
  }

  /**
   * Build company document for Elasticsearch
   */
  private Map<String, Object> buildCompanyDocument(Company company) {
    log.info("Building Elasticsearch document for company: {} (ID: {})", company.getName(), company.getId());
    
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", company.getId());
    doc.put("name", company.getName());
    doc.put("website", company.getWebsite());
    doc.put("logoUrl", company.getLogoUrl());
    doc.put("headquarters", company.getHeadquarters());
    doc.put("companySize", company.getCompanySize());
    doc.put("specialties", company.getSpecialties());
    doc.put("linkedinUrl", company.getLinkedinUrl());
    doc.put("careerPageUrl", company.getCareerPageUrl());
    doc.put("primaryIndustryId", company.getPrimaryIndustryId());
    doc.put("secondaryIndustries", company.getSecondaryIndustries());
    doc.put("shortname", company.getShortname());

    log.info("Basic company fields added for: {} - Fields: {}", company.getName(), doc.size());

    // Add industry name
    if (company.getPrimaryIndustryId() != null) {
      try {
        log.info("Fetching industry name for company: {} - Industry ID: {}", company.getName(), company.getPrimaryIndustryId());
        Industry industry = industryRepository.findById(company.getPrimaryIndustryId())
            .orElse(null);
        if (industry != null) {
          doc.put("primaryIndustryName", industry.getName());
          log.info("Industry name added for company: {} - Industry: {}", company.getName(), industry.getName());
        } else {
          log.info("Industry not found for company: {} - Industry ID: {}", company.getName(), company.getPrimaryIndustryId());
        }
      } catch (Exception e) {
        log.warn("Could not fetch industry name for company {}: {}", company.getId(),
            e.getMessage());
      }
    } else {
      log.info("No primary industry ID for company: {}", company.getName());
    }

    // Add job count
    try {
      log.info("Counting active jobs for company: {}", company.getName());
      long jobCount = jobRepository.countByCompanyIdAndActiveTrue(company.getId());
      doc.put("jobCount", jobCount);
      log.info("Job count added for company: {} - Active jobs: {}", company.getName(), jobCount);
    } catch (Exception e) {
      log.warn("Could not fetch job count for company {}: {}", company.getId(), e.getMessage());
      doc.put("jobCount", 0L);
      log.info("Default job count (0) set for company: {}", company.getName());
    }

    // Add searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for company: {}", company.getName());
    
    if (company.getName() != null) {
        searchableText.append(company.getName()).append(" ");
    }
    if (company.getShortname() != null) {
        searchableText.append(company.getShortname()).append(" ");
    }
    if (company.getDescription() != null) {
        searchableText.append(company.getDescription()).append(" ");
    }
    if (company.getSpecialties() != null) {
        searchableText.append(company.getSpecialties()).append(" ");
    }
    if (company.getHeadquarters() != null) {
        searchableText.append(company.getHeadquarters()).append(" ");
    }
    if (company.getSecondaryIndustries() != null) {
        searchableText.append(company.getSecondaryIndustries()).append(" ");
    }

    // Add industry name to searchable text
    if (doc.containsKey("primaryIndustryName")) {
      searchableText.append(doc.get("primaryIndustryName")).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for company: {} - Length: {} characters", 
        company.getName(), searchableText.length());

    log.info("Company document built successfully for: {} - Total fields: {}", company.getName(), doc.size());

    return doc;
  }

  /**
   * Build skill document for Elasticsearch
   */
  private Map<String, Object> buildSkillDocument(Skill skill) {
    log.info("Building Elasticsearch document for skill: {} (ID: {})", skill.getName(), skill.getId());
    
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", skill.getId());
    doc.put("name", skill.getName());
    doc.put("description", skill.getDescription());
    doc.put("category", skill.getCategory());
    doc.put("enabled", skill.isEnabled());
    doc.put("usageCount", skill.getUsageCount());
    doc.put("updatedAt", skill.getUpdatedAt());
    doc.put("similarSkillsByName", skill.getSimilarSkillsByName());
    doc.put("similarSkillsById", skill.getSimilarSkillsById());
    doc.put("similarSkillsProcessed", skill.isSimilarSkillsProcessed());

    log.info("Basic skill fields added for: {} - Fields: {}", skill.getName(), doc.size());

    // Add searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for skill: {}", skill.getName());
    
    if (skill.getName() != null) {
        searchableText.append(skill.getName()).append(" ");
    }
    if (skill.getDescription() != null) {
        searchableText.append(skill.getDescription()).append(" ");
    }
    if (skill.getCategory() != null) {
        searchableText.append(skill.getCategory()).append(" ");
    }
    if (skill.getSimilarSkillsByName() != null) {
        searchableText.append(skill.getSimilarSkillsByName()).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for skill: {} - Length: {} characters", 
        skill.getName(), searchableText.length());

    log.info("Skill document built successfully for: {} - Total fields: {}", skill.getName(), doc.size());

    return doc;
  }

  /**
   * Build designation document for Elasticsearch
   */
  private Map<String, Object> buildDesignationDocument(Designation designation) {
    log.info("Building Elasticsearch document for designation: {} (ID: {})", designation.getName(), designation.getId());
    
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
    doc.put("departmentAssigned", designation.isDepartmentAssigned());

    log.info("Basic designation fields added for: {} - Fields: {}", designation.getName(), doc.size());

    // Add department name if available
    if (designation.getDepartmentId() != null) {
      try {
        log.info("Fetching department name for designation: {} - Department ID: {}", designation.getName(), designation.getDepartmentId());
        Department department = departmentRepository.findById(designation.getDepartmentId())
            .orElse(null);
        if (department != null) {
          doc.put("departmentName", department.getName());
          log.info("Department name added for designation: {} - Department: {}", designation.getName(), department.getName());
        } else {
          log.info("Department not found for designation: {} - Department ID: {}", designation.getName(), designation.getDepartmentId());
        }
      } catch (Exception e) {
        log.warn("Could not fetch department name for designation {}: {}", designation.getId(),
            e.getMessage());
      }
    } else {
      log.info("No department ID for designation: {}", designation.getName());
    }

    // Add searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for designation: {}", designation.getName());
    
    if (designation.getName() != null) {
        searchableText.append(designation.getName()).append(" ");
    }
    if (designation.getProcessedName() != null) {
        searchableText.append(designation.getProcessedName()).append(" ");
    }
    if (designation.getSimilarDesignationsByName() != null) {
        searchableText.append(designation.getSimilarDesignationsByName()).append(" ");
    }

    // Add department name to searchable text
    if (doc.containsKey("departmentName")) {
      searchableText.append(doc.get("departmentName")).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for designation: {} - Length: {} characters", 
        designation.getName(), searchableText.length());

    log.info("Designation document built successfully for: {} - Total fields: {}", designation.getName(), doc.size());

    return doc;
  }

  /**
   * Build city document for Elasticsearch
   */
  private Map<String, Object> buildCityDocument(City city) {
    log.info("Building Elasticsearch document for city: {} (ID: {})", city.getName(), city.getId());
    
    Map<String, Object> doc = new HashMap<>();
    doc.put("id", city.getId());
    doc.put("name", city.getName());
    doc.put("countryId", city.getCountryId());
    doc.put("zipCode", city.getZipCode());
    doc.put("processedName", city.getProcessedName());
    doc.put("processedNameGenerated", city.isProcessedNameGenerated());

    log.info("Basic city fields added for: {} - Fields: {}", city.getName(), doc.size());

    // Add searchable text
    StringBuilder searchableText = new StringBuilder();
    log.info("Building searchable text for city: {}", city.getName());
    
    if (city.getName() != null) {
        searchableText.append(city.getName()).append(" ");
    }
    if (city.getProcessedName() != null) {
        searchableText.append(city.getProcessedName()).append(" ");
    }
    if (city.getZipCode() != null) {
        searchableText.append(city.getZipCode()).append(" ");
    }

    doc.put("searchableText", searchableText.toString().trim());
    log.info("Searchable text built for city: {} - Length: {} characters", 
        city.getName(), searchableText.length());

    log.info("City document built successfully for: {} - Total fields: {}", city.getName(), doc.size());

    return doc;
  }

  /**
   * Get index name based on entity type
   */
  private String getIndexName(String entityType) {
    switch (entityType.toLowerCase()) {
      case "company":
      case "companies":
        return COMPANIES_INDEX;
      case "designation":
      case "designations":
        return DESIGNATIONS_INDEX;
      case "city":
      case "cities":
        return CITIES_INDEX;
      case "skill":
      case "skills":
        return SKILLS_INDEX;
      default:
        return null;
    }
  }
} 