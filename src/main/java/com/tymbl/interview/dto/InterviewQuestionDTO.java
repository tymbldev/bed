package com.tymbl.interview.dto;

import com.tymbl.interview.entity.CompanyInterviewQuestion;
import com.tymbl.interview.entity.GeneralInterviewQuestion;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewQuestionDTO {

  private Long id;
  private String designation;
  private String topicName;
  private String questionText;
  private String answerText;
  private String difficultyLevel;
  private String questionType;
  private String companyName;
  private String companyContext;
  private String tags;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static InterviewQuestionDTO fromGeneralEntity(GeneralInterviewQuestion entity) {
    return InterviewQuestionDTO.builder()
        .id(entity.getId())
        .designation(entity.getDesignation())
        .topicName(entity.getTopicName())
        .questionText(entity.getQuestionText())
        .answerText(entity.getAnswerText())
        .difficultyLevel(entity.getDifficultyLevel().name())
        .questionType(entity.getQuestionType().name())
        .tags(entity.getTags())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public static InterviewQuestionDTO fromCompanyEntity(CompanyInterviewQuestion entity) {
    return InterviewQuestionDTO.builder()
        .id(entity.getId())
        .designation(entity.getDesignation())
        .topicName(entity.getTopicName())
        .questionText(entity.getQuestionText())
        .answerText(entity.getAnswerText())
        .difficultyLevel(entity.getDifficultyLevel().name())
        .questionType(entity.getQuestionType().name())
        .companyName(entity.getCompanyName())
        .companyContext(entity.getCompanyContext())
        .tags(entity.getTags())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }
} 