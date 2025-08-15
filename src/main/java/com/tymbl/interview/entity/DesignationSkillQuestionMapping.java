package com.tymbl.interview.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "designation_skill_question_mappings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"designation_id", "skill_id",
        "question_id"}))
public class DesignationSkillQuestionMapping {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "designation_id", nullable = false)
  private Long designationId;

  @Column(name = "designation_name", nullable = false)
  private String designationName;

  @Column(name = "skill_id", nullable = false)
  private Long skillId;

  @Column(name = "skill_name", nullable = false)
  private String skillName;

  @Column(name = "question_id", nullable = false)
  private Long questionId;

  @Column(name = "relevance_score")
  private Double relevanceScore;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
} 