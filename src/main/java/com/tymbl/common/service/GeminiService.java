package com.tymbl.common.service;

import com.tymbl.common.dto.CompanyGenerationResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

  private final GeminiCompanyService geminiCompanyService;
  private final GeminiInterviewService geminiInterviewService;
  private final AISimilarContentFetchingService aiSimilarContentFetchingService;

  // Company-related methods
  public Optional<com.tymbl.jobs.entity.Company> generateCompanyInfo(String companyName) {
    CompanyGenerationResponse response = geminiCompanyService.generateCompanyInfo(companyName);
    if (response.isSuccess() && response.getCompany() != null) {
      return Optional.of(response.getCompany());
    }
    return Optional.empty();
  }

  public CompanyGenerationResponse generateCompanyInfoWithJunkDetection(String companyName) {
    return geminiCompanyService.generateCompanyInfo(companyName);
  }

  public Map<String, Object> detectCompanyIndustries(String companyName, String companyDescription,
      String specialties) {
    return geminiCompanyService.detectCompanyIndustries(companyName, companyDescription,
        specialties);
  }

  /**
   * Delegates to GeminiCompanyService to generate a list of companies for an industry.
   */
  public List<Map<String, String>> generateCompanyListForIndustry(String industryName,
      List<String> excludeNames) {
    return geminiCompanyService.generateCompanyListForIndustry(industryName, excludeNames);
  }

  // Interview-related methods
  public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
    return geminiInterviewService.generateTopicsForDesignation(designationName);
  }

  public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation,
      String topicName, String difficultyLevel, int numQuestions) {
    return geminiInterviewService.generateGeneralInterviewQuestions(designation, topicName,
        difficultyLevel, numQuestions);
  }

  public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName,
      String designation, String topicName, String difficultyLevel, int numQuestions) {
    return geminiInterviewService.generateCompanySpecificQuestions(companyName, designation,
        topicName, difficultyLevel, numQuestions);
  }

  // Comprehensive interview question generation
  public List<Map<String, Object>> generateComprehensiveInterviewQuestions(String skillName,
      int numQuestions) {
    return geminiInterviewService.generateComprehensiveInterviewQuestions(skillName, numQuestions);
  }

  public List<Map<String, Object>> generateDetailedQuestionContent(String skillName,
      String questionSummary) {
    return geminiInterviewService.generateDetailedQuestionContent(skillName, questionSummary);
  }

  public List<Map<String, Object>> generateComprehensiveTechSkills() {
    return geminiInterviewService.generateComprehensiveTechSkills();
  }

  public List<Map<String, Object>> generateTopicsForSkill(String skillName) {
    return geminiInterviewService.generateTopicsForSkill(skillName);
  }

  public List<Map<String, Object>> generateQuestionsForSkillAndTopic(String skillName,
      String topicName, int numQuestions) {
    return geminiInterviewService.generateQuestionsForSkillAndTopic(skillName, topicName,
        numQuestions);
  }

  // Designation-related methods (if needed)
  public List<Map<String, Object>> generateDesignationsForDepartment(String departmentName) {
    return geminiInterviewService.generateDesignationsForDepartment(departmentName);
  }

  // Content shortening method
  public String shortenContent(String content, String contentType) {
    return geminiInterviewService.shortenContent(content, contentType);
  }

  // Intelligent content shortening method with minimum 500 characters
  public String shortenContentIntelligently(String content, String contentType) {
    return geminiCompanyService.shortenContentIntelligently(content, contentType);
  }

  // Similar designation generation method
  public List<String> generateSimilarDesignations(String designationName) {
    return aiSimilarContentFetchingService.generateSimilarDesignations(designationName);
  }

  // Similar company generation method
  public List<String> generateSimilarCompanies(String companyName, String industry,
      String description) {
    return aiSimilarContentFetchingService.generateSimilarCompanies(companyName, industry,
        description);
  }

  // Enhanced similar company generation method with additional company details
  public List<String> generateSimilarCompanies(String companyName, String industry,
      String description,
      String companySize, String specialties, String headquarters) {
    return aiSimilarContentFetchingService.generateSimilarCompanies(companyName, industry,
        description,
        companySize, specialties, headquarters);
  }
} 