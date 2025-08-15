package com.tymbl.common.controller;

import com.tymbl.common.dto.NotificationDTO;
import com.tymbl.common.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "APIs for managing user notifications")
public class NotificationController {

  private final NotificationService notificationService;

  /**
   * Get recent notifications (last 7 days) for a user
   */
  @GetMapping("/recent/{userId}")
  @Operation(summary = "Get recent notifications", description = "Returns notifications for a user in the last 7 days")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Recent notifications retrieved successfully",
          content = @Content(
              schema = @Schema(implementation = NotificationDTO.class),
              examples = @ExampleObject(
                  value = "[\n" +
                      "  {\n" +
                      "    \"id\": 1,\n" +
                      "    \"userId\": 123,\n" +
                      "    \"title\": \"New Referral Application\",\n" +
                      "    \"message\": \"Someone applied for the referral you posted for Software Engineer position at Google\",\n"
                      +
                      "    \"type\": \"REFERRAL_APPLICATION\",\n" +
                      "    \"isRead\": false,\n" +
                      "    \"isSent\": true,\n" +
                      "    \"createdAt\": \"2024-01-15T10:30:00\",\n" +
                      "    \"timeAgo\": \"2 hours ago\",\n" +
                      "    \"formattedDate\": \"Jan 15, 2024 at 10:30\"\n" +
                      "  },\n" +
                      "  {\n" +
                      "    \"id\": 2,\n" +
                      "    \"userId\": 123,\n" +
                      "    \"title\": \"Application Shortlisted!\",\n" +
                      "    \"message\": \"Congratulations! Your application for Senior Developer position at Microsoft has been shortlisted.\",\n"
                      +
                      "    \"type\": \"APPLICATION_SHORTLISTED\",\n" +
                      "    \"isRead\": true,\n" +
                      "    \"isSent\": true,\n" +
                      "    \"createdAt\": \"2024-01-14T15:45:00\",\n" +
                      "    \"timeAgo\": \"1 day ago\",\n" +
                      "    \"formattedDate\": \"Jan 14, 2024 at 15:45\"\n" +
                      "  }\n" +
                      "]"
              )
          )
      )
  })
  public ResponseEntity<List<NotificationDTO>> getRecentNotifications(@PathVariable Long userId) {
    List<NotificationDTO> notifications = notificationService.getRecentNotifications(userId);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Get all notifications for a user
   */
  @GetMapping("/all/{userId}")
  @Operation(summary = "Get all notifications", description = "Returns all notifications for a user")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "All notifications retrieved successfully"
      )
  })
  public ResponseEntity<List<NotificationDTO>> getAllNotifications(@PathVariable Long userId) {
    List<NotificationDTO> notifications = notificationService.getAllNotifications(userId);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Get unread notifications for a user
   */
  @GetMapping("/unread/{userId}")
  @Operation(summary = "Get unread notifications", description = "Returns unread notifications for a user")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Unread notifications retrieved successfully"
      )
  })
  public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
    List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId);
    return ResponseEntity.ok(notifications);
  }

  /**
   * Get unread notification count for a user
   */
  @GetMapping("/unread-count/{userId}")
  @Operation(summary = "Get unread notification count", description = "Returns the count of unread notifications for a user")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Unread count retrieved successfully",
          content = @Content(
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"userId\": 123,\n" +
                      "  \"unreadCount\": 5\n" +
                      "}"
              )
          )
      )
  })
  public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
    long unreadCount = notificationService.getUnreadCount(userId);
    Map<String, Object> response = new HashMap<>();
    response.put("userId", userId);
    response.put("unreadCount", unreadCount);
    return ResponseEntity.ok(response);
  }

  /**
   * Mark a notification as read
   */
  @PutMapping("/mark-read/{notificationId}")
  @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notification marked as read successfully"
      ),
      @ApiResponse(responseCode = "404", description = "Notification not found")
  })
  public ResponseEntity<Map<String, String>> markAsRead(
      @PathVariable Long notificationId,
      @RequestParam Long userId) {

    notificationService.markAsRead(notificationId, userId);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Notification marked as read successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Mark all notifications as read for a user
   */
  @PutMapping("/mark-all-read/{userId}")
  @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for a user")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "All notifications marked as read successfully"
      )
  })
  public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
    notificationService.markAllAsRead(userId);
    Map<String, String> response = new HashMap<>();
    response.put("message", "All notifications marked as read successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Delete a notification
   */
  @DeleteMapping("/{notificationId}")
  @Operation(summary = "Delete notification", description = "Deletes a specific notification")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notification deleted successfully"
      ),
      @ApiResponse(responseCode = "404", description = "Notification not found")
  })
  public ResponseEntity<Map<String, String>> deleteNotification(
      @PathVariable Long notificationId,
      @RequestParam Long userId) {

    notificationService.deleteNotification(notificationId, userId);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Notification deleted successfully");
    return ResponseEntity.ok(response);
  }

  /**
   * Get notifications by type for a user
   */
  @GetMapping("/type/{userId}")
  @Operation(summary = "Get notifications by type", description = "Returns notifications of a specific type for a user")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notifications retrieved successfully"
      )
  })
  public ResponseEntity<List<NotificationDTO>> getNotificationsByType(
      @PathVariable Long userId,
      @RequestParam String type) {

    // Convert string to enum
    try {
      com.tymbl.common.entity.Notification.NotificationType notificationType =
          com.tymbl.common.entity.Notification.NotificationType.valueOf(type.toUpperCase());

      List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId)
          .stream()
          .filter(n -> n.getType() == notificationType)
          .collect(Collectors.toList());

      return ResponseEntity.ok(notifications);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }
} 