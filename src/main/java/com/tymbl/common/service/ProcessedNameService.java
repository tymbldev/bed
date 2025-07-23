package com.tymbl.common.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedNameService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CompanyRepository companyRepository;
    private final DesignationRepository designationRepository;

    // Cache to track processed names during batch processing
    private final ConcurrentHashMap<String, String> processedNameCache = new ConcurrentHashMap<>();

    /**
     * Generate processed names for all unprocessed entities
     */
    @Transactional
    public Map<String, Object> generateProcessedNamesForAllEntities() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Starting processed name generation for all entities");
            
            // Process each entity type
            Map<String, Object> countriesResult = generateProcessedNamesForCountries();
            Map<String, Object> citiesResult = generateProcessedNamesForCities();
            Map<String, Object> companiesResult = generateProcessedNamesForCompanies();
            Map<String, Object> designationsResult = generateProcessedNamesForDesignations();
            
            // Compile results
            result.put("success", true);
            result.put("message", "Processed name generation completed");
            result.put("countries", countriesResult);
            result.put("cities", citiesResult);
            result.put("companies", companiesResult);
            result.put("designations", designationsResult);
            
            // Clear cache after processing
            processedNameCache.clear();
            
            log.info("Processed name generation completed successfully");
            
        } catch (Exception e) {
            log.error("Error generating processed names for all entities", e);
            result.put("success", false);
            result.put("error", "Error generating processed names: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate processed names for countries
     */
    @Transactional
    public Map<String, Object> generateProcessedNamesForCountries() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Country> unprocessedCountries = countryRepository.findByProcessedNameGeneratedFalse();
            log.info("Found {} unprocessed countries", unprocessedCountries.size());
            
            int processed = 0;
            int errors = 0;
            
            for (Country country : unprocessedCountries) {
                try {
                    String processedName = generateProcessedName(country.getName(), "country");
                    country.setProcessedName(processedName);
                    country.setProcessedNameGenerated(true);
                    countryRepository.save(country);
                    processed++;
                    
                    log.debug("Generated processed name for country: {} -> {}", country.getName(), processedName);
                    
                } catch (Exception e) {
                    log.error("Error processing country: {}", country.getName(), e);
                    errors++;
                }
            }
            
            result.put("total", unprocessedCountries.size());
            result.put("processed", processed);
            result.put("errors", errors);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("Error generating processed names for countries", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate processed names for cities
     */
    @Transactional
    public Map<String, Object> generateProcessedNamesForCities() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<City> unprocessedCities = cityRepository.findByProcessedNameGeneratedFalse();
            log.info("Found {} unprocessed cities", unprocessedCities.size());
            
            int processed = 0;
            int errors = 0;
            
            for (City city : unprocessedCities) {
                try {
                    String processedName = generateProcessedName(city.getName(), "city");
                    city.setProcessedName(processedName);
                    city.setProcessedNameGenerated(true);
                    cityRepository.save(city);
                    processed++;
                    
                    log.debug("Generated processed name for city: {} -> {}", city.getName(), processedName);
                    
                } catch (Exception e) {
                    log.error("Error processing city: {}", city.getName(), e);
                    errors++;
                }
            }
            
            result.put("total", unprocessedCities.size());
            result.put("processed", processed);
            result.put("errors", errors);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("Error generating processed names for cities", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate processed names for companies
     */
    @Transactional
    public Map<String, Object> generateProcessedNamesForCompanies() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Company> unprocessedCompanies = companyRepository.findByProcessedNameGeneratedFalse();
            log.info("Found {} unprocessed companies", unprocessedCompanies.size());
            
            int processed = 0;
            int errors = 0;
            
            for (Company company : unprocessedCompanies) {
                try {
                    String processedName = generateProcessedName(company.getName(), "company");
                    company.setProcessedName(processedName);
                    company.setProcessedNameGenerated(true);
                    companyRepository.save(company);
                    processed++;
                    
                    log.debug("Generated processed name for company: {} -> {}", company.getName(), processedName);
                    
                } catch (Exception e) {
                    log.error("Error processing company: {}", company.getName(), e);
                    errors++;
                }
            }
            
            result.put("total", unprocessedCompanies.size());
            result.put("processed", processed);
            result.put("errors", errors);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("Error generating processed names for companies", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate processed names for designations
     */
    @Transactional
    public Map<String, Object> generateProcessedNamesForDesignations() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Designation> unprocessedDesignations = designationRepository.findByProcessedNameGeneratedFalse();
            log.info("Found {} unprocessed designations", unprocessedDesignations.size());
            
            int processed = 0;
            int errors = 0;
            
            for (Designation designation : unprocessedDesignations) {
                try {
                    String processedName = generateProcessedName(designation.getName(), "designation");
                    designation.setProcessedName(processedName);
                    designation.setProcessedNameGenerated(true);
                    designationRepository.save(designation);
                    processed++;
                    
                    log.debug("Generated processed name for designation: {} -> {}", designation.getName(), processedName);
                    
                } catch (Exception e) {
                    log.error("Error processing designation: {}", designation.getName(), e);
                    errors++;
                }
            }
            
            result.put("total", unprocessedDesignations.size());
            result.put("processed", processed);
            result.put("errors", errors);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("Error generating processed names for designations", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * Generate a processed name from the original name
     * Rules:
     * 1. Remove special characters (dots, commas, etc.)
     * 2. Remove common suffixes (.com, pvt ltd, pvt ltd., etc.)
     * 3. Convert to lowercase
     * 4. Remove extra spaces
     * 5. Ensure uniqueness
     */
    private String generateProcessedName(String originalName, String entityType) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return null;
        }
        
        // Convert to lowercase and trim
        String processed = originalName.toLowerCase().trim();
        
        // Remove special characters (dots, commas, parentheses, etc.)
        processed = processed.replaceAll("[.,()\\[\\]{}!@#$%^&*+=|\\\\/\"'`~]", "");
        
        // Remove common company suffixes
        processed = removeCompanySuffixes(processed);
        
        // Remove extra spaces and convert to single spaces
        processed = processed.replaceAll("\\s+", " ");
        
        // Trim again
        processed = processed.trim();
        
        // Ensure uniqueness by adding suffix if needed
        String uniqueProcessedName = ensureUniqueness(processed, entityType);
        
        // Cache the result
        processedNameCache.put(originalName.toLowerCase(), uniqueProcessedName);
        
        return uniqueProcessedName;
    }

    /**
     * Remove common company suffixes
     */
    private String removeCompanySuffixes(String name) {
        // Common suffixes to remove
        String[] suffixes = {
            ".com", ".org", ".net", ".edu", ".gov", ".co", ".in", ".uk", ".us",
            "pvt ltd", "pvt ltd.", "private limited", "private ltd", "private ltd.",
            "ltd", "ltd.", "limited", "inc", "inc.", "incorporated",
            "corp", "corp.", "corporation", "company", "co", "co.",
            "group", "technologies", "tech", "solutions", "systems", "services",
            "international", "intl", "intl.", "global", "worldwide", "world wide"
        };
        
        String processed = name;
        for (String suffix : suffixes) {
            // Remove suffix with space before it
            processed = processed.replaceAll("\\s+" + suffix + "\\s*$", "");
            // Remove suffix without space before it
            processed = processed.replaceAll(suffix + "\\s*$", "");
        }
        
        return processed;
    }

    /**
     * Ensure uniqueness of processed name
     */
    private String ensureUniqueness(String processedName, String entityType) {
        String baseName = processedName;
        int counter = 1;
        
        while (isProcessedNameExists(processedName, entityType) || processedNameCache.containsValue(processedName)) {
            processedName = baseName + "_" + counter;
            counter++;
        }
        
        return processedName;
    }

    /**
     * Check if processed name already exists in database
     */
    private boolean isProcessedNameExists(String processedName, String entityType) {
        switch (entityType.toLowerCase()) {
            case "country":
                return countryRepository.existsByProcessedName(processedName);
            case "city":
                return cityRepository.existsByProcessedName(processedName);
            case "company":
                return companyRepository.existsByProcessedName(processedName);
            case "designation":
                return designationRepository.existsByProcessedName(processedName);
            default:
                return false;
        }
    }

    /**
     * Reset processed name generation flag for all entities
     */
    @Transactional
    public Map<String, Object> resetProcessedNameGenerationFlag() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Resetting processed name generation flags for all entities");
            
            // Reset flags for all entity types
            countryRepository.resetProcessedNameGeneratedFlag();
            cityRepository.resetProcessedNameGeneratedFlag();
            companyRepository.resetProcessedNameGeneratedFlag();
            designationRepository.resetProcessedNameGeneratedFlag();
            
            result.put("success", true);
            result.put("message", "Processed name generation flags reset successfully for all entities");
            
            log.info("Processed name generation flags reset successfully");
            
        } catch (Exception e) {
            log.error("Error resetting processed name generation flags", e);
            result.put("success", false);
            result.put("error", "Error resetting processed name generation flags: " + e.getMessage());
        }
        
        return result;
    }
} 