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
import lombok.Data;

@Entity
@Table(name = "external_job_raw_responses")
@Data
public class ExternalJobRawResponse {

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
}
