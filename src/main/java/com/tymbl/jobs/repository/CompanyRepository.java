package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  Optional<Company> findByName(String name);

  Optional<Company> findByNameIgnoreCase(String name);

  List<Company> findAllByNameIgnoreCase(String name);

  boolean existsByName(String name);

  Page<Company> findByIsCrawledFalse(Pageable pageable);

  // Find all companies by primaryIndustryId
  List<Company> findByPrimaryIndustryId(Long primaryIndustryId);

  // Find all companies by primaryIndustryId with pagination
  Page<Company> findByPrimaryIndustryId(Long primaryIndustryId, Pageable pageable);

  // Find all websites by primaryIndustryId
  @Query("SELECT c.website FROM Company c WHERE c.primaryIndustryId = :primaryIndustryId")
  java.util.List<String> findWebsitesByPrimaryIndustryId(Long primaryIndustryId);

  // Find companies that haven't been processed for content shortening and have original content
  // This query will be handled by CompanyContentRepository now

  // Find companies that haven't been processed for similar company generation
  List<Company> findBySimilarCompaniesProcessedFalse();

  // Find companies that haven't been processed for industry detection
  List<Company> findByIndustryProcessedFalse();

  // Find companies by shortname (for deduplication)
  List<Company> findByShortnameIgnoreCase(String shortname);

  // Find companies that haven't been processed for similar company generation and are enabled
  @Query("SELECT c FROM Company c WHERE c.similarCompaniesProcessed = false AND c.primaryIndustryId IS NOT NULL")
  List<Company> findUnprocessedCompaniesWithIndustry();

  // Find companies by primary industry
  List<Company> findByPrimaryIndustryIdAndIdNot(Long primaryIndustryId, Long companyId);

  // Reset industry processed flag for all companies (useful for reprocessing)
  @Query("UPDATE Company c SET c.industryProcessed = false")
  void resetIndustryProcessedFlag();

  // Find companies that haven't been processed for website fetching
  List<Company> findByWebsiteFetched(Integer status);

  // Find companies that haven't been processed for website fetching with pagination
  Page<Company> findByWebsiteFetched(Integer status, Pageable pageable);

  // Reset website fetched flag for all companies
  @Query("UPDATE Company c SET c.websiteFetched = 0")
  void resetWebsiteFetchedFlag();

  // Find companies by name containing (for fuzzy matching)
  List<Company> findByNameContainingIgnoreCase(String name);
} 