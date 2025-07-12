package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Job;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.dto.JobSearchRequest;
import com.tymbl.jobs.dto.JobSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchJobService {

    private final RestHighLevelClient elasticsearchClient;
    private final DropdownService dropdownService;
    private final ObjectMapper objectMapper;
    
    private static final String INDEX_NAME = "jobs";
    private static final String TYPE_NAME = "_doc";

    /**
     * Sync a job to Elasticsearch (save or update)
     * Does not fail the main transaction if ES fails
     */
    public void syncJobToElasticsearch(Job job) {
        try {
            Map<String, Object> jobDocument = buildJobDocument(job);
            
            IndexRequest indexRequest = new IndexRequest(INDEX_NAME, TYPE_NAME, job.getId().toString())
                .source(jobDocument, XContentType.JSON);
            
            IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
            
            log.info("Successfully synced job {} to Elasticsearch with result: {}", 
                job.getId(), response.getResult().name());
                
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
            SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            
            // Build the main query
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // Keyword search in searchableText
            if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                for (String keyword : request.getKeywords()) {
                    keywordQuery.should(QueryBuilders.matchQuery("searchableText", keyword));
                }
                boolQuery.must(keywordQuery);
            }
            
            // City filter
            if (request.getCityId() != null) {
                boolQuery.filter(QueryBuilders.termQuery("cityId", request.getCityId()));
            }
            
            // Country filter
            if (request.getCountryId() != null) {
                boolQuery.filter(QueryBuilders.termQuery("countryId", request.getCountryId()));
            }
            
            // Experience range filter
            if (request.getMinExperience() != null || request.getMaxExperience() != null) {
                BoolQueryBuilder experienceQuery = QueryBuilders.boolQuery();
                
                if (request.getMinExperience() != null) {
                    experienceQuery.must(QueryBuilders.rangeQuery("maxExperience").gte(request.getMinExperience()));
                }
                
                if (request.getMaxExperience() != null) {
                    experienceQuery.must(QueryBuilders.rangeQuery("minExperience").lte(request.getMaxExperience()));
                }
                
                boolQuery.filter(experienceQuery);
            }
            
            // Only show active jobs
            boolQuery.filter(QueryBuilders.termQuery("active", true));
            
            // Apply boosting if user is logged in and has a designation
            if (userDesignationId != null) {
                FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                    boolQuery,
                    new FunctionScoreQueryBuilder.FilterFunctionBuilder[] {
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                            QueryBuilders.termQuery("designationId", userDesignationId),
                            ScoreFunctionBuilders.fieldValueFactorFunction("designationId").factor(2.0f)
                        )
                    }
                );
                searchSourceBuilder.query(functionScoreQuery);
            } else {
                searchSourceBuilder.query(boolQuery);
            }
            
            // Sort by score (relevance) then by creation date
            searchSourceBuilder.sort("_score", SortOrder.DESC);
            searchSourceBuilder.sort("createdAt", SortOrder.DESC);
            
            // Pagination
            searchSourceBuilder.from(request.getPage() * request.getSize());
            searchSourceBuilder.size(request.getSize());
            
            searchRequest.source(searchSourceBuilder);
            
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
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
        document.put("designation", job.getDesignation());
        document.put("minSalary", job.getMinSalary());
        document.put("maxSalary", job.getMaxSalary());
        document.put("minExperience", job.getMinExperience());
        document.put("maxExperience", job.getMaxExperience());
        document.put("jobType", job.getJobType() != null ? job.getJobType().name() : null);
        document.put("currencyId", job.getCurrencyId());
        document.put("companyId", job.getCompanyId());
        document.put("company", job.getCompany());
        document.put("postedById", job.getPostedById());
        document.put("active", job.isActive());
        document.put("createdAt", job.getCreatedAt());
        document.put("updatedAt", job.getUpdatedAt());
        document.put("tags", job.getTags());
        document.put("openingCount", job.getOpeningCount());
        document.put("uniqueUrl", job.getUniqueUrl());
        document.put("platform", job.getPlatform());
        
        // Build searchableText (all text fields except description)
        StringBuilder searchableText = new StringBuilder();
        
        if (job.getTitle() != null) searchableText.append(job.getTitle()).append(" ");
        if (job.getDesignation() != null) searchableText.append(job.getDesignation()).append(" ");
        if (job.getCompany() != null) searchableText.append(job.getCompany()).append(" ");
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
    private JobSearchResponse parseSearchResponse(SearchResponse response) {
        List<Map<String, Object>> jobs = new ArrayList<>();
        
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> jobData = hit.getSourceAsMap();
            jobData.put("score", hit.getScore());
            jobs.add(jobData);
        }
        
        return JobSearchResponse.builder()
            .jobs(jobs)
            .total(response.getHits().getTotalHits().value)
            .page(0) // Will be set by controller
            .size(jobs.size())
            .build();
    }
} 