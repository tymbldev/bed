package com.tymbl.common.repository;

import com.tymbl.common.entity.ReferrerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReferrerFeedbackRepository extends JpaRepository<ReferrerFeedback, Long> {
    List<ReferrerFeedback> findByJobReferrerId(Long jobReferrerId);
    List<ReferrerFeedback> findByApplicantId(Long applicantId);
    ReferrerFeedback findByJobReferrerIdAndApplicantId(Long jobReferrerId, Long applicantId);
} 