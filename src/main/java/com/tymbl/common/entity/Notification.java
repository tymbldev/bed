package com.tymbl.common.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @NotNull
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @NotNull
  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  private String message;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata; // JSON string for additional data

  @NotNull
  @Column(name = "seen", nullable = false)
  @Builder.Default
  private Boolean seen = false;

  @NotNull
  @Column(name = "clicked", nullable = false)
  @Builder.Default
  private Boolean clicked = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "seen_at")
  private LocalDateTime seenAt;

  @Column(name = "clicked_at")
  private LocalDateTime clickedAt;

  // Related entity information for context
  @Column(name = "related_entity_id")
  private Long relatedEntityId;

  @Column(name = "related_entity_type")
  private String relatedEntityType; // e.g., "job", "application", "company"

  // Additional fields for duplicate checking
  @Column(name = "company_id")
  private Long companyId;

  @Column(name = "designation")
  private String designation;

  public enum NotificationType {
    INCOMPLETE_PROFILE("incomplete_profile"),
    COMPANY_JOBS("company_jobs"),
    APPLICATION_STATUS("application_status"),
    POSTED_JOB_APPLICATIONS("posted_job_applications");

    private final String value;

    NotificationType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
