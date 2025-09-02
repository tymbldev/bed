package com.tymbl.common.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_map_cache")
public class SiteMapCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sitemap_type", nullable = false, length = 100)
    private String sitemapType;
    
    @Column(name = "level", nullable = false)
    private Integer level;
    
    @Column(name = "page_number")
    private Integer pageNumber;
    
    @Column(name = "xml_content", columnDefinition = "TEXT")
    private String xmlContent;
    
    @Column(name = "last_generated", nullable = false)
    private LocalDateTime lastGenerated;
    
    @Column(name = "total_pages")
    private Integer totalPages;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public SiteMapCache() {}
    
    public SiteMapCache(String sitemapType, Integer level, Integer pageNumber, String xmlContent) {
        this.sitemapType = sitemapType;
        this.level = level;
        this.pageNumber = pageNumber;
        this.xmlContent = xmlContent;
        this.lastGenerated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSitemapType() {
        return sitemapType;
    }
    
    public void setSitemapType(String sitemapType) {
        this.sitemapType = sitemapType;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public Integer getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public String getXmlContent() {
        return xmlContent;
    }
    
    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }
    
    public LocalDateTime getLastGenerated() {
        return lastGenerated;
    }
    
    public void setLastGenerated(LocalDateTime lastGenerated) {
        this.lastGenerated = lastGenerated;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
