package com.tymbl.interview.repository;

import com.tymbl.interview.entity.InterviewTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewTopicRepository extends JpaRepository<InterviewTopic, Long> {
    
    List<InterviewTopic> findByDesignation(String designation);
    
    List<InterviewTopic> findByDesignationAndDifficultyLevel(String designation, InterviewTopic.DifficultyLevel difficultyLevel);
    
    List<InterviewTopic> findByDesignationAndCategory(String designation, String category);
    
    @Query("SELECT DISTINCT t.designation FROM InterviewTopic t ORDER BY t.designation")
    List<String> findAllDesignations();
    
    @Query("SELECT DISTINCT t.category FROM InterviewTopic t WHERE t.designation = :designation ORDER BY t.category")
    List<String> findCategoriesByDesignation(@Param("designation") String designation);
} 