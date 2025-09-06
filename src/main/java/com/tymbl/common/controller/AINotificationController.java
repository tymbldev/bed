package com.tymbl.common.controller;

import com.tymbl.common.service.NotificationEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/ai-notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Notification Management", description = "APIs for manually triggering notification generation and cleanup")
public class AINotificationController {

  private final NotificationEngine notificationEngine;

  /**
   * Trigger notification generation and cleanup operations
   * @param type Optional notification type to execute. If not provided, executes all operations.
   *             Valid values: company_jobs, application_status, posted_job_applications, cleanup, all
   * @return Response with execution results
   */
  @PostMapping("/trigger")
  @Operation(
      summary = "Trigger notification operations",
      description = "Manually trigger notification generation and cleanup operations. " +
                   "If no type is specified, executes all operations. " +
                   "Valid types: company_jobs, application_status, posted_job_applications, cleanup, all"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Notification operations completed successfully",
          content = @Content(
              schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"success\": true,\n" +
                      "  \"message\": \"All notification operations completed successfully\",\n" +
                      "  \"operations\": {\n" +
                      "    \"company_jobs\": {\n" +
                      "      \"executed\": true,\n" +
                      "      \"message\": \"Company jobs notifications generated successfully\"\n" +
                      "    },\n" +
                      "    \"application_status\": {\n" +
                      "      \"executed\": true,\n" +
                      "      \"message\": \"Application status notifications generated successfully\"\n" +
                      "    },\n" +
                      "    \"posted_job_applications\": {\n" +
                      "      \"executed\": true,\n" +
                      "      \"message\": \"Posted job applications notifications generated successfully\"\n" +
                      "    },\n" +
                      "    \"cleanup\": {\n" +
                      "      \"executed\": true,\n" +
                      "      \"message\": \"Old notifications cleaned up successfully\"\n" +
                      "    }\n" +
                      "  },\n" +
                      "  \"totalOperations\": 4,\n" +
                      "  \"successfulOperations\": 4,\n" +
                      "  \"failedOperations\": 0\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid notification type provided"),
      @ApiResponse(responseCode = "500", description = "Error executing notification operations")
  })
  public ResponseEntity<Map<String, Object>> triggerNotificationOperations(
      @Parameter(description = "Notification type to execute. Valid values: company_jobs, application_status, posted_job_applications, cleanup, all. If not provided, executes all operations.")
      @RequestParam(required = false) String type) {
    
    log.info("Triggering notification operations. Type: {}", type != null ? type : "all");
    
    Map<String, Object> response = new HashMap<>();
    Map<String, Map<String, Object>> operations = new HashMap<>();
    int totalOperations = 0;
    int successfulOperations = 0;
    int failedOperations = 0;
    
    try {
      // If no type specified or type is "all", execute all operations
      if (type == null || type.trim().isEmpty() || "all".equalsIgnoreCase(type.trim())) {
        // Execute all operations
        totalOperations = 4;
        
        // 1. Company Jobs Notifications
        try {
          log.info("Executing company jobs notification generation");
          notificationEngine.generateCompanyJobsNotifications();
          Map<String, Object> companyJobsResult = new HashMap<>();
          companyJobsResult.put("executed", true);
          companyJobsResult.put("message", "Company jobs notifications generated successfully");
          operations.put("company_jobs", companyJobsResult);
          successfulOperations++;
        } catch (Exception e) {
          log.error("Error generating company jobs notifications", e);
          Map<String, Object> companyJobsError = new HashMap<>();
          companyJobsError.put("executed", false);
          companyJobsError.put("message", "Failed to generate company jobs notifications: " + e.getMessage());
          operations.put("company_jobs", companyJobsError);
          failedOperations++;
        }
        
        // 2. Application Status Notifications
        try {
          log.info("Executing application status notification generation");
          notificationEngine.generateApplicationStatusNotifications();
          Map<String, Object> appStatusResult = new HashMap<>();
          appStatusResult.put("executed", true);
          appStatusResult.put("message", "Application status notifications generated successfully");
          operations.put("application_status", appStatusResult);
          successfulOperations++;
        } catch (Exception e) {
          log.error("Error generating application status notifications", e);
          Map<String, Object> appStatusError = new HashMap<>();
          appStatusError.put("executed", false);
          appStatusError.put("message", "Failed to generate application status notifications: " + e.getMessage());
          operations.put("application_status", appStatusError);
          failedOperations++;
        }
        
        // 3. Posted Job Applications Notifications
        try {
          log.info("Executing posted job applications notification generation");
          notificationEngine.generatePostedJobApplicationsNotifications();
          Map<String, Object> postedJobsResult = new HashMap<>();
          postedJobsResult.put("executed", true);
          postedJobsResult.put("message", "Posted job applications notifications generated successfully");
          operations.put("posted_job_applications", postedJobsResult);
          successfulOperations++;
        } catch (Exception e) {
          log.error("Error generating posted job applications notifications", e);
          Map<String, Object> postedJobsError = new HashMap<>();
          postedJobsError.put("executed", false);
          postedJobsError.put("message", "Failed to generate posted job applications notifications: " + e.getMessage());
          operations.put("posted_job_applications", postedJobsError);
          failedOperations++;
        }
        
        // 4. Cleanup Old Notifications
        try {
          log.info("Executing notification cleanup");
          notificationEngine.cleanupOldNotifications();
          Map<String, Object> cleanupResult = new HashMap<>();
          cleanupResult.put("executed", true);
          cleanupResult.put("message", "Old notifications cleaned up successfully");
          operations.put("cleanup", cleanupResult);
          successfulOperations++;
        } catch (Exception e) {
          log.error("Error cleaning up old notifications", e);
          Map<String, Object> cleanupError = new HashMap<>();
          cleanupError.put("executed", false);
          cleanupError.put("message", "Failed to cleanup old notifications: " + e.getMessage());
          operations.put("cleanup", cleanupError);
          failedOperations++;
        }
        
        response.put("success", failedOperations == 0);
        response.put("message", failedOperations == 0 ? 
            "All notification operations completed successfully" : 
            "Some notification operations failed");
            
      } else {
        // Execute specific operation based on type
        totalOperations = 1;
        String normalizedType = type.trim().toLowerCase();
        
        switch (normalizedType) {
          case "company_jobs":
            try {
              log.info("Executing company jobs notification generation");
              notificationEngine.generateCompanyJobsNotifications();
              Map<String, Object> companyJobsResult = new HashMap<>();
              companyJobsResult.put("executed", true);
              companyJobsResult.put("message", "Company jobs notifications generated successfully");
              operations.put("company_jobs", companyJobsResult);
              successfulOperations++;
            } catch (Exception e) {
              log.error("Error generating company jobs notifications", e);
              Map<String, Object> companyJobsError = new HashMap<>();
              companyJobsError.put("executed", false);
              companyJobsError.put("message", "Failed to generate company jobs notifications: " + e.getMessage());
              operations.put("company_jobs", companyJobsError);
              failedOperations++;
            }
            break;
            
          case "application_status":
            try {
              log.info("Executing application status notification generation");
              notificationEngine.generateApplicationStatusNotifications();
              Map<String, Object> appStatusResult = new HashMap<>();
              appStatusResult.put("executed", true);
              appStatusResult.put("message", "Application status notifications generated successfully");
              operations.put("application_status", appStatusResult);
              successfulOperations++;
            } catch (Exception e) {
              log.error("Error generating application status notifications", e);
              Map<String, Object> appStatusError = new HashMap<>();
              appStatusError.put("executed", false);
              appStatusError.put("message", "Failed to generate application status notifications: " + e.getMessage());
              operations.put("application_status", appStatusError);
              failedOperations++;
            }
            break;
            
          case "posted_job_applications":
            try {
              log.info("Executing posted job applications notification generation");
              notificationEngine.generatePostedJobApplicationsNotifications();
              Map<String, Object> postedJobsResult = new HashMap<>();
              postedJobsResult.put("executed", true);
              postedJobsResult.put("message", "Posted job applications notifications generated successfully");
              operations.put("posted_job_applications", postedJobsResult);
              successfulOperations++;
            } catch (Exception e) {
              log.error("Error generating posted job applications notifications", e);
              Map<String, Object> postedJobsError = new HashMap<>();
              postedJobsError.put("executed", false);
              postedJobsError.put("message", "Failed to generate posted job applications notifications: " + e.getMessage());
              operations.put("posted_job_applications", postedJobsError);
              failedOperations++;
            }
            break;
            
          case "cleanup":
            try {
              log.info("Executing notification cleanup");
              notificationEngine.cleanupOldNotifications();
              Map<String, Object> cleanupResult = new HashMap<>();
              cleanupResult.put("executed", true);
              cleanupResult.put("message", "Old notifications cleaned up successfully");
              operations.put("cleanup", cleanupResult);
              successfulOperations++;
            } catch (Exception e) {
              log.error("Error cleaning up old notifications", e);
              Map<String, Object> cleanupError = new HashMap<>();
              cleanupError.put("executed", false);
              cleanupError.put("message", "Failed to cleanup old notifications: " + e.getMessage());
              operations.put("cleanup", cleanupError);
              failedOperations++;
            }
            break;
            
          default:
            response.put("success", false);
            response.put("message", "Invalid notification type. Valid types: company_jobs, application_status, posted_job_applications, cleanup, all");
            response.put("operations", operations);
            response.put("totalOperations", 0);
            response.put("successfulOperations", 0);
            response.put("failedOperations", 0);
            return ResponseEntity.badRequest().body(response);
        }
        
        response.put("success", failedOperations == 0);
        response.put("message", failedOperations == 0 ? 
            "Notification operation completed successfully" : 
            "Notification operation failed");
      }
      
      response.put("operations", operations);
      response.put("totalOperations", totalOperations);
      response.put("successfulOperations", successfulOperations);
      response.put("failedOperations", failedOperations);
      
      log.info("Notification operations completed. Total: {}, Successful: {}, Failed: {}", 
               totalOperations, successfulOperations, failedOperations);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Unexpected error during notification operations", e);
      response.put("success", false);
      response.put("message", "Unexpected error: " + e.getMessage());
      response.put("operations", operations);
      response.put("totalOperations", totalOperations);
      response.put("successfulOperations", successfulOperations);
      response.put("failedOperations", failedOperations);
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Trigger notifications for a specific user
   * @param userId The ID of the user to generate notifications for
   * @param type Optional notification type to execute. If not provided, executes all applicable operations.
   *             Valid values: company_jobs, all
   * @return Response with execution results
   */
  @PostMapping("/trigger/user/{userId}")
  @Operation(
      summary = "Trigger notifications for specific user",
      description = "Manually trigger notification generation for a specific user. " +
                   "This is typically called after user registration to show immediate notifications. " +
                   "Valid types: company_jobs, all"
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "User notification operations completed successfully",
          content = @Content(
              schema = @Schema(implementation = Map.class),
              examples = @ExampleObject(
                  value = "{\n" +
                      "  \"success\": true,\n" +
                      "  \"message\": \"User notification operations completed successfully\",\n" +
                      "  \"userId\": 123,\n" +
                      "  \"operations\": {\n" +
                      "    \"company_jobs\": {\n" +
                      "      \"executed\": true,\n" +
                      "      \"message\": \"Company jobs notification created successfully\"\n" +
                      "    }\n" +
                      "  },\n" +
                      "  \"totalOperations\": 1,\n" +
                      "  \"successfulOperations\": 1,\n" +
                      "  \"failedOperations\": 0\n" +
                      "}"
              )
          )
      ),
      @ApiResponse(responseCode = "400", description = "Invalid notification type provided"),
      @ApiResponse(responseCode = "404", description = "User not found"),
      @ApiResponse(responseCode = "500", description = "Error executing notification operations")
  })
  public ResponseEntity<Map<String, Object>> triggerNotificationsForUser(
      @Parameter(description = "ID of the user to generate notifications for")
      @PathVariable Long userId,
      @Parameter(description = "Notification type to execute. Valid values: welcome, company_jobs, all. If not provided, executes all operations.")
      @RequestParam(required = false) String type) {
    
    log.info("Triggering notifications for user: {}, type: {}", userId, type != null ? type : "all");
    
    Map<String, Object> response = new HashMap<>();
    Map<String, Map<String, Object>> operations = new HashMap<>();
    int totalOperations = 0;
    int successfulOperations = 0;
    int failedOperations = 0;
    
    try {
      // If no type specified or type is "all", execute all operations
      if (type == null || type.trim().isEmpty() || "all".equalsIgnoreCase(type.trim())) {
        // Execute all operations
        totalOperations = 1;
        
        // Company Jobs Notification
        try {
          log.info("Executing company jobs notification generation for user: {}", userId);
          notificationEngine.generateNotificationsForUser(userId);
          Map<String, Object> companyJobsResult = new HashMap<>();
          companyJobsResult.put("executed", true);
          companyJobsResult.put("message", "Company jobs notification created successfully");
          operations.put("company_jobs", companyJobsResult);
          successfulOperations++;
        } catch (Exception e) {
          log.error("Error generating company jobs notification for user: {}", userId, e);
          Map<String, Object> companyJobsError = new HashMap<>();
          companyJobsError.put("executed", false);
          companyJobsError.put("message", "Failed to create company jobs notification: " + e.getMessage());
          operations.put("company_jobs", companyJobsError);
          failedOperations++;
        }
        
        response.put("success", failedOperations == 0);
        response.put("message", failedOperations == 0 ? 
            "User notification operations completed successfully" : 
            "Some user notification operations failed");
            
      } else {
        // Execute specific operation based on type
        totalOperations = 1;
        String normalizedType = type.trim().toLowerCase();
        
        switch (normalizedType) {
          case "company_jobs":
            try {
              log.info("Executing company jobs notification generation for user: {}", userId);
              notificationEngine.generateNotificationsForUser(userId);
              Map<String, Object> companyJobsResult = new HashMap<>();
              companyJobsResult.put("executed", true);
              companyJobsResult.put("message", "Company jobs notification created successfully");
              operations.put("company_jobs", companyJobsResult);
              successfulOperations++;
            } catch (Exception e) {
              log.error("Error generating company jobs notification for user: {}", userId, e);
              Map<String, Object> companyJobsError = new HashMap<>();
              companyJobsError.put("executed", false);
              companyJobsError.put("message", "Failed to create company jobs notification: " + e.getMessage());
              operations.put("company_jobs", companyJobsError);
              failedOperations++;
            }
            break;
            
          default:
            response.put("success", false);
            response.put("message", "Invalid notification type. Valid types: company_jobs, all");
            response.put("userId", userId);
            response.put("operations", operations);
            response.put("totalOperations", 0);
            response.put("successfulOperations", 0);
            response.put("failedOperations", 0);
            return ResponseEntity.badRequest().body(response);
        }
        
        response.put("success", failedOperations == 0);
        response.put("message", failedOperations == 0 ? 
            "User notification operation completed successfully" : 
            "User notification operation failed");
      }
      
      response.put("userId", userId);
      response.put("operations", operations);
      response.put("totalOperations", totalOperations);
      response.put("successfulOperations", successfulOperations);
      response.put("failedOperations", failedOperations);
      
      log.info("User notification operations completed for user {}. Total: {}, Successful: {}, Failed: {}", 
               userId, totalOperations, successfulOperations, failedOperations);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("Unexpected error during user notification operations for user: {}", userId, e);
      response.put("success", false);
      response.put("message", "Unexpected error: " + e.getMessage());
      response.put("userId", userId);
      response.put("operations", operations);
      response.put("totalOperations", totalOperations);
      response.put("successfulOperations", successfulOperations);
      response.put("failedOperations", failedOperations);
      return ResponseEntity.internalServerError().body(response);
    }
  }
}
