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
public class CompanyIndustryResponse {

  private Long companyId;
  private String companyName;
  private String primaryIndustry;
  private Long primaryIndustryId;
  private List<String> secondaryIndustries;
  private boolean processed;
  private String error;
} 