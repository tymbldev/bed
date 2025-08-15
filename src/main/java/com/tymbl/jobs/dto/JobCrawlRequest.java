package com.tymbl.jobs.dto;

import lombok.Data;

@Data
public class JobCrawlRequest {
    
    private String keyword;
    private String portalName;
    private Integer start = 0;
    private Integer limit = 20;
    private String countries = "India";
    private String queryDerived = "true";
    private String variantName = "embeddings512";
    
    // Additional portal-specific parameters
    private String additionalParams;
}
