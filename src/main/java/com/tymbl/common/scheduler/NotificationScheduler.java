package com.tymbl.common.scheduler;

import com.tymbl.common.service.NotificationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationScheduler {

  private final NotificationEngine notificationEngine;

  /**
   * Generate company jobs notifications every 6 hours
   * This creates notifications for users when new jobs are posted in their company
   */
  @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
  public void generateCompanyJobsNotifications() {
    log.info("Starting scheduled company jobs notification generation");
    try {
      notificationEngine.generateCompanyJobsNotifications();
    } catch (Exception e) {
      log.error("Error in scheduled company jobs notification generation", e);
    }
  }

  /**
   * Generate application status change notifications every 30 minutes
   * This creates notifications when referrers update application status
   */
  @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
  public void generateApplicationStatusNotifications() {
    log.info("Starting scheduled application status notification generation");
    try {
      notificationEngine.generateApplicationStatusNotifications();
    } catch (Exception e) {
      log.error("Error in scheduled application status notification generation", e);
    }
  }

  /**
   * Generate posted job applications notifications every 15 minutes
   * This creates notifications when candidates apply to posted jobs
   */
  @Scheduled(fixedRate = 15 * 60 * 1000) // 15 minutes
  public void generatePostedJobApplicationsNotifications() {
    log.info("Starting scheduled posted job applications notification generation");
    try {
      notificationEngine.generatePostedJobApplicationsNotifications();
    } catch (Exception e) {
      log.error("Error in scheduled posted job applications notification generation", e);
    }
  }

  /**
   * Cleanup old notifications daily at 2 AM
   * This removes notifications older than 30 days
   */
  @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
  public void cleanupOldNotifications() {
    log.info("Starting scheduled notification cleanup");
    try {
      notificationEngine.cleanupOldNotifications();
    } catch (Exception e) {
      log.error("Error in scheduled notification cleanup", e);
    }
  }
}
