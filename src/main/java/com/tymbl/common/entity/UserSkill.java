package com.tymbl.common.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "user_skills")
@Data
@NoArgsConstructor
public class UserSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "skill_id")
    private Long skillId;
    
    @Column(name = "skill_name")
    private String skillName;
    
    public UserSkill(Long userId, Long skillId) {
        this.userId = userId;
        this.skillId = skillId;
    }
    
    public UserSkill(Long userId, String skillName) {
        this.userId = userId;
        this.skillName = skillName;
    }
} 