package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tymbl.jobs.constants.ElasticsearchConstants;
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

  /**
   * Get job location combinations for designation or skill
   */
  public Map<String, Object> getJobLocationCombinations(String query, String type) {
    try {
      log.info("Getting job location combinations for {}: {}", type, query);

      // Build search query based on type
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

      if ("designation".equalsIgnoreCase(type)) {
        // Search in designation name
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
      } else if ("skill".equalsIgnoreCase(type)) {
        // Search in tags (skills)
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("tags").query(query))));
      } else {
        // Default to designation search
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
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
        log.warn("Error parsing aggregation response, falling back to manual processing: {}",
            e.getMessage());
        // Fallback to manual processing if aggregation parsing fails
        return getJobLocationCombinationsFallback(query, type);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("query", query);
      result.put("type", type);
      result.put("totalJobs", totalJobs);
      result.put("locationCombinations", locationCombinations);

      return result;

    } catch (Exception e) {
      log.error("Error getting job location combinations for {}: {}", type, query, e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error getting job location combinations: " + e.getMessage());
      return error;
    }
  }

  /**
   * Fallback method for job location combinations (manual processing)
   */
  private Map<String, Object> getJobLocationCombinationsFallback(String query, String type) {
    try {
      log.info("Using fallback method for job location combinations for {}: {}", type, query);

      // Build search query based on type
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

      if ("designation".equalsIgnoreCase(type)) {
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
      } else if ("skill".equalsIgnoreCase(type)) {
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("tags").query(query))));
      } else {
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
      }

      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      // Build search request
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(10000) // Get all matching jobs for manual processing
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Process results to group by location
      Map<String, Long> locationJobCounts = new HashMap<>();
      long totalJobs = 0;

      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> job = hit.source();
        if (job != null && job.get("cityName") != null) {
          String cityName = job.get("cityName").toString();
          locationJobCounts.merge(cityName, 1L, Long::sum);
          totalJobs++;
        }
      }

      // Build response
      List<Map<String, Object>> locationCombinations = new ArrayList<>();
      for (Map.Entry<String, Long> entry : locationJobCounts.entrySet()) {
        Map<String, Object> combination = new HashMap<>();
        combination.put("location", entry.getKey());
        combination.put("jobCount", entry.getValue());
        combination.put("seoText", query + " jobs in " + entry.getKey());
        locationCombinations.add(combination);
      }

      // Sort by job count descending
      locationCombinations.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      Map<String, Object> result = new HashMap<>();
      result.put("query", query);
      result.put("type", type);
      result.put("totalJobs", totalJobs);
      result.put("locationCombinations", locationCombinations);

      return result;

    } catch (Exception e) {
      log.error("Error in fallback method for job location combinations: {}", e.getMessage());
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Error in fallback method: " + e.getMessage());
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
      String similarDesignationsStr = (String) designationDoc.get("similarDesignationsByName");
      String departmentName = (String) designationDoc.get("departmentName");

      List<String> similarDesignations = new ArrayList<>();

      // First, try to get similar designations from the similarDesignationsByName field
      if (similarDesignationsStr != null && !similarDesignationsStr.trim().isEmpty()) {
        String[] similarArray = similarDesignationsStr.split(",");
        for (String similar : similarArray) {
          String trimmed = similar.trim();
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

      // Get job counts for each similar designation using aggregation
      List<Map<String, Object>> similarDesignationsWithCounts = new ArrayList<>();
      for (String similarDesignation : similarDesignations) {
        long jobCount = getJobCountForDesignationWithAggregation(similarDesignation);
        if (jobCount > 0) {
          Map<String, Object> designationWithCount = createDesignationWithJobCount(
              similarDesignation, jobCount);
          similarDesignationsWithCounts.add(designationWithCount);
        }
      }

      // Sort by job count descending
      similarDesignationsWithCounts.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      // Now get similar skills from the skills index
      List<Map<String, Object>> similarSkillsWithCounts = getSimilarSkillsForDesignation(
          designation);

      Map<String, Object> result = new HashMap<>();
      result.put("inputDesignation", designation);
      result.put("department", departmentName);
      result.put("similarDesignations", similarDesignationsWithCounts);
      result.put("totalSimilarDesignations", similarDesignationsWithCounts.size());
      result.put("similarSkills", similarSkillsWithCounts);
      result.put("totalSimilarSkills", similarSkillsWithCounts.size());

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
   * Get similar skills for a designation from the skills index
   */
  private List<Map<String, Object>> getSimilarSkillsForDesignation(String designation) {
    try {
      log.info("Getting similar skills for designation: {}", designation);

      // Search for skills in the skills index that might be related to this designation
      BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();

      // Search in skill name, description, and category
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("name").query(designation))));
      skillQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("description").query(designation))));
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("category").query(designation))));

      // Also search in similar skills to find related skills
      skillQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("similarSkillsByName").query(designation))));

      SearchRequest skillSearchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.SKILLS_INDEX)
          .query(skillQueryBuilder.build()._toQuery())
          .size(20) // Limit to top 20 related skills
      );

      SearchResponse<Map> skillResponse = elasticsearchClient.search(skillSearchRequest, Map.class);

      List<Map<String, Object>> similarSkillsWithCounts = new ArrayList<>();

      for (Hit<Map> hit : skillResponse.hits().hits()) {
        Map<String, Object> skillDoc = hit.source();
        String skillName = (String) skillDoc.get("name");

        if (skillName != null && !skillName.equalsIgnoreCase(designation)) {
          // Get job count for this skill
          long jobCount = getJobCountForSkillWithAggregation(skillName);

          if (jobCount > 0) {
            Map<String, Object> skillWithCount = new HashMap<>();
            skillWithCount.put("skillName", skillName);
            skillWithCount.put("jobCount", jobCount);
            skillWithCount.put("category", skillDoc.get("category"));
            skillWithCount.put("description", skillDoc.get("description"));

            similarSkillsWithCounts.add(skillWithCount);
          }
        }
      }

      // Sort by job count descending
      similarSkillsWithCounts.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      // Limit to top 10 skills
      if (similarSkillsWithCounts.size() > 10) {
        similarSkillsWithCounts = similarSkillsWithCounts.subList(0, 10);
      }

      log.info("Found {} similar skills for designation: {}", similarSkillsWithCounts.size(),
          designation);
      return similarSkillsWithCounts;

    } catch (Exception e) {
      log.error("Error getting similar skills for designation: {}", designation, e);
      return new ArrayList<>();
    }
  }

  /**
   * Get job count for a skill using aggregation
   */
  private long getJobCountForSkillWithAggregation(String skillName) {
    try {
      // Search for jobs that have this skill in their requirements or title
      BoolQuery.Builder skillJobQueryBuilder = new BoolQuery.Builder();
      skillJobQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      // Search in job title, description, and skills
      skillJobQueryBuilder.should(Query.of(q -> q.match(m -> m.field("title").query(skillName))));
      skillJobQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("description").query(skillName))));
      skillJobQueryBuilder.should(Query.of(q -> q.match(m -> m.field("skills").query(skillName))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(skillJobQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the count
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      return response.hits().total().value();

    } catch (Exception e) {
      log.error("Error getting job count for skill: {}", skillName, e);
      return 0;
    }
  }

  /**
   * Get designation/skill + location combinations with similar combinations
   */
  public Map<String, Object> getDesignationSkillLocationCombinations(String query, String location,
      String type) {
    try {
      log.info("Getting designation/skill + location combinations for {}: {} in {}", type, query,
          location);

      // First, get the job count for the specific query + location combination
      long mainJobCount = getJobCountForQueryLocationCombination(query, location, type);

      // Get similar combinations based on the type
      List<Map<String, Object>> similarCombinations = new ArrayList<>();

      if ("designation".equals(type)) {
        // For designations, get similar designations and their job counts in the same location
        similarCombinations = getSimilarDesignationLocationCombinations(query, location);
      } else if ("skill".equals(type)) {
        // For skills, get similar skills and their job counts in the same location
        similarCombinations = getSimilarSkillLocationCombinations(query, location);
      }

      // Sort by job count descending
      similarCombinations.sort((a, b) ->
          Long.compare((Long) b.get("jobCount"), (Long) a.get("jobCount")));

      Map<String, Object> result = new HashMap<>();
      result.put("query", query + " + " + location);
      result.put("type", type);
      result.put("location", location);
      result.put("jobCount", mainJobCount);
      result.put("similarCombinations", similarCombinations);
      result.put("totalSimilarCombinations", similarCombinations.size());

      return result;

    } catch (Exception e) {
      log.error("Error getting designation/skill + location combinations for {}: {} in {}", type,
          query, location, e);
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
        boolQueryBuilder.must(Query.of(q -> q.match(
            m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(query))));
      } else if ("skill".equals(type)) {
        // For skills, search in job title, description, and skills fields
        BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("title").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("description").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("skills").query(query))));
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

  private long getJobCountForDesignationWithAggregation(String designationName) {
    try {
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.match(
              m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(designationName))));
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0)
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      return response.hits().total().value();

    } catch (Exception e) {
      log.error("Error getting job count for designation: {}", designationName, e);
      return 0;
    }
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

      List<String> similarDesignations = getSimilarDesignationsForLocationSearch(designation);

      for (String similarDesignation : similarDesignations) {
        if (!similarDesignation.equalsIgnoreCase(designation)) {
          long jobCount = getJobCountForQueryLocationCombination(similarDesignation, location,
              "designation");

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

      List<String> similarSkills = getSimilarSkillsForLocationSearch(skill);

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
        String similarDesignationsStr = (String) designationDoc.get("similarDesignationsByName");

        if (similarDesignationsStr != null && !similarDesignationsStr.trim().isEmpty()) {
          String[] similarArray = similarDesignationsStr.split(",");
          for (String similar : similarArray) {
            String trimmed = similar.trim();
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
