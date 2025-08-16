package com.tymbl.common.repository;

import com.tymbl.common.entity.PendingContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing pending content that couldn't be tagged by the system
 */
@Repository
public interface PendingContentRepository extends JpaRepository<PendingContent, Long> {
    
    /**
     * Find pending content by entity type
     */
    java.util.List<PendingContent> findByEntityType(PendingContent.EntityType entityType);
    
    /**
     * Find pending content by entity name (case-insensitive)
     */
    java.util.List<PendingContent> findByEntityNameContainingIgnoreCase(String entityName);
    
    /**
     * Find pending content by source table
     */
    java.util.List<PendingContent> findBySourceTable(String sourceTable);
    
    /**
     * Find pending content that hasn't been processed recently
     */
    java.util.List<PendingContent> findByProcessedAtIsNullOrProcessedAtBefore(java.time.LocalDateTime cutoffTime);
    
    /**
     * Find pending content by entity type and source table
     */
    java.util.List<PendingContent> findByEntityTypeAndSourceTable(PendingContent.EntityType entityType, String sourceTable);
    
    /**
     * Count pending content by entity type
     */
    long countByEntityType(PendingContent.EntityType entityType);
    
    /**
     * Find pending content created after a specific time
     */
    java.util.List<PendingContent> findByCreatedAtAfter(java.time.LocalDateTime after);
    
    /**
     * Find pending content with high attempt count (potentially problematic)
     */
    java.util.List<PendingContent> findByAttemptCountGreaterThanEqual(int minAttempts);
}
