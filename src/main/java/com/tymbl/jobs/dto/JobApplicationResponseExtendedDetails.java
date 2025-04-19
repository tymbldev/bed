package com.tymbl.jobs.dto;

import com.tymbl.jobs.entity.ApplicationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String coverLetter;
    private String resumeUrl;
    private ApplicationStatus status;
    private List<Long> applicantSkillIds;
    private String experience;
    private String education;
    private String portfolioUrl;
    private String linkedInUrl;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 