package com.tymbl.common.service;

import com.tymbl.common.dto.NotificationDTO;
import com.tymbl.common.entity.Notification;
import com.tymbl.common.entity.JobReferrer;
import com.tymbl.common.entity.Job;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.common.repository.NotificationRepository;
import com.tymbl.jobs.entity.JobApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final JobRepository jobRepository;
    
    /**
     * Create a notification for referral application
     */
    @Transactional
    public void createReferralApplicationNotification(JobReferrer jobReferrer, JobApplication application) {
        try {
            Job job = jobReferrer.getJob();
            String jobTitle = job != null ? job.getTitle() : "";
            String companyName = job != null ? job.getCompany() : "";
            String title = "New Referral Application";
            String message = String.format("Someone applied for the referral you posted for %s position at %s", jobTitle, companyName);
            
            Notification notification = new Notification(
                jobReferrer.getUser().getId(),
                title,
                message,
                Notification.NotificationType.REFERRAL_APPLICATION,
                application.getId(),
                Notification.RelatedEntityType.JOB_APPLICATION
            );
            
            notificationRepository.save(notification);
            log.info("Created referral application notification for user: {}", jobReferrer.getUser().getId());
        } catch (Exception e) {
            log.error("Error creating referral application notification", e);
        }
    }
    
    /**
     * Create a notification for application shortlisted
     */
    @Transactional
    public void createApplicationShortlistedNotification(JobApplication application) {
        try {
            Job job = jobRepository.findById(application.getJobId()).orElse(null);
            String jobTitle = job != null ? job.getTitle() : "";
            String companyName = job != null ? job.getCompany() : "";
            String title = "Application Shortlisted!";
            String message = String.format("Congratulations! Your application for %s position at %s has been shortlisted.", jobTitle, companyName);
            
            Notification notification = new Notification(
                application.getApplicantId(),
                title,
                message,
                Notification.NotificationType.APPLICATION_SHORTLISTED,
                application.getId(),
                Notification.RelatedEntityType.JOB_APPLICATION
            );
            
            notificationRepository.save(notification);
            log.info("Created application shortlisted notification for user: {}", application.getApplicantId());
        } catch (Exception e) {
            log.error("Error creating application shortlisted notification", e);
        }
    }
    
    /**
     * Create a notification for application status change
     */
    @Transactional
    public void createApplicationStatusNotification(JobApplication application, String status) {
        try {
            Job job = jobRepository.findById(application.getJobId()).orElse(null);
            String jobTitle = job != null ? job.getTitle() : "";
            String companyName = job != null ? job.getCompany() : "";
            String title = "Application Status Update";
            String message = String.format("Your application for %s position at %s has been %s.", jobTitle, companyName, status.toLowerCase());
            
            Notification.NotificationType type;
            switch (status.toUpperCase()) {
                case "SHORTLISTED":
                    type = Notification.NotificationType.APPLICATION_SHORTLISTED;
                    break;
                case "REJECTED":
                    type = Notification.NotificationType.APPLICATION_REJECTED;
                    break;
                default:
                    type = Notification.NotificationType.GENERAL;
            }
            
            Notification notification = new Notification(
                application.getApplicantId(),
                title,
                message,
                type,
                application.getId(),
                Notification.RelatedEntityType.JOB_APPLICATION
            );
            
            notificationRepository.save(notification);
            log.info("Created application status notification for user: {} with status: {}", application.getApplicantId(), status);
        } catch (Exception e) {
            log.error("Error creating application status notification", e);
        }
    }
    
    /**
     * Get notifications for a user in the last 7 days
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Notification> notifications = notificationRepository.findRecentNotificationsByUserId(userId, sevenDaysAgo);
        
        return notifications.stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        return notifications.stream()
            .map(NotificationDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId)
            .filter(notification -> notification.getUserId().equals(userId))
            .ifPresent(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
                log.info("Marked notification {} as read for user: {}", notificationId, userId);
            });
    }
    
    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });
        
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked all notifications as read for user: {}", userId);
    }
    
    /**
     * Get count of unread notifications for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    /**
     * Get pending notifications for sending
     */
    @Transactional(readOnly = true)
    public List<Notification> getPendingNotifications() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1); // Wait 1 minute before sending
        return notificationRepository.findPendingNotifications(cutoffTime);
    }
    
    /**
     * Mark notification as sent
     */
    @Transactional
    public void markAsSent(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setIsSent(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Marked notification {} as sent", notificationId);
        });
    }
    
    /**
     * Mark notification as failed with error message
     */
    @Transactional
    public void markAsFailed(Long notificationId, String errorMessage) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setErrorMessage(errorMessage);
            notificationRepository.save(notification);
            log.error("Marked notification {} as failed with error: {}", notificationId, errorMessage);
        });
    }
    
    /**
     * Delete notification
     */
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId)
            .filter(notification -> notification.getUserId().equals(userId))
            .ifPresent(notification -> {
                notificationRepository.delete(notification);
                log.info("Deleted notification {} for user: {}", notificationId, userId);
            });
    }
} 