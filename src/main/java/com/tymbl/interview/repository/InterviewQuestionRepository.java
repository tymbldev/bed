package com.tymbl.interview.repository;

import com.tymbl.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByTopicId(Long topicId);
    List<InterviewQuestion> findByDifficultyLevel(String difficultyLevel);
    List<InterviewQuestion> findByQuestionContainingIgnoreCase(String question);
} 