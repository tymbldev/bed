package com.tymbl.jobs.repository;

import com.tymbl.common.entity.Job;
import com.tymbl.common.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByPostedByAndActiveTrue(User postedBy, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(String keyword, Pageable pageable);
    
    @Query("SELECT DISTINCT j FROM Job j JOIN j.requiredSkills s " +
           "WHERE j.active = true AND LOWER(s) IN :skills")
    Page<Job> findBySkills(List<String> skills, Pageable pageable);
    
    List<Job> findByCompanyAndActiveTrue(String company);
} 