package com.tymbl.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
public class IndustryWiseCompaniesDTO {
    private Long industryId;
    private String industryName; // Dropdown value for industryId
    private String industryDescription;
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