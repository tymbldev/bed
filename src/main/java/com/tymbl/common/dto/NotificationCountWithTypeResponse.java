package com.tymbl.common.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountWithTypeResponse {

  private Long userId;
  private Long totalCount;
  private Long newCount; // unseen notifications
  private Long seenCount;
  private Long clickedCount;
  
  // Count breakdown by notification type
  private Map<String, NotificationTypeCount> countsByType;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NotificationTypeCount {
    private Long totalCount;
    private Long newCount;
    private Long seenCount;
    private Long clickedCount;
  }
}
