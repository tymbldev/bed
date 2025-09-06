package com.tymbl.common.service;

import com.tymbl.common.dto.NotificationCountResponse;
import com.tymbl.common.dto.NotificationCountWithTypeResponse;
import com.tymbl.common.dto.NotificationListResponse;
import com.tymbl.common.dto.NotificationResponse;
import com.tymbl.common.entity.Notification;
import com.tymbl.common.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Default duration for notifications (last 30 days)
    private static final int DEFAULT_NOTIFICATION_DURATION_DAYS = 30;

    /**
     * Get notification count for a user
     */
    @Transactional(readOnly = true)
    public NotificationCountResponse getNotificationCount(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_NOTIFICATION_DURATION_DAYS);

        Long totalCount = notificationRepository.countByUserIdAndCreatedAtAfter(userId, since);
        Long newCount = notificationRepository.countUnseenByUserIdAndCreatedAtAfter(userId, since);
        Long seenCount = notificationRepository.countSeenByUserIdAndCreatedAtAfter(userId, since);
        Long clickedCount = notificationRepository.countClickedByUserIdAndCreatedAtAfter(userId, since);

        return NotificationCountResponse.builder()
                .userId(userId)
                .totalCount(totalCount)
                .newCount(newCount)
                .seenCount(seenCount)
                .clickedCount(clickedCount)
                .build();
    }

    /**
     * Get notification count with type breakdown for a user
     */
    @Transactional(readOnly = true)
    public NotificationCountWithTypeResponse getNotificationCountWithType(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_NOTIFICATION_DURATION_DAYS);

        // Get overall counts
        Long totalCount = notificationRepository.countByUserIdAndCreatedAtAfter(userId, since);
        Long newCount = notificationRepository.countUnseenByUserIdAndCreatedAtAfter(userId, since);
        Long seenCount = notificationRepository.countSeenByUserIdAndCreatedAtAfter(userId, since);
        Long clickedCount = notificationRepository.countClickedByUserIdAndCreatedAtAfter(userId, since);

        // Get counts by type
        Map<String, NotificationCountWithTypeResponse.NotificationTypeCount> countsByType = new HashMap<>();

        for (Notification.NotificationType type : Notification.NotificationType.values()) {
            Long typeTotalCount = notificationRepository.countByUserIdAndTypeAndCreatedAtAfter(userId, type, since);
            Long typeNewCount = notificationRepository.countUnseenByUserIdAndTypeAndCreatedAtAfter(userId, type, since);
            Long typeSeenCount = notificationRepository.countSeenByUserIdAndTypeAndCreatedAtAfter(userId, type, since);
            Long typeClickedCount = notificationRepository.countClickedByUserIdAndTypeAndCreatedAtAfter(userId, type, since);

            countsByType.put(type.getValue(), NotificationCountWithTypeResponse.NotificationTypeCount.builder()
                    .totalCount(typeTotalCount)
                    .newCount(typeNewCount)
                    .seenCount(typeSeenCount)
                    .clickedCount(typeClickedCount)
                    .build());
        }

        return NotificationCountWithTypeResponse.builder()
                .userId(userId)
                .totalCount(totalCount)
                .newCount(newCount)
                .seenCount(seenCount)
                .clickedCount(clickedCount)
                .countsByType(countsByType)
                .build();
    }

    /**
     * Get notifications list with pagination (all types)
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long userId, int page, int size) {
        LocalDateTime since = LocalDateTime.now().minusDays(DEFAULT_NOTIFICATION_DURATION_DAYS);
        Pageable pageable = PageRequest.of(page, size);

        // Get all notifications for the user (all types)
        Page<Notification> notificationPage = notificationRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, since, pageable);

        List<NotificationResponse> notifications = notificationPage.getContent()
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        return NotificationListResponse.builder()
                .notifications(notifications)
                .totalCount((int) notificationPage.getTotalElements())
                .page(page)
                .size(size)
                .hasNext(notificationPage.hasNext())
                .hasPrevious(notificationPage.hasPrevious())
                .build();
    }


    /**
     * Mark notifications as seen
     */
    @Transactional
    public int markAsSeen(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        int totalUpdated = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long notificationId : notificationIds) {
            int updated = notificationRepository.markAsSeen(notificationId, userId, now);
            totalUpdated += updated;
        }

        return totalUpdated;
    }

    /**
     * Mark all notifications as seen for a user
     */
    @Transactional
    public int markAllAsSeen(Long userId) {
        return notificationRepository.markAllAsSeen(userId, LocalDateTime.now());
    }

    /**
     * Mark notifications as clicked
     */
    @Transactional
    public int markAsClicked(List<Long> notificationIds, Long userId) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return 0;
        }

        int totalUpdated = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long notificationId : notificationIds) {
            int updated = notificationRepository.markAsClicked(notificationId, userId, now);
            totalUpdated += updated;
        }

        return totalUpdated;
    }

    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(Long userId, Notification.NotificationType type,
                                           String message, String metadata, Long relatedEntityId, String relatedEntityType) {

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .metadata(metadata)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .seen(false)
                .clicked(false)
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Create a new notification with company information
     */
    @Transactional
    public Notification createNotification(Long userId, Notification.NotificationType type,
                                           String message, String metadata, Long relatedEntityId, String relatedEntityType,
                                           Long companyId, String designation) {

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .metadata(metadata)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .companyId(companyId)
                .designation(designation)
                .seen(false)
                .clicked(false)
                .build();

        return notificationRepository.save(notification);
    }


    /**
     * Create company jobs notification
     */
    @Transactional
    public void createCompanyJobsNotification(Long userId, String companyName, int jobCount) {
        String message = String.format("<b>%d</b>+ new openings at <b>%s</b>. Start accepting referrals from candidates today!", jobCount, companyName);
        String metadata = String.format("{\"companyName\":\"%s\",\"jobCount\":%d}", companyName, jobCount);

        // Check if similar notification already exists in last 7 days to avoid spam
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByUserIdAndTypeAndRelatedEntityIdAndCreatedAtAfter(
                userId, Notification.NotificationType.COMPANY_JOBS, null, since);

        if (!exists) {
            createNotification(userId, Notification.NotificationType.COMPANY_JOBS,
                    message, metadata, null, "company");
        }
    }

    /**
     * Create company jobs notification with company information
     */
    @Transactional
    public void createCompanyJobsNotification(Long userId, String companyName, int jobCount,
                                              Long companyId, String designation) {
        if (companyName == null || companyName.isEmpty()) {
            return;
        }
        String message = String.format("<b>%d</b>+ new openings at <b>%s</b>. Start accepting referrals from candidates today!", jobCount, companyName);
        String metadata = String.format("{\"companyName\":\"%s\",\"jobCount\":%d}", companyName, jobCount);

        // Check if similar notification already exists in last 7 days to avoid spam
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByUserIdAndTypeAndCompanyIdAndDesignationAndCreatedAtAfter(
                userId, Notification.NotificationType.COMPANY_JOBS, companyId, designation, since);

        if (!exists) {
            createNotification(userId, Notification.NotificationType.COMPANY_JOBS,
                    message, metadata, null, "company", companyId, designation);
        }
    }

    /**
     * Create application status change notification (backward compatibility method)
     */
    @Transactional
    public void createApplicationStatusNotification(com.tymbl.jobs.entity.JobApplication application,
                                                    String status) {
        // Get job details for the notification
        com.tymbl.common.entity.Job job = null;
        try {
            // This would need to be injected or passed as parameter in a real implementation
            // For now, we'll create a basic notification without company/designation info
            String message = String.format("Your application has been <b>%s</b>.", status);
            String metadata = String.format("{\"applicationId\":%d,\"status\":\"%s\"}",
                    application.getId(), status);

            createNotification(application.getApplicantId(), Notification.NotificationType.APPLICATION_STATUS,
                    message, metadata, application.getId(), "application");
        } catch (Exception e) {
            log.error("Error creating application status notification for application {}", application.getId(), e);
        }
    }

    /**
     * Create application status change notification
     */
    @Transactional
    public void createApplicationStatusNotification(Long userId, Long applicationId,
                                                    String status, String jobTitle, Long companyId, String designation) {
        String message = String.format("Your application for <b>'%s'</b> has been <b>%s</b>.", jobTitle, status);
        String metadata = String.format("{\"applicationId\":%d,\"status\":\"%s\",\"jobTitle\":\"%s\"}",
                applicationId, status, jobTitle);

        // Check if similar notification already exists in last 7 days to avoid spam
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByUserIdAndTypeAndCompanyIdAndDesignationAndCreatedAtAfter(
                userId, Notification.NotificationType.APPLICATION_STATUS, companyId, designation, since);

        if (!exists) {
            Notification notification = Notification.builder()
                    .userId(userId)
                    .type(Notification.NotificationType.APPLICATION_STATUS)
                    .message(message)
                    .metadata(metadata)
                    .relatedEntityId(applicationId)
                    .relatedEntityType("application")
                    .companyId(companyId)
                    .designation(designation)
                    .seen(false)
                    .clicked(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * Create referral application notification
     */
    @Transactional
    public void createReferralApplicationNotification(com.tymbl.common.entity.JobReferrer jobReferrer,
                                                      com.tymbl.jobs.entity.JobApplication application) {
        String message = String.format("A new application has been submitted for the job you're referring.");
        String metadata = String.format("{\"applicationId\":%d,\"jobReferrerId\":%d,\"jobId\":%d}",
                application.getId(), jobReferrer.getId(), application.getJobId());

        createNotification(jobReferrer.getUser().getId(), Notification.NotificationType.POSTED_JOB_APPLICATIONS,
                message, metadata, application.getId(), "application");
    }

    /**
     * Create posted job applications notification
     */
    @Transactional
    public void createPostedJobApplicationsNotification(Long userId, Long jobId,
                                                        String jobTitle, int applicationCount) {
        String message;
        if (applicationCount == 1) {
            message = String.format("You have got a new applicant for your job <b>'%s'</b>.", jobTitle);
        } else {
            message = String.format("<b>%d</b> applicants have applied to your job <b>'%s'</b>.", applicationCount, jobTitle);
        }

        String metadata = String.format("{\"jobId\":%d,\"jobTitle\":\"%s\",\"applicationCount\":%d}",
                jobId, jobTitle, applicationCount);

        createNotification(userId, Notification.NotificationType.POSTED_JOB_APPLICATIONS,
                message, metadata, jobId, "job");
    }

    /**
     * Cleanup old notifications
     */
    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return notificationRepository.deleteByCreatedAtBefore(cutoffDate);
    }
}
