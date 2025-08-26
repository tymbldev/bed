package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.ExternalJobCrawlKeyword;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalJobCrawlKeywordRepository extends
    JpaRepository<ExternalJobCrawlKeyword, Long> {

  List<ExternalJobCrawlKeyword> findByIsActiveTrue();

  List<ExternalJobCrawlKeyword> findByPortalNameAndIsActiveTrue(String portalName);

  @Query("SELECT k FROM ExternalJobCrawlKeyword k WHERE k.isActive = true AND (k.lastCrawledDate IS NULL OR k.lastCrawledDate <= :threshold)")
  List<ExternalJobCrawlKeyword> findKeywordsReadyForCrawling(
      @Param("threshold") LocalDateTime threshold);

  ExternalJobCrawlKeyword findByKeywordAndPortalName(String keyword, String portalName);

  @Query("SELECT DISTINCT k.portalName FROM ExternalJobCrawlKeyword k WHERE k.isActive = true")
  List<String> findDistinctActivePortals();
}
