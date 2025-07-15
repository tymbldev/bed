package com.tymbl.jobs.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchResponse {
    
    private List<JobResponse> jobs;
    
    private Long total;
    
    private Integer page;
    
    private Integer size;
    
    private Integer totalPages;

    /**
     * Map of companyId to company meta data for all unique companies in the jobs list.
     */
    private Map<Long, CompanyMetaData> companyMetaData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyMetaData {
        private String companyName;
        private String logoUrl;
        private String website;
        private String headquarters;
        private Long activeJobCount;
        private String secondaryIndustry;
        private String companySize;
        private String specialties;
        private String careerPageUrl;
    }

    public Integer getTotalPages() {
        if (size == null || size == 0) return 0;
        return (int) Math.ceil((double) total / size);
    }
} 