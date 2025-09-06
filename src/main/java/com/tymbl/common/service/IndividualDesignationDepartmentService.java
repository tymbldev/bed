package com.tymbl.common.service;

import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualDesignationDepartmentService {

  private final DesignationRepository designationRepository;
  private final DepartmentRepository departmentRepository;
  private final AIRestService aiRestService;

  /**
   * Assign department to a specific designation with transaction management
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> assignDepartmentToDesignation(String designationName) {
    try {
      log.info("Starting department assignment for designation: {}", designationName);

      // Validate input
      if (designationName == null || designationName.trim().isEmpty()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "Designation name is required");
        return result;
      }

      // Check if designation exists - use a separate transaction for read operations
      Designation designation = findDesignationByName(designationName.trim());
      if (designation == null) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "Designation not found: " + designationName);
        return result;
      }

      // Check if department is already assigned
      if (designation.isDepartmentAssigned() && designation.getDepartmentId() != null) {
        Department existingDepartment = findDepartmentById(designation.getDepartmentId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("designation", designationName);
        result.put("departmentId", designation.getDepartmentId());
        result.put("departmentName",
            existingDepartment != null ? existingDepartment.getName() : "Unknown");
        result.put("message", "Department already assigned");
        result.put("alreadyAssigned", true);
        return result;
      }

      // Get available departments - use a separate transaction for read operations
      List<Department> availableDepartments = findAllDepartments();
      if (availableDepartments.isEmpty()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "No departments available in the system");
        return result;
      }

      // Use GenAI to determine the best department
      Long departmentId = assignDepartmentToDesignationUsingAI(designationName,
          availableDepartments);

      if (departmentId != null) {
        // Update the designation within the transaction
        designation.setDepartmentId(departmentId);
        designation.setDepartmentAssigned(true);

        // Get department details for response
        Department department = findDepartmentById(departmentId);
        designation.setDepartment(department.getName());
        designationRepository.save(designation);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("designation", designationName);
        result.put("designationId", designation.getId());
        result.put("departmentId", departmentId);
        result.put("departmentName", department != null ? department.getName() : "Unknown");
        result.put("message", "Department assigned successfully");
        result.put("alreadyAssigned", false);

        log.info("Successfully assigned department ID {} to designation: {}", departmentId,
            designationName);
        return result;

      } else {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", "Failed to assign department to designation: " + designationName);
        result.put("designation", designationName);
        return result;
      }

    } catch (Exception e) {
      log.error("Error assigning department to designation: {}", designationName, e);
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("error", "Error assigning department: " + e.getMessage());
      result.put("designation", designationName);
      // Transaction will be rolled back automatically due to @Transactional
      throw new RuntimeException("Failed to assign department to designation: " + designationName,
          e);
    }
  }

  /**
   * Use GenAI to determine the best department for a designation
   */
  private Long assignDepartmentToDesignationUsingAI(String designationName,
      List<Department> availableDepartments) {
    try {
      String prompt = buildDepartmentAssignmentPrompt(designationName, availableDepartments);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(
          requestBody,
          "Department Assignment for " + designationName
      );

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return parseDepartmentAssignmentResponse(response.getBody(), availableDepartments);
      } else {
        log.error("Gemini API error for designation: {} - Status: {}",
            designationName, response.getStatusCode());
        return null;
      }

    } catch (Exception e) {
      log.error("Error calling Gemini API for designation: {}", designationName, e);
      return null;
    }
  }

  /**
   * Build prompt for department assignment
   */
  private String buildDepartmentAssignmentPrompt(String designationName,
      List<Department> availableDepartments) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "You are an expert HR professional who needs to assign the most appropriate department to a job designation.\n\n");
    prompt.append("Available departments:\n");

    for (Department dept : availableDepartments) {
      prompt.append("- ").append(dept.getName());
      if (dept.getDescription() != null && !dept.getDescription().trim().isEmpty()) {
        prompt.append(": ").append(dept.getDescription());
      }
      prompt.append("\n");
    }

    prompt.append("\nJob Designation: ").append(designationName).append("\n\n");
    prompt.append(
        "Based on the job designation name, please identify the most appropriate department from the available list.\n");
    prompt.append(
        "Consider the typical responsibilities, skills, and focus areas of the role.\n\n");
    prompt.append("Return ONLY the department name (exactly as listed above), nothing else.\n");
    prompt.append("If no department seems appropriate, return 'Other'.\n\n");
    prompt.append("Examples:\n");
    prompt.append("- 'Software Engineer' → 'Engineering'\n");
    prompt.append("- 'Product Manager' → 'Product'\n");
    prompt.append("- 'Sales Representative' → 'Sales'\n");
    prompt.append("- 'HR Manager' → 'Human Resources'\n");
    prompt.append("- 'Financial Analyst' → 'Finance'\n");

    return prompt.toString();
  }

  /**
   * Parse the AI response to extract department name
   */
  private Long parseDepartmentAssignmentResponse(String responseBody,
      List<Department> availableDepartments) {
    try {
      String response = responseBody.trim();

      // Find the department by name in the available departments
      for (Department dept : availableDepartments) {
        if (dept.getName().equalsIgnoreCase(response)) {
          return dept.getId();
        }
      }

      // If exact match not found, try partial matches
      for (Department dept : availableDepartments) {
        if (response.toLowerCase().contains(dept.getName().toLowerCase()) ||
            dept.getName().toLowerCase().contains(response.toLowerCase())) {
          return dept.getId();
        }
      }

      log.warn("Could not find matching department for AI response: '{}'", response);
      return null;

    } catch (Exception e) {
      log.error("Error parsing AI response: {}", responseBody, e);
      return null;
    }
  }

  /**
   * Find designation by name in a separate read transaction
   */
  @Transactional(readOnly = true)
  public Designation findDesignationByName(String designationName) {
    try {
      Optional<Designation> designationOpt = designationRepository.findByName(designationName);
      return designationOpt.orElse(null);
    } catch (Exception e) {
      log.error("Error finding designation by name: {}", designationName, e);
      return null;
    }
  }

  /**
   * Find department by ID in a separate read transaction
   */
  @Transactional(readOnly = true)
  public Department findDepartmentById(Long departmentId) {
    try {
      Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
      return departmentOpt.orElse(null);
    } catch (Exception e) {
      log.error("Error finding department by ID: {}", departmentId, e);
      return null;
    }
  }

  /**
   * Find all departments in a separate read transaction, sorted by rank
   */
  @Transactional(readOnly = true)
  public List<Department> findAllDepartments() {
    try {
      return departmentRepository.findAllByOrderByRankAsc();
    } catch (Exception e) {
      log.error("Error finding all departments", e);
      return new ArrayList<>();
    }
  }
}
