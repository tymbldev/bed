package com.tymbl.jobs.controller;

import com.tymbl.jobs.service.SiteMapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/sitemap")
@Tag(name = "Sitemap Controller", description = "APIs for generating and serving sitemaps")
@Slf4j
public class SiteMapController {

    @Autowired
    private SiteMapService siteMapService;

    /**
     * Main sitemap index
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Get main sitemap index",
        description = "Returns the main sitemap index containing references to all sitemap files"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Main sitemap index retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
                        "  <url>\n" +
                        "    <loc>https://www.tymblhub.com/sitemap/level1/company-pages.xml</loc>\n" +
                        "    <lastmod>2025-01-01T12:00:00</lastmod>\n" +
                        "  </url>\n" +
                        "  <url>\n" +
                        "    <loc>https://www.tymblhub.com/sitemap/level1/location-wise-jobs.xml</loc>\n" +
                        "    <lastmod>2025-01-01T12:00:00</lastmod>\n" +
                        "  </url>\n" +
                        "</urlset>"
                )
            )
        )
    })
    public ResponseEntity<String> getMainSitemap() {
        try {
            log.info("Serving main sitemap index");
            
            String mainSitemap = generateMainSitemapIndex();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(mainSitemap);
                
        } catch (Exception e) {
            log.error("Error serving main sitemap", e);
            return ResponseEntity.internalServerError()
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>Internal server error</error>");
        }
    }

    /**
     * Level-based sitemap serving
     */
    @GetMapping(value = "/level{level}/{sitemapType}.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(
        summary = "Get level-based sitemap",
        description = "Returns sitemap based on level and type. Level 1 contains company-pages, location-wise-jobs, designation-wise-jobs, skill-wise-jobs, and job-description files."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sitemap retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
                        "  <url>\n" +
                        "    <loc>https://www.tymblhub.com/company-name-careers-cid-123</loc>\n" +
                        "    <lastmod>2025-01-01T12:00:00</lastmod>\n" +
                        "  </url>\n" +
                        "</urlset>"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid sitemap type"),
        @ApiResponse(responseCode = "404", description = "Sitemap not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> getLevelSitemap(
        @Parameter(description = "Sitemap level (1, 2, 3, etc.)", example = "1")
        @PathVariable Integer level,
        
        @Parameter(description = "Sitemap type (company-pages, location-wise-jobs, designation-wise-jobs, skill-wise-jobs, job-description)", 
                  example = "company-pages")
        @PathVariable String sitemapType,
        
        @Parameter(description = "Page number for paginated sitemaps (auto-extracted from sitemapType if ends with -{number}, otherwise defaults to 0)", example = "0")
        @RequestParam(defaultValue = "0") Integer page) {
        
        try {
            // Extract page number from sitemapType if it ends with -{number}
            Integer extractedPage = extractPageFromSitemapType(sitemapType);
            String cleanSitemapType = sitemapType;
            
            if (extractedPage != null) {
                page = extractedPage;
                // Remove the page number from sitemapType to get the clean type
                cleanSitemapType = sitemapType.substring(0, sitemapType.lastIndexOf("_"));
                log.info("Extracted page number {} from sitemapType: {}, clean type: {}", page, sitemapType, cleanSitemapType);
            }
            
            log.info("Serving sitemap - Level: {}, Type: {}, Clean Type: {}, Page: {}", level, sitemapType, cleanSitemapType, page);
            
            // Validate sitemap type for level 1 (use clean type for validation)
            if (level == 1 && !isValidLevel1SitemapType(cleanSitemapType)) {
                log.warn("Invalid sitemap type for level 1: {}", cleanSitemapType);
                return ResponseEntity.badRequest()
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>Invalid sitemap type</error>");
            }
            
            String sitemapContent = siteMapService.getSitemapFromCache(cleanSitemapType, level, page);
            
            if (sitemapContent == null || sitemapContent.isEmpty()) {
                log.warn("Sitemap not found for level: {}, type: {}, page: {}", level, sitemapType, page);
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(sitemapContent);
                
        } catch (Exception e) {
            log.error("Error serving sitemap for level: {}, type: {}, page: {}", level, sitemapType, page, e);
            return ResponseEntity.internalServerError()
                .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>Internal server error</error>");
        }
    }

    /**
     * Refresh sitemap cache manually
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh sitemap cache",
        description = "Manually triggers refresh of all sitemap cache data from Elasticsearch"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Sitemap cache refreshed successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"success\": true,\n" +
                        "  \"message\": \"Sitemap cache refreshed successfully\",\n" +
                        "  \"timestamp\": \"2025-01-01T12:00:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> refreshSitemapCache() {
        try {
            log.info("Manual sitemap cache refresh requested");
            
            Map<String, Object> result = siteMapService.refreshSitemapCache();
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.internalServerError().body(result);
            }
            
        } catch (Exception e) {
            log.error("Error during manual sitemap cache refresh", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error refreshing sitemap cache: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get sitemap cache status
     */
    @GetMapping("/status")
    @Operation(
        summary = "Get sitemap cache status",
        description = "Returns the status of sitemap cache including last generated times and counts"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cache status retrieved successfully",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"company-pages\": {\n" +
                        "    \"lastGenerated\": \"2025-01-01T12:00:00\",\n" +
                        "    \"totalPages\": 5\n" +
                        "  },\n" +
                        "  \"location-wise-jobs\": {\n" +
                        "    \"lastGenerated\": \"2025-01-01T12:00:00\",\n" +
                        "    \"totalPages\": 1\n" +
                        "  }\n" +
                        "}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSitemapCacheStatus() {
        try {
            log.info("Sitemap cache status requested");
            
            Map<String, Object> status = siteMapService.getSitemapCacheStatus();
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error getting sitemap cache status", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error getting cache status: " + e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Generate main sitemap index
     */
    private String generateMainSitemapIndex() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        
        try {
            // Check if any sitemaps exist in cache (generated by background process)
            if (!siteMapService.hasSitemapsInCache()) {
                log.warn("No sitemaps found in cache. Main sitemap will be empty. Run background process first.");
                xml.append("  <!-- No sitemaps generated yet. Run background process to generate sitemaps. -->\n");
            } else {
                // Read sitemap data from database cache (generated by background process)
                Map<String, Object> sitemapStatus = siteMapService.getSitemapCacheStatus();
                
                // Add company-pages sitemap if it exists in cache
                if (sitemapStatus.containsKey("company-pages")) {
                    Map<String, Object> companyPagesStatus = (Map<String, Object>) sitemapStatus.get("company-pages");
                    Integer totalPages = (Integer) companyPagesStatus.get("totalPages");
                    
                    if (totalPages != null && totalPages > 0) {
                        if (totalPages == 1) {
                            // Single page - no pagination needed
                            xml.append("  <url>\n");
                            xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/company-pages.xml</loc>\n");
                            xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                            xml.append("  </url>\n");
                        } else {
                            // Multiple pages - add paginated sitemaps
                            for (int page = 1; page <= totalPages; page++) {
                                xml.append("  <url>\n");
                                xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/company-pages_").append(page).append(".xml</loc>\n");
                                xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                                xml.append("  </url>\n");
                            }
                        }
                    }
                }
                
                // Add location-wise-jobs sitemap if it exists in cache
                if (sitemapStatus.containsKey("location-wise-jobs")) {
                    Map<String, Object> locationJobsStatus = (Map<String, Object>) sitemapStatus.get("location-wise-jobs");
                    Integer totalPages = (Integer) locationJobsStatus.get("totalPages");
                    
                    if (totalPages != null && totalPages > 0) {
                        if (totalPages == 1) {
                            // Single page - no pagination needed
                            xml.append("  <url>\n");
                            xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/location-wise-jobs.xml</loc>\n");
                            xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                            xml.append("  </url>\n");
                        } else {
                            // Multiple pages - add paginated sitemaps
                            for (int page = 1; page <= totalPages; page++) {
                                xml.append("  <url>\n");
                                xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/location-wise-jobs_").append(page).append(".xml</loc>\n");
                                xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                                xml.append("  </url>\n");
                            }
                        }
                    }
                }
                
                // Add designation-wise-jobs sitemap if it exists in cache
                if (sitemapStatus.containsKey("designation-wise-jobs")) {
                    Map<String, Object> designationJobsStatus = (Map<String, Object>) sitemapStatus.get("designation-wise-jobs");
                    Integer totalPages = (Integer) designationJobsStatus.get("totalPages");
                    
                    if (totalPages != null && totalPages > 0) {
                        if (totalPages == 1) {
                            // Single page - no pagination needed
                            xml.append("  <url>\n");
                            xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/designation-wise-jobs.xml</loc>\n");
                            xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                            xml.append("  </url>\n");
                        } else {
                            // Multiple pages - add paginated sitemaps
                            for (int page = 1; page <= totalPages; page++) {
                                xml.append("  <url>\n");
                                xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/designation-wise-jobs_").append(page).append(".xml</loc>\n");
                                xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                                xml.append("  </url>\n");
                            }
                        }
                    }
                }
                
                // Add skill-wise-jobs sitemap if it exists in cache
                if (sitemapStatus.containsKey("skill-wise-jobs")) {
                    Map<String, Object> skillJobsStatus = (Map<String, Object>) sitemapStatus.get("skill-wise-jobs");
                    Integer totalPages = (Integer) skillJobsStatus.get("totalPages");
                    
                    if (totalPages != null && totalPages > 0) {
                        if (totalPages == 1) {
                            // Single page - no pagination needed
                            xml.append("  <url>\n");
                            xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/skill-wise-jobs.xml</loc>\n");
                            xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                            xml.append("  </url>\n");
                        } else {
                            // Multiple pages - add paginated sitemaps
                            for (int page = 1; page <= totalPages; page++) {
                                xml.append("  <url>\n");
                                xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/skill-wise-jobs_").append(page).append(".xml</loc>\n");
                                xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                                xml.append("  </url>\n");
                            }
                        }
                    }
                }
                
                // Add job description sitemaps based on cache data
                if (sitemapStatus.containsKey("job-description")) {
                    Map<String, Object> jobDescriptionStatus = (Map<String, Object>) sitemapStatus.get("job-description");
                    Integer totalPages = (Integer) jobDescriptionStatus.get("totalPages");
                    
                    if (totalPages != null && totalPages > 0) {
                        for (int page = 1; page <= totalPages; page++) {
                            xml.append("  <url>\n");
                            xml.append("    <loc>https://www.tymblhub.com/sitemap/level1/job-description_").append(page).append(".xml</loc>\n");
                            xml.append("    <lastmod>").append(java.time.LocalDateTime.now()).append("</lastmod>\n");
                            xml.append("  </url>\n");
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error reading sitemap cache status for main sitemap generation", e);
            xml.append("  <!-- Error reading sitemap cache status -->\n");
        }
        
        xml.append("</urlset>");
        return xml.toString();
    }

    /**
     * Validate sitemap type for level 1
     */
    private boolean isValidLevel1SitemapType(String sitemapType) {
        return sitemapType.equals("company-pages") ||
               sitemapType.equals("location-wise-jobs") ||
               sitemapType.equals("designation-wise-jobs") ||
               sitemapType.equals("skill-wise-jobs") ||
               sitemapType.equals("job-description");
    }

    /**
     * Extract page number from sitemapType if it ends with _{number}
     * Examples: job-description_1 -> 1, company-pages -> null
     */
    private Integer extractPageFromSitemapType(String sitemapType) {
        if (sitemapType == null || sitemapType.isEmpty()) {
            return null;
        }
        
        // Check if sitemapType ends with _{number} pattern
        if (sitemapType.matches(".*_\\d+$")) {
            try {
                // Extract the number after the last underscore
                String pageStr = sitemapType.substring(sitemapType.lastIndexOf("_") + 1);
                return Integer.parseInt(pageStr);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse page number from sitemapType: {}", sitemapType, e);
                return null;
            }
        }
        
        return null;
    }
}
