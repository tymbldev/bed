package com.tymbl.interview.service;

import com.tymbl.common.entity.Designation;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.interview.dto.*;
import com.tymbl.interview.entity.*;
import com.tymbl.interview.repository.*;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewPreparationService {
    private final SkillRepository skillRepository;
    private final CompanyDesignationSkillRepository companyDesignationSkillRepository;
    private final InterviewTopicRepository interviewTopicRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final CompanyInterviewGuideRepository companyInterviewGuideRepository;
    private final CompanyRepository companyRepository;
    private final DesignationRepository designationRepository;

    // Skill Management
    @Transactional(readOnly = true)
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Skill> getSkillsByCategory(String category) {
        return skillRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Skill> searchSkills(String query) {
        return skillRepository.findByNameContainingIgnoreCase(query);
    }

    // Company-Designation-Skill Management
    @Transactional(readOnly = true)
    public List<CompanyDesignationSkill> getSkillsForCompanyAndDesignation(Long companyId, Long designationId) {
        return companyDesignationSkillRepository.findByCompanyIdAndDesignationId(companyId, designationId);
    }

    @Transactional(readOnly = true)
    public Map<String, List<CompanyDesignationSkill>> getSkillsByCompany(Long companyId) {
        List<CompanyDesignationSkill> skills = companyDesignationSkillRepository.findByCompanyId(companyId);
        return skills.stream()
                .collect(Collectors.groupingBy(skill -> skill.getDesignation().getTitle()));
    }

    // Interview Topics Management
    @Transactional(readOnly = true)
    public List<InterviewTopic> getTopicsForSkill(Long companyDesignationSkillId) {
        return interviewTopicRepository.findByCompanyDesignationSkillId(companyDesignationSkillId);
    }

    @Transactional(readOnly = true)
    public List<InterviewTopic> getTopicsByDifficulty(String difficultyLevel) {
        return interviewTopicRepository.findByDifficultyLevel(difficultyLevel);
    }

    // Interview Questions Management
    @Transactional(readOnly = true)
    public List<InterviewQuestion> getQuestionsForTopic(Long topicId) {
        return interviewQuestionRepository.findByTopicId(topicId);
    }

    @Transactional(readOnly = true)
    public List<InterviewQuestion> getQuestionsByDifficulty(String difficultyLevel) {
        return interviewQuestionRepository.findByDifficultyLevel(difficultyLevel);
    }

    // Company Interview Guide Management
    @Transactional(readOnly = true)
    public List<CompanyInterviewGuide> getCompanyGuide(Long companyId) {
        return companyInterviewGuideRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public List<CompanyInterviewGuide> getCompanyGuideBySection(Long companyId, String section) {
        return companyInterviewGuideRepository.findByCompanyIdAndSection(companyId, section);
    }

    // Admin Operations
    @Transactional
    public Skill createSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Transactional
    public CompanyDesignationSkill createCompanyDesignationSkill(CompanyDesignationSkill cds) {
        return companyDesignationSkillRepository.save(cds);
    }

    @Transactional
    public InterviewTopic createTopic(InterviewTopic topic) {
        return interviewTopicRepository.save(topic);
    }

    @Transactional
    public InterviewQuestion createQuestion(InterviewQuestion question) {
        return interviewQuestionRepository.save(question);
    }

    @Transactional
    public CompanyInterviewGuide createGuide(CompanyInterviewGuide guide) {
        return companyInterviewGuideRepository.save(guide);
    }

    // Update Operations
    @Transactional
    public Skill updateSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    @Transactional
    public InterviewTopic updateTopic(InterviewTopic topic) {
        return interviewTopicRepository.save(topic);
    }

    @Transactional
    public InterviewQuestion updateQuestion(InterviewQuestion question) {
        return interviewQuestionRepository.save(question);
    }

    @Transactional
    public CompanyInterviewGuide updateGuide(CompanyInterviewGuide guide) {
        return companyInterviewGuideRepository.save(guide);
    }

    // Delete Operations
    @Transactional
    public void deleteSkill(Long id) {
        skillRepository.deleteById(id);
    }

    @Transactional
    public void deleteTopic(Long id) {
        interviewTopicRepository.deleteById(id);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        interviewQuestionRepository.deleteById(id);
    }

    @Transactional
    public void deleteGuide(Long id) {
        companyInterviewGuideRepository.deleteById(id);
    }
    
    // New methods for controller
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(this::convertToCompanyDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CompanyDTO getCompanyWithDesignations(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        
        CompanyDTO dto = convertToCompanyDTO(company);
        dto.setDesignations(getDesignationsByCompany(companyId));
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<CompanyDesignationDTO> getDesignationsByCompany(Long companyId) {
        List<CompanyDesignationSkill> skills = companyDesignationSkillRepository.findByCompanyId(companyId);
        Map<Long, List<CompanyDesignationSkill>> designationSkillsMap = skills.stream()
                .collect(Collectors.groupingBy(cds -> cds.getDesignation().getId()));
        
        return designationSkillsMap.entrySet().stream()
                .map(entry -> {
                    Long designationId = entry.getKey();
                    List<CompanyDesignationSkill> designationSkills = entry.getValue();
                    Designation designation = designationSkills.get(0).getDesignation();
                    Company company = designationSkills.get(0).getCompany();
                    
                    return CompanyDesignationDTO.builder()
                            .id(designationId)
                            .title(designation.getTitle())
                            .level(designation.getLevel())
                            .companyId(company.getId())
                            .companyName(company.getName())
                            .skills(designationSkills.stream()
                                    .map(cds -> SkillDTO.builder()
                                            .id(cds.getSkill().getId())
                                            .name(cds.getSkill().getName())
                                            .description(cds.getSkill().getDescription())
                                            .category(cds.getSkill().getCategory())
                                            .importanceLevel(cds.getImportanceLevel())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<String> getSkillsByCompanyAndDesignation(Long companyId, Long designationId) {
        List<CompanyDesignationSkill> skills = companyDesignationSkillRepository
                .findByCompanyIdAndDesignationId(companyId, designationId);
        
        return skills.stream()
                .map(cds -> cds.getSkill().getName())
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<InterviewTopicDTO> getTopicsByCompanyDesignationSkill(Long companyId, Long designationId, Long skillId) {
        List<CompanyDesignationSkill> cdsList = companyDesignationSkillRepository
                .findByCompanyIdAndDesignationIdAndSkillId(companyId, designationId, skillId);
        
        if (cdsList.isEmpty()) {
            return new ArrayList<>();
        }
        
        Long companyDesignationSkillId = cdsList.get(0).getId();
        List<InterviewTopic> topics = interviewTopicRepository.findByCompanyDesignationSkillId(companyDesignationSkillId);
        
        return topics.stream()
                .map(this::convertToInterviewTopicDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public InterviewTopicDTO getTopicWithQuestions(Long topicId) {
        InterviewTopic topic = interviewTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        InterviewTopicDTO dto = convertToInterviewTopicDTO(topic);
        
        List<InterviewQuestion> questions = interviewQuestionRepository.findByTopicId(topicId);
        dto.setQuestions(questions.stream()
                .map(this::convertToInterviewQuestionDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    @Transactional(readOnly = true)
    public InterviewQuestionDTO getInterviewQuestion(Long questionId) {
        InterviewQuestion question = interviewQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        return convertToInterviewQuestionDTO(question);
    }
    
    // Helper methods for DTO conversion
    private CompanyDTO convertToCompanyDTO(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .website(company.getWebsite())
                .logoUrl(company.getLogoUrl())
                .build();
    }
    
    private InterviewTopicDTO convertToInterviewTopicDTO(InterviewTopic topic) {
        CompanyDesignationSkill cds = topic.getCompanyDesignationSkill();
        return InterviewTopicDTO.builder()
                .id(topic.getId())
                .title(topic.getTitle())
                .description(topic.getDescription())
                .content(topic.getContent())
                .difficultyLevel(topic.getDifficultyLevel())
                .companyName(cds.getCompany().getName())
                .designationTitle(cds.getDesignation().getTitle())
                .skillName(cds.getSkill().getName())
                .build();
    }
    
    private InterviewQuestionDTO convertToInterviewQuestionDTO(InterviewQuestion question) {
        return InterviewQuestionDTO.builder()
                .id(question.getId())
                .topicId(question.getTopic().getId())
                .topicTitle(question.getTopic().getTitle())
                .question(question.getQuestion())
                .answer(question.getAnswer())
                .difficultyLevel(question.getDifficultyLevel())
                .build();
    }
} 