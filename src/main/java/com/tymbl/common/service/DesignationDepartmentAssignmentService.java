package com.tymbl.common.service;

import com.tymbl.common.entity.Department;
import com.tymbl.common.entity.Designation;
import com.tymbl.common.repository.DepartmentRepository;
import com.tymbl.common.repository.DesignationRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationDepartmentAssignmentService {

  private final DepartmentRepository departmentRepository;
  private final DesignationRepository designationRepository;
  private final IndividualDesignationDepartmentService individualDesignationDepartmentService;

  /**
   * Assign departments to all designations that don't have a department assigned
   */

  public Map<String, Object> assignDepartmentsToAllDesignations() {
    try {
      log.info("Starting department assignment for all unassigned designations");

      List<Designation> unassignedDesignations = designationRepository.findByDepartmentAssignedFalse();
      List<Department> availableDepartments = departmentRepository.findAll();

      if (unassignedDesignations.isEmpty()) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "All designations already have departments assigned");
        result.put("processed", 0);
        result.put("total", 0);
        return result;
      }

      log.info("Found {} designations without departments assigned", unassignedDesignations.size());

      int processed = 0;
      int failed = 0;
      List<String> errors = new ArrayList<>();

      for (Designation designation : unassignedDesignations) {
        try {
          Map<String, Object> result = individualDesignationDepartmentService.assignDepartmentToDesignation(
              designation.getName());

          if ((Boolean) result.get("success")) {
            processed++;
            log.info("Assigned department to designation: {} - Department: {}",
                designation.getName(), result.get("departmentName"));
          } else {
            failed++;
            errors.add(
                "Failed to assign department to: " + designation.getName() + " - " + result.get(
                    "error"));
          }
        } catch (Exception e) {
          failed++;
          errors.add(
              "Error assigning department to " + designation.getName() + ": " + e.getMessage());
          log.error("Error assigning department to designation: {}", designation.getName(), e);
        }
      }

      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "Department assignment completed");
      result.put("processed", processed);
      result.put("failed", failed);
      result.put("total", unassignedDesignations.size());
      result.put("errors", errors);

      log.info("Department assignment completed. Processed: {}, Failed: {}", processed, failed);
      return result;

    } catch (Exception e) {
      log.error("Error in bulk department assignment", e);
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("error", "Error in bulk department assignment: " + e.getMessage());
      return result;
    }
  }


  /**
   * Get designations grouped by department
   */
  public Map<String, List<String>> getDesignationsByDepartment() {
    try {
      List<Designation> designations = designationRepository.findAll();
      Map<String, List<String>> result = new HashMap<>();

      for (Designation designation : designations) {
        if (designation.getDepartmentId() != null) {
          Department department = departmentRepository.findById(designation.getDepartmentId())
              .orElse(null);
          String deptName = department != null ? department.getName() : "Unknown";

          result.computeIfAbsent(deptName, k -> new ArrayList<>()).add(designation.getName());
        }
      }

      return result;

    } catch (Exception e) {
      log.error("Error getting designations by department", e);
      return new HashMap<>();
    }
  }

  /**
   * Get statistics about department assignments
   */
  public Map<String, Object> getDepartmentAssignmentStats() {
    try {
      long totalDesignations = designationRepository.count();
      long assignedDesignations = designationRepository.countByDepartmentAssignedTrue();
      long unassignedDesignations = designationRepository.countByDepartmentAssignedFalse();

      Map<String, Object> stats = new HashMap<>();
      stats.put("totalDesignations", totalDesignations);
      stats.put("assignedDesignations", assignedDesignations);
      stats.put("unassignedDesignations", unassignedDesignations);
      stats.put("assignmentPercentage", totalDesignations > 0 ?
          (double) assignedDesignations / totalDesignations * 100 : 0);

      return stats;

    } catch (Exception e) {
      log.error("Error getting department assignment stats", e);
      return new HashMap<>();
    }
  }
}
