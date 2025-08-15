package com.tymbl.jobs.dto;

import com.tymbl.common.entity.JobApprovalStatus;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobApprovalRequest {

  @NotNull(message = "Job ID is required")
  private Long jobId;

  @NotNull(message = "Approval status is required")
  private JobApprovalStatus status;

  private String rejectionReason; // Optional reason for rejection
} 