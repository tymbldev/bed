package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "topic_name")
    private String topicName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "LONGTEXT")
    private String answer;

    @Column(name = "summary_answer", columnDefinition = "TEXT")
    private String summaryAnswer;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @Column(name = "question_type")
    private String questionType;

    @Column(name = "tags")
    private String tags;

    @Column(name = "html_content", columnDefinition = "LONGTEXT")
    private String htmlContent;

    @Column(name = "code_examples", columnDefinition = "LONGTEXT")
    private String codeExamples;

    @Column(name = "java_code", columnDefinition = "LONGTEXT")
    private String javaCode;

    @Column(name = "python_code", columnDefinition = "LONGTEXT")
    private String pythonCode;

    @Column(name = "cpp_code", columnDefinition = "LONGTEXT")
    private String cppCode;

    @Column(name = "coding")
    private Boolean coding;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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