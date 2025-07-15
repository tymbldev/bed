package com.tymbl.jobs.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String aboutUs;
    private String vision;
    private String mission;
    private String culture;
    private List<JobResponse> jobs;
    private String careerPageUrl;
    private String linkedinUrl;
    private String headquarters;
    private Long primaryIndustryId;
    private String secondaryIndustries;
    private String companySize;
    private String specialties;
} 