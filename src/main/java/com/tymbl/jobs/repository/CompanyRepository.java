package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    Optional<Company> findByNameIgnoreCase(String name);
    boolean existsByName(String name);
    Page<Company> findByIsCrawledFalse(Pageable pageable);
    // Find all companies by primaryIndustryId
    List<Company> findByPrimaryIndustryId(Long primaryIndustryId);
    // Find all websites by primaryIndustryId
    @Query("SELECT c.website FROM Company c WHERE c.primaryIndustryId = :primaryIndustryId")
    java.util.List<String> findWebsitesByPrimaryIndustryId(Long primaryIndustryId);
} 