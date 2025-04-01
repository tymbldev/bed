package com.tymbl.jobs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class JobRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @NotBlank(message = "Experience level is required")
    private String experienceLevel;

    @NotNull(message = "Salary is required")
    private Double salary;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private String companyName;
} 