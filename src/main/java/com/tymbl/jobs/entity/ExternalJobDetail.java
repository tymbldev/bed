package com.tymbl.jobs.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "external_job_details")
@Data
public class ExternalJobDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "portal_job_id", nullable = false, unique = true)
  private String portalJobId;

  @Column(name = "portal_name", nullable = false)
  private String portalName;

  @Column(name = "job_title", nullable = false)
  private String jobTitle;

  @Column(name = "company_name")
  private String companyName;

  @Column(name = "company_id")
  private String companyId;

  @Column(name = "job_description", columnDefinition = "TEXT")
  private String jobDescription;

  @Column(name = "locations", columnDefinition = "TEXT")
  private String locations;

  @Column(name = "city_id")
  private Long cityId;

  @Column(name = "city_name")
  private String cityName;

  @Column(name = "country_id")
  private Long countryId;

  @Column(name = "country_name")
  private String countryName;

  @Column(name = "minimum_experience")
  private Integer minimumExperience;

  @Column(name = "maximum_experience")
  private Integer maximumExperience;

  @Column(name = "minimum_salary")
  private BigDecimal minimumSalary;

  @Column(name = "maximum_salary")
  private BigDecimal maximumSalary;

  @Column(name = "salary_currency")
  private String salaryCurrency;

  @Column(name = "job_types", columnDefinition = "TEXT")
  private String jobTypes;

  @Column(name = "employment_types", columnDefinition = "TEXT")
  private String employmentTypes;

  @Column(name = "skills", columnDefinition = "TEXT")
  private String skills;

  @Column(name = "skills_json", columnDefinition = "TEXT")
  private String skillsJson;

  @Column(name = "job_tags_json", columnDefinition = "TEXT")
  private String jobTagsJson;

  @Column(name = "refined_description", columnDefinition = "TEXT")
  private String refinedDescription;

  @Column(name = "refined_title", columnDefinition = "TEXT")
  private String refinedTitle;

  @Column(name = "is_refined", nullable = false)
  private Boolean isRefined = false;

  @Column(name = "industries", columnDefinition = "TEXT")
  private String industries;

  @Column(name = "functions", columnDefinition = "TEXT")
  private String functions;

  @Column(name = "roles", columnDefinition = "TEXT")
  private String roles;

  @Column(name = "posted_date")
  private LocalDateTime postedDate;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Column(name = "updated_date")
  private LocalDateTime updatedDate;

  @Column(name = "freshness")
  private String freshness;

  @Column(name = "recruiter_id")
  private String recruiterId;

  @Column(name = "raw_response_id")
  private Long rawResponseId;

  @Column(name = "keyword_used")
  private String keywordUsed;

  @Column(name = "crawl_timestamp", nullable = false)
  private LocalDateTime crawlTimestamp;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "is_synced_to_job_table")
  private Boolean isSyncedToJobTable = false;

  @Column(name = "redirect_url", columnDefinition = "TEXT")
  private String redirectUrl;

  @Column(name = "crawl_status", nullable = false)
  private String crawlStatus = "NOT_CRAWLED"; // NOT_CRAWLED, CRAWLING, CRAWLED, FAILED

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    crawlTimestamp = LocalDateTime.now();
  }
}
