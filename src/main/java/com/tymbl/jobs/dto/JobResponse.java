package com.tymbl.jobs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private Double salary;
    private String currency;
    private Long companyId;
    private String companyName;
    private Long postedBy;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 