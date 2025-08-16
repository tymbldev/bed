package com.tymbl.common.repository;

import com.tymbl.common.entity.Country;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

  Optional<Country> findByName(String name);

  List<Country> findByNameContainingIgnoreCase(String name);

  Optional<Country> findByCode(String code);

  boolean existsByName(String name);

  boolean existsByCode(String code);

  // Find countries that haven't been processed for city generation
  List<Country> findByCitiesProcessedFalse();

  // Reset cities processed flag for all countries (useful for reprocessing)
  @Query("UPDATE Country c SET c.citiesProcessed = false")
  void resetCitiesProcessedFlag();

  // Find countries that haven't been processed for processed name generation
  List<Country> findByProcessedNameGeneratedFalse();

  // Check if processed name exists
  boolean existsByProcessedName(String processedName);

  // Find countries by processed name
  List<Country> findByProcessedName(String processedName);

  // Reset processed name generated flag for all countries
  @Query("UPDATE Country c SET c.processedNameGenerated = false")
  void resetProcessedNameGeneratedFlag();
} 