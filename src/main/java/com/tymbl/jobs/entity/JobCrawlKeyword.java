package com.tymbl.jobs.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "job_crawl_keywords")
public class JobCrawlKeyword {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "keyword", nullable = false)
  private String keyword;

  @Column(name = "portal_name", nullable = false)
  private String portalName;

  @Column(name = "portal_url", nullable = false)
  private String portalUrl;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "last_crawled_date")
  private LocalDateTime lastCrawledDate;

  @Column(name = "crawl_frequency_hours", nullable = false)
  private Integer crawlFrequencyHours = 24;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  public String getPortalName() {
    return portalName;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public String getPortalUrl() {
    return portalUrl;
  }

  public void setPortalUrl(String portalUrl) {
    this.portalUrl = portalUrl;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public LocalDateTime getLastCrawledDate() {
    return lastCrawledDate;
  }

  public void setLastCrawledDate(LocalDateTime lastCrawledDate) {
    this.lastCrawledDate = lastCrawledDate;
  }

  public Integer getCrawlFrequencyHours() {
    return crawlFrequencyHours;
  }

  public void setCrawlFrequencyHours(Integer crawlFrequencyHours) {
    this.crawlFrequencyHours = crawlFrequencyHours;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
