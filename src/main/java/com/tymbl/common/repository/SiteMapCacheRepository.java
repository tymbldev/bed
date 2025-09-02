package com.tymbl.common.repository;

import com.tymbl.common.entity.SiteMapCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteMapCacheRepository extends JpaRepository<SiteMapCache, Long> {
    
    List<SiteMapCache> findBySitemapTypeAndLevelAndIsActiveTrueOrderByPageNumber(String sitemapType, Integer level);
    
    Optional<SiteMapCache> findBySitemapTypeAndLevelAndPageNumberAndIsActiveTrue(String sitemapType, Integer level, Integer pageNumber);
    
    @Query("SELECT s FROM SiteMapCache s WHERE s.sitemapType = :sitemapType AND s.level = :level AND s.isActive = true ORDER BY s.pageNumber")
    List<SiteMapCache> findActiveByTypeAndLevel(@Param("sitemapType") String sitemapType, @Param("level") Integer level);
    
    @Query("SELECT COUNT(s) FROM SiteMapCache s WHERE s.sitemapType = :sitemapType AND s.level = :level AND s.isActive = true")
    Long countActiveByTypeAndLevel(@Param("sitemapType") String sitemapType, @Param("level") Integer level);
    
    void deleteBySitemapTypeAndLevel(String sitemapType, Integer level);
    
    @Query("SELECT DISTINCT s.sitemapType FROM SiteMapCache s WHERE s.isActive = true")
    List<String> findAllActiveSitemapTypes();
}
