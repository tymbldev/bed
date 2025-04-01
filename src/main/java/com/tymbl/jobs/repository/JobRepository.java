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

    Page<Job> findByPostedByAndActiveTrue(User user, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.title LIKE %:keyword% OR j.description LIKE %:keyword% OR j.companyName LIKE %:keyword% AND j.active = true")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND (" +
           "j.title IN :skills OR " +
           "j.description LIKE %:combinedSkills%)")
    Page<Job> findBySkills(@Param("skills") List<String> skills, @Param("combinedSkills") String combinedSkills, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.description LIKE %:skill% AND j.active = true")
    Page<Job> findBySkill(@Param("skill") String skill, Pageable pageable);
    
    List<Job> findByCompany(Company company);
    
    List<Job> findByCompanyAndTitleContainingIgnoreCase(Company company, String title);
    
    @Query("SELECT j FROM Job j WHERE j.company.id = :companyId AND j.title LIKE %:title% AND j.active = true")
    List<Job> findActiveJobsByCompanyAndTitle(@Param("companyId") Long companyId, @Param("title") String title);
    
    List<Job> findByCompanyNameContainingIgnoreCase(String companyName);
    
    @Query("SELECT j FROM Job j WHERE j.companyName LIKE %:companyName% AND j.title LIKE %:title% AND j.active = true")
    List<Job> findActiveJobsByCompanyNameAndTitle(@Param("companyName") String companyName, @Param("title") String title);
} 