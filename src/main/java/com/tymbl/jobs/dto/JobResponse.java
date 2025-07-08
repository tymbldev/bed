package com.tymbl.jobs.dto;

import com.tymbl.common.entity.JobApprovalStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private Long cityId;
    private Long countryId;
    private Long designationId;
    private String designation;
    private BigDecimal salary;
    private Long currencyId;
    private Long companyId;
    private String company;
    private Long postedBy;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags = new HashSet<>();
    private boolean isSuperAdminPosted;
    private Integer openingCount;
    private String uniqueUrl;
    private String platform;
    private Integer approved;
    private Integer referrerCount;
    
    // New fields for user role information
    private String userRole; // "POSTER" or "REFERRER"
    private Long actualPostedBy; // The actual poster's ID (same as postedBy if user is poster, different if user is referrer)
    
    // Helper method to get approval status string
    public String getApprovalStatus() {
        if (approved == null) return "PENDING";
        return JobApprovalStatus.fromValue(approved).name();
    }
} 