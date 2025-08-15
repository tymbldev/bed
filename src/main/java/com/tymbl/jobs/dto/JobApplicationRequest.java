package com.tymbl.jobs.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApplicationRequest {

  @NotNull(message = "Job ID is required")
  private Long jobId;

  @NotBlank(message = "Cover letter is required")
  private String coverLetter;

  @NotNull(message = "Job Referrer ID is required")
  private Long jobReferrerId;
} 