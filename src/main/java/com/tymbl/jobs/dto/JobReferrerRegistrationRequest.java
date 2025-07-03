package com.tymbl.jobs.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class JobReferrerRegistrationRequest {
    @NotNull(message = "Job ID is required")
    private Long jobId;
} 