package com.tymbl.jobs.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobReferrerRegistrationRequest {

  @NotNull(message = "Job ID is required")
  private Long jobId;
} 