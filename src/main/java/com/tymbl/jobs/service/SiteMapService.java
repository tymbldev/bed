package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.tymbl.common.entity.SiteMapCache;
import com.tymbl.common.repository.SiteMapCacheRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.constants.ElasticsearchConstants;
import com.tymbl.jobs.entity.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SiteMapService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    @Autowired
    private SiteMapCacheRepository siteMapCacheRepository;
    
    @Autowired
    private DropdownService dropdownService;
    
    @Value("${sitemap.page.size:1000}")
    private int sitemapPageSize;
    
    @Value("${sitemap.base.url:https://www.tymblhub.com}")
    private String baseUrl;
    
    @Value("${sitemap.job.description.per.page:1000}")
    private int jobDescriptionPerPage;
    
    @Value("${sitemap.elasticsearch.page.size:1000}")
    private int elasticsearchPageSize;

    /**
     * Generate all sitemaps and cache them in MySQL
     * Runs daily at 12 AM
     */
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void generateAllSitemaps() {
        log.info("Starting daily sitemap generation at {}", LocalDateTime.now());
        
        try {
            // Generate company sitemap
            generateCompanySitemap();
            
            // Generate location-wise jobs sitemap
            generateLocationWiseJobsSitemap();
            
            // Generate designation-wise jobs sitemap
            generateDesignationWiseJobsSitemap();
            
            // Generate skill-wise jobs sitemap
            generateSkillWiseJobsSitemap();
            
            // Generate job description sitemaps
            generateJobDescriptionSitemaps();
            
            log.info("✅ Daily sitemap generation completed successfully");
            
        } catch (Exception e) {
            log.error("Error during daily sitemap generation", e);
        }
    }

    /**
     * Generate company sitemap
     */
    @Transactional
    public void generateCompanySitemap() {
        log.info("🏢 Generating company sitemap");
        
        try {
            // Clear existing cache
            siteMapCacheRepository.deleteBySitemapTypeAndLevel("company-pages", 1);
            
            List<Map<String, Object>> companies = getAllCompaniesFromElasticsearch();
            int totalPages = (int) Math.ceil((double) companies.size() / sitemapPageSize);
            
            for (int page = 0; page < totalPages; page++) {
                int startIndex = page * sitemapPageSize;
                int endIndex = Math.min(startIndex + sitemapPageSize, companies.size());
                
                List<Map<String, Object>> pageCompanies = companies.subList(startIndex, endIndex);
                String xmlContent = generateCompanySitemapXml(pageCompanies);
                
                SiteMapCache cache = new SiteMapCache("company-pages", 1, page, xmlContent);
                cache.setTotalPages(totalPages);
                siteMapCacheRepository.save(cache);
            }
            
            log.info("✅ Company sitemap generated with {} pages", totalPages);
            
        } catch (Exception e) {
            log.error("Error generating company sitemap", e);
        }
    }

    /**
     * Generate location-wise jobs sitemap
     */
    @Transactional
    public void generateLocationWiseJobsSitemap() {
        log.info("📍 Generating location-wise jobs sitemap");
        
        try {
            siteMapCacheRepository.deleteBySitemapTypeAndLevel("location-wise-jobs", 1);
            
            List<String> locations = getAllLocationsFromElasticsearch();
            String xmlContent = generateLocationSitemapXml(locations);
            
            SiteMapCache cache = new SiteMapCache("location-wise-jobs", 1, 0, xmlContent);
            cache.setTotalPages(1);
            siteMapCacheRepository.save(cache);
            
            log.info("✅ Location-wise jobs sitemap generated");
            
        } catch (Exception e) {
            log.error("Error generating location-wise jobs sitemap", e);
        }
    }

    /**
     * Generate designation-wise jobs sitemap
     */
    @Transactional
    public void generateDesignationWiseJobsSitemap() {
        log.info("💼 Generating designation-wise jobs sitemap");
        
        try {
            siteMapCacheRepository.deleteBySitemapTypeAndLevel("designation-wise-jobs", 1);
            
            List<String> designations = getAllDesignationsFromElasticsearch();
            String xmlContent = generateDesignationSitemapXml(designations);
            
            SiteMapCache cache = new SiteMapCache("designation-wise-jobs", 1, 0, xmlContent);
            cache.setTotalPages(1);
            siteMapCacheRepository.save(cache);
            
            log.info("✅ Designation-wise jobs sitemap generated");
            
        } catch (Exception e) {
            log.error("Error generating designation-wise jobs sitemap", e);
        }
    }

    /**
     * Generate skill-wise jobs sitemap
     */
    @Transactional
    public void generateSkillWiseJobsSitemap() {
        log.info("🛠️ Generating skill-wise jobs sitemap");
        
        try {
            siteMapCacheRepository.deleteBySitemapTypeAndLevel("skill-wise-jobs", 1);
            
            List<String> skills = getAllSkillsFromElasticsearch();
            String xmlContent = generateSkillSitemapXml(skills);
            
            SiteMapCache cache = new SiteMapCache("skill-wise-jobs", 1, 0, xmlContent);
            cache.setTotalPages(1);
            siteMapCacheRepository.save(cache);
            
            log.info("Skill-wise jobs sitemap generated");
            
        } catch (Exception e) {
            log.error("Error generating skill-wise jobs sitemap", e);
        }
    }

    /**
     * Generate job description sitemaps
     */
    @Transactional
    public void generateJobDescriptionSitemaps() {
        log.info("Generating job description sitemaps");
        
        try {
            siteMapCacheRepository.deleteBySitemapTypeAndLevel("job-description", 1);
            
            List<Map<String, Object>> jobs = getAllJobsFromElasticsearch();
            int totalPages = (int) Math.ceil((double) jobs.size() / jobDescriptionPerPage);
            
            for (int page = 0; page < totalPages; page++) {
                int startIndex = page * jobDescriptionPerPage;
                int endIndex = Math.min(startIndex + jobDescriptionPerPage, jobs.size());
                
                List<Map<String, Object>> pageJobs = jobs.subList(startIndex, endIndex);
                String xmlContent = generateJobDescriptionSitemapXml(pageJobs);
                
                SiteMapCache cache = new SiteMapCache("job-description", 1, page, xmlContent);
                cache.setTotalPages(totalPages);
                siteMapCacheRepository.save(cache);
            }
            
            log.info("Job description sitemaps generated with {} pages", totalPages);
            
        } catch (Exception e) {
            log.error("Error generating job description sitemaps", e);
        }
    }

    /**
     * Get sitemap from cache
     */
    public String getSitemapFromCache(String sitemapType, Integer level, Integer pageNumber) {
        try {
            Optional<SiteMapCache> cache = siteMapCacheRepository
                .findBySitemapTypeAndLevelAndPageNumberAndIsActiveTrue(sitemapType, level, pageNumber);
            
            if (cache.isPresent()) {
                return cache.get().getXmlContent();
            }
            
            return generateSitemapNotFoundXml();
            
        } catch (Exception e) {
            log.error("Error getting sitemap from cache for type: {}, level: {}, page: {}", 
                sitemapType, level, pageNumber, e);
            return generateErrorXml();
        }
    }

    /**
     * Get all companies from DropdownService
     */
    private List<Map<String, Object>> getAllCompaniesFromElasticsearch() {
        List<Map<String, Object>> companies = new ArrayList<>();
        
        try {
            List<Company> companyEntities = dropdownService.getAllCompanies();
            
            for (Company company : companyEntities) {
                Map<String, Object> companyMap = new HashMap<>();
                companyMap.put("id", company.getId());
                companyMap.put("name", company.getName());
                companies.add(companyMap);
            }
            
            log.info("Total companies fetched from DropdownService: {}", companies.size());
            
        } catch (Exception e) {
            log.error("Error fetching companies from DropdownService", e);
        }
        
        return companies;
    }

    /**
     * Get all locations from Elasticsearch
     */
    private List<String> getAllLocationsFromElasticsearch() {
        List<String> locations = new ArrayList<>();
        
        try {
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));
            
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(ElasticsearchConstants.JOBS_INDEX)
                .query(boolQueryBuilder.build()._toQuery())
                .size(0)
                .aggregations("locations", a -> a
                    .terms(t -> t
                        .field("cityName.keyword")
                        .size(1000)
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
                            String location = bucket.key().stringValue();
                            if (location != null && !location.trim().isEmpty()) {
                                locations.add(location);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing location aggregation response: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error fetching locations from Elasticsearch", e);
        }
        
        return locations;
    }

    /**
     * Get all designations from Elasticsearch
     */
    private List<String> getAllDesignationsFromElasticsearch() {
        List<String> designations = new ArrayList<>();
        
        try {
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));
            
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(ElasticsearchConstants.JOBS_INDEX)
                .query(boolQueryBuilder.build()._toQuery())
                .size(0)
                .aggregations("designations", a -> a
                    .terms(t -> t
                        .field("designationName.keyword")
                        .size(1000)
                    )
                )
            );
            
            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
            
            try {
                Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
                if (aggregations != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.Aggregate designationsAgg = aggregations.get("designations");
                    if (designationsAgg != null && designationsAgg.isSterms()) {
                        co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = designationsAgg.sterms();
                        for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
                            String designation = bucket.key().stringValue();
                            if (designation != null && !designation.trim().isEmpty()) {
                                designations.add(designation);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing designation aggregation response: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error fetching designations from Elasticsearch", e);
        }
        
        return designations;
    }

    /**
     * Get all skills from Elasticsearch
     */
    private List<String> getAllSkillsFromElasticsearch() {
        List<String> skills = new ArrayList<>();
        
        try {
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            boolQueryBuilder.must(Query.of(q -> q.term(t -> t.field("active").value(true))));
            
            SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(ElasticsearchConstants.JOBS_INDEX)
                .query(boolQueryBuilder.build()._toQuery())
                .size(0)
                .aggregations("skills", a -> a
                    .terms(t -> t
                        .field("skillNames.keyword")
                        .size(1000)
                    )
                )
            );
            
            SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
            
            try {
                Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> aggregations = response.aggregations();
                if (aggregations != null) {
                    co.elastic.clients.elasticsearch._types.aggregations.Aggregate skillsAgg = aggregations.get("skills");
                    if (skillsAgg != null && skillsAgg.isSterms()) {
                        co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate stringTerms = skillsAgg.sterms();
                        for (co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket bucket : stringTerms.buckets().array()) {
                            String skill = bucket.key().stringValue();
                            if (skill != null && !skill.trim().isEmpty()) {
                                skills.add(skill);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing skills aggregation response: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error fetching skills from Elasticsearch", e);
        }
        
        return skills;
    }

    /**
     * Get all jobs from Elasticsearch using pagination
     */
    private List<Map<String, Object>> getAllJobsFromElasticsearch() {
        List<Map<String, Object>> jobs = new ArrayList<>();
        
        try {
            String searchAfter = null;
            boolean hasMoreResults = true;
            int pageSize = elasticsearchPageSize; // Use configurable page size
            
            while (hasMoreResults) {
                SearchRequest searchRequest;
                
                if (searchAfter != null) {
                    // Use search_after for pagination
                    final String currentSearchAfter = searchAfter;
                    searchRequest = SearchRequest.of(s -> s
                        .index(ElasticsearchConstants.JOBS_INDEX)
                        .size(pageSize)
                        .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Asc)))
                        .searchAfter(currentSearchAfter)
                    );
                } else {
                    // First page
                    searchRequest = SearchRequest.of(s -> s
                        .index(ElasticsearchConstants.JOBS_INDEX)
                        .size(pageSize)
                        .sort(sort -> sort.field(f -> f.field("id").order(SortOrder.Asc)))
                    );
                }
                SearchResponse<Map> response = elasticsearchClient.search(searchRequest, Map.class);
                
                List<Hit<Map>> hits = response.hits().hits();
                
                if (hits.isEmpty()) {
                    hasMoreResults = false;
                    break;
                }
                
                // Process hits
                for (Hit<Map> hit : hits) {
                    Map<String, Object> job = hit.source();
                    if (job != null) {
                        jobs.add(job);
                    }
                }
                
                // Check if we have more results
                if (hits.size() < pageSize) {
                    hasMoreResults = false;
                } else {
                    // Get search_after from last hit for next iteration
                    List<co.elastic.clients.elasticsearch._types.FieldValue> sortValues = hits.get(hits.size() - 1).sort();
                    if (sortValues != null && !sortValues.isEmpty()) {
                        searchAfter = sortValues.get(0).stringValue();
                    } else {
                        hasMoreResults = false;
                    }
                }
                
                log.debug("Fetched {} jobs from Elasticsearch, total so far: {}", hits.size(), jobs.size());
            }
            
            log.info("Total jobs fetched from Elasticsearch: {}", jobs.size());
            
        } catch (Exception e) {
            log.error("Error fetching jobs from Elasticsearch", e);
        }
        
        return jobs;
    }

    /**
     * Generate company sitemap XML
     */
    private String generateCompanySitemapXml(List<Map<String, Object>> companies) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (Map<String, Object> company : companies) {
            String companyName = (String) company.get("name");
            Object companyId = company.get("id");
            
            if (companyName != null && companyId != null) {
                String url = generateCompanyUrl(companyName, companyId.toString());
                xml.append("  <url>\n");
                xml.append("    <loc>").append(url).append("</loc>\n");
                xml.append("    <lastmod>").append(LocalDateTime.now()).append("</lastmod>\n");
                xml.append("  </url>\n");
            }
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Generate location sitemap XML
     */
    private String generateLocationSitemapXml(List<String> locations) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (String location : locations) {
            String url = baseUrl + "/jobs-in-" + location.toLowerCase().replace(" ", "-").replace("&", "-and-");
            xml.append("  <url>\n");
            xml.append("    <loc>").append(url).append("</loc>\n");
            xml.append("    <lastmod>").append(LocalDateTime.now()).append("</lastmod>\n");
            xml.append("  </url>\n");
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Generate designation sitemap XML
     */
    private String generateDesignationSitemapXml(List<String> designations) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (String designation : designations) {
            String url = baseUrl + "/" + designation.toLowerCase().replace(" ", "-").replace("&", "-and-") + "-jobs";
            xml.append("  <url>\n");
            xml.append("    <loc>").append(url).append("</loc>\n");
            xml.append("    <lastmod>").append(LocalDateTime.now()).append("</lastmod>\n");
            xml.append("  </url>\n");
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Generate skill sitemap XML
     */
    private String generateSkillSitemapXml(List<String> skills) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (String skill : skills) {
            String url = baseUrl + "/" + skill.toLowerCase().replace(" ", "-").replace("&", "-and-") + "-jobs";
            xml.append("  <url>\n");
            xml.append("    <loc>").append(url).append("</loc>\n");
            xml.append("    <lastmod>").append(LocalDateTime.now()).append("</lastmod>\n");
            xml.append("  </url>\n");
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Generate job description sitemap XML
     */
    private String generateJobDescriptionSitemapXml(List<Map<String, Object>> jobs) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        for (Map<String, Object> job : jobs) {
            String url = generateJobDescriptionUrl(job);
            if (url != null) {
                xml.append("  <url>\n");
                xml.append("    <loc>").append(url).append("</loc>\n");
                xml.append("    <lastmod>").append(LocalDateTime.now()).append("</lastmod>\n");
                xml.append("  </url>\n");
            }
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Generate company URL
     */
    private String generateCompanyUrl(String companyName, String companyId) {
        String sanitizedName = companyName.toLowerCase().replace(" ", "-").replace("&", "-and-");
        return baseUrl + "/" + sanitizedName + "-careers-cid-" + companyId;
    }

    /**
     * Generate job description URL
     */
    private String generateJobDescriptionUrl(Map<String, Object> job) {
        try {
            String designation = (String) job.get("designationName");
            String location = (String) job.get("cityName");
            String companyName = (String) job.get("companyName");
            Object jobId = job.get("id");
            
            if (designation == null || location == null || companyName == null || jobId == null) {
                return null;
            }
            
            // Handle experience
            String experience = "fresher";
            Object minExp = job.get("minExperience");
            Object maxExp = job.get("maxExperience");
            
            if (minExp != null && maxExp != null) {
                try {
                    int min = Integer.parseInt(minExp.toString());
                    int max = Integer.parseInt(maxExp.toString());
                    if (min > 0 || max > 0) {
                        experience = min + "-to-" + max + "-years";
                    }
                } catch (NumberFormatException e) {
                    // Keep as "fresher"
                }
            }
            
            String sanitizedDesignation = designation.toLowerCase().replace(" ", "-").replace("&", "-and-");
            String sanitizedLocation = location.toLowerCase().replace(" ", "-").replace("&", "-and-");
            String sanitizedCompany = companyName.toLowerCase().replace(" ", "-").replace("&", "-and-");
            
            return baseUrl + "/" + sanitizedDesignation + "-jobs-in-" + sanitizedLocation + 
                   "-in-" + sanitizedCompany + "-for-" + experience + "-jid-" + jobId;
                   
        } catch (Exception e) {
            log.warn("Error generating job description URL for job: {}", job, e);
            return null;
        }
    }

    /**
     * Generate sitemap not found XML
     */
    private String generateSitemapNotFoundXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
               "  <url>\n" +
               "    <loc>" + baseUrl + "/404</loc>\n" +
               "    <lastmod>" + LocalDateTime.now() + "</lastmod>\n" +
               "  </url>\n" +
               "</urlset>";
    }

    /**
     * Generate error XML
     */
    private String generateErrorXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
               "  <url>\n" +
               "    <loc>" + baseUrl + "/error</loc>\n" +
               "    <lastmod>" + LocalDateTime.now() + "</lastmod>\n" +
               "  </url>\n" +
               "</urlset>";
    }

    /**
     * Manual refresh of sitemap cache
     */
    @Transactional
    public Map<String, Object> refreshSitemapCache() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Manual sitemap cache refresh started");
            
            generateAllSitemaps();
            
            result.put("success", true);
            result.put("message", "Sitemap cache refreshed successfully");
            result.put("timestamp", LocalDateTime.now());
            
            log.info("Manual sitemap cache refresh completed");
            
        } catch (Exception e) {
            log.error("Error during manual sitemap cache refresh", e);
            result.put("success", false);
            result.put("message", "Error refreshing sitemap cache: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }
        
        return result;
    }

    /**
     * Get sitemap cache status
     */
    public Map<String, Object> getSitemapCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            List<String> sitemapTypes = siteMapCacheRepository.findAllActiveSitemapTypes();
            
            for (String sitemapType : sitemapTypes) {
                Map<String, Object> typeStatus = new HashMap<>();
                
                List<SiteMapCache> caches = siteMapCacheRepository.findActiveByTypeAndLevel(sitemapType, 1);
                
                if (!caches.isEmpty()) {
                    SiteMapCache latestCache = caches.get(0);
                    typeStatus.put("lastGenerated", latestCache.getLastGenerated());
                    typeStatus.put("totalPages", latestCache.getTotalPages());
                    typeStatus.put("isActive", latestCache.getIsActive());
                }
                
                status.put(sitemapType, typeStatus);
            }
            
        } catch (Exception e) {
            log.error("Error getting sitemap cache status", e);
            status.put("error", "Error getting cache status: " + e.getMessage());
        }
        
        return status;
    }

    /**
     * Check if any sitemaps exist in cache
     */
    public boolean hasSitemapsInCache() {
        try {
            List<String> sitemapTypes = siteMapCacheRepository.findAllActiveSitemapTypes();
            return !sitemapTypes.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if sitemaps exist in cache", e);
            return false;
        }
    }


}
