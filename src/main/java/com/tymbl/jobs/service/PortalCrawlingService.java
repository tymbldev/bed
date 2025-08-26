package com.tymbl.jobs.service;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.entity.ExternalJobCrawlKeyword;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.entity.ExternalJobRawResponse;
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
  String makePortalApiCall(ExternalJobCrawlKeyword keywordConfig, JobCrawlRequest request);

  /**
   * Save raw response from the portal
   *
   * @param keywordConfig The keyword configuration
   * @param request The crawl request
   * @param apiResponse The raw API response
   * @return Saved ExternalJobRawResponse entity
   */
  ExternalJobRawResponse saveRawResponse(ExternalJobCrawlKeyword keywordConfig,
      JobCrawlRequest request,
      String apiResponse);

  /**
   * Parse and save job details from the raw response
   *
   * @param rawResponse The raw response entity
   * @param apiResponse The raw API response string
   * @param request The crawl request
   * @return List of parsed and saved ExternalJobDetail entities
   */
  List<ExternalJobDetail> parseAndSaveJobDetails(ExternalJobRawResponse rawResponse,
      String apiResponse,
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
  String buildPortalApiUrl(ExternalJobCrawlKeyword keywordConfig, JobCrawlRequest request);
}
