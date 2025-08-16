package com.tymbl.jobs.service;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.dto.JobCrawlResponse;

public interface JobCrawlingService {

  /**
   * Process pending raw responses and extract job details
   */
  void processPendingRawResponses();

  /**
   * Crawl all active keywords for all portals
   */
  void crawlAllActiveKeywords();
}
