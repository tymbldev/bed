package com.tymbl.interview.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGenerationRequestDTO {
    
    private String requestType; // GENERAL or COMPANY_SPECIFIC
    private String designation;
    private String companyName; // Optional, only for company-specific
    private String topicName; // Optional, if not provided will generate for all topics
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED
    private Integer numQuestions; // Number of questions to generate per topic
} 