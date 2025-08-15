package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.JobCrawlKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobCrawlKeywordRepository extends JpaRepository<JobCrawlKeyword, Long> {
    
    List<JobCrawlKeyword> findByIsActiveTrue();
    
    List<JobCrawlKeyword> findByPortalNameAndIsActiveTrue(String portalName);
    
    @Query("SELECT k FROM JobCrawlKeyword k WHERE k.isActive = true AND (k.lastCrawledDate IS NULL OR k.lastCrawledDate <= :threshold)")
    List<JobCrawlKeyword> findKeywordsReadyForCrawling(@Param("threshold") LocalDateTime threshold);
    
    JobCrawlKeyword findByKeywordAndPortalName(String keyword, String portalName);
    
    @Query("SELECT DISTINCT k.portalName FROM JobCrawlKeyword k WHERE k.isActive = true")
    List<String> findDistinctActivePortals();
}
