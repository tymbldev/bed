package com.tymbl.interview.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "general_interview_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralInterviewQuestion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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

  @Column(name = "tags")
  private String tags;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public enum DifficultyLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
  }

  public enum QuestionType {
    THEORETICAL,
    PRACTICAL,
    BEHAVIORAL,
    PROBLEM_SOLVING,
    SYSTEM_DESIGN
  }
} 