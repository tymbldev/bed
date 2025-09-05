package com.tymbl.jobs.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipleJobReferrerRegistrationResponse {

  private List<JobReferrerRegistrationResult> results;
  private int totalJobs;
  private int successfulRegistrations;
  private int failedRegistrations;
  private String overallMessage;
}
