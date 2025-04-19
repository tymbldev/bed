package com.tymbl.jobs.repository;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import com.tymbl.jobs.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByPostedByIdAndActiveTrue(Long postedById, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.description) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<Job> findBySkill(@Param("skill") String skill, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.description) LIKE LOWER(CONCAT('%', :combinedSkills, '%'))")
    Page<Job> findBySkills(@Param("combinedSkills") String combinedSkills, Pageable pageable);
    
    List<Job> findByCompanyId(Long companyId);
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.company) LIKE LOWER(CONCAT('%', :companyName, '%'))")
    List<Job> findByCompanyContainingIgnoreCase(@Param("companyName") String companyName);
    
    @Query("SELECT j FROM Job j WHERE j.companyId = :companyId AND j.title LIKE %:title% AND j.active = true")
    List<Job> findActiveJobsByCompanyIdAndTitle(@Param("companyId") Long companyId, @Param("title") String title);
    
    @Query("SELECT j FROM Job j WHERE j.company LIKE %:companyName% AND j.title LIKE %:title% AND j.active = true")
    List<Job> findActiveJobsByCompanyAndTitle(@Param("companyName") String companyName, @Param("title") String title);
    
    List<Job> findByCompany(Company company);
    
    List<Job> findByCompanyAndTitleContainingIgnoreCase(Company company, String title);
    
    @Query("SELECT j FROM Job j WHERE j.company LIKE %:companyName% AND j.title LIKE %:title% AND j.active = true")
    List<Job> findActiveJobsByCompanyNameAndTitle(@Param("companyName") String companyName, @Param("title") String title);
} 