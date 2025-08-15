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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
  @Column(name = "title", nullable = false)
  private String title;

  @NotNull
  @Column(name = "message", nullable = false, columnDefinition = "TEXT")
  private String message;

  @NotNull
  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(name = "related_entity_id")
  private Long relatedEntityId;

  @Column(name = "related_entity_type")
  @Enumerated(EnumType.STRING)
  private RelatedEntityType relatedEntityType;

  @NotNull
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @NotNull
  @Column(name = "is_sent", nullable = false)
  private Boolean isSent = false;

  @NotNull
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @Column(name = "firebase_token")
  private String firebaseToken;

  @Column(name = "error_message")
  private String errorMessage;

  public enum NotificationType {
    REFERRAL_APPLICATION,
    APPLICATION_SHORTLISTED,
    APPLICATION_REJECTED,
    APPLICATION_ACCEPTED,
    REFERRAL_APPROVED,
    REFERRAL_REJECTED,
    GENERAL
  }

  public enum RelatedEntityType {
    JOB_APPLICATION,
    JOB_REFERRAL,
    JOB,
    USER
  }

  public Notification(Long userId, String title, String message, NotificationType type) {
    this.userId = userId;
    this.title = title;
    this.message = message;
    this.type = type;
    this.createdAt = LocalDateTime.now();
    this.isRead = false;
    this.isSent = false;
  }

  public Notification(Long userId, String title, String message, NotificationType type,
      Long relatedEntityId, RelatedEntityType relatedEntityType) {
    this(userId, title, message, type);
    this.relatedEntityId = relatedEntityId;
    this.relatedEntityType = relatedEntityType;
  }
} 