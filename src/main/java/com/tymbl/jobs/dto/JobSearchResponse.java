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
    
    private List<Map<String, Object>> jobs;
    
    private Long total;
    
    private Integer page;
    
    private Integer size;
    
    private Integer totalPages;
    
    public Integer getTotalPages() {
        if (size == null || size == 0) return 0;
        return (int) Math.ceil((double) total / size);
    }
} 