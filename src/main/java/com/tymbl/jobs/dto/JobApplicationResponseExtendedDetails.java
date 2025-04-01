package com.tymbl.jobs.dto;

import com.tymbl.jobs.entity.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobApplicationResponseExtendedDetails {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String jobDescription;
    private String jobLocation;
    private String jobEmploymentType;
    private String jobExperienceLevel;
    private Double jobSalary;
    private String jobCurrency;
    private Long applicantId;
    private String applicantName;
    private String applicantEmail;
    private String coverLetter;
    private String resumeUrl;
    private ApplicationStatus status;
    private List<String> skills;
    private String experience;
    private String education;
    private String portfolioUrl;
    private String linkedInUrl;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 