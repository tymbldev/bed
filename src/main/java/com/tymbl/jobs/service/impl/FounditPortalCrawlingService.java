package com.tymbl.jobs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.jobs.dto.JobCrawlRequest;
import com.tymbl.jobs.entity.ExternalJobCrawlKeyword;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.entity.ExternalJobRawResponse;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import com.tymbl.jobs.repository.ExternalJobRawResponseRepository;
import com.tymbl.jobs.service.PortalCrawlingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
public class FounditPortalCrawlingService implements PortalCrawlingService {

  private static final Logger logger = LoggerFactory.getLogger(FounditPortalCrawlingService.class);

  @Autowired
  private ExternalJobRawResponseRepository rawResponseRepository;

  @Autowired
  private ExternalJobDetailRepository jobDetailRepository;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public boolean canHandlePortal(String portalName) {
    return "foundit".equalsIgnoreCase(portalName) || "monster".equalsIgnoreCase(portalName);
  }

  @Override
  public String makePortalApiCall(ExternalJobCrawlKeyword keywordConfig, JobCrawlRequest request) {
    String url = buildPortalApiUrl(keywordConfig, request);
    HttpHeaders headers = getPortalHeaders(keywordConfig.getPortalName());
    HttpEntity<String> entity = new HttpEntity<>(headers);

    logger.info("Making Foundit API call to: {}", url);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity,
        String.class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException(
          "Foundit API call failed with status: " + response.getStatusCode());
    }

