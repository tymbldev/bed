package com.tymbl.jobs.dto;

import com.tymbl.jobs.entity.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class JobApplicationResponseExtendedDetails {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String jobDescription;
    private Long jobCityId;
    private Long jobCountryId;
    private Long jobDesignationId;
    private String jobDesignation;
    private BigDecimal jobSalary;
    private Long jobCurrencyId;
    private Long jobCompanyId;
    private String jobCompany;
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
    private Long applicantDesignationId;
    private String applicantDesignation;
    private String applicantDepartmentName;
    private Long applicantDepartmentId;
    private String applicantCityName;
    private Long applicantCityId;
    private String applicantCountryName;
    private Long applicantCountryId;
    private String applicantZipCode;
    private Integer applicantYearsOfExperience;
    private Integer applicantMonthsOfExperience;
    private Integer applicantCurrentSalary;
    private Long applicantCurrentSalaryCurrencyId;
    private Integer applicantExpectedSalary;
    private Long applicantExpectedSalaryCurrencyId;
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