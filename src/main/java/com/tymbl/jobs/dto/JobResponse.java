package com.tymbl.jobs.dto;

import com.tymbl.common.entity.Job.JobType;
import com.tymbl.common.entity.Job.ExperienceLevel;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String company;
    private String department;
    private String location;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private Integer minExperience;
    private Integer maxExperience;
    private String minSalary;
    private String maxSalary;
    private Set<String> requiredSkills;
    private Set<String> qualifications;
    private Set<String> responsibilities;
    private String educationRequirement;
    private String workplaceType;
    private boolean isRemoteAllowed;
    private LocalDateTime applicationDeadline;
    private Integer numberOfOpenings;
    private String noticePeriod;
    private String referralBonus;
    private String postedByUserName;
    private LocalDateTime postedDate;
    private LocalDateTime lastModifiedDate;
    private boolean isActive;
} 