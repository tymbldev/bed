package com.tymbl.jobs.repository;

import com.tymbl.jobs.entity.ExternalJobDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalJobDetailRepository extends JpaRepository<ExternalJobDetail, Long> {

  Optional<ExternalJobDetail> findByPortalJobIdAndPortalName(String portalJobId, String portalName);

  List<ExternalJobDetail> findByPortalName(String portalName);

  List<ExternalJobDetail> findByKeywordUsed(String keyword);

  List<ExternalJobDetail> findByPortalNameAndKeywordUsed(String portalName, String keyword);

  @Query("SELECT j FROM ExternalJobDetail j WHERE j.portalName = :portalName AND j.jobTitle LIKE %:title%")
  List<ExternalJobDetail> findByPortalNameAndJobTitleContaining(@Param("portalName") String portalName,
      @Param("title") String title);

  @Query("SELECT j FROM ExternalJobDetail j WHERE j.companyName LIKE %:companyName%")
  List<ExternalJobDetail> findByCompanyNameContaining(@Param("companyName") String companyName);

  @Query("SELECT j FROM ExternalJobDetail j WHERE j.maximumExperience BETWEEN :minExp AND :maxExp")
  List<ExternalJobDetail> findByExperienceRange(@Param("minExp") Integer minExp,
      @Param("maxExp") Integer maxExp);

  // Methods for external job sync
  List<ExternalJobDetail> findByIsSyncedToJobTableFalse();
  
  long countByIsSyncedToJobTableTrue();
}
