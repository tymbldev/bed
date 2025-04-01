package com.tymbl.interview.repository;

import com.tymbl.interview.entity.CompanyInterviewGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyInterviewGuideRepository extends JpaRepository<CompanyInterviewGuide, Long> {
    List<CompanyInterviewGuide> findByCompanyId(Long companyId);
    List<CompanyInterviewGuide> findByCompanyIdAndSection(Long companyId, String section);
    List<CompanyInterviewGuide> findByTitleContainingIgnoreCase(String title);
} 