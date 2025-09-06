package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.dto.IndustryWiseCompaniesDTO;
import com.tymbl.common.entity.Job.JobType;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.IndustryCacheService;
import com.tymbl.jobs.constants.ElasticsearchConstants;
import com.tymbl.jobs.dto.CompanyResponse;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import com.tymbl.jobs.repository.CompanyRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service for querying jobs from Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchJobQueryService {

  /**
   * Inner class to hold cached data with timestamp
   */
  private static class CachedData<T> {

    private final T data;
    private final long timestamp;

    public CachedData(T data) {
      this.data = data;
      this.timestamp = System.currentTimeMillis();
    }

    public T getData() {
      return data;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public boolean isExpired() {
      return System.currentTimeMillis() - timestamp > CACHE_EXPIRATION_TIME;
    }
  }

  private final ElasticsearchClient elasticsearchClient;
  private final DropdownService dropdownService;
  private final IndustryCacheService industryCacheService;
  private final ObjectMapper objectMapper;
  private final CompanyRepository companyRepository;

  // Cache for industry-wise companies with 30-minute expiration
  private final Map<String, CachedData<List<IndustryWiseCompaniesDTO>>> industryWiseCompaniesCache = new HashMap<>();
  private static final long CACHE_EXPIRATION_TIME = 30 * 60 * 1000; // 30 minutes in milliseconds

  /**
   * Get cached data if not expired
   */
  private List<IndustryWiseCompaniesDTO> getCachedIndustryWiseCompanies() {
    CachedData<List<IndustryWiseCompaniesDTO>> cachedData = industryWiseCompaniesCache.get(
        "industry_wise_companies");
    if (cachedData != null && !cachedData.isExpired()) {
      log.debug("Returning industry-wise companies from cache");
      return cachedData.getData();
    }
    return null;
  }

  /**
   * Store data in cache
   */
  private void cacheIndustryWiseCompanies(List<IndustryWiseCompaniesDTO> data) {
    industryWiseCompaniesCache.put("industry_wise_companies", new CachedData<>(data));
    log.debug("Cached industry-wise companies data");
  }

  /**
   * Clear expired cache entries
   */
  private void clearExpiredCache() {
    industryWiseCompaniesCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
  }

  /**
   * Measure and log execution time for a supplier block
   */
  private <T> T measure(String label, java.util.function.Supplier<T> supplier) {
    long start = System.currentTimeMillis();
    try {
      return supplier.get();
    } finally {
      long took = System.currentTimeMillis() - start;
      log.info("{} took {} ms", label, took);
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
              Query.of(q -> q.match(
                  m -> m.field(ElasticsearchConstants.FIELD_SEARCHABLE_TEXT).query(keyword))));
          keywordQueryBuilder.should(
              Query.of(q -> q.match(
                  m -> m.field(ElasticsearchConstants.FIELD_COMPANY_NAME).query(keyword))));
          keywordQueryBuilder.should(
              Query.of(q -> q.match(
                  m -> m.field(ElasticsearchConstants.FIELD_DESIGNATION_NAME).query(keyword))));
        }
        boolQueryBuilder.must(keywordQueryBuilder.build()._toQuery());
      }

      // City filter
      if (cityId != null && cityId != 0) {
        boolQueryBuilder.filter(Query.of(
            q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_CITY_ID).value(cityId))));
      }

      // Country filter
      if (countryId != null && countryId != 0) {
        boolQueryBuilder.filter(Query.of(
            q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_COUNTRY_ID).value(countryId))));
      }

      // Company filter
      if (companyId != null && companyId != 0) {
        boolQueryBuilder.filter(Query.of(
            q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_COMPANY_ID).value(companyId))));
      }

      // Designation filter
      if (designationId != null && designationId != 0) {
        boolQueryBuilder.filter(
            Query.of(q -> q.term(
                t -> t.field(ElasticsearchConstants.FIELD_DESIGNATION_ID).value(designationId))));
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
      boolQueryBuilder.filter(
          Query.of(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true))));

      final Query baseQuery = boolQueryBuilder.build()._toQuery();
      // Apply boosting if user is logged in and has a designation
      final Query finalQuery = finalUserDesignationId != null ?
          FunctionScoreQuery.of(fs -> fs
              .query(baseQuery)
              .functions(f -> f
                  .filter(Query.of(
                      q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_DESIGNATION_ID)
                          .value(finalUserDesignationId))))
                  .weight(2.0)
              )
          )._toQuery() : baseQuery;

      // Build search request
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .query(finalQuery)
          .sort(sort -> sort.field(f -> f.field("_score").order(SortOrder.Desc)))
          .sort(sort -> sort.field(
              f -> f.field(ElasticsearchConstants.FIELD_CREATED_AT).order(SortOrder.Desc)))
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
    if (jobData.get(ElasticsearchConstants.FIELD_ID) != null) {
      response.setId(Long.valueOf(jobData.get(ElasticsearchConstants.FIELD_ID).toString()));
    }
    if (jobData.get("title") != null) {
      response.setTitle(jobData.get("title").toString());
    }
    if (jobData.get("description") != null) {
      response.setDescription(jobData.get("description").toString());
    }
    if (jobData.get(ElasticsearchConstants.FIELD_CITY_ID) != null) {
      response.setCityId(
          Long.valueOf(jobData.get(ElasticsearchConstants.FIELD_CITY_ID).toString()));
    }
    if (jobData.get(ElasticsearchConstants.FIELD_COUNTRY_ID) != null) {
      response.setCountryId(
          Long.valueOf(jobData.get(ElasticsearchConstants.FIELD_COUNTRY_ID).toString()));
    }
    if (jobData.get(ElasticsearchConstants.FIELD_DESIGNATION_ID) != null) {
      response.setDesignationId(
          Long.valueOf(jobData.get(ElasticsearchConstants.FIELD_DESIGNATION_ID).toString()));
    }
    if (jobData.get(ElasticsearchConstants.FIELD_DESIGNATION_NAME) != null) {
      response.setDesignation(
          jobData.get(ElasticsearchConstants.FIELD_DESIGNATION_NAME).toString());
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
    if (jobData.get(ElasticsearchConstants.FIELD_COMPANY_ID) != null) {
      response.setCompanyId(
          Long.valueOf(jobData.get(ElasticsearchConstants.FIELD_COMPANY_ID).toString()));
    }
    if (jobData.get(ElasticsearchConstants.FIELD_COMPANY_NAME) != null) {
      response.setCompany(jobData.get(ElasticsearchConstants.FIELD_COMPANY_NAME).toString());
    }
    if (jobData.get("postedById") != null) {
      response.setPostedBy(Long.valueOf(jobData.get("postedById").toString()));
    }
    if (jobData.get(ElasticsearchConstants.FIELD_ACTIVE) != null) {
      response.setActive(
          Boolean.valueOf(jobData.get(ElasticsearchConstants.FIELD_ACTIVE).toString()));
    }

    // Robustly parse createdAt and updatedAt with a single try-catch block
    try {
      if (jobData.get(ElasticsearchConstants.FIELD_CREATED_AT) != null) {
        Object createdAtObj = jobData.get(ElasticsearchConstants.FIELD_CREATED_AT);
        if (createdAtObj instanceof Number || createdAtObj.toString().matches("\\d+")) {
          long millis = Long.parseLong(createdAtObj.toString());
          response.setCreatedAt(java.time.Instant.ofEpochMilli(millis)
              .atZone(java.time.ZoneId.systemDefault())
              .toLocalDateTime());
        } else {
          response.setCreatedAt(LocalDateTime.parse(createdAtObj.toString()));
        }
      }
      if (jobData.get(ElasticsearchConstants.FIELD_UPDATED_AT) != null) {
        Object updatedAtObj = jobData.get(ElasticsearchConstants.FIELD_UPDATED_AT);
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
          "Failed to parse createdAt/updatedAt in ElasticsearchJobQueryService.convertMapToJobResponse: {}",
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
    response.setUserRole("VIEWER");
    response.setActualPostedBy(response.getPostedBy());

    // Initialize referrer data (will be populated by bulk method for multiple jobs)
    response.setReferrerUserIds(new ArrayList<>());
    response.setReferrerCount(0);

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

  /**
   * Get industry-wise companies using Elasticsearch Logic: 1. Pick companies which have jobs from
   * job index by grouping on industry and sorting by job count 2. Pick all pending industries from
   * companies index 3. Combine and sort: industries with jobs by job count, industries without jobs
   * by rank
   */
  public List<IndustryWiseCompaniesDTO> getIndustryWiseCompanies() {
    try {
      long totalStart = System.currentTimeMillis();
      // Clear expired cache entries
      clearExpiredCache();

      // Check cache first
      List<IndustryWiseCompaniesDTO> cachedResult = getCachedIndustryWiseCompanies();
      if (cachedResult != null) {
        log.info("Returning industry-wise companies from cache ({} items)", cachedResult.size());
        return cachedResult;
      }

      log.info("Cache miss - fetching industry-wise companies from Elasticsearch (parallel)");

      // Run both Elasticsearch calls in parallel to reduce latency
      java.util.concurrent.CompletableFuture<Map<String, Object>> industriesWithJobsFuture =
          java.util.concurrent.CompletableFuture.supplyAsync(
              () -> measure("ES:getIndustriesWithJobsFromJobsIndex",
                  this::getIndustriesWithJobsFromJobsIndex));
      java.util.concurrent.CompletableFuture<Map<String, Object>> allIndustriesFromCompaniesFuture =
          java.util.concurrent.CompletableFuture.supplyAsync(
              () -> measure("ES:getAllIndustriesFromCompaniesIndex",
                  this::getAllIndustriesFromCompaniesIndex));

      // Wait for both to complete
      Map<String, Object> industriesWithJobs = industriesWithJobsFuture.join();
      Map<String, Object> allIndustriesFromCompanies = allIndustriesFromCompaniesFuture.join();

      // Step 3: Combine and sort the results
      long combineStart = System.currentTimeMillis();
      List<IndustryWiseCompaniesDTO> result = combineAndSortIndustries(industriesWithJobs,
          allIndustriesFromCompanies);
      log.info("Combine industries took {} ms", (System.currentTimeMillis() - combineStart));

      // Cache the result
      cacheIndustryWiseCompanies(result);

      log.info("Successfully fetched and cached {} industries with companies in {} ms",
          result.size(), (System.currentTimeMillis() - totalStart));
      return result;

    } catch (Exception e) {
      log.error("Error fetching industry-wise companies from Elasticsearch: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to fetch industry-wise companies", e);
    }
  }

  /**
   * Get industries with jobs from jobs index, grouped by industry and sorted by job count
   */
  private Map<String, Object> getIndustriesWithJobsFromJobsIndex() {
    try {
      long start = System.currentTimeMillis();
      // Aggregation to group by primaryIndustryName and count jobs
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .size(0) // We only need aggregations, not documents
          .query(q -> q
              .bool(b -> b
                  .must(m -> m.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true)))
                  .must(m -> m.exists(
                      e -> e.field(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME)))
              )
          )
          .aggregations(ElasticsearchConstants.AGG_INDUSTRIES_WITH_JOBS, a -> a
              .terms(t -> t
                  .field(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME_KEYWORD)
                  .size(ElasticsearchConstants.MAX_AGGREGATION_SIZE) // Get all industries
              )
              .aggregations(ElasticsearchConstants.AGG_TOP_COMPANIES, a2 -> a2
                  .terms(t -> t
                          .field(ElasticsearchConstants.FIELD_COMPANY_ID)
                          .size(ElasticsearchConstants.TOP_COMPANIES_LIMIT)
                      // Top 5 companies per industry
                  )
                  .aggregations(ElasticsearchConstants.AGG_COMPANY_DETAILS, a3 -> a3
                      .topHits(th -> th
                          .size(1)
                          .source(sf -> sf.filter(
                              f -> f.includes(ElasticsearchConstants.FIELD_COMPANY_NAME,
                                  ElasticsearchConstants.FIELD_COMPANY_ID)))
                      )
                  )
              )
          )
      );

      SearchResponse<Map> response = measure("ES:search industries_with_jobs", () -> {
        try {
          return elasticsearchClient.search(searchRequest, Map.class);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      Map<String, Object> result = new HashMap<>();
      if (response.aggregations() != null
          && response.aggregations().get(ElasticsearchConstants.AGG_INDUSTRIES_WITH_JOBS) != null) {
        Aggregate industriesAggregate = response.aggregations()
            .get(ElasticsearchConstants.AGG_INDUSTRIES_WITH_JOBS);

        // Handle both StringTerms and LongTerms aggregations
        if (industriesAggregate.isSterms()) {
          co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate industriesAgg = industriesAggregate.sterms();
          for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : industriesAgg.buckets()
              .array()) {
            String industryName = bucket.key().stringValue();
            long jobCount = bucket.docCount();

            Map<String, Object> industryData = new HashMap<>();
            industryData.put("jobCount", jobCount);
            industryData.put("hasJobs", true);

            // Get top companies for this industry
            List<Map<String, Object>> topCompanies = new ArrayList<>();
            if (bucket.aggregations().get(ElasticsearchConstants.AGG_TOP_COMPANIES) != null) {
              Aggregate companiesAggregate = bucket.aggregations()
                  .get(ElasticsearchConstants.AGG_TOP_COMPANIES);
              if (companiesAggregate.isSterms()) {
                co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate companiesAgg = companiesAggregate.sterms();
                for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket companyBucket : companiesAgg.buckets()
                    .array()) {
                  Long companyId = Long.valueOf(companyBucket.key().stringValue());
                  long companyJobCount = companyBucket.docCount();

                  Map<String, Object> companyData = new HashMap<>();
                  companyData.put("companyId", companyId);
                  companyData.put("activeJobCount", (int) companyJobCount);

                  // Get company details
                  String companyName = null;
                  if (companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                      != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate topHits =
                        companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                            .topHits();
                    if (!topHits.hits().hits().isEmpty()) {
                      co.elastic.clients.elasticsearch.core.search.Hit<JsonData> hit = topHits.hits()
                          .hits().get(0);
                      if (hit.source() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> source = new ObjectMapper().readValue(
                            hit.source().toJson().toString(), Map.class);
                        if (source != null) {
                          companyName = (String) source.get(
                              ElasticsearchConstants.FIELD_COMPANY_NAME);
                        }
                      }
                    }
                  }

                  // Fallback: If company name is null, try to get it from companies index
                  if (companyName == null) {
                    try {
                      Map<String, Object> companyDetails = getCompanyDetailsFromCompaniesIndex(
                          companyId);
                      if (companyDetails != null) {
                        companyName = (String) companyDetails.get(
                            ElasticsearchConstants.FIELD_COMPANY_NAME);
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Error getting company name from companies index for company ID {}: {}",
                          companyId, e.getMessage());
                    }
                  }

                  companyData.put("companyName", companyName);

                  topCompanies.add(companyData);
                }
              } else if (companiesAggregate.isLterms()) {
                co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate companiesAgg = companiesAggregate.lterms();
                for (co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket companyBucket : companiesAgg.buckets()
                    .array()) {
                  Long companyId = companyBucket.key();
                  long companyJobCount = companyBucket.docCount();

                  Map<String, Object> companyData = new HashMap<>();
                  companyData.put("companyId", companyId);
                  companyData.put("activeJobCount", (int) companyJobCount);

                  // Get company details
                  String companyName = null;
                  if (companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                      != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate topHits =
                        companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                            .topHits();
                    if (!topHits.hits().hits().isEmpty()) {
                      co.elastic.clients.elasticsearch.core.search.Hit<JsonData> hit = topHits.hits()
                          .hits().get(0);
                      if (hit.source() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> source = new ObjectMapper().readValue(
                            hit.source().toJson().toString(), Map.class);
                        if (source != null) {
                          companyName = (String) source.get(
                              ElasticsearchConstants.FIELD_COMPANY_NAME);
                        }
                      }
                    }
                  }

                  // Fallback: If company name is null, try to get it from companies index
                  if (companyName == null) {
                    try {
                      Map<String, Object> companyDetails = getCompanyDetailsFromCompaniesIndex(
                          companyId);
                      if (companyDetails != null) {
                        companyName = (String) companyDetails.get(
                            ElasticsearchConstants.FIELD_COMPANY_NAME);
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Error getting company name from companies index for company ID {}: {}",
                          companyId, e.getMessage());
                    }
                  }

                  companyData.put("companyName", companyName);

                  topCompanies.add(companyData);
                }
              }
            }
            industryData.put("topCompanies", topCompanies);

            result.put(industryName, industryData);
          }
        } else if (industriesAggregate.isLterms()) {
          co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate industriesAgg = industriesAggregate.lterms();
          for (co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket bucket : industriesAgg.buckets()
              .array()) {
            String industryName = String.valueOf(bucket.key()); // Convert Long to String
            long jobCount = bucket.docCount();

            Map<String, Object> industryData = new HashMap<>();
            industryData.put("jobCount", jobCount);
            industryData.put("hasJobs", true);

            // Get top companies for this industry
            List<Map<String, Object>> topCompanies = new ArrayList<>();
            if (bucket.aggregations().get(ElasticsearchConstants.AGG_TOP_COMPANIES) != null) {
              Aggregate companiesAggregate = bucket.aggregations()
                  .get(ElasticsearchConstants.AGG_TOP_COMPANIES);
              if (companiesAggregate.isSterms()) {
                co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate companiesAgg = companiesAggregate.sterms();
                for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket companyBucket : companiesAgg.buckets()
                    .array()) {
                  Long companyId = Long.valueOf(companyBucket.key().stringValue());
                  long companyJobCount = companyBucket.docCount();

                  Map<String, Object> companyData = new HashMap<>();
                  companyData.put("companyId", companyId);
                  companyData.put("activeJobCount", (int) companyJobCount);

                  // Get company details
                  String companyName = null;
                  if (companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                      != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate topHits =
                        companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                            .topHits();
                    if (!topHits.hits().hits().isEmpty()) {
                      co.elastic.clients.elasticsearch.core.search.Hit<JsonData> hit = topHits.hits()
                          .hits().get(0);
                      if (hit.source() != null) {
                        companyName = extractCompanyNameFromSource(
                            hit.source().toJson().toString());
                      }
                    }
                  }

                  // Fallback: If company name is null, try to get it from companies index
                  if (companyName == null) {
                    try {
                      Map<String, Object> companyDetails = getCompanyDetailsFromCompaniesIndex(
                          companyId);
                      if (companyDetails != null) {
                        companyName = (String) companyDetails.get(
                            ElasticsearchConstants.FIELD_COMPANY_NAME);
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Error getting company name from companies index for company ID {}: {}",
                          companyId, e.getMessage());
                    }
                  }

                  companyData.put("companyName", companyName);

                  topCompanies.add(companyData);
                }
              } else if (companiesAggregate.isLterms()) {
                co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate companiesAgg = companiesAggregate.lterms();
                for (co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket companyBucket : companiesAgg.buckets()
                    .array()) {
                  Long companyId = companyBucket.key();
                  long companyJobCount = companyBucket.docCount();

                  Map<String, Object> companyData = new HashMap<>();
                  companyData.put("companyId", companyId);
                  companyData.put("activeJobCount", (int) companyJobCount);

                  // Get company details
                  String companyName = null;
                  if (companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                      != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregate topHits =
                        companyBucket.aggregations().get(ElasticsearchConstants.AGG_COMPANY_DETAILS)
                            .topHits();
                    if (!topHits.hits().hits().isEmpty()) {
                      co.elastic.clients.elasticsearch.core.search.Hit<JsonData> hit = topHits.hits()
                          .hits().get(0);
                      if (hit.source() != null) {
                        companyName = extractCompanyNameFromSource(
                            hit.source().toJson().toString());
                      }
                    }
                  }

                  // Fallback: If company name is null, try to get it from companies index
                  if (companyName == null) {
                    try {
                      Map<String, Object> companyDetails = getCompanyDetailsFromCompaniesIndex(
                          companyId);
                      if (companyDetails != null) {
                        companyName = (String) companyDetails.get(
                            ElasticsearchConstants.FIELD_COMPANY_NAME);
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Error getting company name from companies index for company ID {}: {}",
                          companyId, e.getMessage());
                    }
                  }
                  companyData.put("companyName", companyName);
                  topCompanies.add(companyData);
                }
              }
            }
            industryData.put("topCompanies", topCompanies);

            result.put(industryName, industryData);
          }
        }
      }

      log.info("Parsed industries_with_jobs in {} ms", (System.currentTimeMillis() - start));
      return result;

    } catch (Exception e) {
      log.error("Error getting industries with jobs from jobs index: {}", e.getMessage(), e);
      return new HashMap<>();
    }
  }

  /**
   * Get all industries from companies index
   */
  private Map<String, Object> getAllIndustriesFromCompaniesIndex() {
    try {
      long start = System.currentTimeMillis();
      // Aggregation to get all industries from companies index
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.COMPANIES_INDEX)
          .size(0)
          .query(q -> q
              .bool(b -> b
                  .must(m -> m.exists(
                      e -> e.field(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME)))
              )
          )
          .aggregations(ElasticsearchConstants.AGG_ALL_INDUSTRIES, a -> a
              .terms(t -> t
                  .field(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME_KEYWORD)
                  .size(ElasticsearchConstants.MAX_AGGREGATION_SIZE)
              )
          )
      );

      SearchResponse<Map> response = measure("ES:search all_industries", () -> {
        try {
          return elasticsearchClient.search(searchRequest, Map.class);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      Map<String, Object> result = new HashMap<>();
      if (response.aggregations() != null
          && response.aggregations().get(ElasticsearchConstants.AGG_ALL_INDUSTRIES) != null) {
        co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate industriesAgg =
            response.aggregations().get(ElasticsearchConstants.AGG_ALL_INDUSTRIES).sterms();
        for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : industriesAgg.buckets()
            .array()) {
          String industryName = bucket.key().stringValue();
          long companyCount = bucket.docCount();

          if (!result.containsKey(industryName)) {
            Map<String, Object> industryData = new HashMap<>();
            industryData.put("companyCount", companyCount);
            industryData.put("hasJobs", false);
            result.put(industryName, industryData);
          }
        }
      }

      log.info("Parsed all_industries in {} ms", (System.currentTimeMillis() - start));
      return result;

    } catch (Exception e) {
      log.error("Error getting all industries from companies index: {}", e.getMessage(), e);
      return new HashMap<>();
    }
  }

  /**
   * Combine and sort industries based on the specified logic
   */
  private List<IndustryWiseCompaniesDTO> combineAndSortIndustries(
      Map<String, Object> industriesWithJobs,
      Map<String, Object> allIndustriesFromCompanies) {

    List<IndustryWiseCompaniesDTO> result = new ArrayList<>();

    // Combine all industries
    Set<String> allIndustryNames = new HashSet<>();
    allIndustryNames.addAll(industriesWithJobs.keySet());
    allIndustryNames.addAll(allIndustriesFromCompanies.keySet());

    for (String industryName : allIndustryNames) {
      IndustryWiseCompaniesDTO industryDTO = new IndustryWiseCompaniesDTO();
      industryDTO.setIndustryName(industryName);

      // Get industry details from dropdown service
      try {
        Long industryId = dropdownService.getIndustryIdByName(industryName);
        if (industryId != null) {
          industryDTO.setIndustryId(industryId);
          industryDTO.setIndustryDescription(
              dropdownService.getIndustryDescriptionById(industryId));
          industryDTO.setRankOrder(dropdownService.getIndustryRankById(industryId));
        }
      } catch (Exception e) {
        log.warn("Error getting industry details for {}: {}", industryName, e.getMessage());
      }

      // Check if industry has jobs
      @SuppressWarnings("unchecked")
      Map<String, Object> jobsData = (Map<String, Object>) industriesWithJobs.get(industryName);
      @SuppressWarnings("unchecked")
      Map<String, Object> companiesData = (Map<String, Object>) allIndustriesFromCompanies.get(
          industryName);

      if (jobsData != null) {
        // Industry has jobs - use job count for sorting
        Long jobCount = (Long) jobsData.get("jobCount");
        industryDTO.setCompanyCount(jobCount.intValue());

        // Get top companies from jobs data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topCompaniesData = (List<Map<String, Object>>) jobsData.get(
            "topCompanies");
        List<IndustryWiseCompaniesDTO.TopCompanyDTO> topCompanies = new ArrayList<>();

        for (Map<String, Object> companyData : topCompaniesData) {
          IndustryWiseCompaniesDTO.TopCompanyDTO topCompany = new IndustryWiseCompaniesDTO.TopCompanyDTO();
          topCompany.setCompanyId((Long) companyData.get("companyId"));
          topCompany.setCompanyName((String) companyData.get("companyName"));
          topCompany.setActiveJobCount((Integer) companyData.get("activeJobCount"));

          // Get additional company details from companies index
          try {
            Map<String, Object> companyDetails = getCompanyDetailsFromCompaniesIndex(
                topCompany.getCompanyId());
            if (companyDetails != null) {
              topCompany.setLogoUrl(
                  (String) companyDetails.get(ElasticsearchConstants.FIELD_LOGO_URL));
              topCompany.setWebsite(
                  (String) companyDetails.get(ElasticsearchConstants.FIELD_WEBSITE));
              topCompany.setHeadquarters(
                  (String) companyDetails.get(ElasticsearchConstants.FIELD_HEADQUARTERS));
            }
          } catch (Exception e) {
            log.warn("Error getting company details for company ID {}: {}",
                topCompany.getCompanyId(), e.getMessage());
          }

          topCompanies.add(topCompany);
        }

        industryDTO.setTopCompanies(topCompanies);

      } else if (companiesData != null) {
        // Industry has no jobs - use company count and rank for sorting
        Long companyCount = (Long) companiesData.get("companyCount");
        industryDTO.setCompanyCount(companyCount.intValue());
        industryDTO.setTopCompanies(
            new ArrayList<>()); // No top companies for industries without jobs
      }

      result.add(industryDTO);
    }

    // Sort the results
    result.sort((i1, i2) -> {
      // First, sort by whether they have jobs (industries with jobs come first)
      boolean i1HasJobs = i1.getTopCompanies() != null && !i1.getTopCompanies().isEmpty();
      boolean i2HasJobs = i2.getTopCompanies() != null && !i2.getTopCompanies().isEmpty();

      if (i1HasJobs && !i2HasJobs) {
        return -1;
      }
      if (!i1HasJobs && i2HasJobs) {
        return 1;
      }

      // If both have jobs or both don't have jobs, sort by job count (descending)
      if (i1HasJobs && i2HasJobs) {
        return Integer.compare(i2.getCompanyCount(), i1.getCompanyCount());
      }

      // If neither has jobs, sort by rank (ascending)
      Integer rank1 = i1.getRankOrder() != null ? i1.getRankOrder() : Integer.MAX_VALUE;
      Integer rank2 = i2.getRankOrder() != null ? i2.getRankOrder() : Integer.MAX_VALUE;
      return Integer.compare(rank1, rank2);
    });

    return result;
  }

  /**
   * Extract company name from JsonData source
   */
  private String extractCompanyNameFromSource(String source) {
    if (source == null) {
      return null;
    }

    try {
      // Convert JsonData to Map and extract company name
      @SuppressWarnings("unchecked")
      Map<String, Object> sourceMap = objectMapper.readValue(source, Map.class);
      if (sourceMap != null) {
        Object companyNameObj = sourceMap.get(ElasticsearchConstants.FIELD_COMPANY_NAME);
        if (companyNameObj != null) {
          return companyNameObj.toString();
        }
      }
    } catch (Exception e) {
      log.warn("Error parsing company name from source: {}", e.getMessage());
    }

    return null;
  }

  /**
   * Get company details from companies index
   */
  private Map<String, Object> getCompanyDetailsFromCompaniesIndex(Long companyId) {
    try {
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.COMPANIES_INDEX)
          .size(1)
          .query(q -> q.term(t -> t.field(ElasticsearchConstants.FIELD_ID).value(companyId)))
          .source(sf -> sf.filter(f -> f.includes(ElasticsearchConstants.FIELD_LOGO_URL,
              ElasticsearchConstants.FIELD_WEBSITE, ElasticsearchConstants.FIELD_HEADQUARTERS)))
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);

      if (!response.hits().hits().isEmpty()) {
        return response.hits().hits().get(0).source();
      }

      return null;

    } catch (Exception e) {
      log.warn("Error getting company details for company ID {}: {}", companyId, e.getMessage());
      return null;
    }
  }

  /**
   * Search jobs by company and date range for notification purposes
   * @param companyId The company ID to search for
   * @param daysBack Number of days to look back
   * @return Count of jobs found
   */
  public long searchJobsByCompanyAndDateRange(Long companyId, int daysBack) {
    try {
      if (companyId == null || companyId <= 0) {
        log.warn("Invalid company ID provided: {}", companyId);
        return 0L;
      }
      
      LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
      // Convert LocalDateTime to Timestamp for Elasticsearch compatibility
      java.sql.Timestamp sinceTimestamp = java.sql.Timestamp.valueOf(since);
      
      log.debug("Searching jobs for company {} since {} ({} days back)", companyId, sinceTimestamp, daysBack);
      
      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.JOBS_INDEX)
          .size(0) // We only need the count
          .query(q -> q
              .bool(b -> b
                  .must(m -> m.term(t -> t.field(ElasticsearchConstants.FIELD_COMPANY_ID).value(companyId)))
                  .must(m -> m.term(t -> t.field(ElasticsearchConstants.FIELD_ACTIVE).value(true)))
                  .must(m -> m.range(r -> r
                      .field(ElasticsearchConstants.FIELD_CREATED_AT)
                      .gte(JsonData.of(sinceTimestamp))
                  ))
              )
          )
      );

      SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
      long count = response.hits().total() != null ? response.hits().total().value() : 0L;
      log.debug("Found {} jobs for company {} in the last {} days", count, companyId, daysBack);
      return count;
      
    } catch (Exception e) {
      log.error("Error searching jobs by company and date range for company {} (days back: {}): {}", 
                companyId, daysBack, e.getMessage(), e);
      return 0L;
    }
  }

  /**
   * Get companies by primary industry ID using Elasticsearch with pagination Companies with jobs
   * are prioritized and ordered by rank
   *
   * @param primaryIndustryId The primary industry ID
   * @param pageable The pagination parameters
   * @return Page of companies
   */
  public Page<CompanyResponse> getCompaniesByPrimaryIndustryId(Long primaryIndustryId,
      Pageable pageable) {
    try {
      long totalStart = System.currentTimeMillis();
      log.info("Fetching companies by primary industry ID {} from companies index (single query)",
          primaryIndustryId);

      // Get industry name from ID
      String industryName = dropdownService.getIndustryNameById(primaryIndustryId);
      if (industryName == null) {
        log.warn("Industry name not found for ID: {}", primaryIndustryId);
        return Page.empty(pageable);
      }

      // Single ES query to companies index with industryName filter
      int from = (int) pageable.getOffset();
      int size = pageable.getPageSize();

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(ElasticsearchConstants.COMPANIES_INDEX)
          .query(q -> q.term(
              t -> t.field(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME_KEYWORD)
                  .value(industryName)))
          .sort(sort -> sort.field(f -> f.field("jobCount").order(SortOrder.Desc)))
          .from(from)
          .size(size)
          .source(sf -> sf.filter(f -> f.includes(
              ElasticsearchConstants.FIELD_ID,
              ElasticsearchConstants.FIELD_NAME,
              ElasticsearchConstants.FIELD_WEBSITE,
              ElasticsearchConstants.FIELD_LOGO_URL,
              ElasticsearchConstants.FIELD_LINKEDIN_URL,
              ElasticsearchConstants.FIELD_HEADQUARTERS,
              ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_ID,
              ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME,
              ElasticsearchConstants.FIELD_SECONDARY_INDUSTRIES,
              ElasticsearchConstants.FIELD_COMPANY_SIZE,
              ElasticsearchConstants.FIELD_SPECIALTIES,
              "jobCount")))
      );

      SearchResponse<Map> response = measure("ES:getCompaniesByPrimaryIndustryId search", () -> {
        try {
          return elasticsearchClient.search(searchRequest, Map.class);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      long total = response.hits().total() != null ? response.hits().total().value() : 0L;

      List<CompanyResponse> pageContent = new ArrayList<>();
      for (Hit<Map> hit : response.hits().hits()) {
        Map<String, Object> src = hit.source();
        if (src == null) {
          continue;
        }
        CompanyResponse cr = new CompanyResponse();
        if (src.get(ElasticsearchConstants.FIELD_ID) != null) {
          cr.setId(Long.valueOf(src.get(ElasticsearchConstants.FIELD_ID).toString()));
        }
        cr.setName(src.getOrDefault(ElasticsearchConstants.FIELD_NAME, "").toString());
        if (src.get(ElasticsearchConstants.FIELD_WEBSITE) != null) {
          cr.setWebsite(src.get(ElasticsearchConstants.FIELD_WEBSITE).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_LOGO_URL) != null) {
          cr.setLogoUrl(src.get(ElasticsearchConstants.FIELD_LOGO_URL).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_LINKEDIN_URL) != null) {
          cr.setLinkedinUrl(src.get(ElasticsearchConstants.FIELD_LINKEDIN_URL).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_HEADQUARTERS) != null) {
          cr.setHeadquarters(src.get(ElasticsearchConstants.FIELD_HEADQUARTERS).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_ID) != null) {
          cr.setPrimaryIndustryId(
              Long.valueOf(src.get(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_ID).toString()));
        }
        if (src.get(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME) != null) {
          cr.setPrimaryIndustryName(
              src.get(ElasticsearchConstants.FIELD_PRIMARY_INDUSTRY_NAME).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_SECONDARY_INDUSTRIES) != null) {
          cr.setSecondaryIndustries(
              src.get(ElasticsearchConstants.FIELD_SECONDARY_INDUSTRIES).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_COMPANY_SIZE) != null) {
          cr.setCompanySize(src.get(ElasticsearchConstants.FIELD_COMPANY_SIZE).toString());
        }
        if (src.get(ElasticsearchConstants.FIELD_SPECIALTIES) != null) {
          cr.setSpecialties(src.get(ElasticsearchConstants.FIELD_SPECIALTIES).toString());
        }
        if (src.get("jobCount") != null) {
          cr.setJobCount(Integer.valueOf(src.get("jobCount").toString()));
        } else {
          cr.setJobCount(0);
        }
        pageContent.add(cr);
      }

      log.info("Fetched {} companies from companies index (industryId={}) in {} ms",
          pageContent.size(), primaryIndustryId, (System.currentTimeMillis() - totalStart));
      return new PageImpl<>(pageContent, pageable, total);

    } catch (Exception e) {
      log.error("Error fetching companies by primary industry ID {} from Elasticsearch: {}",
          primaryIndustryId, e.getMessage(), e);
      throw new RuntimeException("Failed to fetch companies by primary industry ID", e);
    }
  }


}
