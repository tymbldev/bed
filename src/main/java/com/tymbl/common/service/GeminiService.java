package com.tymbl.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final GeminiCompanyService geminiCompanyService;
    private final GeminiInterviewService geminiInterviewService;
    
    // Company-related methods
    public Optional<com.tymbl.jobs.entity.Company> generateCompanyInfo(String companyName, String linkedinUrl) {
        return geminiCompanyService.generateCompanyInfo(companyName, linkedinUrl);
    }
    
    public Map<String, Object> detectCompanyIndustries(String companyName, String companyDescription, String specialties) {
        return geminiCompanyService.detectCompanyIndustries(companyName, companyDescription, specialties);
    }

    /**
     * Delegates to GeminiCompanyService to generate a list of companies for an industry.
     */
    public List<Map<String, String>> generateCompanyListForIndustry(String industryName, List<String> excludeNames) {
        return geminiCompanyService.generateCompanyListForIndustry(industryName, excludeNames);
    }
    
    // Interview-related methods
    public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
        return geminiInterviewService.generateTopicsForDesignation(designationName);
    }
    
    public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation, String topicName, String difficultyLevel, int numQuestions) {
        return geminiInterviewService.generateGeneralInterviewQuestions(designation, topicName, difficultyLevel, numQuestions);
    }
    
    public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        return geminiInterviewService.generateCompanySpecificQuestions(companyName, designation, topicName, difficultyLevel, numQuestions);
    }
    
    // Comprehensive interview question generation
    public List<Map<String, Object>> generateComprehensiveInterviewQuestions(String skillName, int numQuestions) {
        return geminiInterviewService.generateComprehensiveInterviewQuestions(skillName, numQuestions);
    }
    
    public List<Map<String, Object>> generateDetailedQuestionContent(String skillName, String questionSummary) {
        return geminiInterviewService.generateDetailedQuestionContent(skillName, questionSummary);
    }
    
    public List<Map<String, Object>> generateComprehensiveTechSkills() {
        return geminiInterviewService.generateComprehensiveTechSkills();
    }
    
    public List<Map<String, Object>> generateTopicsForSkill(String skillName) {
        return geminiInterviewService.generateTopicsForSkill(skillName);
    }
    
    public List<Map<String, Object>> generateQuestionsForSkillAndTopic(String skillName, String topicName, int numQuestions) {
        return geminiInterviewService.generateQuestionsForSkillAndTopic(skillName, topicName, numQuestions);
    }
    
    // Designation-related methods (if needed)
    public List<Map<String, Object>> generateDesignationsForDepartment(String departmentName) {
        return geminiInterviewService.generateDesignationsForDepartment(departmentName);
    }
} 