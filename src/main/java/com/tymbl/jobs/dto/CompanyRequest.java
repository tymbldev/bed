package com.tymbl.jobs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CompanyRequest {
    @NotNull(message = "Company ID is required")
    private Long id;

    @NotBlank(message = "Company name is required")
    private String name;

    private String description;
    private String website;
    private String logoUrl;
    private String aboutUs;
    private String vision;
    private String mission;
    private String culture;
} 