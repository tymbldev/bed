package com.tymbl.jobs.service;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.dto.JobCrawlResponse;

public interface JobCrawlingService {
    
    /**
     * Crawl jobs for a specific keyword and portal
     */
    JobCrawlResponse crawlJobs(JobCrawlRequest request);
    
    /**
     * Process pending raw responses and extract job details
     */
    void processPendingRawResponses();
    
    /**
     * Crawl all active keywords for all portals
     */
    void crawlAllActiveKeywords();
    
    /**
     * Manually trigger crawling for a specific keyword-portal combination
     */
    JobCrawlResponse manualCrawl(String keyword, String portalName);
    
    /**
     * Get crawling statistics
     */
    JobCrawlResponse getCrawlingStats();
}
