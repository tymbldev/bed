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
    
    // Department details
    private Long departmentId;
    private String departmentName;
    
    // City details
    private Long cityId;
    private String cityName;
    
    // Country details
    private Long countryId;
    private String countryName;
    
    private String zipCode;
    private boolean isRemote;
    
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
    
    // Convenience methods
    public String getLocationDisplay() {
        if (isRemote) {
            return "Remote" + (countryName != null ? " - " + countryName : "");
        }
        
        StringBuilder location = new StringBuilder();
        
        if (cityName != null) {
            location.append(cityName);
        }
        
        if (countryName != null) {
            if (location.length() > 0) {
                location.append(", ");
            }
            location.append(countryName);
        }
        
        if (zipCode != null && !zipCode.isEmpty()) {
            location.append(" ").append(zipCode);
        }
        
        return location.length() > 0 ? location.toString() : null;
    }
} 