package com.tymbl.interview.repository;

import com.tymbl.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findBySkillId(Long skillId);
    List<InterviewQuestion> findBySkillName(String skillName);
    List<InterviewQuestion> findByDifficultyLevel(String difficultyLevel);
    List<InterviewQuestion> findByQuestionContainingIgnoreCase(String question);
    
    @Query("SELECT DISTINCT i.skillName FROM InterviewQuestion i ORDER BY i.skillName")
    List<String> findAllSkillNames();
    
    @Query("SELECT COUNT(i) FROM InterviewQuestion i WHERE i.skillId = :skillId")
    Long countBySkillId(@Param("skillId") Long skillId);
} 