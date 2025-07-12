package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_interview_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInterviewQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "company_name", nullable = false)
    private String companyName;
    
    @Column(name = "designation", nullable = false)
    private String designation;
    
    @Column(name = "topic_name", nullable = false)
    private String topicName;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "answer_text", columnDefinition = "LONGTEXT", nullable = false)
    private String answerText;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type")
    private QuestionType questionType;
    
    @Column(name = "company_context", columnDefinition = "TEXT")
    private String companyContext;
    
    @Column(name = "tags")
    private String tags;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
    
    public enum QuestionType {
        THEORETICAL, PRACTICAL, BEHAVIORAL, PROBLEM_SOLVING, SYSTEM_DESIGN
    }
} 