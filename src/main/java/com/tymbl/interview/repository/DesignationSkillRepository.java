package com.tymbl.interview.repository;

import com.tymbl.interview.entity.DesignationSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationSkillRepository extends JpaRepository<DesignationSkill, Long> {
    
    List<DesignationSkill> findByDesignation(String designation);
    
    List<DesignationSkill> findByDesignationAndDifficultyLevel(String designation, DesignationSkill.DifficultyLevel difficultyLevel);
    
    List<DesignationSkill> findByDesignationAndCategory(String designation, String category);
    
    @Query("SELECT DISTINCT d.designation FROM DesignationSkill d ORDER BY d.designation")
    List<String> findAllDesignations();
    
    @Query("SELECT DISTINCT d.category FROM DesignationSkill d WHERE d.designation = :designation ORDER BY d.category")
    List<String> findCategoriesByDesignation(@Param("designation") String designation);
    
    @Query("SELECT d.id, d.designation, d.skillName, d.skillDescription, COUNT(DISTINCT c.id) as companyCount " +
           "FROM DesignationSkill d " +
           "LEFT JOIN com.tymbl.common.entity.Job j ON j.designationId = d.id " +
           "LEFT JOIN com.tymbl.jobs.entity.Company c ON c.id = j.companyId " +
           "GROUP BY d.id, d.designation, d.skillName, d.skillDescription " +
           "ORDER BY companyCount DESC")
    List<Object[]> getDesignationStatistics();
    
    @Query("SELECT c.id, c.name, c.logoUrl, c.website, c.headquarters, COUNT(j.id) as activeJobCount " +
           "FROM com.tymbl.jobs.entity.Company c " +
           "LEFT JOIN com.tymbl.common.entity.Job j ON j.companyId = c.id AND j.active = true AND j.designationId = :designationId " +
           "GROUP BY c.id, c.name, c.logoUrl, c.website, c.headquarters " +
           "HAVING COUNT(j.id) > 0 " +
           "ORDER BY activeJobCount DESC")
    List<Object[]> getTopCompaniesByDesignation(@Param("designationId") Long designationId);
} 