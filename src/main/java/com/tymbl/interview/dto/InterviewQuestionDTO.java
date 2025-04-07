package com.tymbl.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview question information")
public class InterviewQuestionDTO {
    
    @Schema(description = "Question ID")
    private Long id;
    
    @Schema(description = "Topic ID")
    private Long topicId;
    
    @Schema(description = "Topic title")
    private String topicTitle;
    
    @Schema(description = "Question text in HTML format")
    private String question;
    
    @Schema(description = "Answer text in HTML format")
    private String answer;
    
    @Schema(description = "Difficulty level")
    private String difficultyLevel;
} 