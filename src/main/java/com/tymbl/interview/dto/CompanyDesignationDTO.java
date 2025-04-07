package com.tymbl.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Designation information within a company for interview preparation")
public class CompanyDesignationDTO {
    
    @Schema(description = "Designation ID")
    private Long id;
    
    @Schema(description = "Designation title")
    private String title;
    
    @Schema(description = "Designation level")
    private Integer level;
    
    @Schema(description = "Company ID")
    private Long companyId;
    
    @Schema(description = "Company name")
    private String companyName;
    
    @Schema(description = "List of skills associated with this designation at the company")
    private List<SkillDTO> skills;
} 