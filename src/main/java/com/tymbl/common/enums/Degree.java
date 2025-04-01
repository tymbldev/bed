package com.tymbl.common.enums;

public enum Degree {
    BACHELOR_OF_ARTS("Bachelor of Arts"),
    BACHELOR_OF_SCIENCE("Bachelor of Science"),
    BACHELOR_OF_COMMERCE("Bachelor of Commerce"),
    BACHELOR_OF_ENGINEERING("Bachelor of Engineering"),
    BACHELOR_OF_TECHNOLOGY("Bachelor of Technology"),
    BACHELOR_OF_BUSINESS_ADMINISTRATION("Bachelor of Business Administration"),
    MASTER_OF_ARTS("Master of Arts"),
    MASTER_OF_SCIENCE("Master of Science"),
    MASTER_OF_BUSINESS_ADMINISTRATION("Master of Business Administration"),
    MASTER_OF_ENGINEERING("Master of Engineering"),
    MASTER_OF_TECHNOLOGY("Master of Technology"),
    DOCTOR_OF_PHILOSOPHY("Doctor of Philosophy"),
    OTHER("Other");

    private final String displayName;

    Degree(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 