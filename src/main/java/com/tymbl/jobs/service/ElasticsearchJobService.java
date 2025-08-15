package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.Job.JobType;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchJobService {

  private final ElasticsearchClient elasticsearchClient;
  private final DropdownService dropdownService;
  private final ObjectMapper objectMapper;

  private static final String INDEX_NAME = "jobs";

  /**
   * Sync a job to Elasticsearch (save or update) Does not fail the main transaction if ES fails
   */
  public void syncJobToElasticsearch(Job job) {
    try {
      Map<String, Object> jobDocument = buildJobDocument(job);

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(INDEX_NAME)
          .id(job.getId().toString())
          .document(jobDocument)
      );

      IndexResponse response = elasticsearchClient.index(indexRequest);

      log.info("Successfully synced job {} to Elasticsearch with result: {}",
          job.getId(), response.result().name());

    } catch (Exception e) {
      log.error("Failed to sync job {} to Elasticsearch. Error: {}", job.getId(), e.getMessage(),
          e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Search jobs in Elasticsearch
   */
  public JobSearchResponse searchJobs(JobSearchRequest request, Long userDesignationId) {
    try {
      // Extract all request fields to final variables for lambda usage
      final Long cityId = request.getCityId();
      final Long countryId = request.getCountryId();
      final Long companyId = request.getCompanyId();
      final Long designationId = request.getDesignationId();
      final Integer minExperience = request.getMinExperience();
      final Integer maxExperience = request.getMaxExperience();
      final Integer page = request.getPage();
      final Integer size = request.getSize();
      final Long finalUserDesignationId = userDesignationId;

      // Build the main query
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

      // Keyword search in searchableText
      if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
        BoolQuery.Builder keywordQueryBuilder = new BoolQuery.Builder();
        final List<String> keywords = request.getKeywords();
        for (String keyword : keywords) {
          keywordQueryBuilder.should(
              Query.of(q -> q.match(m -> m.field("searchableText").query(keyword))));
          keywordQueryBuilder.should(
              Query.of(q -> q.match(m -> m.field("companyName").query(keyword))));
          keywordQueryBuilder.should(
              Query.of(q -> q.match(m -> m.field("designationName").query(keyword))));
        }
        boolQueryBuilder.must(keywordQueryBuilder.build()._toQuery());
      }

      // City filter
      if (cityId != null) {
        boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("cityId").value(cityId))));
      }

      // Country filter
      if (countryId != null) {
        boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("countryId").value(countryId))));
      }

      // Company filter
      if (companyId != null) {
        boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("companyId").value(companyId))));
      }

      // Designation filter
      if (designationId != null) {
        boolQueryBuilder.filter(
            Query.of(q -> q.term(t -> t.field("designationId").value(designationId))));
      }

      // Experience range filter
      if (minExperience != null || maxExperience != null) {
        BoolQuery.Builder experienceQueryBuilder = new BoolQuery.Builder();

        if (minExperience != null) {
          experienceQueryBuilder.must(Query.of(
              q -> q.range(r -> r.field("maxExperience").gte(JsonData.of(minExperience)))));
        }

        if (maxExperience != null) {
          experienceQueryBuilder.must(Query.of(
              q -> q.range(r -> r.field("minExperience").lte(JsonData.of(maxExperience)))));
        }

        boolQueryBuilder.filter(experienceQueryBuilder.build()._toQuery());
      }

      // Only show active jobs
      boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("active").value(true))));

      final Query baseQuery = boolQueryBuilder.build()._toQuery();
      // Apply boosting if user is logged in and has a designation
      final Query finalQuery = finalUserDesignationId != null ?
          FunctionScoreQuery.of(fs -> fs
              .query(baseQuery)
              .functions(f -> f
                  .filter(Query.of(
                      q -> q.term(t -> t.field("designationId").value(finalUserDesignationId))))
                  .weight(2.0)
              )
          )._toQuery() : baseQuery;

      // Build search request
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(finalQuery)
          .sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)))
          .sort(sort -> sort.field(f -> f.field("createdAt").order(SortOrder.Desc)))
          .from(page * size)
          .size(size)
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      return parseSearchResponse(response);

    } catch (Exception e) {
      log.error("Failed to search jobs in Elasticsearch. Error: {}", e.getMessage(), e);
      return JobSearchResponse.builder()
          .jobs(Collections.emptyList())
          .total(0L)
          .page(request.getPage())
          .size(request.getSize())
          .build();
    }
  }

  /**
   * Reindex all jobs from database to Elasticsearch
   */
  public void reindexAllJobs(List<Job> jobs) {
    log.info("Starting reindex of {} jobs to Elasticsearch", jobs.size());

    int successCount = 0;
    int failureCount = 0;

    for (Job job : jobs) {
      try {
        syncJobToElasticsearch(job);
        successCount++;
      } catch (Exception e) {
        log.error("Failed to reindex job {}. Error: {}", job.getId(), e.getMessage());
        failureCount++;
      }
    }

    log.info("Reindex completed. Success: {}, Failures: {}", successCount, failureCount);
  }

  /**
   * Build job document for Elasticsearch
   */
  private Map<String, Object> buildJobDocument(Job job) {
    Map<String, Object> document = new HashMap<>();

    // Basic job fields
    document.put("id", job.getId());
    document.put("title", job.getTitle());
    document.put("description", job.getDescription());
    document.put("cityId", job.getCityId());
    document.put("countryId", job.getCountryId());
    document.put("designationId", job.getDesignationId());
    document.put("minSalary", job.getMinSalary());
    document.put("maxSalary", job.getMaxSalary());
    document.put("minExperience", job.getMinExperience());
    document.put("maxExperience", job.getMaxExperience());
    document.put("jobType", job.getJobType() != null ? job.getJobType().name() : null);
    document.put("currencyId", job.getCurrencyId());
    document.put("companyId", job.getCompanyId());
    document.put("postedById", job.getPostedById());
    document.put("active", job.isActive());

    // Convert LocalDateTime to Date to avoid Jackson serialization issues
    if (job.getCreatedAt() != null) {
      document.put("createdAt", java.sql.Timestamp.valueOf(job.getCreatedAt()));
    }
    if (job.getUpdatedAt() != null) {
      document.put("updatedAt", java.sql.Timestamp.valueOf(job.getUpdatedAt()));
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
    document.put("companyName", companyName);

    String designationName = null;
    if (job.getDesignationId() != null) {
      designationName = dropdownService.getDesignationNameById(job.getDesignationId());
    }
    document.put("designationName", designationName);

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
    if (job.getCityId() != null) {
      String cityName = dropdownService.getCityNameById(job.getCityId());
        if (cityName != null) {
            searchableText.append(cityName).append(" ");
        }
    }

    if (job.getCountryId() != null) {
      String countryName = dropdownService.getCountryNameById(job.getCountryId());
        if (countryName != null) {
            searchableText.append(countryName).append(" ");
        }
    }

    document.put("searchableText", searchableText.toString().trim());

    return document;
  }

  /**
   * Parse Elasticsearch search response
   */
  private JobSearchResponse parseSearchResponse(SearchResponse<Map> response) {
    List<JobResponse> jobs = new ArrayList<>();

    for (Hit<Map> hit : response.hits().hits()) {
      Map<String, Object> jobData = new HashMap<>(hit.source());
      jobData.put("score", hit.score());
      jobs.add(convertMapToJobResponse(jobData));
    }

    return JobSearchResponse.builder()
        .jobs(jobs)
        .total(response.hits().total().value())
        .page(0) // Will be set by controller
        .size(jobs.size())
        .build();
  }

  /**
   * Convert Map from Elasticsearch to JobResponse
   */
  private JobResponse convertMapToJobResponse(Map<String, Object> jobData) {
    JobResponse response = new JobResponse();

    // Basic fields
    if (jobData.get("id") != null) {
      response.setId(Long.valueOf(jobData.get("id").toString()));
    }
    if (jobData.get("title") != null) {
      response.setTitle(jobData.get("title").toString());
    }
    if (jobData.get("description") != null) {
      response.setDescription(jobData.get("description").toString());
    }
    if (jobData.get("cityId") != null) {
      response.setCityId(Long.valueOf(jobData.get("cityId").toString()));
    }
    if (jobData.get("countryId") != null) {
      response.setCountryId(Long.valueOf(jobData.get("countryId").toString()));
    }
    if (jobData.get("designationId") != null) {
      response.setDesignationId(Long.valueOf(jobData.get("designationId").toString()));
    }
    if (jobData.get("designationName") != null) {
      response.setDesignation(jobData.get("designationName").toString());
    }
    if (jobData.get("minSalary") != null) {
      response.setMinSalary(new BigDecimal(jobData.get("minSalary").toString()));
    }
    if (jobData.get("maxSalary") != null) {
      response.setMaxSalary(new BigDecimal(jobData.get("maxSalary").toString()));
    }
    if (jobData.get("minExperience") != null) {
      response.setMinExperience(Integer.valueOf(jobData.get("minExperience").toString()));
    }
    if (jobData.get("maxExperience") != null) {
      response.setMaxExperience(Integer.valueOf(jobData.get("maxExperience").toString()));
    }
    if (jobData.get("jobType") != null) {
      response.setJobType(JobType.valueOf(jobData.get("jobType").toString()));
    }
    if (jobData.get("currencyId") != null) {
      response.setCurrencyId(Long.valueOf(jobData.get("currencyId").toString()));
    }
    if (jobData.get("companyId") != null) {
      response.setCompanyId(Long.valueOf(jobData.get("companyId").toString()));
    }
    if (jobData.get("companyName") != null) {
      response.setCompany(jobData.get("companyName").toString());
    }
    if (jobData.get("postedById") != null) {
      response.setPostedBy(Long.valueOf(jobData.get("postedById").toString()));
    }
    if (jobData.get("active") != null) {
      response.setActive(Boolean.valueOf(jobData.get("active").toString()));
    }
    // Robustly parse createdAt and updatedAt with a single try-catch block
    try {
      if (jobData.get("createdAt") != null) {
        Object createdAtObj = jobData.get("createdAt");
        if (createdAtObj instanceof Number || createdAtObj.toString().matches("\\d+")) {
          long millis = Long.parseLong(createdAtObj.toString());
          response.setCreatedAt(java.time.Instant.ofEpochMilli(millis)
              .atZone(java.time.ZoneId.systemDefault())
              .toLocalDateTime());
        } else {
          response.setCreatedAt(LocalDateTime.parse(createdAtObj.toString()));
        }
      }
      if (jobData.get("updatedAt") != null) {
        Object updatedAtObj = jobData.get("updatedAt");
        if (updatedAtObj instanceof Number || updatedAtObj.toString().matches("\\d+")) {
          long millis = Long.parseLong(updatedAtObj.toString());
          response.setUpdatedAt(java.time.Instant.ofEpochMilli(millis)
              .atZone(java.time.ZoneId.systemDefault())
              .toLocalDateTime());
        } else {
          response.setUpdatedAt(LocalDateTime.parse(updatedAtObj.toString()));
        }
      }
    } catch (Exception e) {
      log.warn(
          "Failed to parse createdAt/updatedAt in ElasticsearchJobService.convertMapToJobResponse: {}",
          e.getMessage(), e);
    }
    if (jobData.get("tags") != null) {
      @SuppressWarnings("unchecked")
      Set<String> tags = new HashSet<>((List<String>) jobData.get("tags"));
      response.setTags(tags);
    }
    if (jobData.get("openingCount") != null) {
      response.setOpeningCount(Integer.valueOf(jobData.get("openingCount").toString()));
    }
    if (jobData.get("uniqueUrl") != null) {
      response.setUniqueUrl(jobData.get("uniqueUrl").toString());
    }
    if (jobData.get("platform") != null) {
      response.setPlatform(jobData.get("platform").toString());
    }

    // Set default values for fields not in Elasticsearch
    response.setSuperAdminPosted(false);
    response.setApproved(JobApprovalStatus.APPROVED.getValue());
    response.setReferrerCount(0);
    response.setUserRole("VIEWER");
    response.setActualPostedBy(response.getPostedBy());

    // Enrich with dropdown values
    enrichJobResponseWithDropdownValues(response);

    return response;
  }

  /**
   * Enrich job response with dropdown values from DropdownService
   */
  private void enrichJobResponseWithDropdownValues(JobResponse response) {
    try {
      // Get city name
      if (response.getCityId() != null) {
        String cityName = dropdownService.getCityNameById(response.getCityId());
        response.setCityName(cityName);
      }

      // Get country name
      if (response.getCountryId() != null) {
        String countryName = dropdownService.getCountryNameById(response.getCountryId());
        response.setCountryName(countryName);
      }

      // Get designation name
      if (response.getDesignationId() != null) {
        String designationName = dropdownService.getDesignationNameById(
            response.getDesignationId());
        response.setDesignationName(designationName);
      }

      // Get currency information
      if (response.getCurrencyId() != null) {
        String currencyName = dropdownService.getCurrencyNameById(response.getCurrencyId());
        String currencySymbol = dropdownService.getCurrencySymbolById(response.getCurrencyId());
        response.setCurrencyName(currencyName);
        response.setCurrencySymbol(currencySymbol);
      }

      // Get company name
      if (response.getCompanyId() != null) {
        String companyName = dropdownService.getCompanyNameById(response.getCompanyId());
        response.setCompanyName(companyName);
      }
    } catch (Exception e) {
      log.warn("Failed to enrich job response with dropdown values for job {}: {}",
          response.getId(), e.getMessage());
    }
  }

  // ============================================================================
  // SEO INTERLINKING METHODS
  // ============================================================================

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
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("designationName").query(query))));
      } else if ("skill".equalsIgnoreCase(type)) {
        // Search in tags (skills)
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("tags").query(query))));
      } else {
        // Default to designation search
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("designationName").query(query))));
      }

      // Only active jobs
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      // Build search request with aggregation
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the aggregation results
          .aggregations("locations", a -> a
              .terms(t -> t
                  .field("cityName")
                  .size(1000) // Get up to 1000 unique cities
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Extract aggregation results
      long totalJobs = 0;
      List<Map<String, Object>> locationCombinations = new ArrayList<>();

      try {
        // Parse the aggregation response
        Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
        Map<String, Object> aggregations = (Map<String, Object>) responseMap.get("aggregations");

        if (aggregations != null) {
          Map<String, Object> locationsAgg = (Map<String, Object>) aggregations.get("locations");
          if (locationsAgg != null) {
            List<Map<String, Object>> buckets = (List<Map<String, Object>>) locationsAgg.get(
                "buckets");

            if (buckets != null) {
              for (Map<String, Object> bucket : buckets) {
                String location = (String) bucket.get("key");
                Long jobCount = ((Number) bucket.get("doc_count")).longValue();

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
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("designationName").query(query))));
      } else if ("skill".equalsIgnoreCase(type)) {
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("tags").query(query))));
      } else {
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("designationName").query(query))));
      }

      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      // Build search request
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
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
          .index("designations")
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
          .index("skills")
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
      skillJobQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      // Search in job title, description, and skills
      skillJobQueryBuilder.should(Query.of(q -> q.match(m -> m.field("title").query(skillName))));
      skillJobQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("description").query(skillName))));
      skillJobQueryBuilder.should(Query.of(q -> q.match(m -> m.field("skills").query(skillName))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
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
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));
      boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("cityName").query(location))));

      if ("designation".equals(type)) {
        boolQueryBuilder.must(Query.of(q -> q.match(m -> m.field("designationName").query(query))));
      } else if ("skill".equals(type)) {
        // For skills, search in job title, description, and skills fields
        BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("title").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("description").query(query))));
        skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("skills").query(query))));
        boolQueryBuilder.must(skillQueryBuilder.build()._toQuery());
      }

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
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
   * Get similar designation + location combinations
   */
  private List<Map<String, Object>> getSimilarDesignationLocationCombinations(String designation,
      String location) {
    try {
      List<Map<String, Object>> combinations = new ArrayList<>();

      // Get similar designations from the designations index
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

  /**
   * Get similar skill + location combinations
   */
  private List<Map<String, Object>> getSimilarSkillLocationCombinations(String skill,
      String location) {
    try {
      List<Map<String, Object>> combinations = new ArrayList<>();

      // Get similar skills from the skills index
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

  /**
   * Get similar designations for location search (simplified version)
   */
  private List<String> getSimilarDesignationsForLocationSearch(String designation) {
    try {
      List<String> similarDesignations = new ArrayList<>();

      // Search in designations index for similar designations
      BoolQuery.Builder designationQueryBuilder = new BoolQuery.Builder();
      designationQueryBuilder.must(Query.of(q -> q.match(m -> m.field("name").query(designation))));

      SearchRequest designationSearchRequest = SearchRequest.of(s -> s
          .index("designations")
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

  /**
   * Get similar skills for location search (simplified version)
   */
  private List<String> getSimilarSkillsForLocationSearch(String skill) {
    try {
      List<String> similarSkills = new ArrayList<>();

      // Search in skills index for similar skills
      BoolQuery.Builder skillQueryBuilder = new BoolQuery.Builder();
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("name").query(skill))));
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("description").query(skill))));
      skillQueryBuilder.should(Query.of(q -> q.match(m -> m.field("category").query(skill))));
      skillQueryBuilder.should(
          Query.of(q -> q.match(m -> m.field("similarSkillsByName").query(skill))));

      SearchRequest skillSearchRequest = SearchRequest.of(s -> s
          .index("skills")
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
   * Get top designations by job count
   */
  public Map<String, Object> getTopDesignationsByJobCount(int limit) {
    try {
      log.info("Getting top {} designations by job count", limit);

      // Build search request with aggregation
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need the actual documents, just the aggregation results
          .aggregations("designations", a -> a
              .terms(t -> t
                  .field("designationName")
                  .size(limit * 2) // Get more than needed to ensure we have enough after filtering
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Extract aggregation results
      long totalJobs = 0;
      List<Map<String, Object>> topDesignations = new ArrayList<>();

      try {
        // Parse the aggregation response
        Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
        Map<String, Object> aggregations = (Map<String, Object>) responseMap.get("aggregations");

        if (aggregations != null) {
          Map<String, Object> designationsAgg = (Map<String, Object>) aggregations.get(
              "designations");
          if (designationsAgg != null) {
            List<Map<String, Object>> buckets = (List<Map<String, Object>>) designationsAgg.get(
                "buckets");

            if (buckets != null) {
              for (int i = 0; i < Math.min(buckets.size(), limit); i++) {
                Map<String, Object> bucket = buckets.get(i);
                String designationName = (String) bucket.get("key");
                Long jobCount = ((Number) bucket.get("doc_count")).longValue();

                if (designationName != null && !designationName.trim().isEmpty()) {
                  Map<String, Object> designation = new HashMap<>();
                  designation.put("designationName", designationName);
                  designation.put("jobCount", jobCount);
                  topDesignations.add(designation);

                  totalJobs += jobCount;
                }
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
   * Fallback method for top designations (manual processing)
   */
  private Map<String, Object> getTopDesignationsByJobCountFallback(int limit) {
    try {
      log.info("Using fallback method for top designations by job count, limit: {}", limit);

      // Build search request to get all active jobs
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(boolQueryBuilder.build()._toQuery())
          .size(10000) // Get all jobs for manual processing
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Group by designation and count jobs
      Map<String, Long> designationJobCounts = new HashMap<>();
      long totalJobs = 0;

      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> job = hit.source();
        if (job != null && job.get("designationName") != null) {
          String designationName = job.get("designationName").toString();
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


  /**
   * Get designations by department from Elasticsearch designations index
   */
  private List<String> getDesignationsByDepartmentFromElasticsearch(String departmentName) {
    try {
      log.info("Getting designations by department from Elasticsearch: {}", departmentName);

      // Build search request to find designations in the same department
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.term(t -> t.field("departmentName").value(departmentName))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index("designations")
          .query(boolQueryBuilder.build()._toQuery())
          .size(1000) // Get up to 1000 designations
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
   * Get job count for a designation using aggregation
   */
  private long getJobCountForDesignationWithAggregation(String designationName) {
    try {
      // Build search request with aggregation
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.match(m -> m.field("designationName").query(designationName))));
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need documents, just the count
          .aggregations("total_jobs", a -> a
              .valueCount(v -> v.field("_id"))
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Extract the count from aggregation
      try {
        Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
        Map<String, Object> aggregations = (Map<String, Object>) responseMap.get("aggregations");

        if (aggregations != null) {
          Map<String, Object> totalJobsAgg = (Map<String, Object>) aggregations.get("total_jobs");
          if (totalJobsAgg != null) {
            Number value = (Number) totalJobsAgg.get("value");
            if (value != null) {
              return value.longValue();
            }
          }
        }
      } catch (Exception e) {
        log.warn("Error parsing job count aggregation for designation {}: {}", designationName,
            e.getMessage());
      }

      // Fallback to manual count if aggregation fails
      return getJobCountForDesignation(designationName);

    } catch (Exception e) {
      log.error("Error getting job count with aggregation for designation: {}", designationName, e);
      // Fallback to manual count
      return getJobCountForDesignation(designationName);
    }
  }

  /**
   * Helper method to get department for a designation
   */
  private String getDepartmentForDesignation(String designationName) {
    try {
      // This is a simplified implementation
      // In a real scenario, you would query the designation index
      // For now, return a default department based on common patterns
      if (designationName.toLowerCase().contains("software") ||
          designationName.toLowerCase().contains("engineer") ||
          designationName.toLowerCase().contains("developer")) {
        return "Engineering";
      } else if (designationName.toLowerCase().contains("sales") ||
          designationName.toLowerCase().contains("account")) {
        return "Sales";
      } else if (designationName.toLowerCase().contains("marketing")) {
        return "Marketing";
      } else if (designationName.toLowerCase().contains("hr") ||
          designationName.toLowerCase().contains("human")) {
        return "Human Resources";
      } else if (designationName.toLowerCase().contains("finance") ||
          designationName.toLowerCase().contains("accounting")) {
        return "Finance";
      } else {
        return "Other";
      }
    } catch (Exception e) {
      log.warn("Error getting department for designation: {}", designationName, e);
      return "Other";
    }
  }

  /**
   * Helper method to get designations by department with job counts
   */
  private List<Map<String, Object>> getDesignationsByDepartmentWithJobCounts(String department) {
    try {
      // This is a simplified implementation
      // In a real scenario, you would query the designation index and jobs index
      // For now, return some common designations for the department

      List<Map<String, Object>> designations = new ArrayList<>();

      if ("Engineering".equals(department)) {
        designations.add(createDesignationWithJobCount("Senior Software Engineer", 45));
        designations.add(createDesignationWithJobCount("Tech Lead", 32));
        designations.add(createDesignationWithJobCount("Software Developer", 28));
        designations.add(createDesignationWithJobCount("DevOps Engineer", 25));
        designations.add(createDesignationWithJobCount("QA Engineer", 20));
      } else if ("Sales".equals(department)) {
        designations.add(createDesignationWithJobCount("Sales Representative", 40));
        designations.add(createDesignationWithJobCount("Account Manager", 35));
        designations.add(createDesignationWithJobCount("Business Development Manager", 30));
        designations.add(createDesignationWithJobCount("Sales Director", 25));
      } else if ("Marketing".equals(department)) {
        designations.add(createDesignationWithJobCount("Marketing Manager", 30));
        designations.add(createDesignationWithJobCount("Digital Marketing Specialist", 25));
        designations.add(createDesignationWithJobCount("Content Writer", 20));
      } else {
        // Generic designations for other departments
        designations.add(createDesignationWithJobCount("Manager", 30));
        designations.add(createDesignationWithJobCount("Specialist", 25));
        designations.add(createDesignationWithJobCount("Coordinator", 20));
      }

      return designations;

    } catch (Exception e) {
      log.warn("Error getting designations by department: {}", department, e);
      return new ArrayList<>();
    }
  }

  /**
   * Helper method to create designation with job count
   */
  private Map<String, Object> createDesignationWithJobCount(String designationName, long jobCount) {
    Map<String, Object> designation = new HashMap<>();
    designation.put("designationName", designationName);
    designation.put("jobCount", jobCount);
    return designation;
  }

  /**
   * Get job count for a designation (manual fallback method)
   */
  private long getJobCountForDesignation(String designationName) {
    try {
      // Build search request to count jobs for a designation
      BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
      boolQueryBuilder.must(
          Query.of(q -> q.match(m -> m.field("designationName").query(designationName))));
      boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(INDEX_NAME)
          .query(boolQueryBuilder.build()._toQuery())
          .size(0) // We don't need documents, just the count
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      // Return the total hits count
      return response.hits().total().value();

    } catch (Exception e) {
      log.error("Error getting job count for designation: {}", designationName, e);
      return 0;
    }
  }
} 