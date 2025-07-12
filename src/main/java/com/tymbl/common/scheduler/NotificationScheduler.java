package com.tymbl.common.scheduler;

import com.tymbl.common.entity.Notification;
import com.tymbl.common.service.FirebaseNotificationService;
import com.tymbl.common.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    
    private final NotificationService notificationService;
    private final FirebaseNotificationService firebaseNotificationService;
    
    /**
     * Scheduled task to send pending notifications every 2 minutes
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void sendPendingNotifications() {
        try {
            log.info("Starting notification scheduler task");
            
            List<Notification> pendingNotifications = notificationService.getPendingNotifications();
            
            if (pendingNotifications.isEmpty()) {
                log.debug("No pending notifications to send");
                return;
            }
            
            log.info("Found {} pending notifications to send", pendingNotifications.size());
            
            for (Notification notification : pendingNotifications) {
                try {
                    // For now, we'll use a default device token
                    // In a real implementation, you would get the user's device token from a user settings table
                    String deviceToken = getDeviceTokenForUser(notification.getUserId());
                    
                    if (deviceToken != null && !deviceToken.trim().isEmpty()) {
                        boolean sent = firebaseNotificationService.sendNotification(notification, deviceToken);
                        
                        if (sent) {
                            notificationService.markAsSent(notification.getId());
                            log.info("Successfully sent notification {} to user {}", notification.getId(), notification.getUserId());
                        } else {
                            notificationService.markAsFailed(notification.getId(), "Failed to send via Firebase");
                            log.error("Failed to send notification {} to user {}", notification.getId(), notification.getUserId());
                        }
                    } else {
                        notificationService.markAsFailed(notification.getId(), "No device token found for user");
                        log.warn("No device token found for user: {}", notification.getUserId());
                    }
                    
                    // Add a small delay to avoid overwhelming Firebase API
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.error("Error processing notification: {}", notification.getId(), e);
                    notificationService.markAsFailed(notification.getId(), e.getMessage());
                }
            }
            
            log.info("Completed notification scheduler task");
            
        } catch (Exception e) {
            log.error("Error in notification scheduler task", e);
        }
    }
    
    /**
     * Get device token for a user
     * TODO: Implement this to fetch from user settings/device table
     */
    private String getDeviceTokenForUser(Long userId) {
        // This is a placeholder implementation
        // In a real application, you would:
        // 1. Query a user_device_tokens table
        // 2. Get the most recent/active device token for the user
        // 3. Handle multiple devices per user
        
        // For now, return a dummy token for testing
        return "dummy_device_token_for_user_" + userId;
    }
    
    /**
     * Clean up old notifications (older than 30 days)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void cleanupOldNotifications() {
        try {
            log.info("Starting cleanup of old notifications");
            
            // TODO: Implement cleanup logic
            // Delete notifications older than 30 days that are marked as read and sent
            
            log.info("Completed cleanup of old notifications");
            
        } catch (Exception e) {
            log.error("Error in notification cleanup task", e);
        }
    }
} 