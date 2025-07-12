package com.tymbl.interview.repository;

import com.tymbl.interview.entity.GeneralInterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralInterviewQuestionRepository extends JpaRepository<GeneralInterviewQuestion, Long> {
    
    List<GeneralInterviewQuestion> findByDesignation(String designation);
    
    List<GeneralInterviewQuestion> findByDesignationAndTopicName(String designation, String topicName);
    
    List<GeneralInterviewQuestion> findByDesignationAndDifficultyLevel(String designation, GeneralInterviewQuestion.DifficultyLevel difficultyLevel);
    
    List<GeneralInterviewQuestion> findByDesignationAndQuestionType(String designation, GeneralInterviewQuestion.QuestionType questionType);
    
    Page<GeneralInterviewQuestion> findByDesignationAndTopicName(String designation, String topicName, Pageable pageable);
    
    @Query("SELECT DISTINCT q.topicName FROM GeneralInterviewQuestion q WHERE q.designation = :designation ORDER BY q.topicName")
    List<String> findTopicsByDesignation(@Param("designation") String designation);
    
    @Query("SELECT COUNT(q) FROM GeneralInterviewQuestion q WHERE q.designation = :designation AND q.topicName = :topicName")
    Long countByDesignationAndTopic(@Param("designation") String designation, @Param("topicName") String topicName);
} 