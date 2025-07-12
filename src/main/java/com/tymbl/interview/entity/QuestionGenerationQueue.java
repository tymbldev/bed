package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "question_generation_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionGenerationQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;
    
    @Column(name = "designation", nullable = false)
    private String designation;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "topic_name")
    private String topicName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "num_questions")
    private Integer numQuestions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RequestType {
        GENERAL, COMPANY_SPECIFIC
    }
    
    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
    
    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
} 