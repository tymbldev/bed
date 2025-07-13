package com.tymbl.interview.repository;

import com.tymbl.interview.entity.DesignationSkillQuestionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationSkillQuestionMappingRepository extends JpaRepository<DesignationSkillQuestionMapping, Long> {
    
    List<DesignationSkillQuestionMapping> findByDesignationIdAndSkillId(Long designationId, Long skillId);
    
    List<DesignationSkillQuestionMapping> findByDesignationId(Long designationId);
    
    List<DesignationSkillQuestionMapping> findBySkillId(Long skillId);
    
    @Query("SELECT DISTINCT d.designationName FROM DesignationSkillQuestionMapping d WHERE d.skillId = :skillId")
    List<String> findDesignationsBySkillId(@Param("skillId") Long skillId);
    
    @Query("SELECT DISTINCT d.skillName FROM DesignationSkillQuestionMapping d WHERE d.designationId = :designationId")
    List<String> findSkillsByDesignationId(@Param("designationId") Long designationId);
} 