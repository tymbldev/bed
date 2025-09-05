package com.tymbl.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountResponse {

  private Long userId;
  private Long totalCount;
  private Long newCount; // unseen notifications
  private Long seenCount;
  private Long clickedCount;
}
