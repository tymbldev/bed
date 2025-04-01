package com.tymbl.interview.service;

import com.tymbl.interview.entity.*;
import com.tymbl.interview.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
} 