package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobTagTaggerService {

  private final ObjectMapper objectMapper;

  /**
   * Tag job tags from JSON data
   */
  @Transactional
  public JobTagTaggingResult tagJobTags(String jobTagsJson, Long sourceId, String portalName) {
    JobTagTaggingResult result = new JobTagTaggingResult();

    if (jobTagsJson == null || jobTagsJson.trim().isEmpty()) {
      return result;
    }

    try {
      JsonNode tagsArray = objectMapper.readTree(jobTagsJson);

      if (tagsArray.isArray()) {
        List<String> jobTags = new ArrayList<>();
        double totalConfidence = 0.0;
        int processedTags = 0;

        for (JsonNode tagNode : tagsArray) {
          if (tagNode.has("text")) {
            String tagText = tagNode.get("text").asText();
            if (tagText != null && !tagText.trim().isEmpty()) {
              jobTags.add(tagText.trim());
              totalConfidence += 1.0; // High confidence for direct text extraction
              processedTags++;
            }
          }
        }

        result.setJobTags(jobTags);
        result.setConfidence(processedTags > 0 ? totalConfidence / processedTags : 0.0);

        log.info("Tagged {} job tags for external job {}: {}", jobTags.size(), sourceId, jobTags);
      }

    } catch (Exception e) {
      log.error("Error tagging job tags for external job {}: {}", sourceId, e.getMessage(), e);
      result.setError(e.getMessage());
    }

    return result;
  }

  /**
   * Result class for job tag tagging
   */
  public static class JobTagTaggingResult {

    private List<String> jobTags = new ArrayList<>();
    private Double confidence = 0.0;
    private String error;

    // Getters and setters
    public List<String> getJobTags() {
      return jobTags;
    }

    public void setJobTags(List<String> jobTags) {
      this.jobTags = jobTags;
    }

    public Double getConfidence() {
      return confidence;
    }

    public void setConfidence(Double confidence) {
      this.confidence = confidence;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }
  }
}
