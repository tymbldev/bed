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

@Entity
@Table(name = "job_details")
public class JobDetail {

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

  public String getPortalJobId() {
    return portalJobId;
  }

  public void setPortalJobId(String portalJobId) {
    this.portalJobId = portalJobId;
  }

  public String getPortalName() {
    return portalName;
  }

  public void setPortalName(String portalName) {
    this.portalName = portalName;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getCompanyId() {
    return companyId;
  }

  public void setCompanyId(String companyId) {
    this.companyId = companyId;
  }

  public String getJobDescription() {
    return jobDescription;
  }

  public void setJobDescription(String jobDescription) {
    this.jobDescription = jobDescription;
  }

  public String getLocations() {
    return locations;
  }

  public void setLocations(String locations) {
    this.locations = locations;
  }

  public Long getCityId() {
    return cityId;
  }

  public void setCityId(Long cityId) {
    this.cityId = cityId;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public Long getCountryId() {
    return countryId;
  }

  public void setCountryId(Long countryId) {
    this.countryId = countryId;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public Integer getMinimumExperience() {
    return minimumExperience;
  }

  public void setMinimumExperience(Integer minimumExperience) {
    this.minimumExperience = minimumExperience;
  }

  public Integer getMaximumExperience() {
    return maximumExperience;
  }

  public void setMaximumExperience(Integer maximumExperience) {
    this.maximumExperience = maximumExperience;
  }

  public BigDecimal getMinimumSalary() {
    return minimumSalary;
  }

  public void setMinimumSalary(BigDecimal minimumSalary) {
    this.minimumSalary = minimumSalary;
  }

  public BigDecimal getMaximumSalary() {
    return maximumSalary;
  }

  public void setMaximumSalary(BigDecimal maximumSalary) {
    this.maximumSalary = maximumSalary;
  }

  public String getSalaryCurrency() {
    return salaryCurrency;
  }

  public void setSalaryCurrency(String salaryCurrency) {
    this.salaryCurrency = salaryCurrency;
  }

  public String getJobTypes() {
    return jobTypes;
  }

  public void setJobTypes(String jobTypes) {
    this.jobTypes = jobTypes;
  }

  public String getEmploymentTypes() {
    return employmentTypes;
  }

  public void setEmploymentTypes(String employmentTypes) {
    this.employmentTypes = employmentTypes;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public String getIndustries() {
    return industries;
  }

  public void setIndustries(String industries) {
    this.industries = industries;
  }

  public String getFunctions() {
    return functions;
  }

  public void setFunctions(String functions) {
    this.functions = functions;
  }

  public String getRoles() {
    return roles;
  }

  public void setRoles(String roles) {
    this.roles = roles;
  }

  public LocalDateTime getPostedDate() {
    return postedDate;
  }

  public void setPostedDate(LocalDateTime postedDate) {
    this.postedDate = postedDate;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public LocalDateTime getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(LocalDateTime updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getFreshness() {
    return freshness;
  }

  public void setFreshness(String freshness) {
    this.freshness = freshness;
  }

  public String getRecruiterId() {
    return recruiterId;
  }

  public void setRecruiterId(String recruiterId) {
    this.recruiterId = recruiterId;
  }

  public Long getRawResponseId() {
    return rawResponseId;
  }

  public void setRawResponseId(Long rawResponseId) {
    this.rawResponseId = rawResponseId;
  }

  public String getKeywordUsed() {
    return keywordUsed;
  }

  public void setKeywordUsed(String keywordUsed) {
    this.keywordUsed = keywordUsed;
  }

  public LocalDateTime getCrawlTimestamp() {
    return crawlTimestamp;
  }

  public void setCrawlTimestamp(LocalDateTime crawlTimestamp) {
    this.crawlTimestamp = crawlTimestamp;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
