package com.tymbl.common.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@Table(name = "url_contents")
public class UrlContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String url;

  @Column(columnDefinition = "LONGTEXT")
  private String extractedText;

  @Column(name = "extraction_status")
  private String extractionStatus; // SUCCESS, FAILED, PENDING

  @Column(name = "error_message")
  private String errorMessage;

  @Column(name = "extracted_at")
  private LocalDateTime extractedAt;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
} 