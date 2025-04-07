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
@Schema(description = "Skill information for interview preparation")
public class SkillDTO {
    
    @Schema(description = "Skill ID")
    private Long id;
    
    @Schema(description = "Skill name")
    private String name;
    
    @Schema(description = "Skill description")
    private String description;
    
    @Schema(description = "Skill category")
    private String category;
    
    @Schema(description = "Importance level of this skill for the specific company-designation combination")
    private String importanceLevel;
} 