package com.tymbl.interview.repository;

import com.tymbl.interview.entity.CompanyDesignationSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDesignationSkillRepository extends JpaRepository<CompanyDesignationSkill, Long> {
    List<CompanyDesignationSkill> findByCompanyIdAndDesignationId(Long companyId, Long designationId);
    List<CompanyDesignationSkill> findByCompanyId(Long companyId);
    List<CompanyDesignationSkill> findByDesignationId(Long designationId);
    List<CompanyDesignationSkill> findBySkillId(Long skillId);
} 