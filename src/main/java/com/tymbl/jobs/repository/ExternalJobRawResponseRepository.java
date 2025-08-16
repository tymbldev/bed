package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.ExternalJobRawResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalJobRawResponseRepository extends JpaRepository<ExternalJobRawResponse, Long> {

  List<ExternalJobRawResponse> findByPortalNameAndKeyword(String portalName, String keyword);

  List<ExternalJobRawResponse> findByProcessingStatus(ExternalJobRawResponse.ProcessingStatus status);

  @Query("SELECT r FROM ExternalJobRawResponse r WHERE r.processingStatus = 'PENDING' ORDER BY r.createdAt ASC")
  List<ExternalJobRawResponse> findPendingResponses();

  @Query("SELECT r FROM ExternalJobRawResponse r WHERE r.portalName = :portalName AND r.keyword = :keyword ORDER BY r.createdAt DESC")
  List<ExternalJobRawResponse> findLatestResponsesByPortalAndKeyword(@Param("portalName") String portalName,
      @Param("keyword") String keyword);
}
