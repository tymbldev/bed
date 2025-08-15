package com.tymbl.jobs.dto;

import lombok.Data;
import java.util.List;

@Data
public class JobCrawlResponse {
    
    private String status;
    private String message;
    private Long totalJobsFound;
    private Long rawResponseId;
    private Long processedJobsCount;
    private String portalName;
    private String keyword;
    private Long processingTimeMs;
    
    // For detailed response
    private List<JobDetailDTO> jobDetails;
    
    @Data
    public static class JobDetailDTO {
        private String portalJobId;
        private String jobTitle;
        private String companyName;
        private String companyId;
        private String locations;
        private Integer minExperience;
        private Integer maxExperience;
        private String salaryRange;
        private String jobDescription;
        private String skills;
        private String postedDate;
        private String status;
    }
}
