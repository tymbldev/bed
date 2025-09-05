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
public class IncompleteProfileNotification {

  private Long userId;
  private List<String> missingFields;
  private String message;
  private boolean hasIncompleteProfile;

  public static IncompleteProfileNotification create(Long userId, List<String> missingFields) {
    boolean hasIncomplete = missingFields != null && !missingFields.isEmpty();
    String message = hasIncomplete 
        ? String.format("Your profile is missing %d required field(s): %s", 
            missingFields.size(), String.join(", ", missingFields))
        : "Your profile is complete!";

    return IncompleteProfileNotification.builder()
        .userId(userId)
        .missingFields(missingFields)
        .message(message)
        .hasIncompleteProfile(hasIncomplete)
        .build();
  }
}
