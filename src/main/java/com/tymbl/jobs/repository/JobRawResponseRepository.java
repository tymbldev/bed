package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.JobRawResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRawResponseRepository extends JpaRepository<JobRawResponse, Long> {
    
    List<JobRawResponse> findByPortalNameAndKeyword(String portalName, String keyword);
    
    List<JobRawResponse> findByProcessingStatus(JobRawResponse.ProcessingStatus status);
    
    @Query("SELECT r FROM JobRawResponse r WHERE r.processingStatus = 'PENDING' ORDER BY r.createdAt ASC")
    List<JobRawResponse> findPendingResponses();
    
    @Query("SELECT r FROM JobRawResponse r WHERE r.portalName = :portalName AND r.keyword = :keyword ORDER BY r.createdAt DESC")
    List<JobRawResponse> findLatestResponsesByPortalAndKeyword(@Param("portalName") String portalName, @Param("keyword") String keyword);
}
