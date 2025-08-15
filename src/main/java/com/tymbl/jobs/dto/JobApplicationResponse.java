package com.tymbl.jobs.dto;

import com.tymbl.jobs.entity.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class JobApplicationResponse {

  private Long id;
  private Long jobId;
  private String jobTitle;
  private Long applicantId;
  private String applicantName;
  private ApplicationStatus status;
  private LocalDateTime createdAt;
  private Long jobReferrerId;
  private SudoIdentityDTO referrerSudoIdentity;
} 