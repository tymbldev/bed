package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Notification;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

  @Value("${firebase.server.key:}")
  private String firebaseServerKey;

  @Value("${firebase.api.url:https://fcm.googleapis.com/fcm/send}")
  private String firebaseApiUrl;

  @Value("${firebase.notifications.enabled:false}")
  private boolean notificationsEnabled;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  /**
   * Send push notification to a specific device
   */
  public boolean sendNotificationToDevice(String deviceToken, String title, String message,
      Map<String, Object> data) {
    if (!notificationsEnabled) {
      log.info("Firebase notifications are disabled. Skipping notification to device: {}",
          deviceToken);
      return true; // Return true to indicate "success" when disabled
    }

    try {
      Map<String, Object> notification = new HashMap<>();
      notification.put("title", title);
      notification.put("body", message);
      notification.put("sound", "default");
      notification.put("badge", "1");

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("to", deviceToken);
      requestBody.put("notification", notification);
      requestBody.put("data", data);
      requestBody.put("priority", "high");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "key=" + firebaseServerKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl, request,
          String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        boolean success = responseNode.path("success").asInt() > 0;

        if (success) {
          log.info("Successfully sent notification to device: {}", deviceToken);
          return true;
        } else {
          log.error("Failed to send notification to device: {}. Response: {}", deviceToken,
              response.getBody());
          return false;
        }
      } else {
        log.error("Firebase API error: {} - {}", response.getStatusCode(), response.getBody());
        return false;
      }
    } catch (Exception e) {
      log.error("Error sending notification to device: {}", deviceToken, e);
      return false;
    }
  }

  /**
   * Send notification to multiple devices
   */
  public boolean sendNotificationToMultipleDevices(String[] deviceTokens, String title,
      String message, Map<String, Object> data) {
    if (!notificationsEnabled) {
      log.info("Firebase notifications are disabled. Skipping notification to {} devices",
          deviceTokens.length);
      return true; // Return true to indicate "success" when disabled
    }

    try {
      Map<String, Object> notification = new HashMap<>();
      notification.put("title", title);
      notification.put("body", message);
      notification.put("sound", "default");
      notification.put("badge", "1");

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("registration_ids", deviceTokens);
      requestBody.put("notification", notification);
      requestBody.put("data", data);
      requestBody.put("priority", "high");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "key=" + firebaseServerKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl, request,
          String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        int successCount = responseNode.path("success").asInt();
        int failureCount = responseNode.path("failure").asInt();

        log.info("Sent notification to {} devices. Success: {}, Failure: {}",
            deviceTokens.length, successCount, failureCount);

        return successCount > 0;
      } else {
        log.error("Firebase API error: {} - {}", response.getStatusCode(), response.getBody());
        return false;
      }
    } catch (Exception e) {
      log.error("Error sending notification to multiple devices", e);
      return false;
    }
  }

  /**
   * Send notification to a topic
   */
  public boolean sendNotificationToTopic(String topic, String title, String message,
      Map<String, Object> data) {
    if (!notificationsEnabled) {
      log.info("Firebase notifications are disabled. Skipping notification to topic: {}", topic);
      return true; // Return true to indicate "success" when disabled
    }

    try {
      Map<String, Object> notification = new HashMap<>();
      notification.put("title", title);
      notification.put("body", message);
      notification.put("sound", "default");
      notification.put("badge", "1");

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("to", "/topics/" + topic);
      requestBody.put("notification", notification);
      requestBody.put("data", data);
      requestBody.put("priority", "high");

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "key=" + firebaseServerKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(firebaseApiUrl, request,
          String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        JsonNode responseNode = objectMapper.readTree(response.getBody());
        boolean success = responseNode.path("success").asInt() > 0;

        if (success) {
          log.info("Successfully sent notification to topic: {}", topic);
          return true;
        } else {
          log.error("Failed to send notification to topic: {}. Response: {}", topic,
              response.getBody());
          return false;
        }
      } else {
        log.error("Firebase API error: {} - {}", response.getStatusCode(), response.getBody());
        return false;
      }
    } catch (Exception e) {
      log.error("Error sending notification to topic: {}", topic, e);
      return false;
    }
  }

  /**
   * Send notification for a specific notification entity
   */
  public boolean sendNotification(Notification notification, String deviceToken) {
    if (!notificationsEnabled) {
      log.info("Firebase notifications are disabled. Skipping notification for entity: {}",
          notification.getId());
      return true; // Return true to indicate "success" when disabled
    }

    try {
      Map<String, Object> data = new HashMap<>();
      data.put("notificationId", notification.getId().toString());
      data.put("type", notification.getType().toString());
      data.put("relatedEntityId",
          notification.getRelatedEntityId() != null ? notification.getRelatedEntityId().toString()
              : "");
      data.put("relatedEntityType",
          notification.getRelatedEntityType() != null ? notification.getRelatedEntityType()
              .toString() : "");
      data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

      return sendNotificationToDevice(deviceToken, notification.getTitle(),
          notification.getMessage(), data);
    } catch (Exception e) {
      log.error("Error sending notification for entity: {}", notification.getId(), e);
      return false;
    }
  }

  /**
   * Check if Firebase notifications are enabled
   */
  public boolean isNotificationsEnabled() {
    return notificationsEnabled;
  }

  /**
   * Get the current notification status
   */
  public String getNotificationStatus() {
    return notificationsEnabled ? "ENABLED" : "DISABLED";
  }
} 