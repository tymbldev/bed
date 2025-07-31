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
    // All fields are optional for flexible search
    private List<String> keywords;
    private Long cityId;
    private Long countryId;
    private String cityName; // Optional: city name to map to cityId
    private String countryName; // Optional: country name to map to countryId
    private Long companyId; // Optional: filter by company
    private Long designationId; // Optional: filter by designation
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