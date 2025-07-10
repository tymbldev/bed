package com.tymbl.common.service;

import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyDataService {

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Clean up duplicate companies by name before loading new data
     */
    @Transactional
    public List<String> cleanupDuplicateCompanies() {
        List<String> results = new ArrayList<>();
        
        try {
            // Get all companies
            List<Company> allCompanies = companyRepository.findAll();
            Map<String, List<Company>> companiesByName = allCompanies.stream()
                .collect(Collectors.groupingBy(Company::getName));
            
            int duplicatesRemoved = 0;
            
            for (Map.Entry<String, List<Company>> entry : companiesByName.entrySet()) {
                String name = entry.getKey();
                List<Company> companies = entry.getValue();
                
                if (companies.size() > 1) {
                    // Keep the first one, remove the rest
                    Company toKeep = companies.get(0);
                    List<Company> toRemove = companies.subList(1, companies.size());
                    
                    for (Company company : toRemove) {
                        companyRepository.delete(company);
                        duplicatesRemoved++;
                    }
                    
                    results.add("Removed " + toRemove.size() + " duplicate(s) for company: " + name);
                }
            }
            
            if (duplicatesRemoved == 0) {
                results.add("No duplicate companies found");
            } else {
                results.add("Total duplicates removed: " + duplicatesRemoved);
            }
            
        } catch (Exception e) {
            results.add("Error cleaning up duplicates: " + e.getMessage());
        }
        
        return results;
    }

    public List<String> loadBasicCompanyData() {
        List<String> results = new ArrayList<>();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db/companies_basic.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header line
                }
                
                String[] fields = line.split("\\|");
                if (fields.length >= 11) {
                    try {
                        String result = processBasicCompanyData(fields);
                        results.add(result);
                    } catch (Exception e) {
                        String errorMsg = "Error processing line: " + line.substring(0, Math.min(50, line.length())) + "... Error: " + e.getMessage();
                        results.add(errorMsg);
                    }
                } else {
                    results.add("Invalid line format (expected 11 fields): " + line.substring(0, Math.min(50, line.length())) + "...");
                }
            }
        } catch (IOException e) {
            results.add("Error reading file: " + e.getMessage());
        }
        
        return results;
    }

    public List<String> updateDetailedCompanyData() {
        List<String> results = new ArrayList<>();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db/companies_detailed.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header line
                }
                
                String[] fields = line.split("\\|");
                if (fields.length >= 5) {
                    try {
                        String result = processDetailedCompanyData(fields);
                        results.add(result);
                    } catch (Exception e) {
                        String errorMsg = "Error processing detailed data line: " + line.substring(0, Math.min(50, line.length())) + "... Error: " + e.getMessage();
                        results.add(errorMsg);
                    }
                } else {
                    results.add("Invalid detailed data line format (expected 5 fields): " + line.substring(0, Math.min(50, line.length())) + "...");
                }
            }
        } catch (IOException e) {
            results.add("Error reading file: " + e.getMessage());
        }
        
        return results;
    }

    @Transactional
    private String processBasicCompanyData(String[] fields) {
        try {
            Long id = Long.parseLong(fields[0]);
            String name = fields[1];
            String description = fields[2];
            String website = fields[3];
            String logoUrl = fields[4];
            String headquarters = fields[5];
            String industry = fields[6];
            String companySize = fields[7];
            String specialties = fields[8];
            String linkedinUrl = fields[9];
            String careersUrl = fields[10];

            // First check if company exists by name (to handle duplicates)
            Optional<Company> existingCompanyByName = companyRepository.findByName(name);
            
            if (existingCompanyByName.isPresent()) {
                // Update existing company by name
                Company company = existingCompanyByName.get();
                updateCompanyBasicFields(company, name, description, website, logoUrl, headquarters, 
                                       industry, companySize, specialties, linkedinUrl, careersUrl);
                companyRepository.save(company);
                return "Updated existing company by name: " + name;
            } else {
                // Check if company exists by ID
                Optional<Company> existingCompanyById = companyRepository.findById(id);
                
                if (existingCompanyById.isPresent()) {
                    // Update existing company by ID
                    Company company = existingCompanyById.get();
                    updateCompanyBasicFields(company, name, description, website, logoUrl, headquarters, 
                                           industry, companySize, specialties, linkedinUrl, careersUrl);
                    companyRepository.save(company);
                    return "Updated company by ID: " + name;
                } else {
                    // Create new company
                    Company company = new Company();
                    company.setId(id);
                    company.setName(name);
                    company.setDescription(description);
                    company.setWebsite(website);
                    company.setLogoUrl(logoUrl);
                    company.setHeadquarters(headquarters);
                    company.setIndustry(industry);
                    company.setCompanySize(companySize);
                    company.setSpecialties(specialties);
                    company.setLinkedinUrl(linkedinUrl);
                    company.setCareerPageUrl(careersUrl);
                    
                    companyRepository.save(company);
                    return "Created company: " + name;
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error processing company data: " + e.getMessage();
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                errorMessage = "Database constraint violation for company data: " + e.getCause().getMessage();
            }
            return errorMessage;
        }
    }

    @Transactional
    private String processDetailedCompanyData(String[] fields) {
        try {
            String companyName = fields[0];
            String aboutUs = fields[1];
            String vision = fields[2];
            String mission = fields[3];
            String culture = fields[4];

            Optional<Company> existingCompany = companyRepository.findByName(companyName);
            
            if (existingCompany.isPresent()) {
                Company company = existingCompany.get();
                boolean updated = false;
                
                // Only update if current field is null or empty
                if (company.getAboutUs() == null || company.getAboutUs().trim().isEmpty()) {
                    company.setAboutUs(aboutUs);
                    updated = true;
                }
                
                if (company.getVision() == null || company.getVision().trim().isEmpty()) {
                    company.setVision(vision);
                    updated = true;
                }
                
                if (company.getMission() == null || company.getMission().trim().isEmpty()) {
                    company.setMission(mission);
                    updated = true;
                }
                
                if (company.getCulture() == null || company.getCulture().trim().isEmpty()) {
                    company.setCulture(culture);
                    updated = true;
                }
                
                if (updated) {
                    companyRepository.save(company);
                    return "Updated detailed data for company: " + company.getName();
                } else {
                    return "No updates needed for company: " + company.getName() + " (all fields already populated)";
                }
            } else {
                return "Company not found with name: " + companyName;
            }
        } catch (Exception e) {
            String errorMessage = "Error processing detailed company data: " + e.getMessage();
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                errorMessage = "Database constraint violation for detailed company data: " + e.getCause().getMessage();
            }
            return errorMessage;
        }
    }

    private void updateCompanyBasicFields(Company company, String name, String description, String website, 
                                        String logoUrl, String headquarters, String industry, String companySize, 
                                        String specialties, String linkedinUrl, String careersUrl) {
        
        // Update only if current field is null or empty
        if (company.getName() == null || company.getName().trim().isEmpty()) {
            company.setName(name);
        }
        
        if (company.getDescription() == null || company.getDescription().trim().isEmpty()) {
            company.setDescription(description);
        }
        
        if (company.getWebsite() == null || company.getWebsite().trim().isEmpty()) {
            company.setWebsite(website);
        }
        
        if (company.getLogoUrl() == null || company.getLogoUrl().trim().isEmpty()) {
            company.setLogoUrl(logoUrl);
        }
        
        if (company.getHeadquarters() == null || company.getHeadquarters().trim().isEmpty()) {
            company.setHeadquarters(headquarters);
        }
        
        if (company.getIndustry() == null || company.getIndustry().trim().isEmpty()) {
            company.setIndustry(industry);
        }
        
        if (company.getCompanySize() == null || company.getCompanySize().trim().isEmpty()) {
            company.setCompanySize(companySize);
        }
        
        if (company.getSpecialties() == null || company.getSpecialties().trim().isEmpty()) {
            company.setSpecialties(specialties);
        }
        
        if (company.getLinkedinUrl() == null || company.getLinkedinUrl().trim().isEmpty()) {
            company.setLinkedinUrl(linkedinUrl);
        }
        
        if (company.getCareerPageUrl() == null || company.getCareerPageUrl().trim().isEmpty()) {
            company.setCareerPageUrl(careersUrl);
        }
    }
} 