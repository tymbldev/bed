package com.tymbl.interview.repository;

import com.tymbl.interview.entity.CompanyDesignationSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyDesignationSkillRepository extends
    JpaRepository<CompanyDesignationSkill, Long> {

  List<CompanyDesignationSkill> findByCompanyIdAndDesignationId(Long companyId, Long designationId);

  List<CompanyDesignationSkill> findByCompanyId(Long companyId);

  List<CompanyDesignationSkill> findByDesignationId(Long designationId);

  List<CompanyDesignationSkill> findBySkillId(Long skillId);

  List<CompanyDesignationSkill> findByCompanyIdAndDesignationIdAndSkillId(Long companyId,
      Long designationId, Long skillId);
} 