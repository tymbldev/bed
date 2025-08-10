package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tymbl.common.util.CompanyNameCleaner;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyRepository;
import com.tymbl.jobs.repository.CompanyContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyPersistenceService {

    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;

    @Transactional
    public Optional<Company> mapJsonToCompany(String companyName,JsonNode companyData) {
        try {
            Company company = new Company();
            CompanyContent companyContent = new CompanyContent();
            boolean aiError = false;
            
            // Clean and validate the company name
            String cleanedName = CompanyNameCleaner.cleanAndValidateCompanyName(companyName);
            if (cleanedName == null) {
                log.warn("Invalid company name after cleaning: '{}'", companyName);
                return Optional.empty();
            }
            
            // Check if company already exists by name
            Optional<Company> existingCompany = companyRepository.findByName(companyName);
            if (existingCompany.isPresent()) {
                company = existingCompany.get();
                log.info("Found existing company with name: {}, updating details", cleanedName);
                if (containsWebSearchPlaceholder(cleanedName)) {
                    aiError = true;
                }
            } else{
                log.info("Creating new company with name: {}", cleanedName);
                return Optional.empty();
            }

            String description = getStringValue(companyData, "description");
            if (!containsWebSearchPlaceholder(description)) {
                company.setDescription(description);
            } else {
                aiError = true;
            }

            String website = getStringValue(companyData, "website");
            if (!containsWebSearchPlaceholder(website) && isValidUrl(website)) {
                company.setWebsite(website);
            } else {
                company.setWebsite("");
                aiError = true;
            }
            String careerPageUrl = getStringValue(companyData, "career_page_url");
            if (!containsWebSearchPlaceholder(careerPageUrl)) {
                company.setCareerPageUrl(careerPageUrl);
            } else {
                aiError = true;
            }
            String aboutUs = getStringValue(companyData, "about_us");
            if (!containsWebSearchPlaceholder(aboutUs)) {
                companyContent.setAboutUsOriginal(aboutUs);
            } else {
                aiError = true;
            }
            String culture = getStringValue(companyData, "culture");
            if (!containsWebSearchPlaceholder(culture)) {
                companyContent.setCultureOriginal(culture);
            } else {
                aiError = true;
            }
            String mission = getStringValue(companyData, "mission");
            if (!containsWebSearchPlaceholder(mission)) {
                company.setMission(mission);
            } else {
                aiError = true;
            }
            String vision = getStringValue(companyData, "vision");
            if (!containsWebSearchPlaceholder(vision)) {
                company.setVision(vision);
            } else {
                aiError = true;
            }
            String companySize = getStringValue(companyData, "company_size");
            if (!containsWebSearchPlaceholder(companySize)) {
                company.setCompanySize(companySize);
            } else {
                aiError = true;
            }

            String linkedinUrl = getStringValue(companyData, "linkedin_url");
            if (!containsWebSearchPlaceholder(linkedinUrl) && isValidUrl(linkedinUrl)) {
                company.setLinkedinUrl(linkedinUrl);
            } else {
                company.setLinkedinUrl("");
                aiError = true;
            }
            String specialties = getStringValue(companyData, "specialties");
            if (!containsWebSearchPlaceholder(specialties)) {
                company.setSpecialties(specialties);
            } else {
                aiError = true;
            }
            company.setAiError(aiError);

            // Save company first to get the ID, then save company content
            company = companyRepository.save(company);
            companyContent.setCompanyId(company.getId());
            companyContent.setContentShortened(false);
            companyContentRepository.save(companyContent);
            
            return Optional.of(company);
        } catch (Exception e) {
            log.error("Error mapping JSON to Company", e);
            return Optional.empty();
        }
    }

    private String getStringValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    private boolean containsWebSearchPlaceholder(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        return lowerValue.contains("web search") || 
               lowerValue.contains("search results") || 
               lowerValue.contains("i don't have") || 
               lowerValue.contains("i cannot") || 
               lowerValue.contains("i'm unable") ||
               lowerValue.contains("i am unable") ||
               lowerValue.contains("no information") ||
               lowerValue.contains("not available") ||
               lowerValue.contains("unavailable") ||
               lowerValue.contains("i don't know") ||
               lowerValue.contains("i do not know") ||
               lowerValue.contains("cannot provide") ||
               lowerValue.contains("unable to provide") ||
               lowerValue.contains("no data") ||
               lowerValue.contains("no details") ||
               lowerValue.contains("information not found") ||
               lowerValue.contains("details not found") ||
               lowerValue.contains("not found") ||
               lowerValue.contains("unknown") ||
               lowerValue.contains("null") ||
               lowerValue.contains("undefined");
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 