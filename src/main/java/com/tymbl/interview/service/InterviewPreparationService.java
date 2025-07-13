package com.tymbl.interview.service;

import com.tymbl.common.service.GeminiService;
import com.tymbl.interview.dto.InterviewQuestionDTO;
import com.tymbl.interview.dto.DesignationSkillDTO;
import com.tymbl.interview.dto.QuestionGenerationRequestDTO;
import com.tymbl.interview.entity.*;
import com.tymbl.interview.repository.*;
import com.tymbl.common.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewPreparationService {

    private final DesignationSkillRepository designationSkillRepository;
    private final GeneralInterviewQuestionRepository generalInterviewQuestionRepository;
    private final CompanyInterviewQuestionRepository companyInterviewQuestionRepository;
    private final QuestionGenerationQueueRepository questionGenerationQueueRepository;
    private final DesignationRepository designationRepository;
    private final GeminiService geminiService;

    // Topic Management
    public List<DesignationSkillDTO> getTopicsByDesignation(String designation) {
        log.info("Fetching topics for designation: {}", designation);
        return designationSkillRepository.findByDesignation(designation)
                .stream()
                .map(DesignationSkillDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DesignationSkillDTO> getTopicsByDesignationAndDifficulty(String designation, DesignationSkill.DifficultyLevel difficultyLevel) {
        log.info("Fetching topics for designation: {} with difficulty: {}", designation, difficultyLevel);
        return designationSkillRepository.findByDesignationAndDifficultyLevel(designation, difficultyLevel)
                .stream()
                .map(DesignationSkillDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DesignationSkillDTO> getTopicsByDesignationAndCategory(String designation, String category) {
        log.info("Fetching topics for designation: {} with category: {}", designation, category);
        return designationSkillRepository.findByDesignationAndCategory(designation, category)
                .stream()
                .map(DesignationSkillDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getAllDesignations() {
        log.info("Fetching all available designations");
        return designationSkillRepository.findAllDesignations();
    }

    public List<String> getCategoriesByDesignation(String designation) {
        log.info("Fetching categories for designation: {}", designation);
        return designationSkillRepository.findCategoriesByDesignation(designation);
    }

    // General Questions Management
    public List<InterviewQuestionDTO> getGeneralQuestionsByDesignation(String designation) {
        log.info("Fetching general questions for designation: {}", designation);
        return generalInterviewQuestionRepository.findByDesignation(designation)
                .stream()
                .map(InterviewQuestionDTO::fromGeneralEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewQuestionDTO> getGeneralQuestionsByDesignationAndTopic(String designation, String topicName) {
        log.info("Fetching general questions for designation: {} and topic: {}", designation, topicName);
        return generalInterviewQuestionRepository.findByDesignationAndTopicName(designation, topicName)
                .stream()
                .map(InterviewQuestionDTO::fromGeneralEntity)
                .collect(Collectors.toList());
    }

    public Page<InterviewQuestionDTO> getGeneralQuestionsByDesignationAndTopicPaginated(String designation, String topicName, int page, int size) {
        log.info("Fetching paginated general questions for designation: {} and topic: {}", designation, topicName);
        Pageable pageable = PageRequest.of(page, size);
        return generalInterviewQuestionRepository.findByDesignationAndTopicName(designation, topicName, pageable)
                .map(InterviewQuestionDTO::fromGeneralEntity);
    }

    public List<String> getTopicsWithGeneralQuestions(String designation) {
        log.info("Fetching topics with general questions for designation: {}", designation);
        return generalInterviewQuestionRepository.findTopicsByDesignation(designation);
    }

    // Company-Specific Questions Management
    public List<InterviewQuestionDTO> getCompanyQuestionsByCompanyAndDesignation(String companyName, String designation) {
        log.info("Fetching company questions for company: {} and designation: {}", companyName, designation);
        return companyInterviewQuestionRepository.findByCompanyNameAndDesignation(companyName, designation)
                .stream()
                .map(InterviewQuestionDTO::fromCompanyEntity)
                .collect(Collectors.toList());
    }

    public List<InterviewQuestionDTO> getCompanyQuestionsByCompanyAndDesignationAndTopic(String companyName, String designation, String topicName) {
        log.info("Fetching company questions for company: {}, designation: {} and topic: {}", companyName, designation, topicName);
        return companyInterviewQuestionRepository.findByCompanyNameAndDesignationAndTopicName(companyName, designation, topicName)
                .stream()
                .map(InterviewQuestionDTO::fromCompanyEntity)
                .collect(Collectors.toList());
    }

    public Page<InterviewQuestionDTO> getCompanyQuestionsByCompanyAndDesignationAndTopicPaginated(String companyName, String designation, String topicName, int page, int size) {
        log.info("Fetching paginated company questions for company: {}, designation: {} and topic: {}", companyName, designation, topicName);
        Pageable pageable = PageRequest.of(page, size);
        return companyInterviewQuestionRepository.findByCompanyNameAndDesignationAndTopicName(companyName, designation, topicName, pageable)
                .map(InterviewQuestionDTO::fromCompanyEntity);
    }

    public List<String> getTopicsWithCompanyQuestions(String companyName, String designation) {
        log.info("Fetching topics with company questions for company: {} and designation: {}", companyName, designation);
        return companyInterviewQuestionRepository.findTopicsByCompanyAndDesignation(companyName, designation);
    }

    public List<String> getCompaniesWithQuestions(String designation) {
        log.info("Fetching companies with questions for designation: {}", designation);
        return companyInterviewQuestionRepository.findCompaniesByDesignation(designation);
    }

    // Question Generation
    @Transactional
    public Long requestQuestionGeneration(QuestionGenerationRequestDTO request) {
        log.info("Requesting question generation: {}", request);
        
        QuestionGenerationQueue.RequestType requestType = QuestionGenerationQueue.RequestType.valueOf(request.getRequestType().toUpperCase());
        QuestionGenerationQueue.DifficultyLevel difficultyLevel = QuestionGenerationQueue.DifficultyLevel.valueOf(request.getDifficultyLevel().toUpperCase());
        
        QuestionGenerationQueue queueEntry = QuestionGenerationQueue.builder()
                .requestType(requestType)
                .designation(request.getDesignation())
                .companyName(request.getCompanyName())
                .topicName(request.getTopicName())
                .difficultyLevel(difficultyLevel)
                .numQuestions(request.getNumQuestions() != null ? request.getNumQuestions() : 5)
                .status(QuestionGenerationQueue.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        QuestionGenerationQueue saved = questionGenerationQueueRepository.save(queueEntry);
        log.info("Created question generation request with ID: {}", saved.getId());
        
        // Trigger async generation
        generateQuestionsAsync(saved.getId());
        
        return saved.getId();
    }

    @Async
    @Transactional
    public void generateQuestionsAsync(Long queueId) {
        try {
            log.info("Starting async question generation for queue ID: {}", queueId);
            
            QuestionGenerationQueue queueEntry = questionGenerationQueueRepository.findById(queueId)
                    .orElseThrow(() -> new RuntimeException("Queue entry not found: " + queueId));
            
            // Update status to IN_PROGRESS
            queueEntry.setStatus(QuestionGenerationQueue.Status.IN_PROGRESS);
            queueEntry.setUpdatedAt(LocalDateTime.now());
            questionGenerationQueueRepository.save(queueEntry);
            
            List<Map<String, Object>> generatedQuestions;
            
            if (queueEntry.getRequestType() == QuestionGenerationQueue.RequestType.GENERAL) {
                generatedQuestions = generateGeneralQuestions(queueEntry);
            } else {
                generatedQuestions = generateCompanySpecificQuestions(queueEntry);
            }
            
            // Save generated questions
            saveGeneratedQuestions(queueEntry, generatedQuestions);
            
            // Update status to COMPLETED
            queueEntry.setStatus(QuestionGenerationQueue.Status.COMPLETED);
            queueEntry.setUpdatedAt(LocalDateTime.now());
            questionGenerationQueueRepository.save(queueEntry);
            
            log.info("Successfully completed question generation for queue ID: {}", queueId);
            
        } catch (Exception e) {
            log.error("Error in async question generation for queue ID: {}", queueId, e);
            
            // Update status to FAILED
            try {
                QuestionGenerationQueue queueEntry = questionGenerationQueueRepository.findById(queueId).orElse(null);
                if (queueEntry != null) {
                    queueEntry.setStatus(QuestionGenerationQueue.Status.FAILED);
                    queueEntry.setErrorMessage(e.getMessage());
                    queueEntry.setUpdatedAt(LocalDateTime.now());
                    questionGenerationQueueRepository.save(queueEntry);
                }
            } catch (Exception updateError) {
                log.error("Error updating queue status to FAILED for queue ID: {}", queueId, updateError);
            }
        }
    }

    private List<Map<String, Object>> generateGeneralQuestions(QuestionGenerationQueue queueEntry) {
        List<Map<String, Object>> allQuestions = new ArrayList<>();
        
        List<String> topicsToGenerate = queueEntry.getTopicName() != null ? 
                Arrays.asList(queueEntry.getTopicName()) : 
                designationSkillRepository.findByDesignation(queueEntry.getDesignation())
                        .stream()
                        .map(DesignationSkill::getSkillName)
                        .collect(Collectors.toList());
        
        for (String topicName : topicsToGenerate) {
            List<Map<String, Object>> questions = geminiService.generateGeneralInterviewQuestions(
                    queueEntry.getDesignation(),
                    topicName,
                    queueEntry.getDifficultyLevel().name(),
                    queueEntry.getNumQuestions()
            );
            allQuestions.addAll(questions);
        }
        
        return allQuestions;
    }

    private List<Map<String, Object>> generateCompanySpecificQuestions(QuestionGenerationQueue queueEntry) {
        List<Map<String, Object>> allQuestions = new ArrayList<>();
        
        List<String> topicsToGenerate = queueEntry.getTopicName() != null ? 
                Arrays.asList(queueEntry.getTopicName()) : 
                designationSkillRepository.findByDesignation(queueEntry.getDesignation())
                        .stream()
                        .map(DesignationSkill::getSkillName)
                        .collect(Collectors.toList());
        
        for (String topicName : topicsToGenerate) {
            List<Map<String, Object>> questions = geminiService.generateCompanySpecificQuestions(
                    queueEntry.getCompanyName(),
                    queueEntry.getDesignation(),
                    topicName,
                    queueEntry.getDifficultyLevel().name(),
                    queueEntry.getNumQuestions()
            );
            allQuestions.addAll(questions);
        }
        
        return allQuestions;
    }

    private void saveGeneratedQuestions(QuestionGenerationQueue queueEntry, List<Map<String, Object>> questions) {
        LocalDateTime now = LocalDateTime.now();
        
        for (Map<String, Object> questionData : questions) {
            if (queueEntry.getRequestType() == QuestionGenerationQueue.RequestType.GENERAL) {
                GeneralInterviewQuestion question = GeneralInterviewQuestion.builder()
                        .designation(queueEntry.getDesignation())
                        .topicName((String) questionData.get("topic_name"))
                        .questionText((String) questionData.get("question_text"))
                        .answerText((String) questionData.get("answer_text"))
                        .difficultyLevel(GeneralInterviewQuestion.DifficultyLevel.valueOf((String) questionData.get("difficulty_level")))
                        .questionType(GeneralInterviewQuestion.QuestionType.valueOf((String) questionData.get("question_type")))
                        .tags((String) questionData.get("tags"))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                generalInterviewQuestionRepository.save(question);
            } else {
                CompanyInterviewQuestion question = CompanyInterviewQuestion.builder()
                        .companyName(queueEntry.getCompanyName())
                        .designation(queueEntry.getDesignation())
                        .topicName((String) questionData.get("topic_name"))
                        .questionText((String) questionData.get("question_text"))
                        .answerText((String) questionData.get("answer_text"))
                        .difficultyLevel(CompanyInterviewQuestion.DifficultyLevel.valueOf((String) questionData.get("difficulty_level")))
                        .questionType(CompanyInterviewQuestion.QuestionType.valueOf((String) questionData.get("question_type")))
                        .companyContext((String) questionData.get("company_context"))
                        .tags((String) questionData.get("tags"))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                companyInterviewQuestionRepository.save(question);
            }
        }
    }

    // Queue Management
    public List<QuestionGenerationQueue> getPendingRequests() {
        return questionGenerationQueueRepository.findPendingRequests();
    }

    public List<QuestionGenerationQueue> getInProgressRequests() {
        return questionGenerationQueueRepository.findInProgressRequests();
    }

    public QuestionGenerationQueue getQueueEntryById(Long queueId) {
        return questionGenerationQueueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue entry not found: " + queueId));
    }

    // Statistics
    public Map<String, Object> getStatistics(String designation) {
        Map<String, Object> stats = new HashMap<>();
        
        // Topic statistics
        List<DesignationSkill> topics = designationSkillRepository.findByDesignation(designation);
        stats.put("total_topics", topics.size());
        stats.put("topics_by_difficulty", topics.stream()
                .collect(Collectors.groupingBy(topic -> topic.getDifficultyLevel().name(), Collectors.counting())));
        
        // General questions statistics
        List<GeneralInterviewQuestion> generalQuestions = generalInterviewQuestionRepository.findByDesignation(designation);
        stats.put("total_general_questions", generalQuestions.size());
        stats.put("general_questions_by_topic", generalQuestions.stream()
                .collect(Collectors.groupingBy(GeneralInterviewQuestion::getTopicName, Collectors.counting())));
        
        // Company questions statistics
        List<String> companies = companyInterviewQuestionRepository.findCompaniesByDesignation(designation);
        stats.put("companies_with_questions", companies.size());
        
        return stats;
    }

    // New method for generating topics for a designation
    public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
        log.info("Generating topics for designation: {}", designationName);
        
        // Verify the designation exists
        if (!designationRepository.existsByName(designationName)) {
            log.warn("Designation not found: {}", designationName);
            return Collections.emptyList();
        }
        
        // Generate topics using GenAI
        List<Map<String, Object>> generatedTopics = geminiService.generateTopicsForDesignation(designationName);
        
        if (generatedTopics.isEmpty()) {
            log.warn("No topics generated for designation: {}", designationName);
            return Collections.emptyList();
        }
        
        log.info("Successfully generated {} topics for designation: {}", generatedTopics.size(), designationName);
        return generatedTopics;
    }

    // Method to get all designations and generate topics for each
    public Map<String, List<Map<String, Object>>> generateTopicsForAllDesignations() {
        log.info("Generating topics for all designations");
        
        Map<String, List<Map<String, Object>>> allTopics = new HashMap<>();
        
        // Get all enabled designations
        List<com.tymbl.common.entity.Designation> designations = designationRepository.findAll()
                .stream()
                .filter(com.tymbl.common.entity.Designation::isEnabled)
                .collect(Collectors.toList());
        
        log.info("Found {} enabled designations", designations.size());
        
        for (com.tymbl.common.entity.Designation designation : designations) {
            String designationName = designation.getName();
            log.info("Generating topics for designation: {}", designationName);
            
            List<Map<String, Object>> topics = generateTopicsForDesignation(designationName);
            allTopics.put(designationName, topics);
            
            // Add a small delay to avoid rate limiting
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Completed topic generation for {} designations", allTopics.size());
        return allTopics;
    }

    // Method to save generated topics to database
    @Transactional
    public List<DesignationSkillDTO> saveGeneratedTopics(String designationName, List<Map<String, Object>> topics) {
        log.info("Saving {} generated topics for designation: {}", topics.size(), designationName);
        
        List<DesignationSkillDTO> savedTopics = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (Map<String, Object> topicData : topics) {
            try {
                DesignationSkill topic = DesignationSkill.builder()
                        .designation(designationName)
                        .skillName((String) topicData.get("skill_name"))
                        .skillDescription((String) topicData.get("skill_description"))
                        .difficultyLevel(DesignationSkill.DifficultyLevel.valueOf(((String) topicData.get("difficulty_level")).toUpperCase()))
                        .category((String) topicData.get("category"))
                        .estimatedPrepTimeHours((Integer) topicData.get("estimated_prep_time_hours"))
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                
                DesignationSkill savedTopic = designationSkillRepository.save(topic);
                savedTopics.add(DesignationSkillDTO.fromEntity(savedTopic));
                
            } catch (Exception e) {
                log.error("Error saving topic: {}", topicData, e);
            }
        }
        
        log.info("Successfully saved {} topics for designation: {}", savedTopics.size(), designationName);
        return savedTopics;
    }

    // Method to get all designations
    public List<String> getAllDesignationsFromDatabase() {
        log.info("Fetching all designations from database");
        return designationRepository.findAll()
                .stream()
                .filter(com.tymbl.common.entity.Designation::isEnabled)
                .map(com.tymbl.common.entity.Designation::getName)
                .collect(Collectors.toList());
    }
} 