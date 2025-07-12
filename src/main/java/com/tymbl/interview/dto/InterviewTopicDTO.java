package com.tymbl.interview.dto;

import com.tymbl.interview.entity.InterviewTopic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewTopicDTO {
    
    private Long id;
    private String designation;
    private String topicName;
    private String topicDescription;
    private InterviewTopic.DifficultyLevel difficultyLevel;
    private String category;
    private Integer estimatedPrepTimeHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static InterviewTopicDTO fromEntity(InterviewTopic entity) {
        return InterviewTopicDTO.builder()
                .id(entity.getId())
                .designation(entity.getDesignation())
                .topicName(entity.getTopicName())
                .topicDescription(entity.getTopicDescription())
                .difficultyLevel(entity.getDifficultyLevel())
                .category(entity.getCategory())
                .estimatedPrepTimeHours(entity.getEstimatedPrepTimeHours())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
} 