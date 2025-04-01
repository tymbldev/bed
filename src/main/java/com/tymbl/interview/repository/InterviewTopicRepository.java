package com.tymbl.interview.repository;

import com.tymbl.interview.entity.InterviewTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewTopicRepository extends JpaRepository<InterviewTopic, Long> {
    List<InterviewTopic> findByCompanyDesignationSkillId(Long companyDesignationSkillId);
    List<InterviewTopic> findByDifficultyLevel(String difficultyLevel);
    List<InterviewTopic> findByTitleContainingIgnoreCase(String title);
} 