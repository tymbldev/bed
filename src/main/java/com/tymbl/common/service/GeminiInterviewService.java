package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiInterviewService {
    @Value("${gemini.api.key:AIzaSyBseir8xAFoLEFT45w1gT3rn5VbdVwjJNM}")
    private String apiKey;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Qualifier("aiServiceRestTemplate")
    private final RestTemplate restTemplate;

    public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
        try {
            log.info("[Gemini] Generating topics for designation: {}", designationName);
            String prompt = buildTopicGenerationPrompt(designationName);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> topics = parseTopicsResponse(response.getBody());
                log.info("[Gemini] Parsed {} topics for designation: {}", topics.size(), designationName);
                return topics;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating topics for designation: {}", designationName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("[Gemini] Generating general interview questions for designation: {}, topic: {}, difficulty: {}", designation, topicName, difficultyLevel);
            String prompt = buildGeneralQuestionPrompt(designation, topicName, difficultyLevel, numQuestions);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> questions = parseQuestionsResponse(response.getBody());
                log.info("[Gemini] Parsed {} general interview questions for designation: {}, topic: {}", questions.size(), designation, topicName);
                return questions;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating general interview questions for designation: {}, topic: {}", designation, topicName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("[Gemini] Generating company-specific questions for company: {}, designation: {}, topic: {}", companyName, designation, topicName);
            String prompt = buildCompanySpecificQuestionPrompt(companyName, designation, topicName, difficultyLevel, numQuestions);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> questions = parseQuestionsResponse(response.getBody());
                log.info("[Gemini] Parsed {} company-specific questions for company: {}, designation: {}, topic: {}", questions.size(), companyName, designation, topicName);
                return questions;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating company-specific questions for company: {}, designation: {}, topic: {}", companyName, designation, topicName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateDesignationsForDepartment(String departmentName) {
        try {
            log.info("[Gemini] Generating designations for department: {}", departmentName);
            String prompt = buildDesignationGenerationPrompt(departmentName);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> designations = parseDesignationsResponse(response.getBody());
                log.info("[Gemini] Parsed {} designations for department: {}", designations.size(), departmentName);
                return designations;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating designations for department: {}", departmentName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateComprehensiveInterviewQuestions(String skillName, int numQuestions) {
        try {
            log.info("[Gemini] Generating comprehensive interview questions for skill: {}", skillName);
            String prompt = buildComprehensiveQuestionPrompt(skillName, numQuestions);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> questions = parseComprehensiveQuestionsResponse(response.getBody());
                log.info("[Gemini] Parsed {} comprehensive interview questions for skill: {}", questions.size(), skillName);
                return questions;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating comprehensive questions for skill: {}", skillName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateDetailedQuestionContent(String skillName, String questionSummary) {
        try {
            log.info("[Gemini] Generating detailed content for question. Skill: {}", skillName);
            String prompt = buildDetailedContentPrompt(skillName, questionSummary);
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            
            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> content = parseDetailedContentResponse(response.getBody());
                log.info("[Gemini] Parsed {} detailed content items for skill: {}", content.size(), skillName);
                return content;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating detailed content for skill: {}", skillName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateComprehensiveTechSkills() {
        try {
            log.info("[Gemini] Generating comprehensive list of tech skills using Gemini");
            String prompt = buildComprehensiveTechSkillsPrompt();
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);

            if (response.getStatusCodeValue() == 200) {
                List<Map<String, Object>> skills = parseSkillsResponse(response.getBody());
                log.info("[Gemini] Parsed {} tech skills", skills.size());
                return skills;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating tech skills", e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateTopicsForSkill(String skillName) {
        try {
            log.info("[Gemini] Generating topics for skill: {}", skillName);
            String prompt = "Generate a list of 8-12 important topics for the technical skill '" + skillName + "'. " +
                    "Exclude any interpersonal or soft skills such as Leadership, Communication, Teamwork, Collaboration, Problem Solving, Critical Thinking, Creativity, Adaptability, Work Ethic, Time Management, Conflict Resolution, Empathy, Emotional Intelligence, Negotiation, Decision Making, Motivation, Responsibility, Interpersonal Skills, Presentation Skills, Active Listening, etc. " +
                    "Focus only on technical topics relevant to the skill.\n" +
                    "\n" +
                    "INSTRUCTIONS:\n" +
                    "- Do NOT include any soft skills, people skills, or behavioral skills.\n" +
                    "- Only include topics that are technical, conceptual, or practical aspects of the skill.\n" +
                    "- Example of what NOT to include: Leadership, Communication, Teamwork, Empathy, Time Management, etc.\n" +
                    "- Example of what TO include for Java: Collections, Multithreading, Java 8 Features, Exception Handling, Streams API, JVM Internals, etc.\n" +
                    "\n" +
                    "Return as a JSON array of objects with 'topic' and 'description' fields. Example: [{\"topic\":\"Collections\",\"description\":\"Data structures in Java\"}, ...]";
            log.info("[Gemini] Prompt (first 200 chars): {}", prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);

            if (response.getStatusCodeValue() == 200) {
                JsonNode responseNode = objectMapper.readTree(response.getBody());
                JsonNode candidates = responseNode.get("candidates");
                if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                    JsonNode content = candidates.get(0).get("content");
                    if (content != null) {
                        JsonNode parts = content.get("parts");
                        if (parts != null && parts.isArray() && parts.size() > 0) {
                            String generatedText = parts.get(0).get("text").asText();
                            String jsonText = extractJsonFromText(generatedText);
                            JsonNode topicsData = objectMapper.readTree(jsonText);
                            List<Map<String, Object>> topics = new ArrayList<>();
                            if (topicsData.isArray()) {
                                for (JsonNode topicNode : topicsData) {
                                    String topic = getStringValue(topicNode, "topic");
                                    if (topic != null && !topic.trim().isEmpty()) {
                                        Map<String, Object> topicMap = new HashMap<>();
                                        topicMap.put("topic", topic);
                                        topicMap.put("description", getStringValue(topicNode, "description"));
                                        topics.add(topicMap);
                                    }
                                }
                            }
                            log.info("[Gemini] Parsed {} topics for skill: {}", topics.size(), skillName);
                            return topics;
                        }
                    }
                }
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating topics for skill: {}", skillName, e);
        }
        return new ArrayList<>();
    }

    public List<Map<String, Object>> generateQuestionsForSkillAndTopic(String skillName, String topicName, int numQuestions) {
        try {
            log.info("[Gemini] Generating interview questions for skill: {} and topic: {} (custom separator mode)", skillName, topicName);
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate ").append(numQuestions).append(" comprehensive, detailed interview questions for the skill '")
                  .append(skillName).append("' on the topic '").append(topicName).append("'.\n\n");
            prompt.append("REQUIREMENTS:\n");
            prompt.append("1. Each question should be well-elaborated and cover different aspects of the topic.\n");
            prompt.append("2. Provide a detailed answer for each question in HTML format (use <h2>, <h3>, <p>, <ul>, <li>, <code>, <pre> tags as needed).\n");
            prompt.append("3. Include code examples where applicable.\n");
            prompt.append("4. Vary difficulty levels (BEGINNER, INTERMEDIATE, ADVANCED).\n");
            prompt.append("5. Include different question types (THEORETICAL, PRACTICAL, PROBLEM_SOLVING, SYSTEM_DESIGN).\n");
            prompt.append("6. Each question should be engaging and suitable for technical interviews.\n\n");
            prompt.append("OUTPUT FORMAT (one question per line, fields separated by |||||):\n");
            prompt.append("question_text|||||answer_html|||||difficulty_level|||||question_type|||||tags\n");
            prompt.append("Example:\n");
            prompt.append("What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,oop\n");
            prompt.append("Do NOT return JSON or markdown. Only output the questions in the specified format, one per line.\n");
            String promptStr = prompt.toString();
            log.info("[Gemini] Prompt (first 200 chars): {}", promptStr.length() > 200 ? promptStr.substring(0, 200) + "..." : promptStr);
            Map<String, Object> requestBody = buildRequestBody(promptStr);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                GEMINI_API_URL + "?key=" + apiKey,
                HttpMethod.POST,
                request,
                String.class
            );
            log.info("[Gemini] API response status: {}", response.getStatusCodeValue());
            log.info("[Gemini] API response body length: {}", response.getBody() != null ? response.getBody().length() : 0);
            if (response.getStatusCodeValue() == 200) {
                String responseBody = response.getBody();
                List<Map<String, Object>> questions = parseQuestionsResponseCustomSeparator(responseBody);
                log.info("[Gemini] Parsed {} interview questions for skill: {} and topic: {} (custom separator mode)", questions.size(), skillName, topicName);
                return questions;
            } else {
                log.error("[Gemini] API error: {} - {}", response.getStatusCodeValue(), response.getBody());
            }
        } catch (Exception e) {
            log.error("[Gemini] Error generating questions for skill and topic: {} / {} (custom separator mode)", skillName, topicName, e);
        }
        return new ArrayList<>();
    }

    // Helper methods
    private String buildTopicGenerationPrompt(String designationName) {
        return "Generate top 10 interview topics for the designation: " + designationName + 
               ". Return as JSON array of objects with 'topicName' and 'description' fields.";
    }

    private String buildGeneralQuestionPrompt(String designation, String topicName, String difficultyLevel, int numQuestions) {
        return "Generate " + numQuestions + " " + difficultyLevel + " level interview questions for " + designation +
               " on topic: " + topicName + ". Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion. Do NOT return JSON or markdown. Only output the questions in the specified format. Example: What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||<div>Polymorphism is ...</div>|||||INTERMEDIATE|||||THEORETICAL|||||java,oop";
    }

    private String buildCompanySpecificQuestionPrompt(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        return "Generate " + numQuestions + " " + difficultyLevel + " level company-specific interview questions for " +
               companyName + " for " + designation + " role on topic: " + topicName +
               ". Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion. Do NOT return JSON or markdown. Only output the questions in the specified format. Example: What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||<div>Polymorphism is ...</div>|||||INTERMEDIATE|||||THEORETICAL|||||java,oop";
    }

    private String buildDesignationGenerationPrompt(String departmentName) {
        return "Generate common job designations for the department: " + departmentName + 
               ". Return as JSON array of objects with 'name' and 'description' fields.";
    }

    private String buildComprehensiveQuestionPrompt(String skillName, int numQuestions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate ").append(numQuestions).append(" comprehensive interview questions for the skill: ").append(skillName).append("\n\n");
        prompt.append("REQUIREMENTS:\n");
        prompt.append("1. Questions should be detailed and cover different aspects of ").append(skillName).append("\n");
        prompt.append("2. Include theoretical, practical, and problem-solving questions\n");
        prompt.append("3. Vary difficulty levels (BEGINNER, INTERMEDIATE, ADVANCED)\n");
        prompt.append("4. Include different question types (THEORETICAL, PRACTICAL, BEHAVIORAL, PROBLEM_SOLVING, SYSTEM_DESIGN)\n");
        prompt.append("5. For DSA questions, include code examples\n");
        prompt.append("6. Each question should be engaging and detailed\n\n");
        prompt.append("OUTPUT FORMAT: Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion.\n");
        prompt.append("Example: What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||<div>Polymorphism is ...</div>|||||INTERMEDIATE|||||THEORETICAL|||||java,oop\n");
        prompt.append("Do NOT return JSON or markdown. Only output the questions in the specified format.\n");
        prompt.append("Make the questions comprehensive, engaging, and suitable for technical interviews. ");
        prompt.append("Focus on real-world scenarios and practical applications of ").append(skillName).append(".");
        return prompt.toString();
    }

    private String buildDetailedContentPrompt(String skillName, String questionSummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate detailed, comprehensive content for this interview question:\n\n");
        prompt.append("SKILL: ").append(skillName).append("\n");
        prompt.append("QUESTION: ").append(questionSummary).append("\n\n");
        
        prompt.append("REQUIREMENTS:\n");
        prompt.append("1. Provide a detailed, step-by-step answer in HTML format\n");
        prompt.append("2. Include code examples where applicable (especially for DSA)\n");
        prompt.append("3. Use proper HTML formatting with <h2>, <h3>, <p>, <ul>, <li>, <code>, <pre> tags\n");
        prompt.append("4. Include practical examples and real-world scenarios\n");
        prompt.append("5. Explain concepts thoroughly with examples\n");
        prompt.append("6. Make it engaging and easy to understand\n");
        prompt.append("7. Include best practices and common pitfalls\n\n");
        
        prompt.append("OUTPUT FORMAT (JSON):\n");
        prompt.append("{\n");
        prompt.append("  \"detailed_answer\": \"<h2>Detailed Answer</h2><p>Comprehensive explanation in HTML...</p>\",\n");
        prompt.append("  \"code_examples\": \"<h3>Code Examples</h3><pre><code>// Code examples here</code></pre>\",\n");
        prompt.append("  \"html_content\": \"<div>Complete HTML formatted answer</div>\",\n");
        prompt.append("  \"tags\": \"updated,tags,based,on,content\"\n");
        prompt.append("}\n\n");
        
        prompt.append("Make the content comprehensive, well-structured, and engaging for users.");
        
        return prompt.toString();
    }

    private String buildComprehensiveTechSkillsPrompt() {
        return "Generate a comprehensive, up-to-date, and diverse list of technology skills relevant for the tech industry. " +
               "Include programming languages, frameworks, libraries, cloud platforms, devops tools, AI/ML, data engineering, security, frontend, backend, mobile, testing, and emerging technologies. " +
               "For each skill, provide: 'name', 'category' (e.g. Programming Language, Framework, Cloud, DevOps, AI/ML, Data, Security, Frontend, Backend, Mobile, Testing, Emerging Tech), and a one-line 'description'. " +
               "Output as a JSON array of objects. Example: [{\"name\": \"Python\", \"category\": \"Programming Language\", \"description\": \"A versatile high-level programming language used for web, data, and AI.\"}, ...]. " +
               "Include at least 50 skills, covering both popular and niche areas. Use correct spelling and avoid duplicates.";
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contents = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Object[]{part});
        contents.put("contents", new Object[]{content});
        return contents;
    }

    private List<Map<String, Object>> parseTopicsResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        String jsonText = extractJsonFromText(generatedText);
                        JsonNode topicsData = objectMapper.readTree(jsonText);
                        return mapJsonToTopicsList(topicsData);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing topics response", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseQuestionsResponse(String responseBody) {
        return parseQuestionsResponseCustomSeparator(responseBody);
    }

    private List<Map<String, Object>> parseComprehensiveQuestionsResponse(String responseBody) {
        return parseQuestionsResponseCustomSeparator(responseBody);
    }

    private List<Map<String, Object>> parseDesignationsResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        String jsonText = extractJsonFromText(generatedText);
                        JsonNode designationsData = objectMapper.readTree(jsonText);
                        return mapJsonToDesignationsList(designationsData);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing designations response", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseDetailedContentResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("Raw generated text: {}", generatedText);
                        
                        String jsonText = extractJsonFromText(generatedText);
                        log.info("Extracted JSON text: {}", jsonText);
                        
                        try {
                            JsonNode contentData = objectMapper.readTree(jsonText);
                            return mapJsonToDetailedContentList(contentData);
                        } catch (Exception jsonParseException) {
                            log.warn("Failed to parse extracted JSON, trying to create fallback content", jsonParseException);
                            
                            // Create fallback content from the raw text
                            Map<String, Object> fallbackContent = new HashMap<>();
                            fallbackContent.put("detailed_answer", generatedText);
                            fallbackContent.put("code_examples", "");
                            fallbackContent.put("html_content", generatedText);
                            fallbackContent.put("tags", "");
                            
                            List<Map<String, Object>> fallbackList = new ArrayList<>();
                            fallbackList.add(fallbackContent);
                            return fallbackList;
                        }
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing detailed content response", e);
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> parseSkillsResponse(String responseBody) {
        try {
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        String jsonText = extractJsonFromText(generatedText);
                        JsonNode skillsData = objectMapper.readTree(jsonText);
                        List<Map<String, Object>> skills = new ArrayList<>();
                        if (skillsData.isArray()) {
                            for (JsonNode skillNode : skillsData) {
                                Map<String, Object> skill = new HashMap<>();
                                skill.put("name", getStringValue(skillNode, "name"));
                                skill.put("category", getStringValue(skillNode, "category"));
                                skill.put("description", getStringValue(skillNode, "description"));
                                skills.add(skill);
                            }
                        }
                        return skills;
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing skills response", e);
            return new ArrayList<>();
        }
    }

    private String extractJsonFromText(String text) {
        try {
            log.info("Text came to extractJsonFromText is {}", text);
            // Remove markdown code block markers and trim
            text = text.replaceAll("(?s)```json.*?```", "").replaceAll("(?s)```.*?```", "").trim();

            // Prefer extracting a JSON array if present
            int arrayStart = text.indexOf('[');
            if (arrayStart >= 0) {
                int arrayEnd = findMatchingBracket(text, arrayStart, '[', ']');
                if (arrayEnd > arrayStart) {
                    String jsonArray = text.substring(arrayStart, arrayEnd + 1).trim();
                    log.info("[extractJsonFromText] Extracted JSON array: {}", jsonArray.length() > 300 ? jsonArray.substring(0, 300) + "..." : jsonArray);
                    return jsonArray;
                }
            }

            // If no array, try to extract a JSON object
            int objStart = text.indexOf('{');
            if (objStart >= 0) {
                int objEnd = findMatchingBracket(text, objStart, '{', '}');
                if (objEnd > objStart) {
                    String jsonObject = text.substring(objStart, objEnd + 1).trim();
                    log.info("[extractJsonFromText] Extracted JSON object: {}", jsonObject.length() > 300 ? jsonObject.substring(0, 300) + "..." : jsonObject);
                    return jsonObject;
                }
            }

            log.info("[extractJsonFromText] No valid JSON array or object found in text");
            return null;
        } catch (Exception e) {
            log.error("Error extracting JSON from text: {}", e.getMessage(), e);
            return null;
        }
    }
    // NOTE: We use bracket counting instead of regex for JSON extraction because regex cannot handle nested or multiline JSON reliably.
    
    private int findMatchingBracket(String text, int startIndex, char openBracket, char closeBracket) {
        int count = 0;
        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == openBracket) {
                count++;
            } else if (c == closeBracket) {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private List<Map<String, Object>> mapJsonToTopicsList(JsonNode topicsData) {
        List<Map<String, Object>> topics = new ArrayList<>();
        if (topicsData.isArray()) {
            for (JsonNode topicNode : topicsData) {
                Map<String, Object> topic = new HashMap<>();
                topic.put("topicName", getStringValue(topicNode, "topicName"));
                topic.put("description", getStringValue(topicNode, "description"));
                topics.add(topic);
            }
        }
        return topics;
    }

    private List<Map<String, Object>> mapJsonToQuestionsList(JsonNode questionsData) {
        List<Map<String, Object>> questions = new ArrayList<>();
        if (questionsData.isArray()) {
            for (JsonNode questionNode : questionsData) {
                Map<String, Object> question = new HashMap<>();
                question.put("question", getStringValue(questionNode, "question"));
                question.put("answer", getStringValue(questionNode, "answer"));
                questions.add(question);
            }
        } else if (questionsData.isObject()) {
            Map<String, Object> question = new HashMap<>();
            question.put("question", getStringValue(questionsData, "question"));
            question.put("answer", getStringValue(questionsData, "answer"));
            questions.add(question);
        }
        return questions;
    }

    private List<Map<String, Object>> mapJsonToDesignationsList(JsonNode designationsData) {
        List<Map<String, Object>> designations = new ArrayList<>();
        if (designationsData.isArray()) {
            for (JsonNode designationNode : designationsData) {
                Map<String, Object> designation = new HashMap<>();
                designation.put("name", getStringValue(designationNode, "name"));
                designation.put("description", getStringValue(designationNode, "description"));
                designations.add(designation);
            }
        }
        return designations;
    }

    private List<Map<String, Object>> mapJsonToDetailedContent(JsonNode detailedContent) {
        List<Map<String, Object>> content = new ArrayList<>();
        if (detailedContent.isArray()) {
            for (JsonNode itemNode : detailedContent) {
                Map<String, Object> item = new HashMap<>();
                item.put("explanation", getStringValue(itemNode, "explanation"));
                item.put("example", getStringValue(itemNode, "example"));
                content.add(item);
            }
        }
        return content;
    }

    private List<Map<String, Object>> mapJsonToComprehensiveQuestionsList(JsonNode questionsData) {
        List<Map<String, Object>> questions = new ArrayList<>();
        if (questionsData.isArray()) {
            for (JsonNode questionNode : questionsData) {
                Map<String, Object> question = new HashMap<>();
                question.put("question", getStringValue(questionNode, "question"));
                question.put("difficulty_level", getStringValue(questionNode, "difficulty_level"));
                question.put("question_type", getStringValue(questionNode, "question_type"));
                question.put("tags", getStringValue(questionNode, "tags"));
                question.put("summary_answer", getStringValue(questionNode, "summary_answer"));
                
                // Parse applicable designations
                List<String> designations = new ArrayList<>();
                JsonNode designationsArray = questionNode.get("applicable_designations");
                if (designationsArray != null && designationsArray.isArray()) {
                    for (JsonNode designationNode : designationsArray) {
                        if (designationNode.isTextual()) {
                            designations.add(designationNode.asText());
                        }
                    }
                }
                question.put("applicable_designations", designations);
                
                questions.add(question);
            }
        }
        return questions;
    }

    private List<Map<String, Object>> mapJsonToDetailedContentList(JsonNode contentData) {
        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> contentMap = new HashMap<>();
        
        try {
            // Handle both object and array responses
            if (contentData.isObject()) {
                contentMap.put("detailed_answer", getStringValue(contentData, "detailed_answer"));
                contentMap.put("code_examples", getStringValue(contentData, "code_examples"));
                contentMap.put("html_content", getStringValue(contentData, "html_content"));
                contentMap.put("tags", getStringValue(contentData, "tags"));
            } else if (contentData.isArray() && contentData.size() > 0) {
                // If it's an array, take the first element
                JsonNode firstElement = contentData.get(0);
                contentMap.put("detailed_answer", getStringValue(firstElement, "detailed_answer"));
                contentMap.put("code_examples", getStringValue(firstElement, "code_examples"));
                contentMap.put("html_content", getStringValue(firstElement, "html_content"));
                contentMap.put("tags", getStringValue(firstElement, "tags"));
            } else {
                // Fallback: treat the entire content as detailed_answer
                contentMap.put("detailed_answer", contentData.asText(""));
                contentMap.put("code_examples", "");
                contentMap.put("html_content", contentData.asText(""));
                contentMap.put("tags", "");
            }
        } catch (Exception e) {
            log.warn("Error mapping detailed content, using fallback", e);
            contentMap.put("detailed_answer", contentData.asText(""));
            contentMap.put("code_examples", "");
            contentMap.put("html_content", contentData.asText(""));
            contentMap.put("tags", "");
        }
        
        content.add(contentMap);
        return content;
    }

    private String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            // Use asText() to preserve HTML content exactly as received
            // This ensures HTML tags and special characters are not escaped or modified
            return node.get(fieldName).asText("");
        }
        return "";
    }

    // New parser for custom separator format
    private List<Map<String, Object>> parseQuestionsResponseCustomSeparator(String responseBody) {
        List<Map<String, Object>> questions = new ArrayList<>();
        try {
            log.info("[Gemini] Parsing questions response (custom separator mode, tymblQuestion). Raw response length: {}", responseBody != null ? responseBody.length() : 0);
            JsonNode responseNode = objectMapper.readTree(responseBody);
            JsonNode candidates = responseNode.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        String generatedText = parts.get(0).get("text").asText();
                        log.info("[Gemini] Extracted generated text (first 200 chars): {}", generatedText != null && generatedText.length() > 200 ? generatedText.substring(0, 200) + "..." : generatedText);
                        String[] blocks = generatedText.split("tymblQuestion");
                        for (String block : blocks) {
                            String trimmed = block.trim();
                            if (trimmed.isEmpty()) continue;
                            String[] fields = trimmed.split("\\|\\|\\|\\|");
                            if (fields.length < 5) {
                                log.warn("[Gemini] Malformed question block (expected 5 fields): {}", trimmed);
                                continue;
                            }
                            Map<String, Object> q = new HashMap<>();
                            q.put("question", fields[0].trim());
                            q.put("answer", fields[1].trim());
                            q.put("difficulty_level", fields[2].trim());
                            q.put("question_type", fields[3].trim());
                            q.put("tags", fields[4].trim());
                            questions.add(q);
                        }
                        log.info("[Gemini] Parsed {} questions from custom separator response (tymblQuestion)", questions.size());
                        return questions;
                    }
                }
            }
            log.warn("[Gemini] No valid candidates/content/parts found in custom separator response (tymblQuestion)");
            return questions;
        } catch (Exception e) {
            log.error("[Gemini] Error parsing questions response (custom separator mode, tymblQuestion)", e);
            return questions;
        }
    }
} 