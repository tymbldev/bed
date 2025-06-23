package com.tymbl.jobs.service;

import com.tymbl.common.util.LinkedInCrawler;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.Job;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyCrawlerService {

    private final LinkedInCrawler linkedInCrawler;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public void crawlCompanies() {
        log.info("Starting company crawling process");
        List<CompanyInfo> companies = readCompaniesFile();
        
        for (CompanyInfo companyInfo : companies) {
            try {
                Optional<Company> existingCompany = companyRepository.findByName(companyInfo.name);
                
                if (existingCompany.isPresent() && existingCompany.get().isCrawled()) {
                    log.debug("Company {} already crawled, skipping", companyInfo.name);
                    continue;
                }

                Optional<Company> crawledCompany = linkedInCrawler.crawlCompanyPage(companyInfo.linkedinUrl);
                
                if (crawledCompany.isPresent()) {
                    Company company = crawledCompany.get();
                    if (existingCompany.isPresent()) {
                        // Update existing company
                        Company existing = existingCompany.get();
                        updateCompanyFields(existing, company);
                        companyRepository.save(existing);
                        log.info("Updated company information for: {}", company.getName());
                        
                        // Crawl jobs for existing company
                        crawlJobsForCompany(existing);
                    } else {
                        // Save new company
                        Company savedCompany = companyRepository.save(company);
                        log.info("Saved new company: {}", company.getName());
                        
                        // Crawl jobs for new company
                        crawlJobsForCompany(savedCompany);
                    }
                }
                
                // Add delay to avoid rate limiting
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error("Error processing company: " + companyInfo.name, e);
            }
        }
        log.info("Completed company crawling process");
    }

    @Transactional
    public void crawlJobsForCompany(Company company) {
        log.info("Starting job crawling for company: {}", company.getName());
        List<Job> jobs = linkedInCrawler.crawlCompanyJobs(company.getName(), company.getId());
        
        for (Job job : jobs) {
            try {
                // Check if job already exists based on title and company
                Optional<Job> existingJob = jobRepository.findByTitleAndCompanyId(job.getTitle(), company.getId());
                
                if (existingJob.isPresent()) {
                    // Update existing job
                    Job existing = existingJob.get();
                    updateJobFields(existing, job);
                    jobRepository.save(existing);
                    log.info("Updated job: {} for company: {}", job.getTitle(), company.getName());
                } else {
                    // Save new job
                    jobRepository.save(job);
                    log.info("Saved new job: {} for company: {}", job.getTitle(), company.getName());
                }
            } catch (Exception e) {
                log.error("Error saving job: {} for company: {}", job.getTitle(), company.getName(), e);
            }
        }
        log.info("Completed job crawling for company: {}", company.getName());
    }

    private void updateJobFields(Job existing, Job crawled) {
        existing.setLocation(crawled.getLocation());
        existing.setDescription(crawled.getDescription());
        existing.setEmploymentType(crawled.getEmploymentType());
        existing.setApplicationUrl(crawled.getApplicationUrl());
        existing.setStatus(crawled.getStatus());
        existing.setUpdatedAt(crawled.getUpdatedAt());
    }

    private List<CompanyInfo> readCompaniesFile() {
        List<CompanyInfo> companies = new ArrayList<>();
        try {
            ClassPathResource resource = new ClassPathResource("companies.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        companies.add(new CompanyInfo(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error reading companies file", e);
        }
        return companies;
    }

    private void updateCompanyFields(Company existing, Company crawled) {
        existing.setAboutUs(crawled.getAboutUs());
        existing.setWebsite(crawled.getWebsite());
        existing.setLogoUrl(crawled.getLogoUrl());
        existing.setHeadquarters(crawled.getHeadquarters());
        existing.setIndustry(crawled.getIndustry());
        existing.setCompanySize(crawled.getCompanySize());
        existing.setSpecialties(crawled.getSpecialties());
        existing.setLinkedinUrl(crawled.getLinkedinUrl());
        existing.setCrawled(true);
        existing.setLastCrawledAt(crawled.getLastCrawledAt());
    }

    private static class CompanyInfo {
        final String name;
        final String linkedinUrl;

        CompanyInfo(String name, String linkedinUrl) {
            this.name = name;
            this.linkedinUrl = linkedinUrl;
        }
    }
} 