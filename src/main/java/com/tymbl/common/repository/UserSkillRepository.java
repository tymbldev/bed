package com.tymbl.common.repository;

import com.tymbl.common.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    
    List<UserSkill> findByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM UserSkill us WHERE us.userId = ?1")
    void deleteAllByUserId(Long userId);
} 