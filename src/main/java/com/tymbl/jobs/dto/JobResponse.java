package com.tymbl.jobs.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
} 