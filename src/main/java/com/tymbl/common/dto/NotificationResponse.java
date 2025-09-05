package com.tymbl.common.dto;

import com.tymbl.common.entity.Notification;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long notificationId;
  private String type;
  private String message;
  private String metadata;
  private Boolean seen;
  private Boolean clicked;
  private LocalDateTime createdAt;
  private LocalDateTime seenAt;
  private LocalDateTime clickedAt;
  private Long relatedEntityId;
  private String relatedEntityType;

  public static NotificationResponse fromEntity(Notification notification) {
    return NotificationResponse.builder()
        .notificationId(notification.getId())
        .type(notification.getType().getValue())
        .message(notification.getMessage())
        .metadata(notification.getMetadata())
        .seen(notification.getSeen())
        .clicked(notification.getClicked())
        .createdAt(notification.getCreatedAt())
        .seenAt(notification.getSeenAt())
        .clickedAt(notification.getClickedAt())
        .relatedEntityId(notification.getRelatedEntityId())
        .relatedEntityType(notification.getRelatedEntityType())
        .build();
  }
}
