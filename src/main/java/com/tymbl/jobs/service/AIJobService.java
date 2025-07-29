package com.tymbl.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tymbl.common.entity.Skill;
import com.tymbl.common.entity.SkillTopic;
import com.tymbl.common.repository.DesignationRepository;
import com.tymbl.common.repository.IndustryRepository;
import com.tymbl.common.repository.SkillRepository;
import com.tymbl.common.repository.SkillTopicRepository;
import com.tymbl.common.service.DropdownService;
import com.tymbl.common.service.GeminiService;
import com.tymbl.common.util.CompanyNameCleaner;
import com.tymbl.common.util.DesignationNameCleaner;
import com.tymbl.interview.entity.InterviewQuestion;
import com.tymbl.interview.repository.InterviewQuestionRepository;
import com.tymbl.jobs.entity.Company;
import com.tymbl.jobs.entity.CompanyContent;
import com.tymbl.jobs.repository.CompanyContentRepository;
import com.tymbl.jobs.repository.CompanyRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIJobService {

    private final CompanyRepository companyRepository;
    private final CompanyContentRepository companyContentRepository;
    private final IndustryRepository industryRepository;
    private final SkillRepository skillRepository;
    private final SkillTopicRepository skillTopicRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final DesignationRepository designationRepository;
    private final GeminiService geminiService;
    private final DropdownService dropdownService;
    private final CompanyTransactionService companyTransactionService;
    
    // ============================================================================
    // COMPANY GENERATION METHODS
    // ============================================================================

    public Map<String, Object> generateCompaniesBatch() {
        List<Map<String, Object>> industryResults = new ArrayList<>();
        int totalGenerated = 0;
        int totalSkipped = 0;
        List<String> globalErrors = new ArrayList<>();
        
        try {
            List<com.tymbl.common.entity.Industry> industries = industryRepository.findAll();
            for (com.tymbl.common.entity.Industry industry : industries) {
                String industryName = industry.getName();
                Long industryId = industry.getId();
                List<Company> existingCompanies = companyRepository.findByPrimaryIndustryId(industryId);
                Set<String> existingNames = existingCompanies.stream()
                    .map(c -> c.getName().trim().toLowerCase())
                    .collect(Collectors.toSet());
                Set<String> existingWebsites = existingCompanies.stream()
                    .map(c -> normalizeWebsite(c.getWebsite()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

                List<Map<String, String>> generatedCompanies = geminiService.generateCompanyListForIndustry(
                    industryName, new ArrayList<>(existingNames));
                int generated = 0, skipped = 0;
                List<String> errors = new ArrayList<>();
                
                for (Map<String, String> companyMap : generatedCompanies) {
                    String rawName = companyMap.getOrDefault("name", "");
                    String website = normalizeWebsite(companyMap.get("website"));
                    
                    // Clean and validate the company name
                    String cleanedName = CompanyNameCleaner.cleanAndValidateCompanyName(rawName);
                    if (cleanedName == null || website == null || website.isEmpty()) {
                        skipped++;
                        continue;
                    }
                    
                    if (existingNames.contains(cleanedName.toLowerCase()) || existingWebsites.contains(website)) {
                        skipped++;
                        continue;
                    }
                    
                    // Save new company
                    Company company = new Company();
                    company.setName(cleanedName);
                    company.setWebsite(website);
                    company.setPrimaryIndustryId(industryId);
                    try {
                        companyRepository.save(company);
                        existingNames.add(cleanedName.toLowerCase());
                        existingWebsites.add(website);
                        generated++;
                    } catch (Exception e) {
                        errors.add("Failed to save: " + cleanedName + " (" + website + ") - " + e.getMessage());
                        skipped++;
                    }
                }
                
                totalGenerated += generated;
                totalSkipped += skipped;
                Map<String, Object> result = new HashMap<>();
                result.put("industry", industryName);
                result.put("generated", generated);
                result.put("skipped", skipped);
                result.put("errors", errors);
                industryResults.add(result);
            }
        } catch (Exception e) {
            globalErrors.add("Fatal error: " + e.getMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("industry_results", industryResults);
        response.put("total_generated", totalGenerated);
        response.put("total_skipped", totalSkipped);
        response.put("errors", globalErrors);
        response.put("message", "Company generation completed");
        return response;
    }

    // ============================================================================
    // CONTENT SHORTENING METHODS
    // ============================================================================

    public Map<String, Object> shortenCompanyContent(Long companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (!companyOpt.isPresent()) {
            throw new RuntimeException("Company not found with ID: " + companyId);
        }
        
        Company company = companyOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("companyId", companyId);
        response.put("companyName", company.getName());
        
        // Get or create company content record
        Optional<CompanyContent> contentOpt = companyContentRepository.findByCompanyId(companyId);
        CompanyContent companyContent;
        
        if (contentOpt.isPresent()) {
            companyContent = contentOpt.get();
            // Check if company has already been processed for content shortening
            if (companyContent.isContentShortened()) {
                response.put("aboutUsShortened", false);
                response.put("cultureShortened", false);
                response.put("message", "Company content has already been shortened");
                response.put("alreadyProcessed", true);
                log.info("Company content already shortened for: {} (ID: {})", company.getName(), companyId);
                return response;
            }
        } else {
            // Create new company content record
            companyContent = new CompanyContent();
            companyContent.setCompanyId(companyId);
        }
        
        boolean aboutUsShortened = false;
        boolean cultureShortened = false;
        
        // Shorten about us content if original exists
        if (companyContent.getAboutUsOriginal() != null && !companyContent.getAboutUsOriginal().trim().isEmpty()) {
            String shortenedAboutUs = geminiService.shortenContent(companyContent.getAboutUsOriginal(), "about us");
            if (shortenedAboutUs != null && !shortenedAboutUs.trim().isEmpty()) {
                company.setAboutUs(shortenedAboutUs);
                aboutUsShortened = true;
                log.info("Shortened about us content for company: {} (ID: {})", company.getName(), companyId);
            }
        }
        
        // Shorten culture content if original exists
        if (companyContent.getCultureOriginal() != null && !companyContent.getCultureOriginal().trim().isEmpty()) {
            String shortenedCulture = geminiService.shortenContent(companyContent.getCultureOriginal(), "culture");
            if (shortenedCulture != null && !shortenedCulture.trim().isEmpty()) {
                company.setCulture(shortenedCulture);
                cultureShortened = true;
                log.info("Shortened culture content for company: {} (ID: {})", company.getName(), companyId);
            }
        }
        
        // Save the company with shortened content and mark as processed
        if (aboutUsShortened || cultureShortened) {
            companyContent.setContentShortened(true);
            companyContentRepository.save(companyContent);
            companyRepository.save(company);
            response.put("aboutUsShortened", aboutUsShortened);
            response.put("cultureShortened", cultureShortened);
            response.put("message", "Content shortened and saved successfully");
            log.info("Successfully saved shortened content for company: {} (ID: {})", company.getName(), companyId);
        } else {
            response.put("aboutUsShortened", false);
            response.put("cultureShortened", false);
            response.put("message", "No original content found to shorten");
            log.info("No original content found to shorten for company: {} (ID: {})", company.getName(), companyId);
        }
        
        return response;
    }

    public Map<String, Object> shortenAllCompaniesContent() {
        // Only get companies that haven't been processed for content shortening and have original content
        List<CompanyContent> unprocessedContent = companyContentRepository.findUnprocessedContentWithOriginalData();
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalAboutUsShortened = 0;
        int totalCultureShortened = 0;
        int totalErrors = 0;
        int totalSkipped = 0;
        
        log.info("Found {} unprocessed companies with original content for shortening", unprocessedContent.size());
        
        for (CompanyContent companyContent : unprocessedContent) {
            try {
                Map<String, Object> result = companyTransactionService.processCompanyContentShorteningInTransaction(companyContent.getCompanyId());
                companyResults.add(result);
                totalProcessed++;
                
                // Check if company was already processed (shouldn't happen with our query, but just in case)
                if (result.containsKey("alreadyProcessed") && (Boolean) result.get("alreadyProcessed")) {
                    totalSkipped++;
                    log.info("Skipped already processed company: {} (ID: {})", result.get("companyName"), companyContent.getCompanyId());
                    continue;
                }
                
                if ((Boolean) result.get("aboutUsShortened")) {
                    totalAboutUsShortened++;
                }
                if ((Boolean) result.get("cultureShortened")) {
                    totalCultureShortened++;
                }
                
                log.info("Processed company {} of {}: {} (ID: {})", 
                    totalProcessed, unprocessedContent.size(), result.get("companyName"), companyContent.getCompanyId());
                
            } catch (Exception e) {
                totalErrors++;
                log.error("Error shortening content for company: {} (ID: {})", 
                    companyContent.getCompanyId(), companyContent.getCompanyId(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("companyId", companyContent.getCompanyId());
                errorResult.put("companyName", "Unknown");
                errorResult.put("success", false);
                errorResult.put("error", e.getMessage());
                companyResults.add(errorResult);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", totalProcessed);
        result.put("totalAboutUsShortened", totalAboutUsShortened);
        result.put("totalCultureShortened", totalCultureShortened);
        result.put("totalErrors", totalErrors);
        result.put("totalSkipped", totalSkipped);
        result.put("companyResults", companyResults);
        
        log.info("Content shortening completed. Processed: {}, AboutUs: {}, Culture: {}, Errors: {}, Skipped: {}", 
            totalProcessed, totalAboutUsShortened, totalCultureShortened, totalErrors, totalSkipped);
        
        return result;
    }

    /**
     * Shorten content for all companies in batches with individual transactions per batch
     * This ensures that each batch is processed in its own transaction
     */
    public Map<String, Object> shortenAllCompaniesContentInBatches() {
        log.info("Starting content shortening for companies in batches");
        
        // Only get companies that haven't been processed for content shortening and have original content
        List<CompanyContent> unprocessedContent = companyContentRepository.findUnprocessedContentWithOriginalData();
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalAboutUsShortened = 0;
        int totalCultureShortened = 0;
        int totalErrors = 0;
        int totalSkipped = 0;
        int batchSize = 10; // Process 10 companies at a time
        
        log.info("Found {} unprocessed companies with original content for shortening", unprocessedContent.size());
        
        for (int i = 0; i < unprocessedContent.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, unprocessedContent.size());
            List<CompanyContent> batch = unprocessedContent.subList(i, endIndex);
            
            log.info("Processing content shortening batch {} with {} companies", (i / batchSize) + 1, batch.size());
            
            // Process each company in the batch with its own transaction
            for (CompanyContent companyContent : batch) {
                try {
                    Map<String, Object> result = companyTransactionService.processCompanyContentShorteningInTransaction(companyContent.getCompanyId());
                    companyResults.add(result);
                    totalProcessed++;
                    
                    // Check if company was already processed (shouldn't happen with our query, but just in case)
                    if (result.containsKey("alreadyProcessed") && (Boolean) result.get("alreadyProcessed")) {
                        totalSkipped++;
                        log.info("Skipped already processed company: {} (ID: {})", result.get("companyName"), companyContent.getCompanyId());
                        continue;
                    }
                    
                    if ((Boolean) result.get("aboutUsShortened")) {
                        totalAboutUsShortened++;
                    }
                    if ((Boolean) result.get("cultureShortened")) {
                        totalCultureShortened++;
                    }
                    
                    log.info("Successfully processed content shortening for company: {} (ID: {})", 
                        result.get("companyName"), companyContent.getCompanyId());
                    
                } catch (Exception e) {
                    totalErrors++;
                    log.error("Error shortening content for company: {} (ID: {})", 
                        companyContent.getCompanyId(), companyContent.getCompanyId(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("companyId", companyContent.getCompanyId());
                    errorResult.put("companyName", "Unknown");
                    errorResult.put("success", false);
                    errorResult.put("error", e.getMessage());
                    companyResults.add(errorResult);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProcessed", totalProcessed);
        result.put("totalAboutUsShortened", totalAboutUsShortened);
        result.put("totalCultureShortened", totalCultureShortened);
        result.put("totalErrors", totalErrors);
        result.put("totalSkipped", totalSkipped);
        result.put("companyResults", companyResults);
        
        log.info("Content shortening in batches completed. Processed: {}, AboutUs: {}, Culture: {}, Errors: {}, Skipped: {}", 
            totalProcessed, totalAboutUsShortened, totalCultureShortened, totalErrors, totalSkipped);
        
        return result;
    }

    // ============================================================================
    // INTERVIEW QUESTION GENERATION METHODS
    // ============================================================================

    public Map<String, Object> generateComprehensiveInterviewQuestions() {
        List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
        List<Map<String, Object>> skillResults = new ArrayList<>();
        int totalSkillsProcessed = 0;
        int totalQuestionsGenerated = 0;
        
        for (Skill skill : skills) {
            try {
                Map<String, Object> result = generateComprehensiveInterviewQuestionsForSkill(skill.getName());
                skillResults.add(result);
                totalSkillsProcessed++;
                totalQuestionsGenerated += (int) result.getOrDefault("questions_generated", 0);
            } catch (Exception e) {
                log.error("Error generating questions for skill: {}", skill.getName(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("skill_name", skill.getName());
                errorResult.put("skill_id", skill.getId());
                errorResult.put("questions_generated", 0);
                errorResult.put("mappings_created", 0);
                errorResult.put("status", "error");
                errorResult.put("error", e.getMessage());
                skillResults.add(errorResult);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_skills_processed", totalSkillsProcessed);
        response.put("total_questions_generated", totalQuestionsGenerated);
        response.put("skill_results", skillResults);
        response.put("message", "Comprehensive question generation completed");
        return response;
    }

    public Map<String, Object> generateComprehensiveInterviewQuestionsForSkill(String skillName) {
        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName));
        
        log.info("Generating comprehensive interview questions for skill: {}", skillName);
        List<Map<String, Object>> questions = geminiService.generateComprehensiveInterviewQuestions(skillName, 30);
        
        int questionsGenerated = questions.size();
        int mappingsCreated = 0;
        
        for (Map<String, Object> question : questions) {
            try {
                InterviewQuestion interviewQuestion = createInterviewQuestionFromMap(question, skill);
                interviewQuestionRepository.save(interviewQuestion);
                mappingsCreated += 3; // Assuming 3 mappings per question
            } catch (Exception e) {
                log.error("Error saving question for skill: {}", skillName, e);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("skill_name", skill.getName());
        result.put("skill_id", skill.getId());
        result.put("questions_generated", questionsGenerated);
        result.put("mappings_created", mappingsCreated);
        result.put("status", "success");
        return result;
    }

    public Map<String, Object> generateAndSaveTechSkills() {
        List<Map<String, Object>> generatedSkills = geminiService.generateComprehensiveTechSkills();
        List<Map<String, Object>> addedSkills = new ArrayList<>();
        int newSkillsAdded = 0;
        
        for (Map<String, Object> skillData : generatedSkills) {
            String skillName = (String) skillData.get("name");
            if (skillName != null && !skillName.trim().isEmpty()) {
                Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(skillName);
                if (!existingSkill.isPresent()) {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    newSkill.setDescription((String) skillData.get("description"));
                    newSkill.setCategory((String) skillData.get("category"));
                    newSkill.setEnabled(true);
                    newSkill.setUsageCount(0L);
                    
                    try {
                        skillRepository.save(newSkill);
                        newSkillsAdded++;
                        addedSkills.add(skillData);
                        log.info("Added new skill: {}", skillName);
                    } catch (Exception e) {
                        log.error("Error saving skill: {}", skillName, e);
                    }
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("new_skills_added", newSkillsAdded);
        response.put("total_skills_generated", generatedSkills.size());
        response.put("added_skills", addedSkills);
        return response;
    }

    public Map<String, Object> generateAndSaveTopicsForSkill(String skillName) {
        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName));
        
        List<Map<String, Object>> generatedTopics = geminiService.generateTopicsForSkill(skillName);
        List<Map<String, Object>> addedTopics = new ArrayList<>();
        int topicsAdded = 0;
        
        for (Map<String, Object> topicData : generatedTopics) {
            String topicName = (String) topicData.get("topic");
            if (topicName != null && !topicName.trim().isEmpty()) {
                // Check if topic already exists for this skill
                List<SkillTopic> existingTopics = skillTopicRepository.findBySkill(skill);
                boolean topicExists = existingTopics.stream()
                    .anyMatch(topic -> topic.getTopic().equalsIgnoreCase(topicName));
                
                if (!topicExists) {
                    SkillTopic newTopic = new SkillTopic();
                    newTopic.setSkill(skill);
                    newTopic.setTopic(topicName);
                    newTopic.setDescription((String) topicData.get("description"));
                    
                    try {
                        skillTopicRepository.save(newTopic);
                        topicsAdded++;
                        addedTopics.add(topicData);
                        log.info("Added new topic: {} for skill: {}", topicName, skillName);
                    } catch (Exception e) {
                        log.error("Error saving topic: {} for skill: {}", topicName, skillName, e);
                    }
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("skill_name", skill.getName());
        response.put("topics_added", topicsAdded);
        response.put("topics", addedTopics);
        return response;
    }

    public Map<String, Object> generateAndSaveTopicsForAllSkills() {
        List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Skill skill : skills) {
            try {
                Map<String, Object> result = generateAndSaveTopicsForSkill(skill.getName());
                result.put("message", "Topics generated and saved successfully");
                results.add(result);
            } catch (Exception e) {
                log.error("Error generating topics for skill: {}", skill.getName(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("skill_name", skill.getName());
                errorResult.put("topics_added", 0);
                errorResult.put("message", "Error: " + e.getMessage());
                results.add(errorResult);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_skills", skills.size());
        response.put("results", results);
        response.put("message", "Topics generated and saved for all skills");
        return response;
    }

    public Map<String, Object> generateAndSaveQuestionsForSkillAndTopic(String skillName, String topicName, int numQuestions) {
        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName));
        
        // Find the topic by skill and topic name
        List<SkillTopic> topics = skillTopicRepository.findBySkill(skill);
        SkillTopic skillTopic = topics.stream()
            .filter(topic -> topic.getTopic().equalsIgnoreCase(topicName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Topic not found: " + topicName + " for skill: " + skillName));
        
        return generateQuestionsForSkillAndTopicInternal(skill, skillTopic, numQuestions);
    }

    public Map<String, Object> generateAndSaveQuestionsForAllTopicsOfSkill(String skillName, int numQuestions) {
        Skill skill = skillRepository.findByNameIgnoreCase(skillName)
            .orElseThrow(() -> new RuntimeException("Skill not found: " + skillName));
        
        List<SkillTopic> topics = skillTopicRepository.findBySkill(skill);
        List<Map<String, Object>> results = new ArrayList<>();
        int totalQuestions = 0;
        
        // Multithreading: process up to 10 topics in parallel
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();
        
        for (SkillTopic topic : topics) {
            futures.add(executor.submit(() -> generateQuestionsForSkillAndTopicInternal(skill, topic, numQuestions)));
        }
        
        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> topicSummary = future.get();
                results.add(topicSummary);
                totalQuestions += (int) topicSummary.getOrDefault("questions_added", 0);
            } catch (Exception e) {
                log.error("Error processing topic in parallel", e);
            }
        }
        
        executor.shutdown();
        
        Map<String, Object> result = new HashMap<>();
        result.put("skill_name", skill.getName());
        result.put("topics_processed", results.size());
        result.put("total_questions_added", totalQuestions);
        result.put("topic_summaries", results);
        result.put("message", "Questions generated and (optionally) saved for all topics");
        return result;
    }

    public Map<String, Object> generateAndSaveQuestionsForAllSkillsAndTopics(int numQuestions) {
        List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
        List<Map<String, Object>> results = new ArrayList<>();
        int totalSkills = 0;
        
        for (Skill skill : skills) {
            totalSkills++;
            try {
                Map<String, Object> result = generateAndSaveQuestionsForAllTopicsOfSkill(skill.getName(), numQuestions);
                result.put("skill_name", skill.getName());
                results.add(result);
            } catch (Exception e) {
                log.error("Error generating questions for skill: {}", skill.getName(), e);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("skill_name", skill.getName());
                errorResult.put("error", e.getMessage());
                results.add(errorResult);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_skills", totalSkills);
        summary.put("results", results);
        summary.put("message", "Questions generated and saved for all skills and topics");
        return summary;
    }

    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================

    private Map<String, Object> generateQuestionsForSkillAndTopicInternal(Skill skill, SkillTopic skillTopic, int numQuestions) {
        Long existingCount = interviewQuestionRepository.countBySkillIdAndTopicId(skill.getId(), skillTopic.getId());
        if (existingCount != null && existingCount >= 20) {
            log.info("[AI] Skipping question generation for skill='{}', topic='{}' as {} questions already exist", 
                skill.getName(), skillTopic.getTopic(), existingCount);
            Map<String, Object> topicSummary = new HashMap<>();
            topicSummary.put("topic_name", skillTopic.getTopic());
            topicSummary.put("questions_added", 0);
            topicSummary.put("skipped", true);
            topicSummary.put("message", "Skipped: already has " + existingCount + " questions");
            return topicSummary;
        }
        
        log.info("[AI] Starting question generation for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
        List<Map<String, Object>> summaryQuestions = geminiService.generateQuestionsForSkillAndTopic(skill.getName(), skillTopic.getTopic(), numQuestions);
        log.info("[AI] Generated {} summary questions for skill='{}', topic='{}'", summaryQuestions.size(), skill.getName(), skillTopic.getTopic());
        
        int questionsAdded = 0;
        for (Map<String, Object> q : summaryQuestions) {
            String questionText = (String) q.get("question");
            if (questionText == null || questionText.trim().isEmpty()) {
                log.info("[AI] Skipping empty question for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
                continue;
            }
            
            log.info("[AI] Generating detailed content for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
            List<Map<String, Object>> detailedContentList = geminiService.generateDetailedQuestionContent(skill.getName(), questionText);
            Map<String, Object> detailedContent = detailedContentList.isEmpty() ? new HashMap<>() : detailedContentList.get(0);
            
            String answer = (String) detailedContent.getOrDefault("detailed_answer", "");
            String htmlContent = (String) detailedContent.getOrDefault("html_content", answer);
            String codeExamples = (String) detailedContent.getOrDefault("code_examples", "");
            
            boolean isCoding = false;
            String questionType = (String) q.get("question_type");
            String tags = (String) q.get("tags");
            if ((questionType != null && questionType.toLowerCase().contains("coding")) ||
                (tags != null && tags.toLowerCase().contains("coding")) ||
                (questionText.toLowerCase().contains("code") || questionText.toLowerCase().contains("implement") || questionText.toLowerCase().contains("write a function"))) {
                isCoding = true;
            }
            
            String javaCode = null, pythonCode = null, cppCode = null;
            if (isCoding && skill.getName().equalsIgnoreCase("dsa")) {
                log.info("[AI] Detected coding question. Generating code for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
                javaCode = generateCodeWithGemini(skill.getName(), questionText, "Java");
                pythonCode = generateCodeWithGemini(skill.getName(), questionText, "Python");
                cppCode = generateCodeWithGemini(skill.getName(), questionText, "C++");
            }
            
            log.info("[AI] Going to save question for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
            InterviewQuestion iq = InterviewQuestion.builder()
                .skillId(skill.getId())
                .skillName(skill.getName())
                .topicId(skillTopic.getId())
                .topicName(skillTopic.getTopic())
                .question(questionText)
                .answer(answer)
                .difficultyLevel((String) q.get("difficulty_level"))
                .questionType(questionType)
                .tags(tags)
                .htmlContent(htmlContent)
                .codeExamples(codeExamples)
                .javaCode(javaCode)
                .pythonCode(pythonCode)
                .cppCode(cppCode)
                .coding(isCoding)
                .build();
            
            interviewQuestionRepository.save(iq);
            questionsAdded++;
            log.info("[AI] Finished processing and saved question for skill='{}', topic='{}'", skill.getName(), skillTopic.getTopic());
        }
        
        log.info("[AI] Finished generating questions for skill='{}', topic='{}'. Total questions added: {}", 
            skill.getName(), skillTopic.getTopic(), questionsAdded);
        
        Map<String, Object> topicSummary = new HashMap<>();
        topicSummary.put("topic_name", skillTopic.getTopic());
        topicSummary.put("questions_added", questionsAdded);
        return topicSummary;
    }

    private InterviewQuestion createInterviewQuestionFromMap(Map<String, Object> question, Skill skill) {
        return InterviewQuestion.builder()
            .skillId(skill.getId())
            .skillName(skill.getName())
            .question((String) question.get("question"))
            .answer((String) question.get("summary_answer"))
            .difficultyLevel((String) question.get("difficulty_level"))
            .questionType((String) question.get("question_type"))
            .tags((String) question.get("tags"))
            .htmlContent((String) question.get("summary_answer"))
            .coding(false)
            .build();
    }

    private String generateCodeWithGemini(String skillName, String questionText, String language) {
        try {
            String prompt = "Write a working, well-commented solution in " + language + " for the following coding interview question. Only return the code, no explanation.\n\n" +
                    "SKILL: " + skillName + "\nQUESTION: " + questionText;
            List<Map<String, Object>> codeResp = geminiService.generateDetailedQuestionContent(skillName, prompt);
            if (!codeResp.isEmpty()) {
                String code = (String) codeResp.get(0).getOrDefault("detailed_answer", "");
                if (code.isEmpty()) code = (String) codeResp.get(0).getOrDefault("html_content", "");
                return code;
            }
        } catch (Exception e) {
            log.warn("Error generating code for {} in {}: {}", questionText, language, e.getMessage());
        }
        return null;
    }

    private String normalizeWebsite(String website) {
        if (website == null || website.trim().isEmpty()) {
            return null;
        }
        String normalized = website.trim().toLowerCase();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        return normalized;
    }
    
    // ============================================================================
    // SIMILAR DESIGNATION METHODS
    // ============================================================================
    
    public Map<String, Object> generateSimilarDesignationsForAll() {
        List<Map<String, Object>> designationResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalSimilarFound = 0;
        int totalNewDesignationsCreated = 0;
        int totalErrors = 0;
        
        try {
            // Get all designations that haven't been processed for similar designation generation
            List<com.tymbl.common.entity.Designation> unprocessedDesignations = 
                designationRepository.findBySimilarDesignationsProcessedFalseAndEnabledTrue();
            
            log.info("Found {} unprocessed designations for similar designation generation", unprocessedDesignations.size());
            
            for (com.tymbl.common.entity.Designation designation : unprocessedDesignations) {
                try {
                    Map<String, Object> result = generateSimilarDesignationsForDesignation(designation);
                    designationResults.add(result);
                    totalProcessed++;
                    
                    if ((Boolean) result.get("success")) {
                        totalSimilarFound += (Integer) result.get("similarDesignationsFound");
                        totalNewDesignationsCreated += (Integer) result.get("newDesignationsCreated");
                    } else {
                        totalErrors++;
                    }
                    
                    log.info("Processed designation {} of {}: {} (ID: {})", 
                        totalProcessed, unprocessedDesignations.size(), designation.getName(), designation.getId());
                    
                } catch (Exception e) {
                    log.error("Error generating similar designations for designation: {} (ID: {})", 
                        designation.getName(), designation.getId(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("designationId", designation.getId());
                    errorResult.put("designationName", designation.getName());
                    errorResult.put("success", false);
                    errorResult.put("error", e.getMessage());
                    designationResults.add(errorResult);
                    totalErrors++;
                }
            }
            
        } catch (Exception e) {
            log.error("Error in generateSimilarDesignationsForAll", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("designationId", null);
            errorResult.put("designationName", "GLOBAL_ERROR");
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            designationResults.add(errorResult);
            totalErrors++;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_designations_processed", totalProcessed);
        response.put("total_similar_designations_found", totalSimilarFound);
        response.put("total_new_designations_created", totalNewDesignationsCreated);
        response.put("total_errors", totalErrors);
        response.put("designation_results", designationResults);
        response.put("message", "Similar designation generation completed");
        
        return response;
    }
    
    private Map<String, Object> generateSimilarDesignationsForDesignation(com.tymbl.common.entity.Designation designation) {
        Map<String, Object> result = new HashMap<>();
        result.put("designationId", designation.getId());
        result.put("designationName", designation.getName());
        
        try {
            // Generate similar designations using AI
            List<String> similarDesignationNames = geminiService.generateSimilarDesignations(designation.getName());
            
            if (similarDesignationNames == null || similarDesignationNames.isEmpty()) {
                result.put("success", false);
                result.put("error", "No similar designations generated by AI");
                result.put("similarDesignationsFound", 0);
                result.put("newDesignationsCreated", 0);
                return result;
            }
            
            List<String> existingDesignationNames = new ArrayList<>();
            List<String> newDesignationNames = new ArrayList<>();
            List<Long> existingDesignationIds = new ArrayList<>();
            
            // Check which similar designations already exist and create new ones if needed
            for (String rawSimilarDesignationName : similarDesignationNames) {
                // Clean and validate the designation name
                String cleanedSimilarDesignationName = DesignationNameCleaner.cleanAndValidateDesignationName(rawSimilarDesignationName);
                if (cleanedSimilarDesignationName == null) {
                    log.warn("Skipping invalid similar designation name: '{}'", rawSimilarDesignationName);
                    continue;
                }
                
                Optional<com.tymbl.common.entity.Designation> existingDesignation = 
                    designationRepository.findByName(cleanedSimilarDesignationName);
                
                if (existingDesignation.isPresent()) {
                    existingDesignationNames.add(cleanedSimilarDesignationName);
                    existingDesignationIds.add(existingDesignation.get().getId());
                } else {
                    // Create new designation
                    com.tymbl.common.entity.Designation newDesignation = new com.tymbl.common.entity.Designation(cleanedSimilarDesignationName);
                    newDesignation.setEnabled(true);
                    newDesignation.setSimilarDesignationsProcessed(true); // Mark as processed since it's new
                    
                    try {
                        com.tymbl.common.entity.Designation savedDesignation = designationRepository.save(newDesignation);
                        newDesignationNames.add(cleanedSimilarDesignationName);
                        existingDesignationIds.add(savedDesignation.getId());
                        log.info("Created new designation: {} (ID: {})", cleanedSimilarDesignationName, savedDesignation.getId());
                    } catch (Exception e) {
                        log.error("Failed to create new designation: {}", cleanedSimilarDesignationName, e);
                        // Continue with other designations
                    }
                }
            }
            
            // Convert to JSON before storing in database
            ObjectMapper objectMapper = new ObjectMapper();
            String similarDesignationsByNameJson = objectMapper.writeValueAsString(similarDesignationNames);
            String similarDesignationsByIdJson = objectMapper.writeValueAsString(existingDesignationIds);
            
            // Update the original designation with similar designations as JSON
            designation.setSimilarDesignationsByName(similarDesignationsByNameJson);
            designation.setSimilarDesignationsById(similarDesignationsByIdJson);
            designation.setSimilarDesignationsProcessed(true);
            
            designationRepository.save(designation);
            
            result.put("success", true);
            result.put("similarDesignationsFound", similarDesignationNames.size());
            result.put("newDesignationsCreated", newDesignationNames.size());
            result.put("existingDesignationsFound", existingDesignationNames.size());
            result.put("similarDesignationNames", similarDesignationNames);
            result.put("newDesignationNames", newDesignationNames);
            result.put("existingDesignationNames", existingDesignationNames);
            result.put("similarDesignationIds", existingDesignationIds);
            
            log.info("Successfully processed similar designations for: {} (ID: {}). Found: {}, Created: {}", 
                designation.getName(), designation.getId(), similarDesignationNames.size(), newDesignationNames.size());
            
        } catch (Exception e) {
            log.error("Error generating similar designations for designation: {} (ID: {})", 
                designation.getName(), designation.getId(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("similarDesignationsFound", 0);
            result.put("newDesignationsCreated", 0);
        }
        
        return result;
    }
    
    // ============================================================================
    // SIMILAR COMPANY METHODS
    // ============================================================================
    
    public Map<String, Object> generateSimilarCompaniesForAll() {
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalSimilarFound = 0;
        int totalNewCompaniesCreated = 0;
        int totalErrors = 0;
        
        try {
            // Get all companies that haven't been processed for similar company generation and have industry info
            List<Company> unprocessedCompanies = companyRepository.findUnprocessedCompaniesWithIndustry();
            
            log.info("Found {} unprocessed companies with industry info for similar company generation", unprocessedCompanies.size());
            
            for (Company company : unprocessedCompanies) {
                try {
                    Map<String, Object> result = generateSimilarCompaniesForCompany(company);
                    companyResults.add(result);
                    totalProcessed++;
                    
                    if ((Boolean) result.get("success")) {
                        totalSimilarFound += (Integer) result.get("similarCompaniesFound");
                        totalNewCompaniesCreated += (Integer) result.get("newCompaniesCreated");
                    } else {
                        totalErrors++;
                    }
                    
                    log.info("Processed company {} of {}: {} (ID: {})", 
                        totalProcessed, unprocessedCompanies.size(), company.getName(), company.getId());
                    
                } catch (Exception e) {
                    log.error("Error generating similar companies for company: {} (ID: {})", 
                        company.getName(), company.getId(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("companyId", company.getId());
                    errorResult.put("companyName", company.getName());
                    errorResult.put("success", false);
                    errorResult.put("error", e.getMessage());
                    companyResults.add(errorResult);
                    totalErrors++;
                }
            }
            
        } catch (Exception e) {
            log.error("Error in generateSimilarCompaniesForAll", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("companyId", null);
            errorResult.put("companyName", "GLOBAL_ERROR");
            errorResult.put("success", false);
            errorResult.put("error", e.getMessage());
            companyResults.add(errorResult);
            totalErrors++;
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("total_companies_processed", totalProcessed);
        response.put("total_similar_companies_found", totalSimilarFound);
        response.put("total_new_companies_created", totalNewCompaniesCreated);
        response.put("total_errors", totalErrors);
        response.put("company_results", companyResults);
        response.put("message", "Similar company generation completed");
        
        return response;
    }

    /**
     * Generate similar companies for a single company in batches with individual transactions per batch
     * This ensures that each batch is processed in its own transaction
     */
    public Map<String, Object> generateSimilarCompaniesForAllInBatches() {
        log.info("Starting similar company generation for companies in batches");
        
        // Get all companies that haven't been processed for similar company generation and have industry info
        List<Company> unprocessedCompanies = companyRepository.findUnprocessedCompaniesWithIndustry();
        List<Map<String, Object>> companyResults = new ArrayList<>();
        int totalProcessed = 0;
        int totalSimilarFound = 0;
        int totalNewCompaniesCreated = 0;
        int totalErrors = 0;
        int batchSize = 10; // Process 10 companies at a time
        
        log.info("Found {} unprocessed companies with industry info for similar company generation", unprocessedCompanies.size());
        
        for (int i = 0; i < unprocessedCompanies.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, unprocessedCompanies.size());
            List<Company> batch = unprocessedCompanies.subList(i, endIndex);
            
            log.info("Processing similar company generation batch {} with {} companies", (i / batchSize) + 1, batch.size());
            
            // Process each company in the batch with its own transaction
            for (Company company : batch) {
                try {
                    Map<String, Object> result = companyTransactionService.processSimilarCompaniesGenerationInTransaction(company);
                    companyResults.add(result);
                    totalProcessed++;
                    
                    if ((Boolean) result.get("success")) {
                        totalSimilarFound += (Integer) result.get("similarCompaniesFound");
                        totalNewCompaniesCreated += (Integer) result.get("newCompaniesCreated");
                    } else {
                        totalErrors++;
                    }
                    
                    log.info("Successfully processed similar company generation for company: {} (ID: {})", 
                        result.get("companyName"), company.getId());
                    
                } catch (Exception e) {
                    totalErrors++;
                    log.error("Error generating similar companies for company: {} (ID: {})", 
                        company.getName(), company.getId(), e);
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("companyId", company.getId());
                    errorResult.put("companyName", company.getName());
                    errorResult.put("success", false);
                    errorResult.put("error", e.getMessage());
                    companyResults.add(errorResult);
                }
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalProcessed", totalProcessed);
        response.put("totalSimilarFound", totalSimilarFound);
        response.put("totalNewCompaniesCreated", totalNewCompaniesCreated);
        response.put("totalErrors", totalErrors);
        response.put("companyResults", companyResults);
        
        log.info("Similar company generation in batches completed. Processed: {}, Similar Found: {}, New Created: {}, Errors: {}", 
            totalProcessed, totalSimilarFound, totalNewCompaniesCreated, totalErrors);
        
        return response;
    }
    
    private Map<String, Object> generateSimilarCompaniesForCompany(Company company) {
        Map<String, Object> result = new HashMap<>();
        result.put("companyId", company.getId());
        result.put("companyName", company.getName());
        
        try {
            // Get industry name for the company
            String industryName = "Unknown Industry";
            if (company.getPrimaryIndustryId() != null) {
                Optional<com.tymbl.common.entity.Industry> industryOpt = industryRepository.findById(company.getPrimaryIndustryId());
                if (industryOpt.isPresent()) {
                    industryName = industryOpt.get().getName();
                }
            }
            
            // Generate similar companies using AI with enhanced company details
            List<String> similarCompanyNames = geminiService.generateSimilarCompanies(
                company.getName(), 
                industryName, 
                company.getDescription(),
                company.getCompanySize(),
                company.getSpecialties(),
                company.getHeadquarters()
            );
            
            if (similarCompanyNames == null || similarCompanyNames.isEmpty()) {
                result.put("success", false);
                result.put("error", "No similar companies generated by AI");
                result.put("similarCompaniesFound", 0);
                result.put("newCompaniesCreated", 0);
                return result;
            }
            
            List<String> existingCompanyNames = new ArrayList<>();
            List<String> newCompanyNames = new ArrayList<>();
            List<Long> existingCompanyIds = new ArrayList<>();
            
            // Check which similar companies already exist and create new ones if needed
            for (String rawSimilarCompanyName : similarCompanyNames) {
                // Clean and validate the company name
                String cleanedSimilarCompanyName = CompanyNameCleaner.cleanAndValidateCompanyName(rawSimilarCompanyName);
                if (cleanedSimilarCompanyName == null) {
                    log.warn("Skipping invalid similar company name: '{}'", rawSimilarCompanyName);
                    continue;
                }
                
                Optional<Company> existingCompany = companyRepository.findByName(cleanedSimilarCompanyName);
                
                if (existingCompany.isPresent()) {
                    existingCompanyNames.add(cleanedSimilarCompanyName);
                    existingCompanyIds.add(existingCompany.get().getId());
                } else {
                    // Create new company
                    Company newCompany = new Company();
                    newCompany.setName(cleanedSimilarCompanyName);
                    newCompany.setPrimaryIndustryId(company.getPrimaryIndustryId()); // Same industry as original
                    newCompany.setSimilarCompaniesProcessed(true); // Mark as processed since it's new
                    
                    try {
                        Company savedCompany = companyRepository.save(newCompany);
                        newCompanyNames.add(cleanedSimilarCompanyName);
                        existingCompanyIds.add(savedCompany.getId());
                        log.info("Created new company: {} (ID: {})", cleanedSimilarCompanyName, savedCompany.getId());
                    } catch (Exception e) {
                        log.error("Failed to create new company: {}", cleanedSimilarCompanyName, e);
                        // Continue with other companies
                    }
                }
            }
            
            // Convert to JSON before storing in database - use cleaned names
            ObjectMapper objectMapper = new ObjectMapper();
            String similarCompaniesByNameJson = objectMapper.writeValueAsString(existingCompanyNames);
            String similarCompaniesByIdJson = objectMapper.writeValueAsString(existingCompanyIds);
            
            // Update the original company with similar companies as JSON
            company.setSimilarCompaniesByName(similarCompaniesByNameJson);
            company.setSimilarCompaniesById(similarCompaniesByIdJson);
            company.setSimilarCompaniesProcessed(true);
            
            companyRepository.save(company);
            
            result.put("success", true);
            result.put("similarCompaniesFound", existingCompanyNames.size() + newCompanyNames.size());
            result.put("newCompaniesCreated", newCompanyNames.size());
            result.put("existingCompaniesFound", existingCompanyNames.size());
            result.put("similarCompanyNames", existingCompanyNames);
            result.put("newCompanyNames", newCompanyNames);
            result.put("existingCompanyNames", existingCompanyNames);
            result.put("similarCompanyIds", existingCompanyIds);
            result.put("industry", industryName);
            
            log.info("Successfully processed similar companies for: {} (ID: {}). Found: {}, Created: {}", 
                company.getName(), company.getId(), existingCompanyNames.size() + newCompanyNames.size(), newCompanyNames.size());
            
        } catch (Exception e) {
            log.error("Error generating similar companies for company: {} (ID: {})", 
                company.getName(), company.getId(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("similarCompaniesFound", 0);
            result.put("newCompaniesCreated", 0);
        }
        
        return result;
    }
} 