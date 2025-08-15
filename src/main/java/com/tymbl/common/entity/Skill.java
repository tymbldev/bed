package com.tymbl.common.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "skills")
public class Skill {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String category;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "usage_count")
  private Long usageCount = 0L;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "similar_skills_by_name", columnDefinition = "TEXT")
  private String similarSkillsByName;

  @Column(name = "similar_skills_by_id", columnDefinition = "TEXT")
  private String similarSkillsById;

  @Column(name = "similar_skills_processed", nullable = false)
  private boolean similarSkillsProcessed = false;

  @PrePersist
  protected void onCreate() {
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
} 