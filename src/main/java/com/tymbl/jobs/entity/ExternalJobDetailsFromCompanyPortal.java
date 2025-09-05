package com.tymbl.jobs.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "external_job_details_from_company_portal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalJobDetailsFromCompanyPortal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "external_job_detail_id", nullable = false)
  private Long externalJobDetailId;

  @Column(name = "redirect_url", columnDefinition = "TEXT", nullable = false)
  private String redirectUrl;

  @Column(name = "raw_html_content", columnDefinition = "LONGTEXT")
  private String rawHtmlContent;

  @Column(name = "parsed_text_content", columnDefinition = "LONGTEXT")
  private String parsedTextContent;

  @Column(name = "crawl_status", nullable = false)
  @Builder.Default
  private String crawlStatus = "PENDING"; // PENDING, SUCCESS, FAILED

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "crawl_duration_ms")
  private Long crawlDurationMs;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  public void setUpdatedAt() {
    updatedAt = LocalDateTime.now();
  }
}
