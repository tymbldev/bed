package com.tymbl.common.service;

import com.tymbl.common.entity.NotificationJobApplicationCount;
import com.tymbl.common.entity.User;
import com.tymbl.common.repository.NotificationJobApplicationCountRepository;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.jobs.entity.JobApplication;
import com.tymbl.jobs.repository.JobApplicationRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.jobs.service.ElasticsearchJobQueryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationEngine {

  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final ElasticsearchJobQueryService elasticsearchJobQueryService;
  private final JobApplicationRepository jobApplicationRepository;
  private final JobRepository jobRepository;
  private final NotificationJobApplicationCountRepository notificationJobApplicationCountRepository;
  private final DropdownService dropdownService;
  
  // Configuration for job count notification
  private static final int DEFAULT_JOB_COUNT_DAYS = 30;
  
  // Configuration for application status notification
  private static final int DEFAULT_APPLICATION_STATUS_DAYS = 7;

  /**
   * Generate company jobs notifications for all users
   * This method is called by the scheduler
   */
  @Transactional
  public void generateCompanyJobsNotifications() {
    generateCompanyJobsNotifications(DEFAULT_JOB_COUNT_DAYS);
  }

  /**
   * Generate company jobs notifications for all users with configurable days
   * @param daysBack Number of days to look back for job count
   */
  @Transactional
  public void generateCompanyJobsNotifications(int daysBack) {
    log.info("Starting company jobs notification generation for last {} days", daysBack);
    
    try {
      // Get all users with company information
      List<User> allUsers = userRepository.findAll();
      List<User> usersWithCompanies = allUsers.stream()
          .filter(user -> user.getCompanyId() != null)
          .collect(java.util.stream.Collectors.toList());
      
      int notificationsCreated = 0;
      int usersProcessed = 0;
      
      for (User user : usersWithCompanies) {
        if (user.getCompanyId() == null) continue;
        
        usersProcessed++;
        
        // Fetch job count for the company in the specified days using Elasticsearch
        long jobCount = elasticsearchJobQueryService.searchJobsByCompanyAndDateRange(
            user.getCompanyId(), daysBack);
        
        // Only create notification if there are jobs available
        if (jobCount > 0) {
          // Get actual company name from DropdownService
          String companyName = "your company"; // Default fallback
          try {
            String fetchedCompanyName = dropdownService.getCompanyNameById(user.getCompanyId());
            if (fetchedCompanyName != null && !fetchedCompanyName.trim().isEmpty()) {
              companyName = fetchedCompanyName;
            }
          } catch (Exception e) {
            log.warn("Could not fetch company name for company ID {}: {}", user.getCompanyId(), e.getMessage());
          }
          
          // Get user's designation for context
          String designation = user.getDesignation() != null ? user.getDesignation() : null;
          
          notificationService.createCompanyJobsNotification(
              user.getId(), companyName, (int) jobCount, user.getCompanyId(), designation);
          notificationsCreated++;
          
          log.info("Created company jobs notification for user {}: {} jobs in {} (last {} days)", 
              user.getId(), jobCount, companyName, daysBack);
        } else {
          log.info("No jobs found for user {} in company {} (last {} days), skipping notification", 
              user.getId(), user.getCompanyId(), daysBack);
        }
      }
      
      log.info("Completed company jobs notification generation: {} notifications created for {} users (last {} days)", 
          notificationsCreated, usersProcessed, daysBack);
      
    } catch (Exception e) {
      log.error("Error generating company jobs notifications for last {} days", daysBack, e);
    }
  }

  /**
   * Generate application status change notifications
   * This method is called by the scheduler
   */
  @Transactional
  public void generateApplicationStatusNotifications() {
    generateApplicationStatusNotifications(DEFAULT_APPLICATION_STATUS_DAYS);
  }

  /**
   * Generate application status change notifications with configurable days
   * @param daysBack Number of days to look back for status changes
   */
  @Transactional
  public void generateApplicationStatusNotifications(int daysBack) {
    log.info("Starting application status notification generation for last {} days", daysBack);
    
    try {
      LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
      
      // Find applications that have been updated recently with status changes
      List<JobApplication> applicationsWithStatusChanges = jobApplicationRepository
          .findApplicationsWithStatusChangesSince(since);
      
      int notificationsCreated = 0;
      int applicationsProcessed = 0;
      
      for (JobApplication application : applicationsWithStatusChanges) {
        applicationsProcessed++;
        
        try {
          // Get job details for the notification message
          com.tymbl.common.entity.Job job = jobRepository.findById(application.getJobId())
              .orElse(null);
          
          if (job == null) {
            log.warn("Job not found for application ID: {}, skipping notification", application.getId());
            continue;
          }
          
          // Create notification for the applicant using the updated method
          String statusText = application.getStatus().toString().toLowerCase();
          
          notificationService.createApplicationStatusNotification(
              application.getApplicantId(),
              application.getId(),
              statusText,
              job.getTitle(),
              job.getCompanyId(),
              job.getDesignation()
          );
          
          notificationsCreated++;
          
          log.info("Created application status notification for user {}: application {} status changed to {} for job '{}'", 
              application.getApplicantId(), application.getId(), application.getStatus(), job.getTitle());
          
        } catch (Exception e) {
          log.error("Error creating notification for application ID: {}", application.getId(), e);
        }
      }
      
      log.info("Completed application status notification generation: {} notifications created for {} applications (last {} days)", 
          notificationsCreated, applicationsProcessed, daysBack);
      
    } catch (Exception e) {
      log.error("Error generating application status notifications for last {} days", daysBack, e);
    }
  }

  /**
   * Generate posted job applications notifications
   * This method is called by the scheduler
   */
  @Transactional
  public void generatePostedJobApplicationsNotifications() {
    log.info("Starting posted job applications notification generation");
    
    try {
      // Get all jobs that have applications using efficient database query
      List<com.tymbl.common.entity.Job> jobsWithApplications = jobRepository.findJobsWithApplications();
      
      int notificationsCreated = 0;
      int jobsProcessed = 0;
      
      for (com.tymbl.common.entity.Job job : jobsWithApplications) {
        jobsProcessed++;
        
        try {
          // Get current application count for this job
          int currentApplicationCount = jobApplicationRepository.countByJobId(job.getId());
          
          // Check if we have a record for this job and user
          NotificationJobApplicationCount existingCount = notificationJobApplicationCountRepository
              .findByJobIdAndPostedByUserId(job.getId(), job.getPostedById())
              .orElse(null);
          
          if (existingCount == null) {
            // First time tracking this job - create new record
            if (currentApplicationCount > 0) {
              NotificationJobApplicationCount newCount = NotificationJobApplicationCount.builder()
                  .jobId(job.getId())
                  .postedByUserId(job.getPostedById())
                  .lastNotifiedCount(0)
                  .currentApplicationCount(currentApplicationCount)
                  .build();
              
              notificationJobApplicationCountRepository.save(newCount);
              
              // Create notification
              notificationService.createPostedJobApplicationsNotification(
                  job.getPostedById(), 
                  job.getId(), 
                  job.getTitle(), 
                  currentApplicationCount
              );
              
              notificationsCreated++;
              log.info("Created first-time notification for job {}: {} applications", 
                  job.getId(), currentApplicationCount);
            }
          } else {
            // Update existing record
            int lastNotifiedCount = existingCount.getLastNotifiedCount();
            
            if (currentApplicationCount > lastNotifiedCount) {
              // Application count has increased - update and notify
              existingCount.setCurrentApplicationCount(currentApplicationCount);
              existingCount.setLastNotifiedCount(currentApplicationCount);
              notificationJobApplicationCountRepository.save(existingCount);
              
              // Create notification
              notificationService.createPostedJobApplicationsNotification(
                  job.getPostedById(), 
                  job.getId(), 
                  job.getTitle(), 
                  currentApplicationCount
              );
              
              notificationsCreated++;
              log.info("Created updated notification for job {}: {} applications (was {})", 
                  job.getId(), currentApplicationCount, lastNotifiedCount);
            } else {
              // No change in application count - just update the current count
              existingCount.setCurrentApplicationCount(currentApplicationCount);
              notificationJobApplicationCountRepository.save(existingCount);
              
              log.info("No new applications for job {}: {} applications (unchanged)", 
                  job.getId(), currentApplicationCount);
            }
          }
          
        } catch (Exception e) {
          log.error("Error processing job {} for posted job applications notification", job.getId(), e);
        }
      }
      
      log.info("Completed posted job applications notification generation: {} notifications created for {} jobs", 
          notificationsCreated, jobsProcessed);
      
    } catch (Exception e) {
      log.error("Error generating posted job applications notifications", e);
    }
  }
  
  /**
   * Cleanup old notifications
   * This method is called by the scheduler
   */
  @Transactional
  public void cleanupOldNotifications() {
    log.info("Starting notification cleanup");
    
    try {
      int deletedCount = notificationService.cleanupOldNotifications(30); // Keep last 30 days
      log.info("Cleaned up {} old notifications", deletedCount);
      
    } catch (Exception e) {
      log.error("Error cleaning up old notifications", e);
    }
  }

  /**
   * Generate notifications for a specific user after registration
   * This includes company jobs notifications if the user has a company
   */
  @Transactional
  public void generateNotificationsForUser(Long userId) {
    log.info("Generating notifications for user: {}", userId);
    
    try {
      // Get user details
      User user = userRepository.findById(userId).orElse(null);
      if (user == null) {
        log.warn("User not found with ID: {}", userId);
        return;
      }
      
      int notificationsCreated = 0;
      
      // Generate company jobs notification if user has a company
      if (user.getCompanyId() != null) {
        try {
          // Fetch job count for the company in the last 30 days using Elasticsearch
          long jobCount = elasticsearchJobQueryService.searchJobsByCompanyAndDateRange(
              user.getCompanyId(), DEFAULT_JOB_COUNT_DAYS);
          
          // Only create notification if there are jobs available
          if (jobCount > 0) {
            // Get actual company name from DropdownService
            String companyName = "your company"; // Default fallback
            try {
              String fetchedCompanyName = dropdownService.getCompanyNameById(user.getCompanyId());
              if (fetchedCompanyName != null && !fetchedCompanyName.trim().isEmpty()) {
                companyName = fetchedCompanyName;
              }
            } catch (Exception e) {
              log.warn("Could not fetch company name for company ID {}: {}", user.getCompanyId(), e.getMessage());
            }
            
            // Get user's designation for context
            String designation = user.getDesignation() != null ? user.getDesignation() : null;
            
            notificationService.createCompanyJobsNotification(
                user.getId(), companyName, (int) jobCount, user.getCompanyId(), designation);
            notificationsCreated++;
            
            log.info("Created company jobs notification for user {}: {} jobs in {} (last {} days)", 
                user.getId(), jobCount, companyName, DEFAULT_JOB_COUNT_DAYS);
          } else {
            log.info("No jobs found for user {} in company {} (last {} days), skipping notification", 
                user.getId(), user.getCompanyId(), DEFAULT_JOB_COUNT_DAYS);
          }
        } catch (Exception e) {
          log.error("Error generating company jobs notification for user {}: {}", userId, e.getMessage(), e);
        }
      } else {
        log.info("User {} has no company associated, skipping company jobs notification", userId);
      }
      
      log.info("Completed notification generation for user {}. Notifications created: {}", userId, notificationsCreated);
      
    } catch (Exception e) {
      log.error("Error generating notifications for user {}: {}", userId, e.getMessage(), e);
      throw e;
    }
  }

}
