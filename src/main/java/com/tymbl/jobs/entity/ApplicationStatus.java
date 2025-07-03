package com.tymbl.jobs.entity;

/**
 * Enum representing the status of a job application.
 * This should be kept in sync with the status values in JobApplication.
 */
public enum ApplicationStatus {
    /**
     * Application has been submitted but not yet reviewed
     */
    PENDING,
    
    /**
     * Application has been shortlisted for interview
     */
    SHORTLISTED,
    
    /**
     * Application has been rejected
     */
    REJECTED
} 