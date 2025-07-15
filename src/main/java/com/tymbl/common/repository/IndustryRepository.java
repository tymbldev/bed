package com.tymbl.common.repository;

import com.tymbl.common.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {
    Optional<Industry> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT i.id, i.name, i.description, COUNT(c.id) as companyCount " +
           "FROM Industry i " +
           "LEFT JOIN com.tymbl.jobs.entity.Company c ON c.primaryIndustryId = i.id " +
           "GROUP BY i.id, i.name, i.description " +
           "ORDER BY companyCount DESC")
    List<Object[]> getIndustryStatistics();
    
    @Query("SELECT c.id, c.name, c.logoUrl, c.website, c.headquarters, COUNT(j.id) as activeJobCount " +
           "FROM com.tymbl.jobs.entity.Company c " +
           "LEFT JOIN com.tymbl.common.entity.Job j ON j.companyId = c.id AND j.active = true " +
           "WHERE c.primaryIndustryId = :industryId " +
           "GROUP BY c.id, c.name, c.logoUrl, c.website, c.headquarters " +
           "ORDER BY activeJobCount DESC")
    List<Object[]> getTopCompaniesByIndustry(@Param("industryId") Long industryId);

    // Remove the broken join query
    // Add a method to fetch all industry IDs
    @Query("SELECT i.id FROM Industry i")
    List<Long> getAllIndustryIds();

    // Add a native query to count jobs for a given industryId
    @Query(value = "SELECT COUNT(j.id) FROM jobs_job j JOIN jobs_company c ON j.company_id = c.id WHERE c.primary_industry_id = :industryId AND j.active = true", nativeQuery = true)
    Long countActiveJobsByIndustryId(@Param("industryId") Long industryId);

    @Query(
      value = "SELECT c.primary_industry_id AS industryId, COUNT(j.id) AS jobCount " +
              "FROM jobs_company c " +
              "LEFT JOIN jobs_job j ON j.company_id = c.id AND j.active = true " +
              "GROUP BY c.primary_industry_id",
      nativeQuery = true
    )
    List<Object[]> getActiveJobCountsForAllIndustries();
} 