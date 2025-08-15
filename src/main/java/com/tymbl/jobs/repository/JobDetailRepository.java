package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.JobDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDetailRepository extends JpaRepository<JobDetail, Long> {
    
    Optional<JobDetail> findByPortalJobIdAndPortalName(String portalJobId, String portalName);
    
    List<JobDetail> findByPortalName(String portalName);
    
    List<JobDetail> findByKeywordUsed(String keyword);
    
    List<JobDetail> findByPortalNameAndKeywordUsed(String portalName, String keyword);
    
    @Query("SELECT j FROM JobDetail j WHERE j.portalName = :portalName AND j.jobTitle LIKE %:title%")
    List<JobDetail> findByPortalNameAndJobTitleContaining(@Param("portalName") String portalName, @Param("title") String title);
    
    @Query("SELECT j FROM JobDetail j WHERE j.companyName LIKE %:companyName%")
    List<JobDetail> findByCompanyNameContaining(@Param("companyName") String companyName);
    
    @Query("SELECT j FROM JobDetail j WHERE j.minimumExperience >= :minExp AND j.maximumExperience <= :maxExp")
    List<JobDetail> findByExperienceRange(@Param("minExp") Integer minExp, @Param("maxExp") Integer maxExp);
}
