package com.tymbl.common.dto;

import java.util.List;
import lombok.Data;

@Data
public class IndustryWiseCompaniesDTO {

  private Long industryId;
  private String industryName; // Dropdown value for industryId
  private String industryDescription;
  private Integer rankOrder; // New field for industry ranking

  private Integer companyCount;
  private List<TopCompanyDTO> topCompanies;

  @Data
  public static class TopCompanyDTO {

    private Long companyId;
    private String companyName;
    private String logoUrl;
    private String website;
    private String headquarters;
    private Integer activeJobCount;
  }
} 