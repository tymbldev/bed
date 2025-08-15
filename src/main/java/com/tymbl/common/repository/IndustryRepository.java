package com.tymbl.common.repository;

import com.tymbl.common.entity.Industry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {

  Optional<Industry> findByName(String name);

  boolean existsByName(String name);

  @Query(value = "SELECT i.id, i.name, i.description, i.rank_order, COUNT(c.id) as companyCount " +
      "FROM industries i " +
      "LEFT JOIN companies c ON c.primary_industry_id = i.id " +
      "GROUP BY i.id, i.name, i.description, i.rank_order " +
      "ORDER BY i.rank_order ASC, companyCount DESC", nativeQuery = true)
  List<Object[]> getIndustryStatistics();

  @Query(value =
      "SELECT c.id, c.name, c.logo_url, c.website, c.headquarters, COUNT(j.id) as activeJobCount " +
          "FROM companies c " +
          "LEFT JOIN jobs j ON j.company_id = c.id AND j.active = true " +
          "WHERE c.primary_industry_id = :industryId " +
          "GROUP BY c.id, c.name, c.logo_url, c.website, c.headquarters " +
          "ORDER BY activeJobCount DESC", nativeQuery = true)
  List<Object[]> getTopCompaniesByIndustry(@Param("industryId") Long industryId);

  // Remove the broken join query
  // Add a method to fetch all industry IDs
  @Query("SELECT i.id FROM Industry i")
  List<Long> getAllIndustryIds();

  // Add a native query to count jobs for a given industryId
  @Query(value = "SELECT COUNT(j.id) FROM jobs j JOIN companies c ON j.company_id = c.id WHERE c.primary_industry_id = :industryId AND j.active = true", nativeQuery = true)
  Long countActiveJobsByIndustryId(@Param("industryId") Long industryId);

  @Query(
      value = "SELECT c.primary_industry_id AS industryId, COUNT(j.id) AS jobCount " +
          "FROM companies c " +
          "LEFT JOIN jobs j ON j.company_id = c.id AND j.active = true " +
          "GROUP BY c.primary_industry_id",
      nativeQuery = true
  )
  List<Object[]> getActiveJobCountsForAllIndustries();

  // Find industries ordered by rank
  List<Industry> findAllByOrderByRankOrderAsc();

  // Find industry by rank order
  Optional<Industry> findByRankOrder(Integer rankOrder);
} 