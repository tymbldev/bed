package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tymbl.jobs.constants.ElasticsearchConstants;
import com.tymbl.common.service.DropdownService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for SEO-related Elasticsearch operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchSEOService {

  private final ElasticsearchClient elasticsearchClient;
  private final DropdownService dropdownService;

  /**
   * Get job location combinations for designation or skill
   */
  public Map<String, Object> getJobLocationCombinations(String query) {
    try {
      log.info("Getting job location combinations for: {} (type param ignored)", query);

      // Build search query based on type
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

      // Auto-detect whether the query matches a known designation or skill using dropdown caches
      String lowered = query == null ? "" : query.trim().toLowerCase();
      boolean isDesignation = false;
      boolean isSkill = false;
      try {
        // Check designation cache via DropdownService
        java.util.List<com.tymbl.common.entity.Designation> allDesignations = dropdownService.getAllDesignations();
        isDesignation = allDesignations.stream()
            .anyMatch(d -> d.getName() != null && d.getName().trim().equalsIgnoreCase(lowered));

        // Heuristic for skills: search jobs index via title/description/tags match when not designation
        if (!isDesignation) {
          isSkill = true; // default to skill if not designation; reduces client responsibility
        }
      } catch (Exception ignore) {
        // Fallback: default to designation
        isDesignation = true;
      }

      if (isDesignation) {
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
      } else if (isSkill) {
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("skillNames").query(query))));
      }

      // Only active jobs
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      // Build search request with aggregation
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the aggregation results
          .aggregations("locations", a -> a
              .terms(t -> t
                  .field("cityName.keyword")
                  .size(ElasticsearchConstants.MAX_AGGREGATION_SIZE) // Get up to 1000 unique cities
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Extract aggregation results
      long totalJobs = 0;
      List<Map<String, Object>> locationCombinations = new ArrayList<>();

      try {
        // Parse the aggregation response using proper type handling
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();

        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate locationsAgg = aggregations.get(
              "locations");
          if (locationsAgg != null && locationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = locationsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets()
                .array()) {
              String location = bucket.key().stringValue();
              Long jobCount = bucket.docCount();

              if (location != null && !location.trim().isEmpty()) {
                Map<String, Object> combination = new HashMap<>();
                combination.put("location", location);
                combination.put("jobCount", jobCount);
                combination.put("seoText", query + " jobs in " + location);
                locationCombinations.add(combination);

                totalJobs += jobCount;
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing aggregation response, falling back to manual processing: {}", e);
        // Fallback to manual processing if aggregation parsing fails
        return null;
      }

      Map<String, Object> result = new HashMap<>();
      result.put("query", query);
      result.put("type", isDesignation ? "designation" : "skill");
      result.put("totalJobs", totalJobs);
      result.put("locationCombinations", locationCombinations);

      return result;

    } catch (Exception e) {
      log.error("Error getting job location combinations for {} : ", query, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting job location combinations: " + e.getMessage());
      return error;
    }
  }

  /**
   * Get similar designations and skills with job counts
   */
  public Map<String, Object> getSimilarDesignationsWithJobCounts(String designation) {
    try {
      log.info("Getting similar designations and skills with job counts for: {}", designation);

      // First, get the designation details from designations index
      BoolQuery.Builder designationQueryBuilder = new BoolQuery.Builder();
      designationQueryBuilder.must(Query.of(q -> q.match(m -> m.field("name").query(designation))));

      SearchRequest designationSearchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.DESIGNATIONS_INDEX)
          .query(designationQueryBuilder.build()._toQuery())
          .size(1)
      );

      SearchResponse<Map> designationResponse = elasticsearchClient.search(designationSearchRequest,
          Map.class);

      if (designationResponse.hits().hits().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Designation not found: " + designation);
        return error;
      }

      // Extract designation details
      Map<String, Object> designationDoc = designationResponse.hits().hits().get(0).source();
      String departmentName = (String) designationDoc.get("departmentName");

      List<String> similarDesignations = new ArrayList<>();
      Object similarByNameObj = designationDoc.get("similarDesignationsByName");
      if (similarByNameObj instanceof List<?>) {
        for (Object o : (List<?>) similarByNameObj) {
          if (o != null) {
            String name = o.toString().trim();
            if (!name.isEmpty()) {
              similarDesignations.add(name);
            }
          }
        }
      } else if (similarByNameObj != null) {
        String s = similarByNameObj.toString().trim();
        if (s.startsWith("[") && s.endsWith("]")) {
          s = s.substring(1, s.length() - 1);
        }
        String[] similarArray = s.split(",");
        for (String similar : similarArray) {
          String trimmed = similar.trim().replaceAll("^\"|\"$", "");
          if (!trimmed.isEmpty()) {
            similarDesignations.add(trimmed);
          }
        }
      }

      // If no similar designations found, try to get designations from the same department
      if (similarDesignations.isEmpty() && departmentName != null && !departmentName.trim()
          .isEmpty()) {
        similarDesignations = getDesignationsByDepartmentFromElasticsearch(departmentName);
      }

      // Get job counts for all similar designations in a single optimized query
      List<Map<String, Object>> similarDesignationsWithCounts = new ArrayList<>();
      if (!similarDesignations.isEmpty()) {
        Map<String, Long> designationJobCounts = getJobCountsForMultipleDesignations(similarDesignations);
        
        for (String similarDesignation : similarDesignations) {
          Long jobCount = designationJobCounts.get(similarDesignation);
          if (jobCount != null && jobCount > 0) {
            Map<String, Object> designationWithCount = createCommonResponseItem(
                similarDesignation, jobCount, "designation");
            similarDesignationsWithCounts.add(designationWithCount);
          }
        }
      }
      
      // If no jobs found for similar designations, fallback to department-based search
      if (similarDesignationsWithCounts.isEmpty() && departmentName != null && !departmentName.trim().isEmpty()) {
        log.info("No jobs found for similar designations, falling back to department-based search for: {}", departmentName);
        similarDesignationsWithCounts = getDesignationsByDepartmentWithJobCounts(departmentName);
      }

      // Sort by job count descending
      similarDesignationsWithCounts.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));


      Map<String, Object> result = new HashMap<>();
      result.put("keywordType", "designation");
      result.put("similarContent", similarDesignationsWithCounts);
      result.put("count", similarDesignationsWithCounts.size());

      return result;

    } catch (Exception e) {
      log.error("Error getting similar designations and skills with job counts for: {}",
          designation, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting similar designations and skills: " + e.getMessage());
      return error;
    }
  }





  /**
   * Get designation/skill + location combinations with similar combinations
   */
  public Map<String, Object> getDesignationSkillLocationCombinations(String query, String location) {
    try {
      log.info("Getting designation/skill + location combinations for: {} in {}", query, location);

      // Auto-detect whether the query matches a known designation or skill using dropdown caches
      String lowered = query == null ? "" : query.trim().toLowerCase();
      boolean isDesignation = false;
      boolean isSkill = false;
      try {
        // Check designation cache via DropdownService
        java.util.List<com.tymbl.common.entity.Designation> allDesignations = dropdownService.getAllDesignations();
        isDesignation = allDesignations.stream()
            .anyMatch(d -> d.getName() != null && d.getName().trim().equalsIgnoreCase(lowered));

        // Heuristic for skills: default to skill if not designation; reduces client responsibility
        if (!isDesignation) {
          isSkill = true;
        }
      } catch (Exception ignore) {
        // Fallback: default to designation
        isDesignation = true;
      }

      String detectedType = isDesignation ? "designation" : "skill";
      log.info("Auto-detected type for query '{}': {}", query, detectedType);

      // First, get the job count for the specific query + location combination
      long mainJobCount = getJobCountForQueryLocationCombination(query, location, detectedType);

      // Get similar combinations based on the detected type
      List<Map<String, Object>> similarCombinations = new ArrayList<>();

      if (isDesignation) {
        // For designations, get similar designations and their job counts in the same location
        similarCombinations = getSimilarDesignationLocationCombinations(query, location);
      } else if (isSkill) {
        // For skills, get similar skills and their job counts in the same location
        similarCombinations = getSimilarSkillLocationCombinations(query, location);
      }

      // Sort by job count descending
      similarCombinations.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      Map<String, Object> result = new HashMap<>();
      result.put("query", query + " + " + location);
      result.put("type", detectedType);
      result.put("location", location);
      result.put("jobCount", mainJobCount);
      result.put("similarCombinations", similarCombinations);
      result.put("totalSimilarCombinations", similarCombinations.size());

      return result;

    } catch (Exception e) {
      log.error("Error getting designation/skill + location combinations for: {} in {}", query, location, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error",
          "Error getting designation/skill + location combinations: " + e.getMessage());
      return error;
    }
  }

  /**
   * Get job count for a specific query + location combination
   */
  private long getJobCountForQueryLocationCombination(String query, String location, String type) {
    try {
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("cityName").query(location))));

      if ("designation".equals(type)) {
        boolQueryBuilder.must(Query.of(q -> q.term(
            t -> t.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME + ".keyword").value(query))));
      } else if ("skill".equals(type)) {
        // For skills, search in job title, description, and skillNames fields
        BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("title").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("description").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("skillNames").query(query))));
        boolQueryBuilder.must(skillQueryBuilder.build()._toQuery());
      }

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the count
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      return response.hits().total().value();

    } catch (Exception e) {
      log.error("Error getting job count for query + location combination: {} + {}", query,
          location, e);
      return 0;
    }
  }

  /**
   * Get top designations by job count
   */
  public Map<String, Object> getTopDesignationsByJobCount(int limit) {
    try {
      log.info("Getting top {} designations by job count", limit);

      // Build search request with aggregation
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the aggregation results
          .aggregations("designations", a -> a
              .terms(t -> t
                  .field("designationName.keyword")
                  .size(limit * 2) // Get more than needed to ensure we have enough after filtering
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Extract aggregation results
      long totalJobs = 0;
      List<Map<String, Object>> topDesignations = new ArrayList<>();

      try {
        // Parse the aggregation response using proper type handling
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();

        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate designationsAgg = aggregations.get(
              "designations");
          if (designationsAgg != null && designationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = designationsAgg.sterms();
            int count = 0;
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets()
                .array()) {
              if (count >= limit) {
                break;
              }

              String designationName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();

              if (designationName != null && !designationName.trim().isEmpty()) {
                Map<String, Object> designation = new HashMap<>();
                designation.put("designationName", designationName);
                designation.put("jobCount", jobCount);
                topDesignations.add(designation);

                totalJobs += jobCount;
                count++;
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing aggregation response, falling back to manual processing: {}",
            e.getMessage());
        // Fallback to manual processing if aggregation parsing fails
        return getTopDesignationsByJobCountFallback(limit);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("topDesignations", topDesignations);
      result.put("totalDesignations", topDesignations.size());
      result.put("totalJobs", totalJobs);

      return result;

    } catch (Exception e) {
      log.error("Error getting top designations by job count", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting top designations: " + e.getMessage());
      return error;
    }
  }

  /**
   * Get top skills by job count
   */
  public Map<String, Object> getTopSkillsByJobCount(int limit) {
    try {
      log.info("Getting top {} skills by job count", limit);

      // Build search request with aggregation on tags (skills)
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("skills", a -> a
              .terms(t -> t
                  .field("skillNames.keyword")
                  .size(limit * 2)
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      long totalJobs = 0;
      List<Map<String, Object>> topSkills = new ArrayList<>();

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate skillsAgg = aggregations.get("skills");
          if (skillsAgg != null && skillsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = skillsAgg.sterms();
            int count = 0;
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              if (count >= limit) {
                break;
              }
              String skillName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
              if (skillName != null && !skillName.trim().isEmpty()) {
                Map<String, Object> skill = new HashMap<>();
                skill.put("skillName", skillName);
                skill.put("jobCount", jobCount);
                topSkills.add(skill);
                totalJobs += jobCount;
                count++;
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing skills aggregation, returning empty list: {}", e.getMessage());
        log.debug("Aggregation parsing error details: {}", e);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("topSkills", topSkills);
      result.put("totalSkills", topSkills.size());
      result.put("totalJobs", totalJobs);
      return result;

    } catch (Exception e) {
      log.error("Error getting top skills by job count", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting top skills: " + e.getMessage());
      return error;
    }
  }

  /**
   * Get similar skills with job counts for a given skill
   */
  public Map<String, Object> getSimilarSkillsWithJobCounts(String skill) {
    try {
      log.info("Getting similar skills with job counts for: {}", skill);

      // First, get the skill details from skills index
      BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
      skillQueryBuilder.must(Query.of(q -> q.match(m -> m.field("name").query(skill))));

      SearchRequest skillSearchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.SKILLS_INDEX)
          .query(skillQueryBuilder.build()._toQuery())
          .size(1)
      );

      SearchResponse<Map> skillResponse = elasticsearchClient.search(skillSearchRequest, Map.class);

      if (skillResponse.hits().hits().isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Skill not found: " + skill);
        return error;
      }

      Map<String, Object> skillDoc = skillResponse.hits().hits().get(0).source();

      // Parse similarSkillsByName list
      java.util.List<String> similarSkills = new java.util.ArrayList<>();
      Object similarByNameObj = skillDoc.get("similarSkillsByName");
      if (similarByNameObj instanceof java.util.List<?>) {
        for (Object o : (java.util.List<?>) similarByNameObj) {
          if (o != null) {
            String name = o.toString().trim();
            if (!name.isEmpty()) {
              similarSkills.add(name);
            }
          }
        }
      } else if (similarByNameObj != null) {
        String s = similarByNameObj.toString().trim();
        if (s.startsWith("[") && s.endsWith("]")) {
          s = s.substring(1, s.length() - 1);
        }
        String[] similarArray = s.split(",");
        for (String similar : similarArray) {
          String trimmed = similar.trim().replaceAll("^\"|\"$", "");
          if (!trimmed.isEmpty()) {
            similarSkills.add(trimmed);
          }
        }
      }

      // If none found in doc, fallback by searching skills index for related names
      if (similarSkills.isEmpty()) {
        similarSkills = getSimilarSkillsForLocationSearch(skill);
      }

      // Get job counts for all similar skills in a single optimized query
      java.util.List<java.util.Map<String, Object>> similarSkillsWithCounts = new java.util.ArrayList<>();
      if (!similarSkills.isEmpty()) {
        Map<String, Long> skillJobCounts = getJobCountsForMultipleSkills(similarSkills);
        
        for (String similarSkill : similarSkills) {
          Long jobCount = skillJobCounts.get(similarSkill);
          if (jobCount != null && jobCount > 0) {
            java.util.Map<String, Object> skillWithCount = createCommonResponseItem(
                similarSkill, jobCount, "skill");
            similarSkillsWithCounts.add(skillWithCount);
          }
        }
      }

      // Sort by job count desc and limit to top 10
      similarSkillsWithCounts.sort((a, b) -> Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));
      if (similarSkillsWithCounts.size() > 10) {
        similarSkillsWithCounts = similarSkillsWithCounts.subList(0, 10);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("keywordType", "skill");
      result.put("similarContent", similarSkillsWithCounts);
      result.put("count", similarSkillsWithCounts.size());
      return result;

    } catch (Exception e) {
      log.error("Error getting similar skills with job counts for: {}", skill, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting similar skills with job counts: " + e.getMessage());
      return error;
    }
  }

  // Helper methods

  private List<String> getDesignationsByDepartmentFromElasticsearch(String departmentName) {
    try {
      log.info("Getting designations by department from Elasticsearch: {}", departmentName);

      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field("departmentName").value(departmentName))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.DESIGNATIONS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(ElasticsearchConstants.MAX_AGGREGATION_SIZE)
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      List<String> designations = new ArrayList<>();
      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> designation = hit.source();
        if (designation != null && designation.get("name") != null) {
          String name = designation.get("name").toString();
          if (!name.trim().isEmpty()) {
            designations.add(name);
          }
        }
      }

      log.info("Found {} designations in department: {}", designations.size(), departmentName);
      return designations;

    } catch (Exception e) {
      log.error("Error getting designations by department from Elasticsearch: {}", departmentName,
          e);
      return new ArrayList<>();
    }
  }

  /**
   * Get job counts for multiple designations in a single query using aggregation
   */
  private Map<String, Long> getJobCountsForMultipleDesignations(List<String> designationNames) {
    try {
      if (designationNames == null || designationNames.isEmpty()) {
        return new HashMap<>();
      }

      log.debug("Getting job counts for {} designations in single query", designationNames.size());

      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      
      // Use multiple should clauses for each designation (equivalent to terms query)
      BoolQuery.Builder designationQueryBuilder = new BoolQuery.Builder();
      for (String designationName : designationNames) {
        designationQueryBuilder.should(
            Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME + ".keyword")
                .value(designationName))));
      }
      boolQueryBuilder.must(designationQueryBuilder.build()._toQuery());

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("designations", a -> a
              .terms(t -> t
                  .field(ElasticsearchConstants.FIELD_DESIGNATION_NAME + ".keyword")
                  .size(designationNames.size() * 2) // Ensure we get all designations
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      Map<String, Long> designationJobCounts = new HashMap<>();

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate designationsAgg = aggregations.get("designations");
          if (designationsAgg != null && designationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = designationsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              String designationName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
              if (designationName != null && !designationName.trim().isEmpty()) {
                designationJobCounts.put(designationName, jobCount);
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing designation aggregation response: {}", e.getMessage());
        return new HashMap<>();
      }

      log.debug("Successfully retrieved job counts for {} designations", designationJobCounts.size());
      return designationJobCounts;

    } catch (Exception e) {
      log.error("Error getting job counts for multiple designations: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  /**
   * Get job counts for multiple skills in a single query using aggregation
   */
  private Map<String, Long> getJobCountsForMultipleSkills(List<String> skillNames) {
    try {
      if (skillNames == null || skillNames.isEmpty()) {
        return new HashMap<>();
      }

      log.debug("Getting job counts for {} skills in single query", skillNames.size());

      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      
      // Use multiple should clauses for each skill (equivalent to terms query)
      BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
      for (String skillName : skillNames) {
        skillQueryBuilder.should(
            Query.of(q -> q.term(t -> t.field("skillNames.keyword").value(skillName))));
      }
      boolQueryBuilder.must(skillQueryBuilder.build()._toQuery());

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("skills", a -> a
              .terms(t -> t
                  .field("skillNames.keyword")
                  .size(skillNames.size() * 2) // Ensure we get all skills
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      Map<String, Long> skillJobCounts = new HashMap<>();

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate skillsAgg = aggregations.get("skills");
          if (skillsAgg != null && skillsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = skillsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              String skillName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
              if (skillName != null && !skillName.trim().isEmpty()) {
                skillJobCounts.put(skillName, jobCount);
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing skill aggregation response: {}", e.getMessage());
        return new HashMap<>();
      }

      log.debug("Successfully retrieved job counts for {} skills", skillJobCounts.size());
      return skillJobCounts;

    } catch (Exception e) {
      log.error("Error getting job counts for multiple skills: {}", e.getMessage());
      return new HashMap<>();
    }
  }

  /**
   * Get designations by department with job counts from jobs index
   * This is used as a fallback when no jobs are found for similar designations
   */
  private List<Map<String, Object>> getDesignationsByDepartmentWithJobCounts(String departmentName) {
    try {
      log.info("Getting designations by department with job counts from jobs index: {}", departmentName);

      // First, get the department ID from the designations index
      Long departmentId = getDepartmentIdByName(departmentName);
      if (departmentId == null) {
        log.warn("Department ID not found for department: {}", departmentName);
        return new ArrayList<>();
      }

      // Query jobs index for all designations in this department
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field("departmentId").value(departmentId))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("designations", a -> a
              .terms(t -> t
                  .field(ElasticsearchConstants.FIELD_DESIGNATION_NAME + ".keyword")
                  .size(100) // Get up to 100 designations
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      List<Map<String, Object>> designationsWithCounts = new ArrayList<>();

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate designationsAgg = aggregations.get("designations");
          if (designationsAgg != null && designationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = designationsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              String designationName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
                          if (designationName != null && !designationName.trim().isEmpty() && jobCount > 0) {
              Map<String, Object> designationWithCount = createCommonResponseItem(
                  designationName, jobCount, "designation");
              designationsWithCounts.add(designationWithCount);
            }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing department designation aggregation response: {}", e.getMessage());
        return new ArrayList<>();
      }

      // Sort by job count descending
      designationsWithCounts.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      log.info("Found {} designations with jobs for department: {}", designationsWithCounts.size(), departmentName);
      return designationsWithCounts;

    } catch (Exception e) {
      log.error("Error getting designations by department with job counts: {}", departmentName, e);
      return new ArrayList<>();
    }
  }

  /**
   * Get department ID by department name from designations index
   */
  private Long getDepartmentIdByName(String departmentName) {
    try {
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field("departmentName.keyword").value(departmentName))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.DESIGNATIONS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(1)
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      if (!response.hits().hits().isEmpty()) {
        Map<String, Object> designationDoc = response.hits().hits().get(0).source();
        Object departmentIdObj = designationDoc.get("departmentId");
        if (departmentIdObj != null) {
          if (departmentIdObj instanceof Long) {
            return (Long) departmentIdObj;
          } else if (departmentIdObj instanceof Integer) {
            return ((Integer) departmentIdObj).longValue();
          } else if (departmentIdObj instanceof String) {
            return Long.parseLong((String) departmentIdObj);
          }
        }
      }
      return null;

    } catch (Exception e) {
      log.warn("Error getting department ID for department: {}", departmentName, e);
      return null;
    }
  }

  /**
   * Create a common response format for both designations and skills
   */
  private Map<String, Object> createCommonResponseItem(String keyword, long jobCount, String type) {
    Map<String, Object> item = new HashMap<>();
    item.put("similarKeyword", keyword);
    item.put("seoText", keyword+" Jobs");
    item.put("jobCount", jobCount);
    return item;
  }

  private Map<String, Object> createDesignationWithJobCount(String designationName, long jobCount) {
    Map<String, Object> designation = new HashMap<>();
    designation.put("designationName", designationName);
    designation.put("jobCount", jobCount);
    return designation;
  }

  private List<Map<String, Object>> getSimilarDesignationLocationCombinations(String designation,
      String location) {
    try {
      List<Map<String, Object>> combinations = new ArrayList<>();

      // Get similar designations first
      List<String> similarDesignations = getSimilarDesignationsForLocationSearch(designation);

      // For each similar designation, get job count in the same location
      for (String similarDesignation : similarDesignations) {
        if (!similarDesignation.equalsIgnoreCase(designation)) {
          long jobCount = getJobCountForQueryLocationCombination(similarDesignation, location, "designation");

          if (jobCount > 0) {
            Map<String, Object> combination = new HashMap<>();
            combination.put("designationName", similarDesignation);
            combination.put("location", location);
            combination.put("jobCount", jobCount);
            combination.put("seoText", similarDesignation + " jobs in " + location);
            combinations.add(combination);
          }
        }
      }

      return combinations;

    } catch (Exception e) {
      log.error("Error getting similar designation + location combinations for {} in {}",
          designation, location, e);
      return new ArrayList<>();
    }
  }

  private List<Map<String, Object>> getSimilarSkillLocationCombinations(String skill,
      String location) {
    try {
      List<Map<String, Object>> combinations = new ArrayList<>();

      // Get similar skills first
      List<String> similarSkills = getSimilarSkillsForLocationSearch(skill);

      // For each similar skill, get job count in the same location
      for (String similarSkill : similarSkills) {
        if (!similarSkill.equalsIgnoreCase(skill)) {
          long jobCount = getJobCountForQueryLocationCombination(similarSkill, location, "skill");

          if (jobCount > 0) {
            Map<String, Object> combination = new HashMap<>();
            combination.put("skillName", similarSkill);
            combination.put("location", location);
            combination.put("jobCount", jobCount);
            combination.put("seoText", similarSkill + " jobs in " + location);
            combinations.add(combination);
          }
        }
      }

      return combinations;

    } catch (Exception e) {
      log.error("Error getting similar skill + location combinations for {} in {}", skill, location,
          e);
      return new ArrayList<>();
    }
  }

  private List<String> getSimilarDesignationsForLocationSearch(String designation) {
    try {
      List<String> similarDesignations = new ArrayList<>();

      BoolQuery.Builder designationQueryBuilder = new BoolQuery.Builder();
      designationQueryBuilder.must(Query.of(q -> q.match(m -> m.field("name").query(designation))));

      SearchRequest designationSearchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.DESIGNATIONS_INDEX)
          .query(designationQueryBuilder.build()._toQuery())
          .size(1)
      );

      SearchResponse<Map> designationResponse = elasticsearchClient.search(designationSearchRequest,
          Map.class);

      if (!designationResponse.hits().hits().isEmpty()) {
        Map<String, Object> designationDoc = designationResponse.hits().hits().get(0).source();
        Object similarByNameObj = designationDoc.get("similarDesignationsByName");
        if (similarByNameObj instanceof List<?>) {
          for (Object o : (List<?>) similarByNameObj) {
            if (o != null) {
              String name = o.toString().trim();
              if (!name.isEmpty()) {
                similarDesignations.add(name);
              }
            }
          }
        } else if (similarByNameObj != null) {
          String s = similarByNameObj.toString().trim();
          if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
          }
          String[] similarArray = s.split(",");
          for (String similar : similarArray) {
            String trimmed = similar.trim().replaceAll("^\"|\"$", "");
            if (!trimmed.isEmpty()) {
              similarDesignations.add(trimmed);
            }
          }
        }
      }

      return similarDesignations;

    } catch (Exception e) {
      log.error("Error getting similar designations for location search: {}", designation, e);
      return new ArrayList<>();
    }
  }

  private List<String> getSimilarSkillsForLocationSearch(String skill) {
    try {
      List<String> similarSkills = new ArrayList<>();

      BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("name").query(skill))));
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("description").query(skill))));
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("category").query(skill))));
      skillQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("similarSkillsByName").query(skill))));

      SearchRequest skillSearchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.SKILLS_INDEX)
          .query(skillQueryBuilder.build()._toQuery())
          .size(10)
      );

      SearchResponse<Map> skillResponse = elasticsearchClient.search(skillSearchRequest, Map.class);

      for (Hit<Map> hit : skillResponse.hits().hits()) {
        Map<String, Object> skillDoc = hit.source();
        String skillName = (String) skillDoc.get("name");

        if (skillName != null && !skillName.equalsIgnoreCase(skill)) {
          similarSkills.add(skillName);
        }
      }

      return similarSkills;

    } catch (Exception e) {
      log.error("Error getting similar skills for location search: {}", skill, e);
      return new ArrayList<>();
    }
  }

  /**
   * Get job counts for a designation across different locations
   */
  private List<Map<String, Object>> getDesignationLocationCombinations(String designation) {
    try {
      List<Map<String, Object>> locationCombinations = new ArrayList<>();

      // Query jobs index for this designation across all locations
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      boolQueryBuilder.must(Query.of(q -> q.term(
          t -> t.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME + ".keyword").value(designation))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("locations", a -> a
              .terms(t -> t
                  .field("cityName.keyword")
                  .size(50) // Get up to 50 locations
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate locationsAgg = aggregations.get("locations");
          if (locationsAgg != null && locationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = locationsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              String cityName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
              if (cityName != null && !cityName.trim().isEmpty() && jobCount > 0) {
                Map<String, Object> locationCombo = new HashMap<>();
                locationCombo.put("location", cityName);
                locationCombo.put("jobCount", jobCount);
                locationCombinations.add(locationCombo);
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing designation location aggregation response: {}", e.getMessage());
      }

      return locationCombinations;

    } catch (Exception e) {
      log.error("Error getting designation location combinations for: {}", designation, e);
      return new ArrayList<>();
    }
  }

  /**
   * Get job counts for a skill across different locations
   */
  private List<Map<String, Object>> getSkillLocationCombinations(String skill) {
    try {
      List<Map<String, Object>> locationCombinations = new ArrayList<>();

      // Query jobs index for this skill across all locations
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));
      
      // Search in skillNames field
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("skillNames.keyword").value(skill))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
          .aggregations("locations", a -> a
              .terms(t -> t
                  .field("cityName.keyword")
                  .size(50) // Get up to 50 locations
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      try {
        Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
        if (aggregations != null) {
          co.elastic.clients.elasticsearch._types.aggregations.Aggregate locationsAgg = aggregations.get("locations");
          if (locationsAgg != null && locationsAgg.isSterms()) {
            co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = locationsAgg.sterms();
            for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
              String cityName = bucket.key().stringValue();
              Long jobCount = bucket.docCount();
              if (cityName != null && !cityName.trim().isEmpty() && jobCount > 0) {
                Map<String, Object> locationCombo = new HashMap<>();
                locationCombo.put("location", cityName);
                locationCombo.put("jobCount", jobCount);
                locationCombinations.add(locationCombo);
              }
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing skill location aggregation response: {}", e.getMessage());
      }

      return locationCombinations;

    } catch (Exception e) {
      log.error("Error getting skill location combinations for: {}", skill, e);
      return new ArrayList<>();
    }
  }

  private Map<String, Object> getTopDesignationsByJobCountFallback(int limit) {
    try {
      log.info("Using fallback method for top designations by job count, limit: {}", limit);

      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(10000) // Get all jobs for manual processing
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Group by designation and count jobs
      Map<String, Long> designationJobCounts = new HashMap<>();
      long totalJobs = 0;

      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> job = hit.source();
        if (job != null && job.get(ElasticsearchConstants.FIELD_DESIGNATION_NAME) != null) {
          String designationName = job.get(ElasticsearchConstants.FIELD_DESIGNATION_NAME)
              .toString();
          designationJobCounts.merge(designationName, 1L, Long::sum);
          totalJobs++;
        }
      }

      // Convert to list and sort by job count
      List<Map<String, Object>> topDesignations = designationJobCounts.entrySet().stream()
          .map(entry -> {
            Map<String, Object> designation = new HashMap<>();
            designation.put("designationName", entry.getKey());
            designation.put("jobCount", entry.getValue());
            return designation;
          })
          .sorted((a, b) -> Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")))
          .limit(limit)
          .collect(Collectors.toList());

      Map<String, Object> result = new HashMap<>();
      result.put("topDesignations", topDesignations);
      result.put("totalDesignations", designationJobCounts.size());
      result.put("totalJobs", totalJobs);

      return result;

    } catch (Exception e) {
      log.error("Error in fallback method for top designations: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error in fallback method: " + e.getMessage());
      return error;
    }
  }
}
