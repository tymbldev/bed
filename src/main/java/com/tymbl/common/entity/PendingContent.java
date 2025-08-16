package com.tymbl.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to track content that couldn't be tagged by the system
 * Used for monitoring and improving tagging logic over time
 */
@Entity
@Table(name = "pending_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingContent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_name", nullable = false, length = 500)
    private String entityName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;
    
    @Column(name = "source_table", nullable = false, length = 100)
    private String sourceTable;
    
    @Column(name = "source_id")
    private Long sourceId;
    
    @Column(name = "portal_name", length = 100)
    private String portalName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "attempt_count")
    private Integer attemptCount;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Types of entities that can be pending
     */
    public enum EntityType {
        COMPANY,
        DESIGNATION,
        CITY,
        COUNTRY
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }
}
