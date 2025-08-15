package com.tymbl.jobs.dto;

import com.tymbl.common.entity.Job.JobType;
import com.tymbl.jobs.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class JobApplicationResponseExtendedDetails {

  private Long id;
  private Long jobId;
  private String jobTitle;
  private String jobDescription;
  private Long jobCityId;
  private String jobCityName; // Dropdown value for jobCityId
  private Long jobCountryId;
  private String jobCountryName; // Dropdown value for jobCountryId
  private Long jobDesignationId;
  private String jobDesignation;
  private String jobDesignationName; // Dropdown value for jobDesignationId
  private BigDecimal jobMinSalary;
  private BigDecimal jobMaxSalary;
  private Integer jobMinExperience;
  private Integer jobMaxExperience;
  private JobType jobJobType;
  private Long jobCurrencyId;
  private String jobCurrencyName; // Dropdown value for jobCurrencyId
  private String jobCurrencySymbol; // Dropdown value for jobCurrencyId
  private Long jobCompanyId;
  private String jobCompany;
  private String jobCompanyName; // Dropdown value for jobCompanyId
  private List<Long> jobSkillIds;

  private LocalDateTime createdAt;

  private Long applicantId;
  private String applicantName;
  private String applicantEmail;
  private ApplicationStatus applicationStatus;
  private List<Long> applicantSkillIds;
  private String applicantPortfolioUrl;
  private String applicantLinkedInUrl;
  private String applicantGithubUrl;

  // Additional applicant details
  private String applicantPhoneNumber;
  private String applicantCompany;
  private Long applicantCompanyId;
  private String applicantCompanyName; // Dropdown value for applicantCompanyId
  private Long applicantDesignationId;
  private String applicantDesignation;
  private String applicantDesignationName; // Dropdown value for applicantDesignationId
  private String applicantDepartmentName;
  private Long applicantDepartmentId;
  private String applicantDepartmentNameValue; // Dropdown value for applicantDepartmentId
  private String applicantCityName;
  private Long applicantCityId;
  private String applicantCityNameValue; // Dropdown value for applicantCityId
  private String applicantCountryName;
  private Long applicantCountryId;
  private String applicantCountryNameValue; // Dropdown value for applicantCountryId
  private String applicantZipCode;
  private Integer applicantYearsOfExperience;
  private Integer applicantMonthsOfExperience;
  private Integer applicantCurrentSalary;
  private Long applicantCurrentSalaryCurrencyId;
  private String applicantCurrentSalaryCurrencyName; // Dropdown value for applicantCurrentSalaryCurrencyId
  private String applicantCurrentSalaryCurrencySymbol; // Dropdown value for applicantCurrentSalaryCurrencyId
  private Integer applicantExpectedSalary;
  private Long applicantExpectedSalaryCurrencyId;
  private String applicantExpectedSalaryCurrencyName; // Dropdown value for applicantExpectedSalaryCurrencyId
  private String applicantExpectedSalaryCurrencySymbol; // Dropdown value for applicantExpectedSalaryCurrencyId
  private Integer applicantNoticePeriod;
  private String applicantResume;
  private String applicantResumeContentType;
  private Set<String> applicantSkillNames;
  private Set<com.tymbl.common.entity.User.Education> applicantEducationDetails;
  private String applicantProvider;
  private String applicantProviderId;
  private boolean applicantEmailVerified;
  private boolean applicantEnabled;
  private String applicantProfilePicture;
  private int applicantProfileCompletionPercentage;
  private LocalDateTime applicantUpdatedAt;
} 