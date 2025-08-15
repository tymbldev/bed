package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.CompanyContent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyContentRepository extends JpaRepository<CompanyContent, Long> {

  Optional<CompanyContent> findByCompanyId(Long companyId);

  // Find company content that hasn't been processed for content shortening and has original content
  @Query("SELECT cc FROM CompanyContent cc WHERE cc.contentShortened = false AND (cc.aboutUsOriginal IS NOT NULL OR cc.cultureOriginal IS NOT NULL)")
  List<CompanyContent> findUnprocessedContentWithOriginalData();

  // Find company content by company ID that hasn't been processed
  @Query("SELECT cc FROM CompanyContent cc WHERE cc.companyId = :companyId AND cc.contentShortened = false")
  Optional<CompanyContent> findUnprocessedContentByCompanyId(Long companyId);

  boolean existsByCompanyId(Long companyId);
} 