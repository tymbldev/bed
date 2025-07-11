package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "designation", nullable = false)
    private String designation;
    
    @Column(name = "topic_name", nullable = false)
    private String topicName;
    
    @Column(name = "topic_description", columnDefinition = "TEXT")
    private String topicDescription;
    
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
        BEGINNER, INTERMEDIATE, ADVANCED
    }
} 