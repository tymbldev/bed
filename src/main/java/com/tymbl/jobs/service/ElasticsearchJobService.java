package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.Job.JobType;
import com.tymbl.common.entity.JobApprovalStatus;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.dto.JobResponse;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.FieldValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchJobService {

    private final ElasticsearchClient elasticsearchClient;
    private final DropdownService dropdownService;
    private final ObjectMapper objectMapper;
    
    private static final String INDEX_NAME = "jobs";

    /**
     * Sync a job to Elasticsearch (save or update)
     * Does not fail the main transaction if ES fails
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
            log.error("Failed to sync job {} to Elasticsearch. Error: {}", job.getId(), e.getMessage(), e);
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
                    keywordQueryBuilder.should(Query.of(q -> q.match(m -> m.field("searchableText").query(keyword))));
                    keywordQueryBuilder.should(Query.of(q -> q.match(m -> m.field("companyName").query(keyword))));
                    keywordQueryBuilder.should(Query.of(q -> q.match(m -> m.field("designationName").query(keyword))));
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
                boolQueryBuilder.filter(Query.of(q -> q.term(t -> t.field("designationId").value(designationId))));
            }
            
            // Experience range filter
            if (minExperience != null || maxExperience != null) {
                BoolQuery.Builder experienceQueryBuilder = new BoolQuery.Builder();
                
                if (minExperience != null) {
                    experienceQueryBuilder.must(Query.of(q -> q.range(r -> r.field("maxExperience").gte(JsonData.of(minExperience)))));
                }
                
                if (maxExperience != null) {
                    experienceQueryBuilder.must(Query.of(q -> q.range(r -> r.field("minExperience").lte(JsonData.of(maxExperience)))));
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
                        .filter(Query.of(q -> q.term(t -> t.field("designationId").value(finalUserDesignationId))))
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
        
        if (job.getTitle() != null) searchableText.append(job.getTitle()).append(" ");
        if (designationName != null) searchableText.append(designationName).append(" ");
        if (companyName != null) searchableText.append(companyName).append(" ");
        if (job.getTags() != null) searchableText.append(String.join(" ", job.getTags())).append(" ");
        if (job.getPlatform() != null) searchableText.append(job.getPlatform()).append(" ");
        if (job.getJobType() != null) searchableText.append(job.getJobType().name()).append(" ");
        
        // Add city and country names
        if (job.getCityId() != null) {
            String cityName = dropdownService.getCityNameById(job.getCityId());
            if (cityName != null) searchableText.append(cityName).append(" ");
        }
        
        if (job.getCountryId() != null) {
            String countryName = dropdownService.getCountryNameById(job.getCountryId());
            if (countryName != null) searchableText.append(countryName).append(" ");
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
        if (jobData.get("createdAt") != null) {
            response.setCreatedAt(LocalDateTime.parse(jobData.get("createdAt").toString()));
        }
        if (jobData.get("updatedAt") != null) {
            response.setUpdatedAt(LocalDateTime.parse(jobData.get("updatedAt").toString()));
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
        
        return response;
    }
} 