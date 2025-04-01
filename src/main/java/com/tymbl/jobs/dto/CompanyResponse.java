package com.tymbl.jobs.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String website;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 