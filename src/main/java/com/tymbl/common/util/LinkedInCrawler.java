package com.tymbl.common.util;

import com.tymbl.common.entity.Job;
import com.tymbl.common.service.GeminiService;
import com.tymbl.jobs.entity.Company;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    private final GeminiService geminiService;

    public Optional<Company> crawlCompanyPage(String linkedinUrl) {
        try {
            Document doc = Jsoup.connect(linkedinUrl)
                    .userAgent(USER_AGENT)
                    .get();

            // Extract the document text for Gemini AI processing
            String documentText = doc.text();
            
            // Use Gemini AI to extract company information
            Optional<Company> extractedCompany = geminiService.extractCompanyInfo(documentText);
            
            if (extractedCompany.isPresent()) {
                Company company = extractedCompany.get();
                // Ensure the LinkedIn URL is set
                company.setLinkedinUrl(linkedinUrl);
                log.info("Successfully extracted company information for: {}", company.getName());
                return Optional.of(company);
            } else {
                log.warn("Failed to extract company information from LinkedIn page: {}", linkedinUrl);
                // Fallback: create a basic company object
                Company company = new Company();
                company.setLinkedinUrl(linkedinUrl);
                company.setLastCrawledAt(LocalDateTime.now());
                company.setCrawled(true);
                return Optional.of(company);
            }
        } catch (IOException e) {
            log.error("Error crawling LinkedIn page: " + linkedinUrl, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error while crawling LinkedIn page: " + linkedinUrl, e);
            return Optional.empty();
        }
    }

    public List<Job> crawlCompanyJobs(String companyName, Long companyId) {
        List<Job> jobs = new ArrayList<>();
        String jobsUrl = String.format("https://www.linkedin.com/company/%s/jobs/", companyName.toLowerCase().replace(" ", "-"));

        try {
            Document doc = Jsoup.connect(jobsUrl)
                    .userAgent(USER_AGENT)
                    .get();

            Elements jobElements = doc.select("li.jobs-search-results__list-item");

            for (Element jobElement : jobElements) {
                try {
                    Job job = new Job();
                    job.setCompanyId(companyId);
                    job.setPostedById(0L); // Set as super admin
                    job.setCreatedAt(LocalDateTime.now());
                    job.setUpdatedAt(LocalDateTime.now());

                    // Extract job title
                    Elements titleElement = jobElement.select("h3.job-card-list__title");
                    job.setTitle(titleElement.text().trim());

                    // Extract job location
                    Elements locationElement = jobElement.select("span.job-card-container__metadata-item");
                   // job.setLocation(locationElement.text().trim());

                    // Extract job description
                    Elements descriptionElement = jobElement.select("div.job-card-list__description");
                    job.setDescription(descriptionElement.text().trim());

                    // Extract employment type
                    Elements typeElement = jobElement.select("span.job-card-container__metadata-item--workplace-type");
                   // job.setEmploymentType(typeElement.text().trim());

                    // Extract application URL
                    Elements linkElement = jobElement.select("a.job-card-list__title");
                    String jobUrl = linkElement.attr("href");
                    //job.setApplicationUrl("https://www.linkedin.com" + jobUrl);

                    // Set default values
                   // job.setStatus("ACTIVE");
                    //job.setSalaryRange("Not specified");
                   // job.setExperienceRequired("Not specified");
                    //job.setSkillsRequired("Not specified");

                    jobs.add(job);
                } catch (Exception e) {
                    log.error("Error parsing job element for company: " + companyName, e);
                }
            }
        } catch (IOException e) {
            log.error("Error crawling jobs for company: " + companyName, e);
        }

        return jobs;
    }
} 