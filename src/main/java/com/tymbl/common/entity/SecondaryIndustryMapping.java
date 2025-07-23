package com.tymbl.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "secondary_industry_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecondaryIndustryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "secondary_industry_id")
    private Long secondaryIndustryId;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "mapped_id", nullable = false)
    private Long mappedId;

    @Column(name = "mapped_name", nullable = false, length = 500)
    private String mappedName;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 