package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.ExternalJobDetailsFromCompanyPortal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalJobDetailsFromCompanyPortalRepository extends JpaRepository<ExternalJobDetailsFromCompanyPortal, Long> {

  /**
   * Find crawled content by external job detail ID
   */
  Optional<ExternalJobDetailsFromCompanyPortal> findByExternalJobDetailId(Long externalJobDetailId);

  /**
   * Find all crawled content for a specific external job detail ID
   */
  List<ExternalJobDetailsFromCompanyPortal> findAllByExternalJobDetailId(Long externalJobDetailId);

  /**
   * Find crawled content by status
   */
  List<ExternalJobDetailsFromCompanyPortal> findByCrawlStatus(String crawlStatus);

  /**
   * Find crawled content by external job detail ID and status
   */
  Optional<ExternalJobDetailsFromCompanyPortal> findByExternalJobDetailIdAndCrawlStatus(
      Long externalJobDetailId, String crawlStatus);

  /**
   * Count crawled content by status
   */
  long countByCrawlStatus(String crawlStatus);

  /**
   * Find all successful crawls
   */
  @Query("SELECT e FROM ExternalJobDetailsFromCompanyPortal e WHERE e.crawlStatus = 'SUCCESS'")
  List<ExternalJobDetailsFromCompanyPortal> findSuccessfulCrawls();

  /**
   * Find all failed crawls
   */
  @Query("SELECT e FROM ExternalJobDetailsFromCompanyPortal e WHERE e.crawlStatus = 'FAILED'")
  List<ExternalJobDetailsFromCompanyPortal> findFailedCrawls();

  /**
   * Find all pending crawls
   */
  @Query("SELECT e FROM ExternalJobDetailsFromCompanyPortal e WHERE e.crawlStatus = 'PENDING'")
  List<ExternalJobDetailsFromCompanyPortal> findPendingCrawls();

  /**
   * Delete crawled content by external job detail ID
   */
  void deleteByExternalJobDetailId(Long externalJobDetailId);
}
