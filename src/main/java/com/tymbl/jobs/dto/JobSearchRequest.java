package com.tymbl.jobs.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.Min;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchRequest {
    
    private List<String> keywords;
    
    private Long cityId;
    
    private Long countryId;
    
    @Min(0)
    private Integer minExperience;
    
    @Min(0)
    private Integer maxExperience;
    
    @Builder.Default
    @Min(0)
    private Integer page = 0;
    
    @Builder.Default
    @Min(1)
    private Integer size = 20;
} 