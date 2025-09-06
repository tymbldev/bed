package com.tymbl.common.repository;

import com.tymbl.common.entity.JobReferrer;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobReferrerRepository extends JpaRepository<JobReferrer, Long> {

  List<JobReferrer> findByJobId(Long jobId);

  List<JobReferrer> findByUserId(Long userId);

  JobReferrer findByJobIdAndUserId(Long jobId, Long userId);

  int countByJobId(Long jobId);

  @Modifying
  @Query("DELETE FROM JobReferrer jr WHERE jr.job.id = :jobId")
  void deleteByJobId(@Param("jobId") Long jobId);

  @Query("SELECT jr.job.id as jobId, jr.user.id as userId FROM JobReferrer jr WHERE jr.job.id IN :jobIds")
  List<Map<String, Object>> findReferrerUserIdsByJobIds(@Param("jobIds") List<Long> jobIds);
} 