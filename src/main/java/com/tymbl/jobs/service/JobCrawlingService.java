package com.tymbl.jobs.service;

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
