package com.tymbl.jobs.dto;

import lombok.Data;

@Data
public class ReferrerFeedbackRequest {

  private Long jobId;
  private Long referrerUserId;
  private String feedbackText;
  private Boolean gotCall;
  private Integer score;
} 