package com.tymbl.jobs.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class JobApplicationRequest {
    
    @NotNull(message = "Job ID is required")
    private Long jobId;
    
    @NotBlank(message = "Cover letter is required")
    private String coverLetter;
    
    private String resumeUrl;
} 