    return response.getBody();
  }

  @Override
  public ExternalJobRawResponse saveRawResponse(ExternalJobCrawlKeyword keywordConfig, JobCrawlRequest request,
      String apiResponse) {
    ExternalJobRawResponse rawResponse = new ExternalJobRawResponse();
    rawResponse.setPortalName(keywordConfig.getPortalName());
    rawResponse.setKeyword(request.getKeyword());
    rawResponse.setRawResponse(apiResponse);
    rawResponse.setApiUrl(buildPortalApiUrl(keywordConfig, request));
    rawResponse.setHttpStatusCode(200);
    rawResponse.setResponseSizeBytes((long) apiResponse.length());
    rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.COMPLETED);

    return rawResponseRepository.save(rawResponse);
  }

  @Override
  public List<ExternalJobDetail> parseAndSaveJobDetails(ExternalJobRawResponse rawResponse, String apiResponse,
      JobCrawlRequest request) {
    List<ExternalJobDetail> jobDetails = new ArrayList<>();

    try {
      JsonNode rootNode = objectMapper.readTree(apiResponse);
      JsonNode dataNode = rootNode.get("data");

      if (dataNode != null && dataNode.isArray()) {
        for (JsonNode jobNode : dataNode) {
          try {
            ExternalJobDetail jobDetail = parseFounditJobNode(jobNode, rawResponse, request);

            // Check if job already exists
            Optional<ExternalJobDetail> existingJob = jobDetailRepository.findByPortalJobIdAndPortalName(
                jobDetail.getPortalJobId(), jobDetail.getPortalName());

            if (!existingJob.isPresent()) {
              jobDetails.add(jobDetailRepository.save(jobDetail));
            }
          } catch (Exception e) {
            logger.warn("Error parsing Foundit job node: {}", e.getMessage());
          }
        }
      }

    } catch (Exception e) {
      logger.error("Error parsing Foundit API response", e);
      rawResponse.setProcessingStatus(ExternalJobRawResponse.ProcessingStatus.FAILED);
      rawResponse.setErrorMessage("Error parsing response: " + e.getMessage());
      rawResponseRepository.save(rawResponse);
    }

    return jobDetails;
  }

  @Override
  public HttpHeaders getPortalHeaders(String portalName) {
    HttpHeaders headers = new HttpHeaders();

    // Foundit-specific headers
    headers.set("accept", "*/*");
    headers.set("accept-language", "en-IN,en;q=0.9,hi-IN;q=0.8,hi;q=0.7,en-GB;q=0.6,en-US;q=0.5");
    headers.set("cache-control", "no-cache");
    headers.set("pragma", "no-cache");
    headers.set("user-agent",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36");
    headers.set("x-source-site-context", "rexmonster");
    headers.set("referer", "https://www.foundit.in/search/software-engineer-jobs");

    return headers;
  }

  @Override
  public String buildPortalApiUrl(ExternalJobCrawlKeyword keywordConfig, JobCrawlRequest request) {
    StringBuilder urlBuilder = new StringBuilder(keywordConfig.getPortalUrl());

    // Foundit-specific URL parameters
    urlBuilder.append("?start=").append(request.getStart())
        .append("&limit=").append(request.getLimit())
        .append("&query=").append(request.getKeyword())
        .append("&queryDerived=").append(request.getQueryDerived())
        .append("&countries=").append(request.getCountries())
        .append("&variantName=").append(request.getVariantName());

    return urlBuilder.toString();
  }

  private ExternalJobDetail parseFounditJobNode(JsonNode jobNode, ExternalJobRawResponse rawResponse,
      JobCrawlRequest request) {
    ExternalJobDetail jobDetail = new ExternalJobDetail();

    // Extract basic job information
    jobDetail.setPortalJobId(jobNode.has("id") ? jobNode.get("id").asText() : null);
    jobDetail.setPortalName(rawResponse.getPortalName());
    jobDetail.setJobTitle(jobNode.has("title") ? jobNode.get("title").asText() : null);
    jobDetail.setKeywordUsed(request.getKeyword());
    jobDetail.setRawResponseId(rawResponse.getId());

    // Extract company information
    JsonNode companyNode = jobNode.get("company");
    if (companyNode != null) {
      jobDetail.setCompanyName(companyNode.has("name") ? companyNode.get("name").asText() : null);
      jobDetail.setCompanyId(
          companyNode.has("companyId") ? companyNode.get("companyId").asText() : null);
    }

    // Extract locations - handle the nested structure
    JsonNode locationsNode = jobNode.get("locations");
    if (locationsNode != null && locationsNode.isArray()) {
      List<String> locationList = new ArrayList<>();
      for (JsonNode location : locationsNode) {
        String city = location.has("city") ? location.get("city").asText() : null;
        String state = location.has("state") ? location.get("state").asText() : null;
        String country = location.has("country") ? location.get("country").asText() : null;

        StringBuilder locationStr = new StringBuilder();
          if (city != null) {
              locationStr.append(city);
          }
        if (state != null) {
            if (locationStr.length() > 0) {
                locationStr.append(", ");
            }
          locationStr.append(state);
        }
        if (country != null) {
            if (locationStr.length() > 0) {
                locationStr.append(", ");
            }
          locationStr.append(country);
        }

        if (locationStr.length() > 0) {
          locationList.add(locationStr.toString());
        }
      }
      jobDetail.setLocations(String.join("; ", locationList));
    }

    // Extract experience - handle nested structure
    jobDetail.setMinimumExperience(getIntegerValue(jobNode, "minimumExperience"));
    jobDetail.setMaximumExperience(getIntegerValue(jobNode, "maximumExperience"));

    // Extract salary - handle nested structure
    jobDetail.setMinimumSalary(getBigDecimalValue(jobNode, "minimumSalary"));
    jobDetail.setMaximumSalary(getBigDecimalValue(jobNode, "maximumSalary"));

    // Extract job description
    jobDetail.setJobDescription(
        jobNode.has("description") ? jobNode.get("description").asText() : null);

    // Extract arrays as comma-separated strings
    jobDetail.setJobTypes(extractArrayAsString(jobNode, "jobTypes"));
    jobDetail.setEmploymentTypes(extractArrayAsString(jobNode, "employmentTypes"));
    jobDetail.setSkills(extractArrayAsString(jobNode, "itSkills"));
    jobDetail.setIndustries(extractArrayAsString(jobNode, "industries"));
    jobDetail.setFunctions(extractArrayAsString(jobNode, "functions"));
    jobDetail.setRoles(extractArrayAsString(jobNode, "roles"));

    // Extract dates - handle timestamp format
    String postedAt = jobNode.has("postedAt") ? jobNode.get("postedAt").asText() : null;
    String createdAt = jobNode.has("createdAt") ? jobNode.get("createdAt").asText() : null;
    String updatedAt = jobNode.has("updatedAt") ? jobNode.get("updatedAt").asText() : null;

    jobDetail.setPostedDate(parseTimestamp(postedAt));
    jobDetail.setCreatedDate(parseTimestamp(createdAt));
    jobDetail.setUpdatedDate(parseTimestamp(updatedAt));

    // Extract other fields
    jobDetail.setFreshness(jobNode.has("freshness") ? jobNode.get("freshness").asText() : null);
    jobDetail.setRecruiterId(
        jobNode.has("recruiterId") ? jobNode.get("recruiterId").asText() : null);

    return jobDetail;
  }

  private String getStringValue(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode != null && !fieldNode.isNull()) {
      if (fieldNode.isTextual()) {
        return fieldNode.asText();
      } else if (fieldNode.isObject()) {
        // Handle nested objects - try to get common fields
        if (fieldNode.has("name")) {
          return fieldNode.get("name").asText();
        } else if (fieldNode.has("text")) {
          return fieldNode.get("text").asText();
        }
      }
    }
    return null;
  }

  private Integer getIntegerValue(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode != null && !fieldNode.isNull()) {
      if (fieldNode.isNumber()) {
        return fieldNode.asInt();
      } else if (fieldNode.isObject() && fieldNode.has("years")) {
        // Handle nested structure like {"years": 4}
        return fieldNode.get("years").asInt();
      }
    }
    return null;
  }

  private BigDecimal getBigDecimalValue(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode != null && !fieldNode.isNull()) {
      if (fieldNode.isNumber()) {
        return new BigDecimal(fieldNode.asText());
      } else if (fieldNode.isObject() && fieldNode.has("absoluteValue")) {
        // Handle nested structure like {"currency": "INR", "absoluteValue": 400000}
        JsonNode absoluteValueNode = fieldNode.get("absoluteValue");
        if (absoluteValueNode != null && !absoluteValueNode.isNull()) {
          try {
            return new BigDecimal(absoluteValueNode.asText());
          } catch (NumberFormatException e) {
            return null;
          }
        }
      } else {
        try {
          return new BigDecimal(fieldNode.asText());
        } catch (NumberFormatException e) {
          return null;
        }
      }
    }
    return null;
  }

  private String extractArrayAsString(JsonNode node, String fieldName) {
    JsonNode fieldNode = node.get(fieldName);
    if (fieldNode != null && fieldNode.isArray()) {
      List<String> values = new ArrayList<>();
      for (JsonNode item : fieldNode) {
        if (item.isTextual()) {
          values.add(item.asText());
        } else if (item.has("name")) {
          values.add(item.get("name").asText());
        } else if (item.has("text")) {
          // Handle foundit response structure for skills
          values.add(item.get("text").asText());
        } else if (item.has("id")) {
          // Handle foundit response structure for skills with id and text
          if (item.has("text")) {
            values.add(item.get("text").asText());
          }
        }
      }
      return String.join(", ", values);
    }
    return null;
  }

  private LocalDateTime parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }

    try {
      // Try different date formats
      String[] formats = {
          "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
          "yyyy-MM-dd'T'HH:mm:ss'Z'",
          "yyyy-MM-dd HH:mm:ss"
      };

      for (String format : formats) {
        try {
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
          return LocalDateTime.parse(dateString, formatter);
        } catch (Exception e) {
          // Continue to next format
        }
      }
    } catch (Exception e) {
      logger.warn("Could not parse date: {}", dateString);
    }

    return null;
  }

  private LocalDateTime parseTimestamp(String timestampString) {
    if (timestampString == null || timestampString.trim().isEmpty()) {
      return null;
    }

    try {
      // Handle Unix timestamp in milliseconds
      long timestamp = Long.parseLong(timestampString);
      return LocalDateTime.ofInstant(
          java.time.Instant.ofEpochMilli(timestamp),
          java.time.ZoneId.systemDefault()
      );
    } catch (NumberFormatException e) {
      // If not a timestamp, try parsing as date string
      return parseDate(timestampString);
    }
  }
}
