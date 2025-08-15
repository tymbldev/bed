package com.tymbl.interview.repository;

import com.tymbl.interview.entity.CompanyInterviewQuestion;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyInterviewQuestionRepository extends
    JpaRepository<CompanyInterviewQuestion, Long> {

  List<CompanyInterviewQuestion> findByCompanyNameAndDesignation(String companyName,
      String designation);

  List<CompanyInterviewQuestion> findByCompanyNameAndDesignationAndTopicName(String companyName,
      String designation, String topicName);

  List<CompanyInterviewQuestion> findByCompanyNameAndDesignationAndDifficultyLevel(
      String companyName, String designation,
      CompanyInterviewQuestion.DifficultyLevel difficultyLevel);

  List<CompanyInterviewQuestion> findByCompanyNameAndDesignationAndQuestionType(String companyName,
      String designation, CompanyInterviewQuestion.QuestionType questionType);

  Page<CompanyInterviewQuestion> findByCompanyNameAndDesignationAndTopicName(String companyName,
      String designation, String topicName, Pageable pageable);

  @Query("SELECT DISTINCT q.topicName FROM CompanyInterviewQuestion q WHERE q.companyName = :companyName AND q.designation = :designation ORDER BY q.topicName")
  List<String> findTopicsByCompanyAndDesignation(@Param("companyName") String companyName,
      @Param("designation") String designation);

  @Query("SELECT COUNT(q) FROM CompanyInterviewQuestion q WHERE q.companyName = :companyName AND q.designation = :designation AND q.topicName = :topicName")
  Long countByCompanyAndDesignationAndTopic(@Param("companyName") String companyName,
      @Param("designation") String designation, @Param("topicName") String topicName);

  @Query("SELECT DISTINCT q.companyName FROM CompanyInterviewQuestion q WHERE q.designation = :designation ORDER BY q.companyName")
  List<String> findCompaniesByDesignation(@Param("designation") String designation);
} 