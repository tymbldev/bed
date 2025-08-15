package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Industry;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String COMPANIES_INDEX = "companies";
    private static final String DESIGNATIONS_INDEX = "designations";
    private static final String CITIES_INDEX = "cities";

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
            
            for (Company company : companies) {
                Map<String, Object> companyDoc = buildCompanyDocument(company);
                bulkRequest.operations(op -> op
                    .index(idx -> idx
                        .index(COMPANIES_INDEX)
                        .id(company.getId().toString())
                        .document(companyDoc)
                    )
                );
            }
            
            BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
            
            int successCount = 0;
            int failureCount = 0;
            
            if (response.errors()) {
                failureCount = response.items().size();
                log.error("Failed to index companies: {}", response.items().stream().map(item -> item.error().reason()).collect(Collectors.joining(", ")));
            } else {
                successCount = companies.size();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalCompanies", companies.size());
            result.put("indexedSuccessfully", successCount);
            result.put("failedToIndex", failureCount);
            result.put("message", "Company indexing completed");
            
            log.info("Company indexing completed - Success: {}, Failures: {}", successCount, failureCount);
            
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
            
            for (Designation designation : designations) {
                Map<String, Object> designationDoc = buildDesignationDocument(designation);
                bulkRequest.operations(op -> op
                    .index(idx -> idx
                        .index(DESIGNATIONS_INDEX)
                        .id(designation.getId().toString())
                        .document(designationDoc)
                    )
                );
            }
            
            BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
            
            int successCount = 0;
            int failureCount = 0;
            
            if (response.errors()) {
                failureCount = response.items().size();
                log.error("Failed to index designations: {}", response.items().stream().map(item -> item.error().reason()).collect(Collectors.joining(", ")));
            } else {
                successCount = designations.size();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalDesignations", designations.size());
            result.put("indexedSuccessfully", successCount);
            result.put("failedToIndex", failureCount);
            result.put("message", "Designation indexing completed");
            
            log.info("Designation indexing completed - Success: {}, Failures: {}", successCount, failureCount);
            
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
            
            for (City city : cities) {
                Map<String, Object> cityDoc = buildCityDocument(city);
                bulkRequest.operations(op -> op
                    .index(idx -> idx
                        .index(CITIES_INDEX)
                        .id(city.getId().toString())
                        .document(cityDoc)
                    )
                );
            }
            
            BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
            
            int successCount = 0;
            int failureCount = 0;
            
            if (response.errors()) {
                failureCount = response.items().size();
                log.error("Failed to index cities: {}", response.items().stream().map(item -> item.error().reason()).collect(Collectors.joining(", ")));
            } else {
                successCount = cities.size();
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
     * Delete all documents from a specific index
     */
    public Map<String, Object> deleteAllDocumentsFromIndex(String indexName) {
        log.info("Starting to delete all documents from index: {}", indexName);
        
        try {
            // Check if index exists
            boolean indexExists = elasticsearchClient.indices().exists(e -> e.index(indexName)).value();
            if (!indexExists) {
                log.info("Index {} does not exist, nothing to delete", indexName);
                Map<String, Object> result = new HashMap<>();
                result.put("indexName", indexName);
                result.put("documentsDeleted", 0);
                result.put("message", "Index does not exist, nothing to delete");
                return result;
            }

            // Delete all documents using match_all query
            DeleteByQueryResponse deleteResponse = elasticsearchClient.deleteByQuery(d -> d
                .index(indexName)
                .query(q -> q.matchAll(m -> m))
            );

            Map<String, Object> result = new HashMap<>();
            result.put("indexName", indexName);
            result.put("documentsDeleted", deleteResponse.deleted());
            result.put("message", "All documents deleted from index: " + indexName);
            
            log.info("Successfully deleted {} documents from index: {}", deleteResponse.deleted(), indexName);
            return result;
            
        } catch (Exception e) {
            log.error("Error deleting documents from index: {}", indexName, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to delete documents from index " + indexName + ": " + e.getMessage());
            return error;
        }
    }

    /**
     * Delete all documents from all indices before re-indexing
     */
    public Map<String, Object> deleteAllDocumentsFromAllIndices() {
        log.info("Starting to delete all documents from all indices");
        
        Map<String, Object> companiesDeleteResult = deleteAllDocumentsFromIndex(COMPANIES_INDEX);
        Map<String, Object> designationsDeleteResult = deleteAllDocumentsFromIndex(DESIGNATIONS_INDEX);
        Map<String, Object> citiesDeleteResult = deleteAllDocumentsFromIndex(CITIES_INDEX);
        
        Map<String, Object> result = new HashMap<>();
        result.put("companies", companiesDeleteResult);
        result.put("designations", designationsDeleteResult);
        result.put("cities", citiesDeleteResult);
        result.put("message", "All documents deleted from all indices");
        
        log.info("All documents deletion completed");
        return result;
    }

    /**
     * Index all entities (companies, designations, cities) to Elasticsearch
     * First deletes all existing documents, then re-indexes everything fresh
     */
    @Transactional(readOnly = true)
    public Map<String, Object> indexAllEntities() {
        log.info("Starting to index all entities to Elasticsearch with cleanup");
        
        // First, delete all existing documents from all indices
        log.info("Step 1: Deleting all existing documents from Elasticsearch indices");
        Map<String, Object> deleteResult = deleteAllDocumentsFromAllIndices();
        
        // Wait a moment for deletion to complete
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted during deletion wait");
        }
        
        // Then, re-index all entities
        log.info("Step 2: Re-indexing all entities to Elasticsearch");
        Map<String, Object> companiesResult = indexAllCompanies();
        Map<String, Object> designationsResult = indexAllDesignations();
        Map<String, Object> citiesResult = indexAllCities();
        
        Map<String, Object> result = new HashMap<>();
        result.put("cleanup", deleteResult);
        result.put("companies", companiesResult);
        result.put("designations", designationsResult);
        result.put("cities", citiesResult);
        result.put("message", "All entities re-indexed to Elasticsearch after cleanup");
        
        log.info("All entities re-indexing completed after cleanup");
        
        return result;
    }

    /**
     * Search for autosuggest results
     */
    public List<Map<String, Object>> searchAutosuggest(String keyword, String entityType, int limit) {
        log.info("Searching for autosuggest with keyword: '{}', entityType: '{}', limit: {}", keyword, entityType, limit);
        
        try {
            String indexName = getIndexName(entityType);
            if (indexName == null) {
                log.error("Invalid entity type: {}", entityType);
                return Collections.emptyList();
            }
            
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
            
            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
            
            List<Map<String, Object>> results = new ArrayList<>();
            
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
                }
                
                results.add(result);
            }
            
            log.info("Found {} autosuggest results for keyword: '{}'", results.size(), keyword);
            return results;
            
        } catch (Exception e) {
            log.error("Error searching autosuggest for keyword: '{}', entityType: '{}'", keyword, entityType, e);
            return Collections.emptyList();
        }
    }

    /**
     * Update job count for a specific company in Elasticsearch
     */
    public void updateCompanyJobCount(Long companyId) {
        try {
            Company company = companyRepository.findById(companyId).orElse(null);
            if (company == null) {
                log.warn("Company not found for job count update: {}", companyId);
                return;
            }
            
            long jobCount = jobRepository.countByCompanyIdAndActiveTrue(companyId);
            
            Map<String, Object> updateDoc = new HashMap<>();
            updateDoc.put("jobCount", jobCount);
            
            elasticsearchClient.update(u -> u
                .index(COMPANIES_INDEX)
                .id(companyId.toString())
                .doc(updateDoc)
            , Map.class);
            
            log.info("Updated job count for company {} to {}", companyId, jobCount);
            
        } catch (Exception e) {
            log.error("Error updating job count for company {} in Elasticsearch", companyId, e);
        }
    }

    /**
     * Search companies with filters and pagination
     */
    public Map<String, Object> searchCompanies(String location, String industryName, String secondaryIndustryName, 
                                              int page, int size) {
        try {
            // Build the query
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            // Location filter (headquarters)
            if (location != null && !location.trim().isEmpty()) {
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
                boolQueryBuilder.must(Query.of(q -> q
                    .match(MatchQuery.of(m -> m
                        .field("secondaryIndustries")
                        .query(secondaryIndustryName.trim())
                        .fuzziness("AUTO")
                    ))
                ));
            }
            
            // Build search request with sorting by job count
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(COMPANIES_INDEX)
                .query(boolQueryBuilder.build()._toQuery())
                .sort(sort -> sort.field(f -> f.field("jobCount").order(SortOrder.Desc)))
                .sort(sort -> sort.field(f -> f.field("name").order(SortOrder.Asc)))
                .from(page * size)
                .size(size)
            );
            
            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
            
            List<Map<String, Object>> companies = new ArrayList<>();
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
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", company.getId());
        doc.put("name", company.getName());
        doc.put("description", company.getDescription());
        doc.put("website", company.getWebsite());
        doc.put("logoUrl", company.getLogoUrl());
        doc.put("headquarters", company.getHeadquarters());
        doc.put("companySize", company.getCompanySize());
        doc.put("specialties", company.getSpecialties());
        doc.put("linkedinUrl", company.getLinkedinUrl());
        doc.put("careerPageUrl", company.getCareerPageUrl());
        doc.put("aboutUs", company.getAboutUs());
        doc.put("culture", company.getCulture());
        doc.put("mission", company.getMission());
        doc.put("vision", company.getVision());
        doc.put("primaryIndustryId", company.getPrimaryIndustryId());
        doc.put("secondaryIndustries", company.getSecondaryIndustries());
        doc.put("shortname", company.getShortname());
        
        // Add industry name
        if (company.getPrimaryIndustryId() != null) {
            try {
                Industry industry = industryRepository.findById(company.getPrimaryIndustryId()).orElse(null);
                if (industry != null) {
                    doc.put("primaryIndustryName", industry.getName());
                }
            } catch (Exception e) {
                log.warn("Could not fetch industry name for company {}: {}", company.getId(), e.getMessage());
            }
        }
        
        // Add job count
        try {
            long jobCount = jobRepository.countByCompanyIdAndActiveTrue(company.getId());
            doc.put("jobCount", jobCount);
        } catch (Exception e) {
            log.warn("Could not fetch job count for company {}: {}", company.getId(), e.getMessage());
            doc.put("jobCount", 0L);
        }
        
        // Add searchable text
        StringBuilder searchableText = new StringBuilder();
        if (company.getName() != null) searchableText.append(company.getName()).append(" ");
        if (company.getShortname() != null) searchableText.append(company.getShortname()).append(" ");
        if (company.getDescription() != null) searchableText.append(company.getDescription()).append(" ");
        if (company.getSpecialties() != null) searchableText.append(company.getSpecialties()).append(" ");
        if (company.getHeadquarters() != null) searchableText.append(company.getHeadquarters()).append(" ");
        if (company.getSecondaryIndustries() != null) searchableText.append(company.getSecondaryIndustries()).append(" ");
        
        // Add industry name to searchable text
        if (doc.containsKey("primaryIndustryName")) {
            searchableText.append(doc.get("primaryIndustryName")).append(" ");
        }
        
        doc.put("searchableText", searchableText.toString().trim());
        
        return doc;
    }

    /**
     * Build designation document for Elasticsearch
     */
    private Map<String, Object> buildDesignationDocument(Designation designation) {
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
        
        // Add searchable text
        StringBuilder searchableText = new StringBuilder();
        if (designation.getName() != null) searchableText.append(designation.getName()).append(" ");
        if (designation.getProcessedName() != null) searchableText.append(designation.getProcessedName()).append(" ");
        if (designation.getSimilarDesignationsByName() != null) searchableText.append(designation.getSimilarDesignationsByName()).append(" ");
        
        doc.put("searchableText", searchableText.toString().trim());
        
        return doc;
    }

    /**
     * Build city document for Elasticsearch
     */
    private Map<String, Object> buildCityDocument(City city) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", city.getId());
        doc.put("name", city.getName());
        doc.put("countryId", city.getCountryId());
        doc.put("zipCode", city.getZipCode());
        doc.put("processedName", city.getProcessedName());
        doc.put("processedNameGenerated", city.isProcessedNameGenerated());
        
        // Add searchable text
        StringBuilder searchableText = new StringBuilder();
        if (city.getName() != null) searchableText.append(city.getName()).append(" ");
        if (city.getProcessedName() != null) searchableText.append(city.getProcessedName()).append(" ");
        if (city.getZipCode() != null) searchableText.append(city.getZipCode()).append(" ");
        
        doc.put("searchableText", searchableText.toString().trim());
        
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
            default:
                return null;
        }
    }
} 