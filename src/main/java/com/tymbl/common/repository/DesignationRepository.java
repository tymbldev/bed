package com.tymbl.common.repository;

import com.tymbl.common.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByName(String name);
    boolean existsByName(String name);
    
    // Find designations that haven't been processed for similar designation generation
    List<Designation> findBySimilarDesignationsProcessedFalse();
    
    // Find designations that haven't been processed for similar designation generation and are enabled
    List<Designation> findBySimilarDesignationsProcessedFalseAndEnabledTrue();
    
    // Find all enabled designations
    List<Designation> findByEnabledTrue();
}