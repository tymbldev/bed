package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    SyncResult result = new SyncResult();

    try {
      // Get all external job details that haven't been synced yet
      List<ExternalJobDetail> unsyncedJobs = externalJobDetailRepository.findByIsSyncedToJobTableFalse();

      if (unsyncedJobs.isEmpty()) {
        result.setMessage("No unsynced external jobs found");
        result.setSuccess(true);
        return result;
      }

      log.info("Starting sync of {} external jobs to Job table", unsyncedJobs.size());

      int successCount = 0;
      int errorCount = 0;

      for (ExternalJobDetail externalJob : unsyncedJobs) {
        try {
          // Use the individual job sync service (which handles its own transaction)
          IndividualJobSyncService.SyncResult individualResult = individualJobSyncService.syncIndividualJob(
              externalJob);

          if (individualResult.isSuccess()) {
            successCount++;
            log.debug("Successfully synced external job {}: {}", externalJob.getId(),
                individualResult.getMessage());
          } else {
            errorCount++;
            log.warn("Failed to sync external job {}: {}", externalJob.getId(),
                individualResult.getMessage());
          }

        } catch (Exception e) {
          log.error("Error syncing external job {}: {}", externalJob.getId(), e.getMessage(), e);
          errorCount++;
        }
      }

      result.setSuccess(true);
      result.setTotalJobs(unsyncedJobs.size());
      result.setSuccessCount(successCount);
      result.setErrorCount(errorCount);
      result.setMessage(
          String.format("Sync completed. Success: %d, Errors: %d", successCount, errorCount));

      log.info("External job sync completed. Total: {}, Success: {}, Errors: {}",
          unsyncedJobs.size(), successCount, errorCount);

    } catch (Exception e) {
      log.error("Error during external job sync: {}", e.getMessage(), e);
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
    long unsyncedJobs = totalExternalJobs - syncedJobs;

    stats.setTotalExternalJobs(totalExternalJobs);
    stats.setSyncedJobs(syncedJobs);
    stats.setUnsyncedJobs(unsyncedJobs);
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
