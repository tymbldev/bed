package com.tymbl.common.util;

import com.tymbl.common.dto.CompanyGenerationResponse;
import com.tymbl.common.service.AIService;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyContentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlingService {

    private final AIService aiService;
    private final CompanyContentRepository companyContentRepository;

    public static class CrawlResult {
        private final Company company;
        private final String rawData;
        
        public CrawlResult(Company company, String rawData) {
            this.company = company;
            this.rawData = rawData;
        }
        
        public Company getCompany() {
            return company;
        }
        
        public String getRawData() {
            return rawData;
        }
    }

    public Optional<CrawlResult> crawlCompanyPage(String companyName) {
        try {
            log.info("Generating company information for: {} using AI Service", companyName);
            
            // Use AI Service to generate company information based on company name
            Optional<Company> generatedCompany = aiService.generateCompanyInfo(companyName);
            
            if (generatedCompany.isPresent()) {
                Company company = generatedCompany.get();
                log.info("Successfully generated company information for: {}", company.getName());
                // Create a summary of the generated data as raw data
                String rawData = createRawDataSummary(company);
                return Optional.of(new CrawlResult(company, rawData));
            } else {
                log.warn("Failed to generate company information for: {}", companyName);
                // Fallback: create a basic company object
                Company company = new Company();
                company.setName(companyName);
                company.setLastCrawledAt(LocalDateTime.now());
                company.setCrawled(true);
                company.setCrawledData("Generated using AI Service - Company: " + companyName);
                return Optional.of(new CrawlResult(company, "Generated using AI Service"));
            }
        } catch (Exception e) {
            log.error("Error generating company information for company: {}", companyName, e);
            return Optional.empty();
        }
    }
    
    public Optional<CrawlResult> crawlCompanyPageWithJunkDetection(String companyName) {
        try {
            log.info("Generating company information with junk detection for: {} using AI Service", companyName);

            // Use the enhanced method that includes junk detection
            CompanyGenerationResponse response = aiService.generateCompanyInfoWithJunkDetection(companyName);

            if (response.isSuccess()) {
                if (response.isJunkIdentified()) {
                    log.warn("Company '{}' identified as junk: {}", companyName, response.getJunkReason());
                    // Create a company object marked as junk
                    Company company = new Company();
                    company.setName(companyName);
                    company.setJunkIdentified(true);
                    company.setLastCrawledAt(LocalDateTime.now());
                    company.setCrawled(true);
                    company.setCrawledData("JUNK COMPANY - Reason: " + response.getJunkReason());
                    return Optional.of(new CrawlResult(company, "JUNK COMPANY - " + response.getJunkReason()));
                } else if (response.getCompany() != null) {
                    Company company = response.getCompany();
                    company.setJunkIdentified(false); // Ensure it's marked as not junk
                    log.info("Successfully generated company information for: {}", company.getName());
                    String rawData = createRawDataSummary(company);
                    return Optional.of(new CrawlResult(company, rawData));
                }
            }

            log.warn("Failed to generate company information for: {}", companyName);
            // Fallback: create a basic company object
            Company company = new Company();
            company.setName(companyName);
            company.setLastCrawledAt(LocalDateTime.now());
            company.setCrawled(true);
            company.setCrawledData("Generated using AI Service - Company: " + companyName);
            return Optional.of(new CrawlResult(company, "Generated using AI Service"));
        } catch (Exception e) {
            log.error("Error generating company information for company: {}", companyName, e);
            return Optional.empty();
        }
    }



    private String createRawDataSummary(Company company) {
        StringBuilder summary = new StringBuilder();
        summary.append("Company Information Generated by AI Service\n");
        summary.append("==========================================\n\n");
        summary.append("Name: ").append(company.getName()).append("\n");
        summary.append("Description: ").append(company.getDescription()).append("\n");
        summary.append("Website: ").append(company.getWebsite()).append("\n");
        summary.append("Career Page URL: ").append(company.getCareerPageUrl()).append("\n");
        summary.append("Headquarters: ").append(company.getHeadquarters()).append("\n");
        summary.append("Company Size: ").append(company.getCompanySize()).append("\n");
        summary.append("Specialties: ").append(company.getSpecialties()).append("\n");
        
        // Get original content from CompanyContent table
        Optional<CompanyContent> contentOpt = companyContentRepository.findByCompanyId(company.getId());
        String aboutUsOriginal = "";
        String cultureOriginal = "";
        if (contentOpt.isPresent()) {
            CompanyContent content = contentOpt.get();
            aboutUsOriginal = content.getAboutUsOriginal() != null ? content.getAboutUsOriginal() : "";
            cultureOriginal = content.getCultureOriginal() != null ? content.getCultureOriginal() : "";
        }
        
        summary.append("About Us (Original): ").append(aboutUsOriginal).append("\n");
        summary.append("About Us (Shortened): ").append(company.getAboutUs()).append("\n");
        summary.append("Mission: ").append(company.getMission()).append("\n");
        summary.append("Vision: ").append(company.getVision()).append("\n");
        summary.append("Culture (Original): ").append(cultureOriginal).append("\n");
        summary.append("Culture (Shortened): ").append(company.getCulture()).append("\n");
        summary.append("LinkedIn URL: ").append(company.getLinkedinUrl()).append("\n");
        summary.append("Generated At: ").append(company.getLastCrawledAt()).append("\n");
        return summary.toString();
    }
} 