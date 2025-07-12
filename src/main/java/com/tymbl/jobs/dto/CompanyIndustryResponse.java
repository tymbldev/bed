package com.tymbl.jobs.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

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