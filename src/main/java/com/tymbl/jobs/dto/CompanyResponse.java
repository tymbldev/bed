package com.tymbl.jobs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {

  private Long id;
  private String name;
  private String description;
  private String website;
  private String logoUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String aboutUs;
  private String vision;
  private String mission;
  private String culture;
  private List<JobResponse> jobs;
  private String careerPageUrl;
  private String linkedinUrl;
  private String headquarters;
  private Long primaryIndustryId;
  private String primaryIndustryName; // Dropdown value for primaryIndustryId
  private String secondaryIndustries;
  private String companySize;
  private String specialties;
  private Integer jobCount;
} 