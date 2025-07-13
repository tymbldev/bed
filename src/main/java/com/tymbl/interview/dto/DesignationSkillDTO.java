package com.tymbl.interview.dto;

import com.tymbl.interview.entity.DesignationSkill;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationSkillDTO {
    
    private Long id;
    private String designation;
    private String skillName;
    private String skillDescription;
    private DesignationSkill.DifficultyLevel difficultyLevel;
    private String category;
    private Integer estimatedPrepTimeHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static DesignationSkillDTO fromEntity(DesignationSkill entity) {
        return DesignationSkillDTO.builder()
                .id(entity.getId())
                .designation(entity.getDesignation())
                .skillName(entity.getSkillName())
                .skillDescription(entity.getSkillDescription())
                .difficultyLevel(entity.getDifficultyLevel())
                .category(entity.getCategory())
                .estimatedPrepTimeHours(entity.getEstimatedPrepTimeHours())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public DesignationSkill toEntity() {
        return DesignationSkill.builder()
                .id(this.id)
                .designation(this.designation)
                .skillName(this.skillName)
                .skillDescription(this.skillDescription)
                .difficultyLevel(this.difficultyLevel)
                .category(this.category)
                .estimatedPrepTimeHours(this.estimatedPrepTimeHours)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
} 