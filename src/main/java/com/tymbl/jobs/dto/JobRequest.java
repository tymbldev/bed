package com.tymbl.jobs.dto;

import com.tymbl.common.entity.Job.JobType;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JobRequest {

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  @NotNull(message = "City ID is required")
  private Long cityId;

  @NotNull(message = "Country ID is required")
  private Long countryId;

  @NotNull(message = "Designation ID is required")
  private Long designationId;

  private String designation;

  @NotNull(message = "Minimum salary is required")
  private BigDecimal minSalary;

  @NotNull(message = "Maximum salary is required")
  private BigDecimal maxSalary;

  private Integer minExperience;

  private Integer maxExperience;

  private JobType jobType;

  @NotNull(message = "Currency ID is required")
  private Long currencyId;

  @NotNull(message = "Company ID is required")
  private Long companyId;

  private String company;

  private Set<String> tags = new HashSet<>();

  private Integer openingCount = 1;

  private String uniqueUrl;
  private String platform;
} 