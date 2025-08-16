package com.tymbl.jobs.service.impl;

import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.dto.JobCrawlResponse;
import com.tymbl.jobs.entity.ExternalJobCrawlKeyword;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.entity.ExternalJobRawResponse;
import com.tymbl.jobs.repository.ExternalJobCrawlKeywordRepository;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import com.tymbl.jobs.repository.ExternalJobRawResponseRepository;
import com.tymbl.jobs.service.JobCrawlingService;
import com.tymbl.jobs.service.PortalCrawlingFactory;
import com.tymbl.jobs.service.PortalCrawlingService;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobCrawlingServiceImpl implements JobCrawlingService {

  private static final Logger logger = LoggerFactory.getLogger(JobCrawlingServiceImpl.class);

  @Autowired
  private ExternalJobCrawlKeywordRepository keywordRepository;

  @Autowired
  private ExternalJobRawResponseRepository rawResponseRepository;

  @Autowired
  private ExternalJobDetailRepository jobDetailRepository;

  @Autowired
  private PortalCrawlingFactory portalFactory;

  @Override
  public JobCrawlResponse crawlJobs(JobCrawlRequest request) {
    long startTime = System.currentTimeMillis();
    JobCrawlResponse response = new JobCrawlResponse();

    try {
      logger.info("Starting job crawl for keyword: {} and portal: {}", request.getKeyword(),
          request.getPortalName());

      // Find the keyword configuration
      ExternalJobCrawlKeyword keywordConfig = keywordRepository.findByKeywordAndPortalName(
          request.getKeyword(), request.getPortalName());

      if (keywordConfig == null) {
        response.setStatus("ERROR");
        response.setMessage("Keyword configuration not found");
        return response;
      }

      // Get the appropriate portal service
      PortalCrawlingService portalService = portalFactory.getPortalService(request.getPortalName());

      // Make API call to the portal
      String apiResponse = portalService.makePortalApiCall(keywordConfig, request);

      // Save raw response
      ExternalJobRawResponse rawResponse = portalService.saveRawResponse(keywordConfig, request,
          apiResponse);

      // Parse and save job details
      List<ExternalJobDetail> jobDetails = portalService.parseAndSaveJobDetails(rawResponse, apiResponse,
          request);

      // Update last crawled date
      updateLastCrawledDate(keywordConfig);

      // Prepare response
      response.setStatus("SUCCESS");
      response.setMessage("Jobs crawled successfully");
      response.setTotalJobsFound((long) jobDetails.size());
      response.setRawResponseId(rawResponse.getId());
      response.setProcessedJobsCount((long) jobDetails.size());
      response.setPortalName(request.getPortalName());
      response.setKeyword(request.getKeyword());
      response.setProcessingTimeMs(System.currentTimeMillis() - startTime);

      logger.info("Job crawl completed successfully. Found {} jobs", jobDetails.size());

    } catch (Exception e) {
      logger.error("Error during job crawling", e);
      response.setStatus("ERROR");
      response.setMessage("Error during crawling: " + e.getMessage());
    }

    return response;
  }


  private void updateLastCrawledDate(ExternalJobCrawlKeyword keywordConfig) {
    keywordConfig.setLastCrawledDate(LocalDateTime.now());
    keywordRepository.save(keywordConfig);
  }

  @Override
  public void processPendingRawResponses() {
    List<ExternalJobRawResponse> pendingResponses = rawResponseRepository.findPendingResponses();

    for (ExternalJobRawResponse rawResponse : pendingResponses) {
      try {
        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.PROCESSING);
        rawResponseRepository.save(rawResponse);

        // Parse and save job details using portal service
        JobCrawlRequest request = new JobCrawlRequest();
        request.setKeyword(rawResponse.getKeyword());
        request.setPortalName(rawResponse.getPortalName());

        PortalCrawlingService portalService = portalFactory.getPortalService(
            rawResponse.getPortalName());
        portalService.parseAndSaveJobDetails(rawResponse, rawResponse.getRawResponse(), request);

        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.COMPLETED);
        rawResponseRepository.save(rawResponse);

      } catch (Exception e) {
        logger.error("Error processing raw response ID: {}", rawResponse.getId(), e);
        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.FAILED);
        rawResponse.setErrorMessage(e.getMessage());
        rawResponseRepository.save(rawResponse);
      }
    }
  }

  @Override
  public void crawlAllActiveKeywords() {
    LocalDateTime threshold = LocalDateTime.now().minusHours(24);
    List<ExternalJobCrawlKeyword> keywordsToCrawl = keywordRepository.findKeywordsReadyForCrawling(
        threshold);

    for (ExternalJobCrawlKeyword keyword : keywordsToCrawl) {
      try {
        JobCrawlRequest request = new JobCrawlRequest();
        request.setKeyword(keyword.getKeyword());
        request.setPortalName(keyword.getPortalName());

        crawlJobs(request);

        // Add delay between requests to avoid overwhelming the API
        Thread.sleep(2000);

      } catch (Exception e) {
        logger.error("Error crawling keyword: {} for portal: {}", keyword.getKeyword(),
            keyword.getPortalName(), e);
      }
    }
  }

  @Override
  public JobCrawlResponse manualCrawl(String keyword, String portalName) {
    JobCrawlRequest request = new JobCrawlRequest();
    request.setKeyword(keyword);
    request.setPortalName(portalName);

    return crawlJobs(request);
  }

  @Override
  public JobCrawlResponse getCrawlingStats() {
    JobCrawlResponse response = new JobCrawlResponse();

    long totalKeywords = keywordRepository.count();
    long activeKeywords = keywordRepository.findByIsActiveTrue().size();
    long totalRawResponses = rawResponseRepository.count();
    long totalJobDetails = jobDetailRepository.count();

    response.setStatus("SUCCESS");
    response.setMessage("Crawling statistics retrieved successfully");
    response.setTotalJobsFound(totalJobDetails);
    response.setRawResponseId(totalRawResponses);
    response.setProcessedJobsCount(totalJobDetails);

    return response;
  }
}
