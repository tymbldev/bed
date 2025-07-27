package com.tymbl.common.service;

import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.util.DesignationNameCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationDataService {

    private final DesignationRepository designationRepository;

    /**
     * Clean up duplicate designations by name before loading new data
     */
    @Transactional
    public List<String> cleanupDuplicateDesignations() {
        List<String> results = new ArrayList<>();
        
        try {
            // Get all designations
            List<Designation> allDesignations = designationRepository.findAll();
            Map<String, List<Designation>> designationsByName = allDesignations.stream()
                .collect(Collectors.groupingBy(Designation::getName));
            
            int duplicatesRemoved = 0;
            
            for (Map.Entry<String, List<Designation>> entry : designationsByName.entrySet()) {
                String name = entry.getKey();
                List<Designation> designations = entry.getValue();
                
                if (designations.size() > 1) {
                    // Keep the first one, remove the rest
                    Designation toKeep = designations.get(0);
                    List<Designation> toRemove = designations.subList(1, designations.size());
                    
                    for (Designation designation : toRemove) {
                        designationRepository.delete(designation);
                        duplicatesRemoved++;
                    }
                    
                    results.add("Removed " + toRemove.size() + " duplicate(s) for designation: " + name);
                }
            }
            
            if (duplicatesRemoved == 0) {
                results.add("No duplicate designations found");
            } else {
                results.add("Total duplicates removed: " + duplicatesRemoved);
            }
            
        } catch (Exception e) {
            results.add("Error cleaning up duplicates: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Clean up duplicate designations using the same logic as the SQL query:
     * DELETE FROM designations WHERE id NOT IN (SELECT id FROM (SELECT MIN(id) AS id FROM designations GROUP BY CASE WHEN RIGHT(name, 2) = '"]' THEN LEFT(name, LENGTH(name) - 2) ELSE name END) AS t);
     */
    @Transactional
    public List<String> cleanupDuplicateDesignationsWithNormalization() {
        List<String> results = new ArrayList<>();
        
        try {
            // Get all designations
            List<Designation> allDesignations = designationRepository.findAll();
            
            // Group designations by normalized name (same logic as SQL query)
            Map<String, List<Designation>> designationsByNormalizedName = allDesignations.stream()
                .collect(Collectors.groupingBy(designation -> 
                    DesignationNameCleaner.getNormalizedNameForDuplicateDetection(designation.getName())));
            
            int duplicatesRemoved = 0;
            
            for (Map.Entry<String, List<Designation>> entry : designationsByNormalizedName.entrySet()) {
                String normalizedName = entry.getKey();
                List<Designation> designations = entry.getValue();
                
                if (designations.size() > 1) {
                    // Keep the one with minimum ID, remove the rest
                    Designation toKeep = designations.stream()
                        .min((d1, d2) -> Long.compare(d1.getId(), d2.getId()))
                        .orElse(designations.get(0));
                    
                    List<Designation> toRemove = designations.stream()
                        .filter(designation -> !designation.getId().equals(toKeep.getId()))
                        .collect(Collectors.toList());
                    
                    for (Designation designation : toRemove) {
                        designationRepository.delete(designation);
                        duplicatesRemoved++;
                    }
                    
                    results.add("Removed " + toRemove.size() + " duplicate(s) for normalized name: " + normalizedName);
                }
            }
            
            if (duplicatesRemoved == 0) {
                results.add("No duplicate designations found with normalization");
            } else {
                results.add("Total duplicates removed with normalization: " + duplicatesRemoved);
            }
            
        } catch (Exception e) {
            results.add("Error cleaning up duplicates with normalization: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * Comprehensive cleanup method that applies all the cleaning logic from the SQL queries
     * and then removes duplicates. This method:
     * 1. Cleans all designation names using DesignationNameCleaner
     * 2. Removes duplicates based on cleaned names
     * 3. Keeps the designation with minimum ID for each group
     */
    @Transactional
    public List<String> comprehensiveDesignationCleanup() {
        List<String> results = new ArrayList<>();
        
        try {
            // Get all designations
            List<Designation> allDesignations = designationRepository.findAll();
            int totalDesignations = allDesignations.size();
            int cleanedCount = 0;
            int duplicatesRemoved = 0;
            
            // Step 1: Clean all designation names
            for (Designation designation : allDesignations) {
                String originalName = designation.getName();
                String cleanedName = DesignationNameCleaner.cleanAndValidateDesignationName(originalName);
                
                if (cleanedName != null && !cleanedName.equals(originalName)) {
                    designation.setName(cleanedName);
                    designationRepository.save(designation);
                    cleanedCount++;
                    results.add("Cleaned designation name: '" + originalName + "' -> '" + cleanedName + "'");
                }
            }
            
            // Step 2: Remove duplicates based on cleaned names
            allDesignations = designationRepository.findAll(); // Refresh list after cleaning
            
            // Group designations by their cleaned names
            Map<String, List<Designation>> designationsByCleanedName = allDesignations.stream()
                .collect(Collectors.groupingBy(Designation::getName));
            
            for (Map.Entry<String, List<Designation>> entry : designationsByCleanedName.entrySet()) {
                String cleanedName = entry.getKey();
                List<Designation> designations = entry.getValue();
                
                if (designations.size() > 1) {
                    // Keep the one with minimum ID, remove the rest
                    Designation toKeep = designations.stream()
                        .min((d1, d2) -> Long.compare(d1.getId(), d2.getId()))
                        .orElse(designations.get(0));
                    
                    List<Designation> toRemove = designations.stream()
                        .filter(designation -> !designation.getId().equals(toKeep.getId()))
                        .collect(Collectors.toList());
                    
                    for (Designation designation : toRemove) {
                        designationRepository.delete(designation);
                        duplicatesRemoved++;
                    }
                    
                    results.add("Removed " + toRemove.size() + " duplicate(s) for: " + cleanedName);
                }
            }
            
            // Summary
            results.add("=== SUMMARY ===");
            results.add("Total designations processed: " + totalDesignations);
            results.add("Designation names cleaned: " + cleanedCount);
            results.add("Duplicates removed: " + duplicatesRemoved);
            results.add("Final designation count: " + (totalDesignations - duplicatesRemoved));
            
        } catch (Exception e) {
            results.add("Error during comprehensive cleanup: " + e.getMessage());
        }
        
        return results;
    }
} 