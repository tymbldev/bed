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
import javax.annotation.PostConstruct;
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
  private ExternalJobDetailRepository externalJobDetailRepository;

  @Autowired
  private PortalCrawlingFactory portalFactory;

  /**
   * Crawl jobs for a specific keyword and portal
   */
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
      List<ExternalJobDetail> jobDetails = portalService.parseAndSaveJobDetails(rawResponse,
          apiResponse,
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
    long startTime = System.currentTimeMillis();
    logger.info("🚀 Starting processing of pending raw responses");
    
    List<ExternalJobRawResponse> pendingResponses = rawResponseRepository.findPendingResponses();
    logger.info("📋 Found {} pending raw responses to process", pendingResponses.size());

    if (pendingResponses.isEmpty()) {
      logger.info("✅ No pending raw responses found - process completed immediately");
      return;
    }

    int successCount = 0;
    int errorCount = 0;
    int currentResponseIndex = 0;

    for (ExternalJobRawResponse rawResponse : pendingResponses) {
      currentResponseIndex++;
      long responseStartTime = System.currentTimeMillis();
      
      logger.info("⏳ Processing raw response {}/{}: ID={}, Keyword='{}', Portal='{}', Status='{}'", 
          currentResponseIndex, pendingResponses.size(), rawResponse.getId(), 
          rawResponse.getKeyword(), rawResponse.getPortalName(), rawResponse.getProcessingStatus());

      try {
        logger.debug("🔄 Setting processing status to PROCESSING for response ID: {}", rawResponse.getId());
        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.PROCESSING);
        rawResponseRepository.save(rawResponse);

        // Parse and save job details using portal service
        JobCrawlRequest request = new JobCrawlRequest();
        request.setKeyword(rawResponse.getKeyword());
        request.setPortalName(rawResponse.getPortalName());

        logger.debug("🔧 Creating portal service for: '{}'", rawResponse.getPortalName());
        PortalCrawlingService portalService = portalFactory.getPortalService(
            rawResponse.getPortalName());
        
        logger.debug("🔄 Parsing and saving job details for response ID: {}", rawResponse.getId());
        portalService.parseAndSaveJobDetails(rawResponse, rawResponse.getRawResponse(), request);

        logger.debug("✅ Setting processing status to COMPLETED for response ID: {}", rawResponse.getId());
        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.COMPLETED);
        rawResponseRepository.save(rawResponse);

        long responseProcessingTime = System.currentTimeMillis() - responseStartTime;
        successCount++;
        logger.info("✅ Successfully processed raw response {}/{}: ID={}, Keyword='{}', Portal='{}' in {}ms", 
            currentResponseIndex, pendingResponses.size(), rawResponse.getId(), 
            rawResponse.getKeyword(), rawResponse.getPortalName(), responseProcessingTime);

      } catch (Exception e) {
        long responseProcessingTime = System.currentTimeMillis() - responseStartTime;
        errorCount++;
        logger.error("💥 Error processing raw response {}/{}: ID={}, Keyword='{}', Portal='{}' after {}ms: {}", 
            currentResponseIndex, pendingResponses.size(), rawResponse.getId(), 
            rawResponse.getKeyword(), rawResponse.getPortalName(), responseProcessingTime, e.getMessage(), e);
        
        logger.debug("❌ Setting processing status to FAILED for response ID: {}", rawResponse.getId());
        rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.FAILED);
        rawResponse.setErrorMessage(e.getMessage());
        rawResponseRepository.save(rawResponse);
      }
    }

    long totalProcessingTime = System.currentTimeMillis() - startTime;
    logger.info("🎉 Raw response processing completed! 📊 Summary: Total={}, Success={}, Errors={}, TotalTime={}ms, AvgTimePerResponse={}ms", 
        pendingResponses.size(), successCount, errorCount, totalProcessingTime, 
        pendingResponses.size() > 0 ? totalProcessingTime / pendingResponses.size() : 0);
  }

  @Override
  public void crawlAllActiveKeywords() {
    long startTime = System.currentTimeMillis();
    logger.info("🚀 Starting crawl of all active keywords");
    
    LocalDateTime threshold = LocalDateTime.now().minusHours(24);
    logger.info("⏰ Crawl threshold set to: {} (24 hours ago)", threshold);
    
    List<ExternalJobCrawlKeyword> keywordsToCrawl = keywordRepository.findKeywordsReadyForCrawling(
        threshold);
    logger.info("📋 Found {} keywords ready for crawling", keywordsToCrawl.size());

    if (keywordsToCrawl.isEmpty()) {
      logger.info("✅ No keywords ready for crawling - process completed immediately");
      return;
    }

    int successCount = 0;
    int errorCount = 0;
    int currentKeywordIndex = 0;

    for (ExternalJobCrawlKeyword keyword : keywordsToCrawl) {
      currentKeywordIndex++;
      long keywordStartTime = System.currentTimeMillis();
      
      logger.info("⏳ Processing keyword {}/{}: '{}' for portal '{}' (ID: {})", 
          currentKeywordIndex, keywordsToCrawl.size(), keyword.getKeyword(), 
          keyword.getPortalName(), keyword.getId());

      try {
        JobCrawlRequest request = new JobCrawlRequest();
        request.setKeyword(keyword.getKeyword());
        request.setPortalName(keyword.getPortalName());

        logger.debug("🔄 Initiating crawl for keyword: '{}' on portal: '{}'", 
            keyword.getKeyword(), keyword.getPortalName());
        
        crawlJobs(request);

        long keywordProcessingTime = System.currentTimeMillis() - keywordStartTime;
        successCount++;
        logger.info("✅ Successfully crawled keyword {}/{}: '{}' for portal '{}' in {}ms", 
            currentKeywordIndex, keywordsToCrawl.size(), keyword.getKeyword(), 
            keyword.getPortalName(), keywordProcessingTime);

        // Add delay between requests to avoid overwhelming the API
        logger.debug("⏸️ Adding 2-second delay before next keyword crawl");
        Thread.sleep(2000);

      } catch (Exception e) {
        long keywordProcessingTime = System.currentTimeMillis() - keywordStartTime;
        errorCount++;
        logger.error("💥 Error crawling keyword {}/{}: '{}' for portal '{}' after {}ms: {}", 
            currentKeywordIndex, keywordsToCrawl.size(), keyword.getKeyword(), 
            keyword.getPortalName(), keywordProcessingTime, e.getMessage(), e);
      }
    }

    long totalProcessingTime = System.currentTimeMillis() - startTime;
    logger.info("🎉 Keyword crawling completed! 📊 Summary: Total={}, Success={}, Errors={}, TotalTime={}ms, AvgTimePerKeyword={}ms", 
        keywordsToCrawl.size(), successCount, errorCount, totalProcessingTime, 
        keywordsToCrawl.size() > 0 ? totalProcessingTime / keywordsToCrawl.size() : 0);
  }
}
