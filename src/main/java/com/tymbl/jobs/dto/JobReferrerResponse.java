package com.tymbl.jobs.dto;

import lombok.Data;

@Data
public class JobReferrerResponse {
    private Long userId;
    private String userName;
    private String designation;
    private int numApplicationsAccepted;
    private double feedbackScore;
    private double overallScore;
} 