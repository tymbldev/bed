package com.tymbl.jobs.dto;

import com.tymbl.common.entity.Job.JobType;
import com.tymbl.common.entity.Job.ExperienceLevel;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Future;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class JobPostRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Company is required")
    private String company;
    
    private Long departmentId;
    private Long cityId;
    private Long countryId;
    private String zipCode;
    private boolean isRemote;
    
    @NotNull(message = "Job type is required")
    private JobType jobType;
    
    @NotNull(message = "Experience level is required")
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
    private boolean remoteAllowed;
    
    @Future(message = "Application deadline must be in the future")
    private LocalDateTime applicationDeadline;
    
    private Integer numberOfOpenings;
    private String noticePeriod;
    private String referralBonus;
} 