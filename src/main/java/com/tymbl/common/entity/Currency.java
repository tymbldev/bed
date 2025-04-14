package com.tymbl.common.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies")
@Data
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 3)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "exchange_rate", nullable = false)
    private Double exchangeRate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
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