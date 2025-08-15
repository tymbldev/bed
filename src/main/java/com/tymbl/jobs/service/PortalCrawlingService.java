package com.tymbl.jobs.service;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.entity.JobCrawlKeyword;
import com.tymbl.jobs.entity.JobDetail;
import com.tymbl.jobs.entity.JobRawResponse;
import java.util.List;

/**
 * Interface for portal-specific job crawling implementations. Each portal (foundit, LinkedIn,
 * Naukri, etc.) will have its own implementation.
 */
public interface PortalCrawlingService {

  /**
   * Check if this service can handle the specified portal
   *
   * @param portalName The name of the portal
   * @return true if this service can handle the portal, false otherwise
   */
  boolean canHandlePortal(String portalName);

  /**
   * Make API call to the specific portal
   *
   * @param keywordConfig The keyword configuration
   * @param request The crawl request
   * @return Raw API response as string
   */
  String makePortalApiCall(JobCrawlKeyword keywordConfig, JobCrawlRequest request);

  /**
   * Save raw response from the portal
   *
   * @param keywordConfig The keyword configuration
   * @param request The crawl request
   * @param apiResponse The raw API response
   * @return Saved JobRawResponse entity
   */
  JobRawResponse saveRawResponse(JobCrawlKeyword keywordConfig, JobCrawlRequest request,
      String apiResponse);

  /**
   * Parse and save job details from the raw response
   *
   * @param rawResponse The raw response entity
   * @param apiResponse The raw API response string
   * @param request The crawl request
   * @return List of parsed and saved JobDetail entities
   */
  List<JobDetail> parseAndSaveJobDetails(JobRawResponse rawResponse, String apiResponse,
      JobCrawlRequest request);

  /**
   * Get portal-specific headers for API calls
   *
   * @param portalName The name of the portal
   * @return HttpHeaders configured for the portal
   */
  org.springframework.http.HttpHeaders getPortalHeaders(String portalName);

  /**
   * Build portal-specific API URL
   *
   * @param keywordConfig The keyword configuration
   * @param request The crawl request
   * @return Complete API URL for the portal
   */
  String buildPortalApiUrl(JobCrawlKeyword keywordConfig, JobCrawlRequest request);
}
