package com.tymbl.common.repository;

import com.tymbl.common.entity.UserResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserResumeRepository extends JpaRepository<UserResume, Long> {
    List<UserResume> findByUserId(Long userId);
    UserResume findFirstByUserIdOrderByCreatedAtDesc(Long userId);
} 