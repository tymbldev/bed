package com.tymbl.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Interview topic information for a specific company-designation-skill combination")
public class InterviewTopicDTO {
    
    @Schema(description = "Topic ID")
    private Long id;
    
    @Schema(description = "Topic title")
    private String title;
    
    @Schema(description = "Topic description")
    private String description;
    
    @Schema(description = "Topic content in HTML format")
    private String content;
    
    @Schema(description = "Difficulty level")
    private String difficultyLevel;
    
    @Schema(description = "Company name")
    private String companyName;
    
    @Schema(description = "Designation title")
    private String designationTitle;
    
    @Schema(description = "Skill name")
    private String skillName;
    
    @Schema(description = "List of interview questions related to this topic")
    private List<InterviewQuestionDTO> questions;
} 