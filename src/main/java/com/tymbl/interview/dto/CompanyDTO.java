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
@Schema(description = "Company information for interview preparation")
public class CompanyDTO {
    
    @Schema(description = "Company ID")
    private Long id;
    
    @Schema(description = "Company name")
    private String name;
    
    @Schema(description = "Company description")
    private String description;
    
    @Schema(description = "Company website URL")
    private String website;
    
    @Schema(description = "Company logo URL")
    private String logoUrl;
    
    @Schema(description = "List of available designations for interview preparation")
    private List<CompanyDesignationDTO> designations;
} 