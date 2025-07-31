package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyCleanupService {

  private static final Logger logger = LoggerFactory.getLogger(CompanyCleanupService.class);

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private CompanyTransactionService companyTransactionService;


  /**
   * Process all unprocessed companies for cleanup in batches
   */
  public Map<String, Object> processAllCompaniesInBatches() {
    logger.info("Starting company cleanup process for all unprocessed companies in batches");

    List<Company> unprocessedCompanies = companyRepository.findByCleanupProcessedFalse();
    logger.info("Found {} unprocessed companies for cleanup", unprocessedCompanies.size());

    int processedCount = 0;
    int removedCount = 0;
    int junkMarkedCount = 0;
    int renamedCount = 0;
    int noActionCount = 0;
    int errorCount = 0;
    int batchSize = 10; // Process 10 companies at a time

    List<Map<String, Object>> companyResults = new ArrayList<>();

    for (int i = 0; i < unprocessedCompanies.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, unprocessedCompanies.size());
      List<Company> batch = unprocessedCompanies.subList(i, endIndex);

      logger.info("Processing cleanup batch {} with {} companies", (i / batchSize) + 1, batch.size());

      for (Company company : batch) {
        try {
          Map<String, Object> result = companyTransactionService.processCompanyCleanupInTransaction(company);

          // Add company info to result
          result.put("companyId", company.getId());
          result.put("originalName", company.getName());
          companyResults.add(result);

          // Count based on action
          String action = (String) result.get("action");
          if ("removed".equals(action)) {
            removedCount++;
          } else if ("junk_marked".equals(action)) {
            junkMarkedCount++;
          } else if ("renamed".equals(action)) {
            renamedCount++;
          } else if ("no_action".equals(action)) {
            noActionCount++;
          }

          processedCount++;

          logger.info("Processed company: {} - Action: {}", company.getName(), action);

        } catch (Exception e) {
          errorCount++;
          logger.error("Error processing company {}: {}", company.getName(), e.getMessage());

          // Mark as processed to avoid infinite retries
          try {
            company.setCleanupProcessed(true);
            companyRepository.save(company);
          } catch (Exception saveError) {
            logger.error("Failed to mark company as processed: {}", company.getName(), saveError);
          }

          Map<String, Object> errorResult = new HashMap<>();
          errorResult.put("companyId", company.getId());
          errorResult.put("originalName", company.getName());
          errorResult.put("error", e.getMessage());
          companyResults.add(errorResult);
        }
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("totalProcessed", processedCount);
    result.put("removedCount", removedCount);
    result.put("junkMarkedCount", junkMarkedCount);
    result.put("renamedCount", renamedCount);
    result.put("noActionCount", noActionCount);
    result.put("errorCount", errorCount);
    result.put("companyResults", companyResults);

    logger.info("Company cleanup in batches completed. Processed: {}, Removed: {}, Junk Marked: {}, Renamed: {}, No Action: {}, Errors: {}",
        processedCount, removedCount, junkMarkedCount, renamedCount, noActionCount, errorCount);

    return result;
  }
} 