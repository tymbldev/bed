package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for orchestrating job content refinement operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobContentRefinementService {

  @Autowired
  private ExternalJobDetailRepository externalJobDetailRepository;

  @Autowired
  private SingleJobContentRefinementService singleJobContentRefinementService;

  /**
   * Refine all unprocessed external job content
   */
  public int refineAllUnprocessedContent() {
    long startTime = System.currentTimeMillis();
    log.info("🚀 Starting content refinement for all unprocessed external jobs");
    
    try {
      log.info("📊 Querying database for unrefined external jobs...");
      List<ExternalJobDetail> unprocessedJobs = externalJobDetailRepository.findByIsRefinedFalse();
      log.info("📋 Found {} unrefined external jobs in database", unprocessedJobs.size());

      if (unprocessedJobs.isEmpty()) {
        log.info("✅ No unrefined external jobs found - refinement process completed immediately");
        return 0;
      }

      int processedCount = 0;
      int errorCount = 0;
      int currentJobIndex = 0;

      log.info("🔄 Starting content refinement for {} unrefined jobs", unprocessedJobs.size());

      for (ExternalJobDetail job : unprocessedJobs) {
        currentJobIndex++;
        long jobStartTime = System.currentTimeMillis();
        
        log.info("⏳ Processing job {}/{}: ID={}, Title='{}', Portal='{}'", 
            currentJobIndex, unprocessedJobs.size(), job.getId(), 
            job.getJobTitle(), job.getPortalName());

        try {
          log.debug("🔄 Calling single job content refinement service for job ID: {}", job.getId());
          
          // Use the single job refinement service with transaction
          singleJobContentRefinementService.refineJobContent(job,
              null); // No designation available for unprocessed jobs
          
          long jobProcessingTime = System.currentTimeMillis() - jobStartTime;
          processedCount++;
          
          log.info("✅ Successfully refined job {}/{}: ID={}, Title='{}', ProcessingTime={}ms", 
              currentJobIndex, unprocessedJobs.size(), job.getId(), 
              job.getJobTitle(), jobProcessingTime);

          // Add small delay to avoid overwhelming the AI service
          log.debug("⏸️ Adding 1-second delay before next job refinement to avoid overwhelming AI service");
          Thread.sleep(1000);

        } catch (Exception e) {
          long jobProcessingTime = System.currentTimeMillis() - jobStartTime;
          errorCount++;
          log.error("💥 Error refining job content {}/{}: ID={}, Title='{}', ProcessingTime={}ms, Error='{}'", 
              currentJobIndex, unprocessedJobs.size(), job.getId(), 
              job.getJobTitle(), jobProcessingTime, e.getMessage(), e);
          // Continue with next job
        }
      }

      long totalProcessingTime = System.currentTimeMillis() - startTime;
      log.info("🎉 Content refinement completed! 📊 Summary: Total={}, Processed={}, Errors={}, TotalTime={}ms, AvgTimePerJob={}ms", 
          unprocessedJobs.size(), processedCount, errorCount, totalProcessingTime, 
          unprocessedJobs.size() > 0 ? totalProcessingTime / unprocessedJobs.size() : 0);
      
      return processedCount;

    } catch (Exception e) {
      long totalProcessingTime = System.currentTimeMillis() - startTime;
      log.error("💥 Fatal error in bulk content refinement after {}ms: {}", totalProcessingTime, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * Refine content for a specific external job by ID
   */
  public boolean refineSpecificJobContent(Long externalJobId) {
    try {
      Optional<ExternalJobDetail> jobOpt = externalJobDetailRepository.findById(externalJobId);
      if (jobOpt.isPresent()) {
        ExternalJobDetail job = jobOpt.get();
        // Use the single job refinement service with transaction
        singleJobContentRefinementService.refineJobContent(job,
            null); // No designation available for specific job refinement
        log.info("Successfully refined content for external job ID: {}", externalJobId);
        return true;
      } else {
        log.warn("External job not found with ID: {}", externalJobId);
        return false;
      }
    } catch (Exception e) {
      log.error("Error refining specific job content for ID {}: {}", externalJobId, e.getMessage(),
          e);
      throw e;
    }
  }
}
