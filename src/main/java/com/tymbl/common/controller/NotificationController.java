package com.tymbl.common.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.dto.NotificationCountWithTypeResponse;
import com.tymbl.common.dto.NotificationListResponse;
import com.tymbl.common.entity.User;
import com.tymbl.common.service.NotificationService;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "APIs for managing user notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final JwtService jwtService;
  private final RegistrationService registrationService;

  /**
   * Get notification count for a user
   */
  @GetMapping("/count")
  @Operation(
      summary = "Get notification count with type breakdown",
      description = "Returns total count, new count (unseen), and count breakdown by notification type for the authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notification count retrieved successfully",
          content = @Content(
              schema = @Schema(implementation = NotificationCountWithTypeResponse.class),
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"userId\": 123,\n" +
                      "  \"totalCount\": 15,\n" +
                      "  \"newCount\": 5,\n" +
                      "  \"seenCount\": 8,\n" +
                      "  \"clickedCount\": 2,\n" +
                      "  \"countsByType\": {\n" +
                      "    \"company_jobs\": {\n" +
                      "      \"totalCount\": 3,\n" +
                      "      \"newCount\": 1,\n" +
                      "      \"seenCount\": 2,\n" +
                      "      \"clickedCount\": 0\n" +
                      "    },\n" +
                      "    \"application_status\": {\n" +
                      "      \"totalCount\": 8,\n" +
                      "      \"newCount\": 3,\n" +
                      "      \"seenCount\": 4,\n" +
                      "      \"clickedCount\": 1\n" +
                      "    },\n" +
                      "    \"posted_job_applications\": {\n" +
                      "      \"totalCount\": 4,\n" +
                      "      \"newCount\": 1,\n" +
                      "      \"seenCount\": 2,\n" +
                      "      \"clickedCount\": 1\n" +
                      "    }\n" +
                      "  }\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<NotificationCountWithTypeResponse> getNotificationCount(
      @RequestHeader("Authorization") String token) {
    
    String email = jwtService.extractUsername(token.substring(7));
    User currentUser = registrationService.getUserByEmail(email);
    Long userId = currentUser.getId();
    
    log.info("Getting notification count with type breakdown for user: {}", userId);
    NotificationCountWithTypeResponse response = notificationService.getNotificationCountWithType(userId);
    return ResponseEntity.ok(response);
  }

  /**
   * Get notifications list with pagination
   */
  @GetMapping("/list")
  @Operation(
      summary = "Get notifications list",
      description = "Returns paginated list of all notification types for the authenticated user with type mentioned in response"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notifications list retrieved successfully",
          content = @Content(
              schema = @Schema(implementation = NotificationListResponse.class),
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"notifications\": [\n" +
                      "    {\n" +
                      "      \"notificationId\": 1,\n" +
                      "      \"type\": \"company_jobs\",\n" +
                      "      \"message\": \"100+ jobs available in Microsoft, act as a referrer.\",\n" +
                      "      \"seen\": false,\n" +
                      "      \"clicked\": false,\n" +
                      "      \"createdAt\": \"2024-01-15T10:30:00\"\n" +
                      "    },\n" +
                      "    {\n" +
                      "      \"notificationId\": 2,\n" +
                      "      \"type\": \"application_status\",\n" +
                      "      \"message\": \"Your application status has been updated.\",\n" +
                      "      \"seen\": true,\n" +
                      "      \"clicked\": false,\n" +
                      "      \"createdAt\": \"2024-01-15T09:15:00\"\n" +
                      "    }\n" +
                      "  ],\n" +
                      "  \"totalCount\": 15,\n" +
                      "  \"page\": 0,\n" +
                      "  \"size\": 10,\n" +
                      "  \"hasNext\": true,\n" +
                      "  \"hasPrevious\": false\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<NotificationListResponse> getNotifications(
      @RequestHeader("Authorization") String token,
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size", example = "10")
      @RequestParam(defaultValue = "10") int size) {
    
    String email = jwtService.extractUsername(token.substring(7));
    User currentUser = registrationService.getUserByEmail(email);
    Long userId = currentUser.getId();
    
    log.info("Getting all notifications for user: {}, page: {}, size: {}", userId, page, size);
    NotificationListResponse response = notificationService.getNotifications(userId, page, size);
    return ResponseEntity.ok(response);
  }



  /**
   * Mark notifications as seen
   */
  @PutMapping("/mark-seen")
  @Operation(
      summary = "Mark notifications as seen",
      description = "Marks multiple notifications as seen when user opens notification tray"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notifications marked as seen successfully"
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<Map<String, Object>> markAsSeen(
      @RequestHeader("Authorization") String token,
      @Parameter(description = "List of notification IDs", required = true)
      @RequestParam List<Long> notificationIds) {
    
    String email = jwtService.extractUsername(token.substring(7));
    User currentUser = registrationService.getUserByEmail(email);
    Long userId = currentUser.getId();
    
    log.info("Marking {} notifications as seen for user: {}", notificationIds.size(), userId);
    int updatedCount = notificationService.markAsSeen(notificationIds, userId);
    
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("updatedCount", updatedCount);
    response.put("requestedCount", notificationIds.size());
    response.put("message", String.format("Marked %d out of %d notifications as seen", updatedCount, notificationIds.size()));
    
    return ResponseEntity.ok(response);
  }

  /**
   * Mark all notifications as seen
   */
  @PutMapping("/mark-all-seen")
  @Operation(
      summary = "Mark all notifications as seen",
      description = "Marks all unseen notifications as seen for the authenticated user"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "All notifications marked as seen successfully"
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<Map<String, Object>> markAllAsSeen(
      @RequestHeader("Authorization") String token) {
    
    String email = jwtService.extractUsername(token.substring(7));
    User currentUser = registrationService.getUserByEmail(email);
    Long userId = currentUser.getId();
    
    log.info("Marking all notifications as seen for user: {}", userId);
    int updatedCount = notificationService.markAllAsSeen(userId);
    
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("updatedCount", updatedCount);
    response.put("message", String.format("Marked %d notifications as seen", updatedCount));
    
    return ResponseEntity.ok(response);
  }

  /**
   * Mark notifications as clicked
   */
  @PutMapping("/mark-clicked")
  @Operation(
      summary = "Mark notifications as clicked",
      description = "Marks multiple notifications as clicked when user explicitly clicks them"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notifications marked as clicked successfully"
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  public ResponseEntity<Map<String, Object>> markAsClicked(
      @RequestHeader("Authorization") String token,
      @Parameter(description = "List of notification IDs", required = true)
      @RequestParam List<Long> notificationIds) {
    
    String email = jwtService.extractUsername(token.substring(7));
    User currentUser = registrationService.getUserByEmail(email);
    Long userId = currentUser.getId();
    
    log.info("Marking {} notifications as clicked for user: {}", notificationIds.size(), userId);
    int updatedCount = notificationService.markAsClicked(notificationIds, userId);
    
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("updatedCount", updatedCount);
    response.put("requestedCount", notificationIds.size());
    response.put("message", String.format("Marked %d out of %d notifications as clicked", updatedCount, notificationIds.size()));
    return ResponseEntity.ok(response);
  }
}
