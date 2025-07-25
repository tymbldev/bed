package com.tymbl.common.repository;

import com.tymbl.common.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryIdOrderByNameAsc(Long countryId);
    List<City> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    
    // Find cities that haven't been processed for processed name generation
    List<City> findByProcessedNameGeneratedFalse();
    
    // Check if processed name exists
    boolean existsByProcessedName(String processedName);
    
    // Reset processed name generated flag for all cities
    @Query("UPDATE City c SET c.processedNameGenerated = false")
    void resetProcessedNameGeneratedFlag();
} 