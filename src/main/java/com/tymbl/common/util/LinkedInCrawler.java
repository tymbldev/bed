package com.tymbl.common.util;

import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.Job;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LinkedInCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public Optional<Company> crawlCompanyPage(String linkedinUrl) {
        try {
            Document doc = Jsoup.connect(linkedinUrl)
                    .userAgent(USER_AGENT)
                    .get();

            Company company = new Company();
            company.setLinkedinUrl(linkedinUrl);
            company.setLastCrawledAt(LocalDateTime.now());

            // Extract company name
            Elements nameElement = doc.select("h1.org-top-card-summary__title");
            company.setName(nameElement.text().trim());

            // Extract about/description
            Elements aboutElement = doc.select("div.org-about-us-organization-description__text");
            company.setAboutUs(aboutElement.text().trim());

            // Extract headquarters
            Elements locationElement = doc.select("div.org-location-card");
            company.setHeadquarters(locationElement.text().trim());

            // Extract industry
            Elements industryElement = doc.select("div.org-about-company-module__industry");
            company.setIndustry(industryElement.text().trim());

            // Extract company size
            Elements sizeElement = doc.select("div.org-about-company-module__company-size");
            company.setCompanySize(sizeElement.text().trim());

            // Extract specialties
            Elements specialtiesElement = doc.select("div.org-about-company-module__specialties");
            company.setSpecialties(specialtiesElement.text().trim());

            // Extract website
            Elements websiteElement = doc.select("a.org-about-us-company-module__website");
            company.setWebsite(websiteElement.attr("href"));

            // Extract logo
            Elements logoElement = doc.select("img.org-top-card-primary-content__logo");
            company.setLogoUrl(logoElement.attr("src"));

            company.setCrawled(true);

            return Optional.of(company);
        } catch (IOException e) {
            log.error("Error crawling LinkedIn page: " + linkedinUrl, e);
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
                    job.setLocation(locationElement.text().trim());

                    // Extract job description
                    Elements descriptionElement = jobElement.select("div.job-card-list__description");
                    job.setDescription(descriptionElement.text().trim());

                    // Extract employment type
                    Elements typeElement = jobElement.select("span.job-card-container__metadata-item--workplace-type");
                    job.setEmploymentType(typeElement.text().trim());

                    // Extract application URL
                    Elements linkElement = jobElement.select("a.job-card-list__title");
                    String jobUrl = linkElement.attr("href");
                    job.setApplicationUrl("https://www.linkedin.com" + jobUrl);

                    // Set default values
                    job.setStatus("ACTIVE");
                    job.setSalaryRange("Not specified");
                    job.setExperienceRequired("Not specified");
                    job.setSkillsRequired("Not specified");

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