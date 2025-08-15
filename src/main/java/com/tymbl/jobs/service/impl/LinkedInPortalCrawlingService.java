package com.tymbl.jobs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.entity.JobCrawlKeyword;
import com.tymbl.jobs.entity.JobDetail;
import com.tymbl.jobs.entity.JobRawResponse;
import com.tymbl.jobs.repository.JobDetailRepository;
import com.tymbl.jobs.repository.JobRawResponseRepository;
import com.tymbl.jobs.service.PortalCrawlingService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LinkedInPortalCrawlingService implements PortalCrawlingService {

  private static final Logger logger = LoggerFactory.getLogger(LinkedInPortalCrawlingService.class);

  @Autowired
  private JobRawResponseRepository rawResponseRepository;

  @Autowired
  private JobDetailRepository jobDetailRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public boolean canHandlePortal(String portalName) {
    return "linkedin".equalsIgnoreCase(portalName);
  }

  @Override
  public String makePortalApiCall(JobCrawlKeyword keywordConfig, JobCrawlRequest request) {
    String url = buildPortalApiUrl(keywordConfig, request);
    HttpHeaders headers = getPortalHeaders(keywordConfig.getPortalName());
    HttpEntity<String> entity = new HttpEntity<>(headers);

    logger.info("Making LinkedIn API call to: {}", url);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity,
        String.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException(
          "LinkedIn API call failed with status: " + response.getStatusCode());
    }

    return response.getBody();
  }

  @Override
  public JobRawResponse saveRawResponse(JobCrawlKeyword keywordConfig, JobCrawlRequest request,
      String apiResponse) {
    JobRawResponse rawResponse = new JobRawResponse();
    rawResponse.setPortalName(keywordConfig.getPortalName());
    rawResponse.setKeyword(request.getKeyword());
    rawResponse.setRawResponse(apiResponse);
    rawResponse.setApiUrl(buildPortalApiUrl(keywordConfig, request));
    rawResponse.setHttpStatusCode(200);
    rawResponse.setResponseSizeBytes((long) apiResponse.length());
    rawResponse.setProcessingStatus(JobRawResponse.ProcessingStatus.COMPLETED);

    return rawResponseRepository.save(rawResponse);
  }

  @Override
  public List<JobDetail> parseAndSaveJobDetails(JobRawResponse rawResponse, String apiResponse,
      JobCrawlRequest request) {
    List<JobDetail> jobDetails = new ArrayList<>();

    try {
      JsonNode rootNode = objectMapper.readTree(apiResponse);
      // LinkedIn API response structure would be different
      // This is a placeholder implementation
      logger.info("LinkedIn response parsing - implementation needed for actual API structure");

    } catch (Exception e) {
      logger.error("Error parsing LinkedIn API response", e);
      rawResponse.setProcessingStatus(JobRawResponse.ProcessingStatus.FAILED);
      rawResponse.setErrorMessage("Error parsing response: " + e.getMessage());
      rawResponseRepository.save(rawResponse);
    }

    return jobDetails;
  }

  @Override
  public HttpHeaders getPortalHeaders(String portalName) {
    HttpHeaders headers = new HttpHeaders();

    // LinkedIn-specific headers
    headers.set("accept", "application/json");
    headers.set("accept-language", "en-US,en;q=0.9");
    headers.set("cache-control", "no-cache");
    headers.set("pragma", "no-cache");
    headers.set("user-agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
    headers.set("x-li-lang", "en_US");
    headers.set("x-li-track", "{\"clientVersion\":\"1.10.0\"}");

    return headers;
  }

  @Override
  public String buildPortalApiUrl(JobCrawlKeyword keywordConfig, JobCrawlRequest request) {
    StringBuilder urlBuilder = new StringBuilder(keywordConfig.getPortalUrl());

    // LinkedIn-specific URL parameters
    urlBuilder.append("?keywords=").append(request.getKeyword())
        .append("&start=").append(request.getStart())
        .append("&count=").append(request.getLimit())
        .append("&location=").append(request.getCountries());

    return urlBuilder.toString();
  }
}
