package com.tymbl.common.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "similar_content")
@Data
public class SimilarContent {

  public enum ContentType {
    COMPANY,
    DESIGNATION,
    CITY,
    COUNTRY
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "parent_name", nullable = false, length = 500)
  private String parentName;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ContentType type;

  @Column(name = "similar_name", nullable = false, length = 500)
  private String similarName;

  @Column(name = "confidence_score", precision = 3, scale = 2)
  private BigDecimal confidenceScore = new BigDecimal("0.80");

  @Column(name = "source", length = 100)
  private String source = "AI_MAPPING";

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
