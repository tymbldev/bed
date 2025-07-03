package com.tymbl.common.entity;

public enum JobApprovalStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);
    
    private final int value;
    
    JobApprovalStatus(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
    
    public static JobApprovalStatus fromValue(int value) {
        for (JobApprovalStatus status : JobApprovalStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid approval status value: " + value);
    }
} 