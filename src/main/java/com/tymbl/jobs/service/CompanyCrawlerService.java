package com.tymbl.jobs.service;

import com.tymbl.common.util.CrawlingService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.CompanyContentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyCrawlerService {

    private final CrawlingService crawlingService;
    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;
    private final CompanyTransactionService companyTransactionService;
    
    private static final int BATCH_SIZE = 10; // Process 10 companies at a time

    //@Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void crawlCompanies() {
        log.info("Starting company crawling process");
        
        int pageNumber = 0;
        boolean hasMoreCompanies = true;
        
        while (hasMoreCompanies) {
            try {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Company> companyPage = companyRepository.findByIsCrawledFalse(pageable);
                if (companyPage.isEmpty()) {
                    log.info("No more companies to crawl");
                    hasMoreCompanies = false;
                    break;
                }
                
                log.info("Processing batch {} with {} companies", pageNumber + 1, companyPage.getContent().size());
                
                for (Company company : companyPage.getContent()) {
                    try {
                        companyTransactionService.processCompanyCrawlingInTransaction(company);
                        // Add delay to avoid rate limiting
                       //Thread.sleep(60000);
                    } catch (Exception e) {
                        log.error("Error processing company: " + company.getName(), e);
                    }
                }
                
                pageNumber++;
                
            } catch (Exception e) {
                log.error("Error processing batch {}", pageNumber + 1, e);
                break;
            }
        }
        
        log.info("Completed company crawling process");
    }

    /**
     * Crawl companies in batches with individual transactions per batch
     * This ensures that each batch is processed in its own transaction
     */
    public void crawlCompaniesInBatches() {
        log.info("Starting company crawling process in batches");
        
        int pageNumber = 0;
        boolean hasMoreCompanies = true;
        int totalProcessed = 0;
        int totalErrors = 0;
        
        while (hasMoreCompanies) {
            try {
                Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
                Page<Company> companyPage = companyRepository.findByIsCrawledFalse(pageable);
                if (companyPage.isEmpty()) {
                    log.info("No more companies to crawl");
                    hasMoreCompanies = false;
                    break;
                }
                
                log.info("Processing crawling batch {} with {} companies", pageNumber + 1, companyPage.getContent().size());
                
                // Process each company in the batch with its own transaction
                for (Company company : companyPage.getContent()) {
                    try {
                        companyTransactionService.processCompanyCrawlingInTransaction(company);
                        totalProcessed++;
                        log.info("Successfully processed company: {} (ID: {})", company.getName(), company.getId());
                    } catch (Exception e) {
                        totalErrors++;
                        log.error("Error processing company: {} (ID: {})", company.getName(), company.getId(), e);
                    }
                }
                
                pageNumber++;
                
            } catch (Exception e) {
                log.error("Error processing crawling batch {}", pageNumber + 1, e);
                totalErrors++;
                pageNumber++;
            }
        }
        
        log.info("Completed company crawling in batches. Total processed: {}, Total errors: {}", totalProcessed, totalErrors);
    }
} 