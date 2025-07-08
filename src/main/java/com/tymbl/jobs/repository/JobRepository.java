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
import java.util.Optional;
import java.util.Set;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Page<Job> findByActiveTrue(Pageable pageable);

    Page<Job> findByPostedByIdAndActiveTrue(Long postedById, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchJobs(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND " +
           "EXISTS (SELECT s FROM j.skillIds s WHERE s IN :skills)")
    Page<Job> findBySkillsIn(@Param("skills") Set<Long> skills, Pageable pageable);
    
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
    
    @Query("SELECT j FROM Job j JOIN j.tags t WHERE t IN :tags AND j.active = true")
    Page<Job> findByTagsIn(@Param("tags") Set<String> tags, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND EXISTS (SELECT t FROM j.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :tagKeyword, '%')))")
    Page<Job> findByTagsContaining(@Param("tagKeyword") String tagKeyword, Pageable pageable);
    
    @Query("SELECT DISTINCT t FROM Job j JOIN j.tags t WHERE j.active = true")
    List<String> findAllTags();
    
    List<Job> findByCompanyIdAndTitleContainingIgnoreCase(Long companyId, String title);
    List<Job> findByCompanyContainingIgnoreCaseAndTitleContainingIgnoreCase(String companyName, String title);
    List<Job> findByPostedById(Long postedById);

    Optional<Job> findByTitleAndCompanyId(String title, Long companyId);

    List<Job> findByCompanyIdAndPostedByIdAndActiveTrue(Long companyId, Long postedById);

    Job findByUniqueUrlAndPlatform(String uniqueUrl, String platform);
    
    // New methods for approval functionality
    Page<Job> findByActiveTrueAndApproved(Integer approved, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.approved = :approved AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchApprovedJobs(@Param("keyword") String keyword, @Param("approved") Integer approved, Pageable pageable);
    
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.approved = :approved AND " +
           "EXISTS (SELECT s FROM j.skillIds s WHERE s IN :skills)")
    Page<Job> findBySkillsInAndApproved(@Param("skills") Set<Long> skills, @Param("approved") Integer approved, Pageable pageable);
    
    List<Job> findByCompanyIdAndApproved(Long companyId, Integer approved);
    
    @Query("SELECT j FROM Job j WHERE LOWER(j.company) LIKE LOWER(CONCAT('%', :companyName, '%')) AND j.approved = :approved")
    List<Job> findByCompanyContainingIgnoreCaseAndApproved(@Param("companyName") String companyName, @Param("approved") Integer approved);
    
    @Query("SELECT j FROM Job j WHERE j.companyId = :companyId AND j.title LIKE %:title% AND j.active = true AND j.approved = :approved")
    List<Job> findActiveJobsByCompanyIdAndTitleAndApproved(@Param("companyId") Long companyId, @Param("title") String title, @Param("approved") Integer approved);
    
    @Query("SELECT j FROM Job j WHERE j.company LIKE %:companyName% AND j.title LIKE %:title% AND j.active = true AND j.approved = :approved")
    List<Job> findActiveJobsByCompanyAndTitleAndApproved(@Param("companyName") String companyName, @Param("title") String title, @Param("approved") Integer approved);
    
    // Override existing searchJobs method to use approved jobs
    @Query("SELECT j FROM Job j WHERE j.active = true AND j.approved = 1 AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Job> searchApprovedJobs(@Param("keyword") String keyword, Pageable pageable);
    
    // Find jobs by IDs that are active
    List<Job> findByIdInAndActiveTrue(List<Long> jobIds);
} 