package com.tymbl.interview.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "interview_topics")
public class InterviewTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_designation_skill_id", nullable = false)
    private CompanyDesignationSkill companyDesignationSkill;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "difficulty_level")
    private String difficultyLevel;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL)
    private List<InterviewQuestion> questions;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 