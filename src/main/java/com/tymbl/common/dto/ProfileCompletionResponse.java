package com.tymbl.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProfileCompletionResponse {
    private int completionPercentage;
    private List<PendingField> pendingFields;
    private boolean canApply;
    private List<String> missingMandatoryFields;

    @Data
    public static class PendingField {
        private String fieldName;
        private String fieldLabel;
        private String description;
    }
} 