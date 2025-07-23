package com.tymbl.common.repository;

import com.tymbl.common.entity.SecondaryIndustryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecondaryIndustryMappingRepository extends JpaRepository<SecondaryIndustryMapping, Long> {

    /**
     * Find all unprocessed secondary industry mappings
     */
    List<SecondaryIndustryMapping> findByProcessedFalse();

    /**
     * Check if a name already exists in the mapping table
     */
    boolean existsByName(String name);

    /**
     * Find by name
     */
    Optional<SecondaryIndustryMapping> findByName(String name);

    /**
     * Find all unique mapped names
     */
    @Query("SELECT DISTINCT sim.mappedName FROM SecondaryIndustryMapping sim")
    List<String> findAllDistinctMappedNames();

    /**
     * Find the next available mapped ID
     */
    @Query("SELECT COALESCE(MAX(sim.mappedId), 0) + 1 FROM SecondaryIndustryMapping sim")
    Long getNextMappedId();

    /**
     * Find all mappings by mapped name
     */
    List<SecondaryIndustryMapping> findByMappedName(String mappedName);

    /**
     * Reset processed flag for all mappings
     */
    @Modifying
    @Query("UPDATE SecondaryIndustryMapping sim SET sim.processed = false")
    void resetProcessedFlag();

    /**
     * Find all unique secondary industry names from companies table
     */
    @Query("SELECT DISTINCT c.secondaryIndustries FROM Company c WHERE c.secondaryIndustries IS NOT NULL AND c.secondaryIndustries != ''")
    List<String> findAllUniqueSecondaryIndustriesFromCompanies();
} 