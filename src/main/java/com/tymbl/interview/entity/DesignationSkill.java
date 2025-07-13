package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "designation_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "designation", nullable = false)
    private String designation;
    
    @Column(name = "skill_name", nullable = false)
    private String skillName;
    
    @Column(name = "skill_description", columnDefinition = "TEXT")
    private String skillDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "category")
    private String category;
    
    @Column(name = "estimated_prep_time_hours")
    private Integer estimatedPrepTimeHours;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DifficultyLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 