package com.tymbl.jobs.dto;

import com.tymbl.common.entity.Job.JobType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class JobDetailsWithReferrersResponse {
    // Job details
    private Long id;
    private String title;
    private String description;
    private Long cityId;
    private Long countryId;
    private Long designationId;
    private String designation;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private Integer minExperience;
    private Integer maxExperience;
    private JobType jobType;
    private Long currencyId;
    private Long companyId;
    private String company;
    private Long postedBy;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags;
    private int openingCount;
    private String uniqueUrl;
    private String platform;
    private int approved;
    private String approvalStatus;
    private int referrerCount;
    
    // Referrer details
    private List<JobReferrerWithProfileResponse> referrers;
    
    @Data
    public static class JobReferrerWithProfileResponse {
        private Long userId;
        private String userName;
        private String email;
        private String designation;
        private String company;
        private Long companyId;
        private String yearsOfExperience;
        private String monthsOfExperience;
        private String education;
        private String portfolioWebsite;
        private String linkedInProfile;
        private String githubProfile;
        private int numApplicationsAccepted;
        private double feedbackScore;
        private double overallScore;
        private LocalDateTime registeredAt;
    }
} 