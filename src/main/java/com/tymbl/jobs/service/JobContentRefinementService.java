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
    try {
      List<ExternalJobDetail> unprocessedJobs = externalJobDetailRepository.findByIsRefinedFalse();
      int processedCount = 0;

      for (ExternalJobDetail job : unprocessedJobs) {
        try {
          // Use the single job refinement service with transaction
          singleJobContentRefinementService.refineJobContent(job,
              null); // No designation available for unprocessed jobs
          processedCount++;

          // Add small delay to avoid overwhelming the AI service
          Thread.sleep(1000);

        } catch (Exception e) {
          log.error("Error refining job content for ID {}: {}", job.getId(), e.getMessage(), e);
          // Continue with next job
        }
      }

      log.info("Completed content refinement for {} out of {} unprocessed jobs",
          processedCount, unprocessedJobs.size());
      return processedCount;

    } catch (Exception e) {
      log.error("Error in bulk content refinement: {}", e.getMessage(), e);
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
