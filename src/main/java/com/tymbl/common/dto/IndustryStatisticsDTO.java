package com.tymbl.common.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryStatisticsDTO {
    private Long industryId;
    private String industryName;
    private String industryDescription;
    private Long companyCount;
    private List<TopCompanyDTO> topCompanies;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCompanyDTO {
        private Long companyId;
        private String companyName;
        private String logoUrl;
        private String website;
        private String headquarters;
        private Long activeJobCount;
    }
} 