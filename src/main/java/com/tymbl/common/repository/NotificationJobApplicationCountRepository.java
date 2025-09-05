package com.tymbl.common.repository;

import com.tymbl.common.entity.NotificationJobApplicationCount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationJobApplicationCountRepository extends JpaRepository<NotificationJobApplicationCount, Long> {

  Optional<NotificationJobApplicationCount> findByJobIdAndPostedByUserId(Long jobId, Long postedByUserId);

  @Query("SELECT COUNT(jac) FROM NotificationJobApplicationCount jac WHERE jac.jobId = :jobId")
  int countByJobId(@Param("jobId") Long jobId);
}
