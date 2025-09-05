package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalJobSyncService {

  private final ExternalJobDetailRepository externalJobDetailRepository;
  private final IndividualJobSyncService individualJobSyncService;

  /**
   * Sync external job details to the main Job table Only processes jobs that haven't been synced
   * yet (isSyncedFromExternal = false) Note: Transaction is handled at the individual job level for
   * better granularity
   */
  public SyncResult syncExternalJobsToJobTable() {
    long startTime = System.currentTimeMillis();
    log.info("🚀 Starting external job sync process to Job table");
    
    SyncResult result = new SyncResult();

    try {
      // Get all external job details that haven't been synced yet
              log.info("📊 Querying database for refined but unsynced external jobs...");
      List<ExternalJobDetail> unsyncedJobs = externalJobDetailRepository.findByIsRefinedTrueAndIsSyncedToJobTableFalse();
              log.info("📋 Found {} refined but unsynced external jobs in database", unsyncedJobs.size());

      if (unsyncedJobs.isEmpty()) {
        log.info("✅ No unsynced external jobs found - sync process completed immediately");
        result.setMessage("No unsynced external jobs found");
        result.setSuccess(true);
        return result;
      }

      log.info("🔄 Starting sync of {} external jobs to Job table", unsyncedJobs.size());

      int successCount = 0;
      int errorCount = 0;
      int currentJobIndex = 0;

      for (ExternalJobDetail externalJob : unsyncedJobs) {
        currentJobIndex++;
        long jobStartTime = System.currentTimeMillis();
        
        log.info("⏳ Processing job {}/{}: ID={}, Title='{}', Portal='{}'", 
            currentJobIndex, unsyncedJobs.size(), externalJob.getId(), 
            externalJob.getJobTitle(), externalJob.getPortalName());

        try {
          // Use the individual job sync service (which handles its own transaction)
          log.info("🔄 Calling individual job sync service for job ID: {}", externalJob.getId());
          IndividualJobSyncService.SyncResult individualResult = individualJobSyncService.syncIndividualJob(
              externalJob);

          long jobProcessingTime = System.currentTimeMillis() - jobStartTime;

          if (individualResult.isSuccess()) {
            successCount++;
            log.info("✅ Successfully synced external job {}/{}: ID={}, Title='{}', ProcessingTime={}ms, Message='{}'", 
                currentJobIndex, unsyncedJobs.size(), externalJob.getId(), 
                externalJob.getJobTitle(), jobProcessingTime, individualResult.getMessage());
          } else {
            errorCount++;
            log.warn("❌ Failed to sync external job {}/{}: ID={}, Title='{}', ProcessingTime={}ms, Message='{}'", 
                currentJobIndex, unsyncedJobs.size(), externalJob.getId(), 
                externalJob.getJobTitle(), jobProcessingTime, individualResult.getMessage());
          }

        } catch (Exception e) {
          long jobProcessingTime = System.currentTimeMillis() - jobStartTime;
          log.error("💥 Exception syncing external job {}/{}: ID={}, Title='{}', ProcessingTime={}ms, Error='{}'", 
              currentJobIndex, unsyncedJobs.size(), externalJob.getId(), 
              externalJob.getJobTitle(), jobProcessingTime, e.getMessage(), e);
          errorCount++;
        }
      }

      long totalProcessingTime = System.currentTimeMillis() - startTime;
      
      result.setSuccess(true);
      result.setTotalJobs(unsyncedJobs.size());
      result.setSuccessCount(successCount);
      result.setErrorCount(errorCount);
      result.setMessage(
          String.format("Sync completed. Success: %d, Errors: %d", successCount, errorCount));

      log.info("🎉 External job sync completed successfully! 📊 Summary: Total={}, Success={}, Errors={}, TotalTime={}ms, AvgTimePerJob={}ms", 
          unsyncedJobs.size(), successCount, errorCount, totalProcessingTime, 
          unsyncedJobs.size() > 0 ? totalProcessingTime / unsyncedJobs.size() : 0);

    } catch (Exception e) {
      long totalProcessingTime = System.currentTimeMillis() - startTime;
      log.error("💥 Fatal error during external job sync after {}ms: {}", totalProcessingTime, e.getMessage(), e);
      result.setSuccess(false);
      result.setMessage("Error during sync: " + e.getMessage());
    }

    return result;
  }

  /**
   * Get sync statistics
   */
  public SyncStatistics getSyncStatistics() {
    SyncStatistics stats = new SyncStatistics();

    long totalExternalJobs = externalJobDetailRepository.count();
    long syncedJobs = externalJobDetailRepository.countByIsSyncedToJobTableTrue();
    long refinedUnsyncedJobs = externalJobDetailRepository.countByIsRefinedTrueAndIsSyncedToJobTableFalse();
    long totalUnsyncedJobs = totalExternalJobs - syncedJobs;

    stats.setTotalExternalJobs(totalExternalJobs);
    stats.setSyncedJobs(syncedJobs);
    stats.setUnsyncedJobs(refinedUnsyncedJobs); // Only count refined unsynced jobs
    stats.setSyncPercentage(
        totalExternalJobs > 0 ? (double) syncedJobs / totalExternalJobs * 100 : 0);

    return stats;
  }

  // Result classes
  public static class SyncResult {

    private boolean success;
    private String message;
    private int totalJobs;
    private int successCount;
    private int errorCount;

    // Getters and setters
    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public int getTotalJobs() {
      return totalJobs;
    }

    public void setTotalJobs(int totalJobs) {
      this.totalJobs = totalJobs;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public void setSuccessCount(int successCount) {
      this.successCount = successCount;
    }

    public int getErrorCount() {
      return errorCount;
    }

    public void setErrorCount(int errorCount) {
      this.errorCount = errorCount;
    }
  }

  public static class SyncStatistics {

    private long totalExternalJobs;
    private long syncedJobs;
    private long unsyncedJobs;
    private double syncPercentage;

    // Getters and setters
    public long getTotalExternalJobs() {
      return totalExternalJobs;
    }

    public void setTotalExternalJobs(long totalExternalJobs) {
      this.totalExternalJobs = totalExternalJobs;
    }

    public long getSyncedJobs() {
      return syncedJobs;
    }

    public void setSyncedJobs(long syncedJobs) {
      this.syncedJobs = syncedJobs;
    }

    public long getUnsyncedJobs() {
      return unsyncedJobs;
    }

    public void setUnsyncedJobs(long unsyncedJobs) {
      this.unsyncedJobs = unsyncedJobs;
    }

    public double getSyncPercentage() {
      return syncPercentage;
    }

    public void setSyncPercentage(double syncPercentage) {
      this.syncPercentage = syncPercentage;
    }
  }
}
