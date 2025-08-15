package com.tymbl.jobs.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "job_raw_responses")
public class JobRawResponse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "portal_name", nullable = false)
  private String portalName;

  @Column(name = "keyword", nullable = false)
  private String keyword;

  @Column(name = "raw_response", columnDefinition = "TEXT")
  private String rawResponse;

  @Column(name = "api_url", nullable = false)
  private String apiUrl;

  @Column(name = "http_status_code")
  private Integer httpStatusCode;

  @Column(name = "response_size_bytes")
  private Long responseSizeBytes;

  @Column(name = "crawl_timestamp", nullable = false)
  private LocalDateTime crawlTimestamp;

  @Column(name = "processing_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public enum ProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    crawlTimestamp = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPortalName() {
    return portalName;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getRawResponse() {
    return rawResponse;
  }

  public void setRawResponse(String rawResponse) {
    this.rawResponse = rawResponse;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
  }

  public Integer getHttpStatusCode() {
    return httpStatusCode;
  }

  public void setHttpStatusCode(Integer httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }

  public Long getResponseSizeBytes() {
    return responseSizeBytes;
  }

  public void setResponseSizeBytes(Long responseSizeBytes) {
    this.responseSizeBytes = responseSizeBytes;
  }

  public LocalDateTime getCrawlTimestamp() {
    return crawlTimestamp;
  }

  public void setCrawlTimestamp(LocalDateTime crawlTimestamp) {
    this.crawlTimestamp = crawlTimestamp;
  }

  public ProcessingStatus getProcessingStatus() {
    return processingStatus;
  }

  public void setProcessingStatus(ProcessingStatus processingStatus) {
    this.processingStatus = processingStatus;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
