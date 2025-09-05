package com.tymbl.jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobReferrerRegistrationResult {

  private Long jobId;
  private boolean success;
  private String message;
  private String errorCode; // For specific error types like ALREADY_REGISTERED, JOB_NOT_FOUND, etc.
}
