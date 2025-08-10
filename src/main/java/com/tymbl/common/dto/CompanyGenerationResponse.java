package com.tymbl.common.dto;

import com.tymbl.jobs.entity.Company;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyGenerationResponse {
    private Company company;
    private boolean success;
    private String errorMessage;
} 