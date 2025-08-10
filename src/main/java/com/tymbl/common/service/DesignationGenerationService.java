package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.util.DesignationNameCleaner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tymbl.common.service.AIRestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationGenerationService {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AIRestService aiRestService;

    public Map<String, Object> generateDesignationsForAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> departmentResults = new ArrayList<>();
        
        int totalDepartments = departments.size();
        int totalDesignationsGenerated = 0;
        int totalErrors = 0;
        
        log.info("Starting designation generation for {} departments", totalDepartments);
        
        for (Department department : departments) {
            Map<String, Object> departmentResult = new HashMap<>();
            departmentResult.put("departmentId", department.getId());
            departmentResult.put("departmentName", department.getName());
            departmentResult.put("departmentDescription", department.getDescription());
            
            try {
                // Process each department in its own transaction
                Map<String, Object> departmentProcessResult = processDepartmentInTransaction(department);
                
                if ((Boolean) departmentProcessResult.get("success")) {
                    departmentResult.put("success", true);
                    departmentResult.put("designationsGenerated", departmentProcessResult.get("designationsGenerated"));
                    departmentResult.put("designationsSaved", departmentProcessResult.get("designationsSaved"));
                    departmentResult.put("designations", departmentProcessResult.get("designations"));
                    totalDesignationsGenerated += (Integer) departmentProcessResult.get("designationsSaved");
                    
                    log.info("Generated {} designations for department: {} ({})", 
                            departmentProcessResult.get("designationsSaved"), department.getName(), department.getId());
                } else {
                    departmentResult.put("success", false);
                    departmentResult.put("error", departmentProcessResult.get("error"));
                    totalErrors++;
                    log.warn("Failed to generate designations for department: {} ({}), Error: {}", 
                            department.getName(), department.getId(), departmentProcessResult.get("error"));
                }
            } catch (Exception e) {
                departmentResult.put("success", false);
                departmentResult.put("error", "Error processing department: " + e.getMessage());
                totalErrors++;
                log.error("Error generating designations for department: {} ({}), Error: {}", 
                        department.getName(), department.getId(), e.getMessage());
            }
            
            departmentResults.add(departmentResult);
        }
        
        result.put("totalDepartments", totalDepartments);
        result.put("totalDesignationsGenerated", totalDesignationsGenerated);
        result.put("totalErrors", totalErrors);
        result.put("departmentResults", departmentResults);
        result.put("message", "Designation generation completed for all departments");
        
        log.info("Designation generation completed. Total departments: {}, Total designations: {}, Errors: {}", 
                totalDepartments, totalDesignationsGenerated, totalErrors);
        
        return result;
    }

    @Transactional
    public Map<String, Object> processDepartmentInTransaction(Department department) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> designations = generateDesignationsForDepartment(department.getName(), department.getDescription());
            
            if (designations.isEmpty()) {
                result.put("success", false);
                result.put("error", "No designations generated for department: " + department.getName());
                return result;
            }
            
            int savedCount = saveDesignationsToDatabase(designations, department);
            
            result.put("success", true);
            result.put("departmentId", department.getId());
            result.put("departmentName", department.getName());
            result.put("designationsGenerated", designations.size());
            result.put("designationsSaved", savedCount);
            result.put("designations", designations);
            
            log.info("Successfully processed department: {} ({}). Generated: {}, Saved: {}", 
                    department.getName(), department.getId(), designations.size(), savedCount);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Error processing department: " + e.getMessage());
            log.error("Transaction failed for department: {} ({}), Error: {}", 
                    department.getName(), department.getId(), e.getMessage());
        }
        
        return result;
    }

    private List<String> generateDesignationsForDepartment(String departmentName, String departmentDescription) {
        try {
            log.info("[Gemini] Generating designations for department: {}", departmentName);
            String prompt = buildDesignationGenerationPrompt(departmentName, departmentDescription);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);
            
            ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody, "Designation Generation for " + departmentName);
            List<String> designations = parseDesignationsResponse(response.getBody());
            log.info("[Gemini] Parsed {} designations for department: {}", designations.size(), departmentName);
            return designations;
        } catch (Exception e) {
            log.error("[Gemini] Error generating designations for department: {}", departmentName, e);
            return new ArrayList<>();
        }
    }

    private String buildDesignationGenerationPrompt(String departmentName, String departmentDescription) {
        return String.format(
            "You are a comprehensive HR and career expert helping to populate a database with job designations for departments. " +
            "Given the department '%s' with description '%s', provide 20-35 valid job designations that are commonly used in this department. " +
            "Your goal is to provide COMPREHENSIVE coverage across all levels and specializations.\n\n" +
            "INCLUSION CRITERIA - Include designations that are:\n" +
            "1. ENTRY-LEVEL POSITIONS:\n" +
            "   - Junior roles and entry-level positions\n" +
            "   - Associate and assistant roles\n" +
            "   - Trainee and intern positions\n" +
            "   - Coordinator and specialist roles\n\n" +
            "2. MID-LEVEL POSITIONS:\n" +
            "   - Senior roles and experienced positions\n" +
            "   - Lead and principal roles\n" +
            "   - Manager and supervisor positions\n" +
            "   - Consultant and advisor roles\n\n" +
            "3. SENIOR-LEVEL POSITIONS:\n" +
            "   - Senior manager and director roles\n" +
            "   - Head of department positions\n" +
            "   - Principal and senior consultant roles\n" +
            "   - Expert and specialist positions\n\n" +
            "4. EXECUTIVE-LEVEL POSITIONS:\n" +
            "   - Vice President and C-level roles\n" +
            "   - Chief positions (CTO, CFO, CMO, etc.)\n" +
            "   - Executive director roles\n" +
            "   - Senior vice president positions\n\n" +
            "5. SPECIALIZED ROLES:\n" +
            "   - Technical specialist positions\n" +
            "   - Domain expert roles\n" +
            "   - Niche and specialized positions\n" +
            "   - Cross-functional roles\n\n" +
            "6. EMERGING ROLES:\n" +
            "   - New and emerging job titles\n" +
            "   - Digital transformation roles\n" +
            "   - Innovation and R&D positions\n" +
            "   - Future-focused roles\n\n" +
            "QUALITY REQUIREMENTS:\n" +
            "- Use standard, recognized job titles\n" +
            "- Ensure designations are currently in use in the industry\n" +
            "- Include both traditional and modern job titles\n" +
            "- Prefer designations with clear career progression paths\n" +
            "- Include designations from different company sizes (startup to enterprise)\n" +
            "- Consider both technical and non-technical roles\n" +
            "- Include remote and hybrid work designations where applicable\n\n" +
            "DEPARTMENT-SPECIFIC CONSIDERATIONS:\n" +
            "- For Engineering: Include software, hardware, data, DevOps, QA roles\n" +
            "- For Product: Include product management, strategy, analytics roles\n" +
            "- For Design: Include UX, UI, graphic, industrial design roles\n" +
            "- For Marketing: Include digital, content, brand, growth marketing roles\n" +
            "- For Sales: Include business development, account management, sales operations roles\n" +
            "- For Finance: Include accounting, financial analysis, treasury, audit roles\n" +
            "- For HR: Include recruitment, talent management, HR operations, learning roles\n" +
            "- For Operations: Include business operations, process improvement, project management roles\n" +
            "- For Data Science: Include data analysis, machine learning, business intelligence roles\n" +
            "- For Legal: Include legal counsel, compliance, regulatory, contract management roles\n\n" +
            "Return ONLY the designation names separated by '||||' (4 pipe characters). " +
            "Do not include any explanations, comments, or additional text. " +
            "Example format: Designation 1||||Designation 2||||Designation 3\n\n" +
            "Comprehensive list of designations for department '%s':",
            departmentName, departmentDescription != null ? departmentDescription : "No description available", departmentName
        );
    }

    private List<String> parseDesignationsResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text for designations: {}", generatedText);
                        
                        // Parse 4-pipe separated values
                        String[] designationParts = generatedText.split("\\|\\|\\|\\|");
                        List<String> designations = new ArrayList<>();
                        for (String part : designationParts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                // Remove any quotes or brackets that might be present
                                trimmed = trimmed.replaceAll("^[\"\\[\\s]+", "").replaceAll("[\"\\]\\s]+$", "");
                                if (!trimmed.isEmpty()) {
                                    designations.add(trimmed);
                                }
                            }
                        }
                        
                        log.info("[Gemini] Parsed {} designations from 4-pipe separated response", designations.size());
                        return designations;
                    }
                }
            }
            log.error("Unexpected Gemini API response structure for designations: {}", responseBody);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing Gemini response for designations", e);
            return new ArrayList<>();
        }
    }

    private int saveDesignationsToDatabase(List<String> designations, Department department) {
        int savedCount = 0;
        int errorCount = 0;
        int skippedCount = 0;
        
        log.info("Starting to save {} designations for department: {} ({})", designations.size(), department.getName(), department.getId());
        
        for (String rawDesignationName : designations) {
            try {
                // Clean and validate the designation name
                String cleanedDesignationName = DesignationNameCleaner.cleanAndValidateDesignationName(rawDesignationName);
                if (cleanedDesignationName == null) {
                    log.warn("Skipping invalid designation name: '{}' for department: {}", rawDesignationName, department.getName());
                    skippedCount++;
                    continue;
                }
                
                // Check if designation already exists
                boolean designationExists = designationRepository.existsByName(cleanedDesignationName);
                
                if (!designationExists) {
                    Designation designation = new Designation(cleanedDesignationName);
                    designation.setEnabled(true);
                    designationRepository.save(designation);
                    savedCount++;
                            log.info("Saved designation: {} for department: {}", cleanedDesignationName, department.getName());
      } else {
        log.info("Designation already exists: {} for department: {}", cleanedDesignationName, department.getName());
                }
            } catch (Exception e) {
                errorCount++;
                log.error("Error saving designation: {} for department: {}, Error: {}", rawDesignationName, department.getName(), e.getMessage());
            }
        }
        
        log.info("Designation save completed for department: {} ({}). Saved: {}, Skipped: {}, Errors: {}", 
                department.getName(), department.getId(), savedCount, skippedCount, errorCount);
        
        return savedCount;
    }



    /**
     * Process a single department for designation generation
     * This method processes one department in its own transaction
     */
    @Transactional
    public Map<String, Object> generateDesignationsForSingleDepartment(Long departmentId) {
        try {
            Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));
            
            return processDepartmentInTransaction(department);
        } catch (Exception e) {
            log.error("Error processing single department with ID: {}", departmentId, e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Error processing department: " + e.getMessage());
            result.put("departmentId", departmentId);
            return result;
        }
    }
} 