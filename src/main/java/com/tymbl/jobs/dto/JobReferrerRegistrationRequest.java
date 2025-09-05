package com.tymbl.jobs.dto;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobReferrerRegistrationRequest {

  @NotNull(message = "Job IDs list is required")
  @NotEmpty(message = "At least one job ID is required")
  private List<Long> jobIds;
} 