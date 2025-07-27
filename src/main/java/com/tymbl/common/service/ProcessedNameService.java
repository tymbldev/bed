package com.tymbl.common.service;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.util.DesignationNameCleaner;
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
            int cleaned = 0;
            
            for (Designation designation : unprocessedDesignations) {
                try {
                    // First clean the designation name if needed
                    String originalName = designation.getName();
                    String cleanedName = DesignationNameCleaner.cleanAndValidateDesignationName(originalName);
                    
                    if (cleanedName != null && !cleanedName.equals(originalName)) {
                        designation.setName(cleanedName);
                        designationRepository.save(designation);
                        cleaned++;
                        log.debug("Cleaned designation name: '{}' -> '{}'", originalName, cleanedName);
                    }
                    
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
            result.put("cleaned", cleaned);
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
        
        // Remove brackets and parentheses with their content using regex
        String bracketsRegEx = "(\\[|\\().+?(\\]|\\))\\s*";
        processed = processed.replaceAll(bracketsRegEx, "");
        
        // Remove special characters (dots, commas, parentheses, etc.)
        processed = processed.replaceAll("[.,()\\[\\]{}!@#$%^&*+=|\\\\/\"'`~]", "");
        
        // Remove common company suffixes
        processed = removeCompanySuffixes(processed);
        
        // Remove extra spaces and convert to single spaces
        processed = processed.replaceAll("\\s+", " ");
        
        // Handle ampersand surrounded by words (convert "word and word" to "word & word")
        processed = processed.replaceAll("(.*[a-zA-Z0-9]+.*) and (.*[a-zA-Z0-9]+.*)", "$1 & $2");
        
        // Remove everything except alphanumeric, hyphens, and spaces
        processed = processed.replaceAll("[^a-zA-Z0-9&\\-\\s]+", "");
        
        // Final cleanup - remove extra spaces again
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
     * Ensure uniqueness of processed name by removing duplicates
     */
    private String ensureUniqueness(String processedName, String entityType) {
        // If processed name already exists, remove the duplicate entry
        if (isProcessedNameExists(processedName, entityType)) {
            log.info("Duplicate processed name '{}' found for entity type '{}'. Removing duplicate entry.", processedName, entityType);
            removeDuplicateByProcessedName(processedName, entityType);
        }
        
        // Check cache as well
        if (processedNameCache.containsValue(processedName)) {
            log.info("Duplicate processed name '{}' found in cache for entity type '{}'. Removing from cache.", processedName, entityType);
            // Remove from cache by finding the key with this value
            processedNameCache.entrySet().removeIf(entry -> processedName.equals(entry.getValue()));
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
     * Remove duplicate entries by processed name
     */
    private void removeDuplicateByProcessedName(String processedName, String entityType) {
        try {
            switch (entityType.toLowerCase()) {
                case "country":
                    List<Country> countries = countryRepository.findByProcessedName(processedName);
                    if (countries.size() > 1) {
                        // Keep the first one, delete the rest
                        for (int i = 1; i < countries.size(); i++) {
                            countryRepository.delete(countries.get(i));
                            log.info("Deleted duplicate country with processed name: {}", processedName);
                        }
                    }
                    break;
                case "city":
                    List<City> cities = cityRepository.findByProcessedName(processedName);
                    if (cities.size() > 1) {
                        // Keep the first one, delete the rest
                        for (int i = 1; i < cities.size(); i++) {
                            cityRepository.delete(cities.get(i));
                            log.info("Deleted duplicate city with processed name: {}", processedName);
                        }
                    }
                    break;
                case "company":
                    List<Company> companies = companyRepository.findByProcessedName(processedName);
                    if (companies.size() > 1) {
                        // Keep the first one, delete the rest
                        for (int i = 1; i < companies.size(); i++) {
                            companyRepository.delete(companies.get(i));
                            log.info("Deleted duplicate company with processed name: {}", processedName);
                        }
                    }
                    break;
                case "designation":
                    List<Designation> designations = designationRepository.findByProcessedName(processedName);
                    if (designations.size() > 1) {
                        // Keep the first one, delete the rest
                        for (int i = 1; i < designations.size(); i++) {
                            designationRepository.delete(designations.get(i));
                            log.info("Deleted duplicate designation with processed name: {}", processedName);
                        }
                    }
                    break;
                default:
                    log.warn("Unknown entity type: {}", entityType);
            }
        } catch (Exception e) {
            log.error("Error removing duplicate entries for processed name: {} and entity type: {}", processedName, entityType, e);
        }
    }

    /**
     * Remove all duplicate processed names from database
     */
    @Transactional
    public Map<String, Object> removeDuplicateProcessedNames() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Starting duplicate processed name removal for all entities");
            
            int totalDuplicatesRemoved = 0;
            
            // Remove duplicates for each entity type
            totalDuplicatesRemoved += removeDuplicateProcessedNamesForEntity("country");
            totalDuplicatesRemoved += removeDuplicateProcessedNamesForEntity("city");
            totalDuplicatesRemoved += removeDuplicateProcessedNamesForEntity("company");
            totalDuplicatesRemoved += removeDuplicateProcessedNamesForEntity("designation");
            
            result.put("success", true);
            result.put("message", "Duplicate processed names removal completed");
            result.put("totalDuplicatesRemoved", totalDuplicatesRemoved);
            
            log.info("Duplicate processed names removal completed. Total duplicates removed: {}", totalDuplicatesRemoved);
            
        } catch (Exception e) {
            log.error("Error removing duplicate processed names", e);
            result.put("success", false);
            result.put("error", "Error removing duplicate processed names: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Remove duplicate processed names for a specific entity type
     */
    private int removeDuplicateProcessedNamesForEntity(String entityType) {
        int duplicatesRemoved = 0;
        
        try {
            switch (entityType.toLowerCase()) {
                case "country":
                    List<Country> countries = countryRepository.findAll();
                    duplicatesRemoved = removeDuplicatesByProcessedName(countries, "country");
                    break;
                case "city":
                    List<City> cities = cityRepository.findAll();
                    duplicatesRemoved = removeDuplicatesByProcessedName(cities, "city");
                    break;
                case "company":
                    List<Company> companies = companyRepository.findAll();
                    duplicatesRemoved = removeDuplicatesByProcessedName(companies, "company");
                    break;
                case "designation":
                    List<Designation> designations = designationRepository.findAll();
                    duplicatesRemoved = removeDuplicatesByProcessedName(designations, "designation");
                    break;
                default:
                    log.warn("Unknown entity type: {}", entityType);
            }
        } catch (Exception e) {
            log.error("Error removing duplicate processed names for entity type: {}", entityType, e);
        }
        
        return duplicatesRemoved;
    }

    /**
     * Generic method to remove duplicates by processed name
     */
    private <T> int removeDuplicatesByProcessedName(List<T> entities, String entityType) {
        int duplicatesRemoved = 0;
        Map<String, T> uniqueProcessedNames = new HashMap<>();
        
        for (T entity : entities) {
            String processedName = getProcessedName(entity);
            if (processedName != null) {
                if (uniqueProcessedNames.containsKey(processedName)) {
                    // This is a duplicate, remove it
                    deleteEntity(entity, entityType);
                    duplicatesRemoved++;
                    log.debug("Removed duplicate {} with processed name: {}", entityType, processedName);
                } else {
                    // First occurrence, keep it
                    uniqueProcessedNames.put(processedName, entity);
                }
            }
        }
        
        log.info("Removed {} duplicate {} entities", duplicatesRemoved, entityType);
        return duplicatesRemoved;
    }

    /**
     * Get processed name from entity
     */
    private <T> String getProcessedName(T entity) {
        if (entity instanceof Country) {
            return ((Country) entity).getProcessedName();
        } else if (entity instanceof City) {
            return ((City) entity).getProcessedName();
        } else if (entity instanceof Company) {
            return ((Company) entity).getProcessedName();
        } else if (entity instanceof Designation) {
            return ((Designation) entity).getProcessedName();
        }
        return null;
    }

    /**
     * Delete entity based on type
     */
    private <T> void deleteEntity(T entity, String entityType) {
        switch (entityType.toLowerCase()) {
            case "country":
                countryRepository.delete((Country) entity);
                break;
            case "city":
                cityRepository.delete((City) entity);
                break;
            case "company":
                companyRepository.delete((Company) entity);
                break;
            case "designation":
                designationRepository.delete((Designation) entity);
                break;
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