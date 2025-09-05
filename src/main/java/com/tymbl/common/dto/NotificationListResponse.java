package com.tymbl.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

  private List<NotificationResponse> notifications;
  private int totalCount;
  private int page;
  private int size;
  private boolean hasNext;
  private boolean hasPrevious;
}
