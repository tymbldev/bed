package com.tymbl.common.repository;

import com.tymbl.common.entity.JobReferrer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface JobReferrerRepository extends JpaRepository<JobReferrer, Long> {
    List<JobReferrer> findByJobId(Long jobId);
    List<JobReferrer> findByUserId(Long userId);
    JobReferrer findByJobIdAndUserId(Long jobId, Long userId);
    int countByJobId(Long jobId);
    
    @Modifying
    @Query("DELETE FROM JobReferrer jr WHERE jr.job.id = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);
} 