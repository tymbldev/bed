package com.tymbl.common.repository;

import com.tymbl.common.entity.Designation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

  Optional<Designation> findByName(String name);

  List<Designation> findByNameIn(List<String> names);

  boolean existsByName(String name);

  // Find designations that haven't been processed for similar designation generation
  List<Designation> findBySimilarDesignationsProcessedFalse();

  // Find designations that haven't been processed for similar designation generation and are enabled
  List<Designation> findBySimilarDesignationsProcessedFalseAndEnabledTrue();

  // Find all enabled designations
  List<Designation> findByEnabledTrue();

  // Find designations that haven't been processed for processed name generation
  List<Designation> findByProcessedNameGeneratedFalse();

  // Check if processed name exists
  boolean existsByProcessedName(String processedName);

  // Find designations by processed name
  List<Designation> findByProcessedName(String processedName);

  // Reset processed name generated flag for all designations
  @Modifying
  @Query("UPDATE Designation d SET d.processedNameGenerated = false")
  void resetProcessedNameGeneratedFlag();

  // Find designations by department assigned status
  List<Designation> findByDepartmentAssignedFalse();

  // Count designations by department assigned status
  long countByDepartmentAssignedTrue();

  long countByDepartmentAssignedFalse();

  // Find designations by name containing (for fuzzy matching)
  List<Designation> findByNameContainingIgnoreCase(String name);
}