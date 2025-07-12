package com.tymbl.jobs.service;

import com.tymbl.common.util.CrawlingService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyCrawlerService {

    private final CrawlingService crawlingService;
    private final CompanyRepository companyRepository;
    
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
                        processCompanyInTransaction(company);
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

    @Transactional
    public void processCompanyInTransaction(Company company) {
        try {
            log.info("Processing company: {} (ID: {})", company.getName(), company.getId());
            
            // Check if company has LinkedIn URL
            if (company.getLinkedinUrl() == null || company.getLinkedinUrl().trim().isEmpty()) {
                log.warn("Company {} has no LinkedIn URL, marking as failed to crawl", company.getName());
                company.setCrawled(false);
                company.setLastCrawledAt(LocalDateTime.now());
                companyRepository.save(company);
                return;
            }

            Optional<CrawlingService.CrawlResult> crawlResult = crawlingService.crawlCompanyPage(company.getLinkedinUrl());
            
            if (crawlResult.isPresent()) {
                CrawlingService.CrawlResult result = crawlResult.get();
                Company generatedCompany = result.getCompany();
                String rawData = result.getRawData();
                
                // Update company with generated information
                updateCompanyFields(company, generatedCompany);
                company.setCrawledData(rawData);
                company.setCrawled(true); // Success - flag = 1
                company.setLastCrawledAt(LocalDateTime.now());
                
                companyRepository.save(company);
                log.info("Successfully updated company information for: {} (ID: {})", company.getName(), company.getId());
                
            } else {
                // Failed to generate information - flag = 2
                log.warn("Failed to generate company information for: {} (ID: {}), marking as failed", company.getName(), company.getId());
                company.setCrawled(false);
                company.setLastCrawledAt(LocalDateTime.now());
                companyRepository.save(company);
            }
            
        } catch (Exception e) {
            log.error("Error processing company: {} (ID: {})", company.getName(), company.getId(), e);
            // Mark as failed to crawl - flag = 2
            company.setCrawled(false);
            company.setLastCrawledAt(LocalDateTime.now());
            companyRepository.save(company);
            throw e; // Re-throw to ensure transaction rollback
        }
    }

    private void updateCompanyFields(Company existing, Company generated) {
        // Update all available fields from the generated company data
        if (generated.getDescription() != null && !generated.getDescription().trim().isEmpty()) {
            existing.setDescription(generated.getDescription());
        }
        if (generated.getLogoUrl() != null && !generated.getLogoUrl().trim().isEmpty()) {
            existing.setLogoUrl(generated.getLogoUrl());
        }
        if (generated.getWebsite() != null && !generated.getWebsite().trim().isEmpty()) {
            existing.setWebsite(generated.getWebsite());
        }
        if (generated.getCareerPageUrl() != null && !generated.getCareerPageUrl().trim().isEmpty()) {
            existing.setCareerPageUrl(generated.getCareerPageUrl());
        }
        if (generated.getAboutUs() != null && !generated.getAboutUs().trim().isEmpty()) {
            existing.setAboutUs(generated.getAboutUs());
        }
        if (generated.getCulture() != null && !generated.getCulture().trim().isEmpty()) {
            existing.setCulture(generated.getCulture());
        }
        if (generated.getMission() != null && !generated.getMission().trim().isEmpty()) {
            existing.setMission(generated.getMission());
        }
        if (generated.getVision() != null && !generated.getVision().trim().isEmpty()) {
            existing.setVision(generated.getVision());
        }
        if (generated.getCompanySize() != null && !generated.getCompanySize().trim().isEmpty()) {
            existing.setCompanySize(generated.getCompanySize());
        }
        if (generated.getHeadquarters() != null && !generated.getHeadquarters().trim().isEmpty()) {
            existing.setHeadquarters(generated.getHeadquarters());
        }
        if (generated.getSpecialties() != null && !generated.getSpecialties().trim().isEmpty()) {
            existing.setSpecialties(generated.getSpecialties());
        }
        // Keep the existing LinkedIn URL as it was provided
        if (generated.getLinkedinUrl() != null && !generated.getLinkedinUrl().trim().isEmpty()) {
            existing.setLinkedinUrl(generated.getLinkedinUrl());
        }
    }
} 