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
            log.info("Generating topics for designation: {}", designationName);
            String prompt = buildTopicGenerationPrompt(designationName);
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
            
            if (response.getStatusCodeValue() == 200) {
                return parseTopicsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating topics for designation: {}", designationName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("Generating general interview questions for designation: {}, topic: {}, difficulty: {}", designation, topicName, difficultyLevel);
            String prompt = buildGeneralQuestionPrompt(designation, topicName, difficultyLevel, numQuestions);
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
            
            if (response.getStatusCodeValue() == 200) {
                return parseQuestionsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating general interview questions", e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        try {
            log.info("Generating company-specific questions for company: {}, designation: {}, topic: {}", companyName, designation, topicName);
            String prompt = buildCompanySpecificQuestionPrompt(companyName, designation, topicName, difficultyLevel, numQuestions);
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
            
            if (response.getStatusCodeValue() == 200) {
                return parseQuestionsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating company-specific questions", e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateDesignationsForDepartment(String departmentName) {
        try {
            log.info("Generating designations for department: {}", departmentName);
            String prompt = buildDesignationGenerationPrompt(departmentName);
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
            
            if (response.getStatusCodeValue() == 200) {
                return parseDesignationsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating designations for department: {}", departmentName, e);
            return new ArrayList<>();
        }
    }

    // Helper methods
    private String buildTopicGenerationPrompt(String designationName) {
        return "Generate top 10 interview topics for the designation: " + designationName + 
               ". Return as JSON array of objects with 'topicName' and 'description' fields.";
    }

    private String buildGeneralQuestionPrompt(String designation, String topicName, String difficultyLevel, int numQuestions) {
        return "Generate " + numQuestions + " " + difficultyLevel + " level interview questions for " + designation + 
               " on topic: " + topicName + ". Return as JSON array of objects with 'question' and 'answer' fields.";
    }

    private String buildCompanySpecificQuestionPrompt(String companyName, String designation, String topicName, String difficultyLevel, int numQuestions) {
        return "Generate " + numQuestions + " " + difficultyLevel + " level company-specific interview questions for " + 
               companyName + " for " + designation + " role on topic: " + topicName + 
               ". Return as JSON array of objects with 'question' and 'answer' fields.";
    }

    private String buildDesignationGenerationPrompt(String departmentName) {
        return "Generate common job designations for the department: " + departmentName + 
               ". Return as JSON array of objects with 'name' and 'description' fields.";
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
                        JsonNode questionsData = objectMapper.readTree(jsonText);
                        return mapJsonToQuestionsList(questionsData);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing questions response", e);
            return new ArrayList<>();
        }
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

    private String extractJsonFromText(String text) {
        text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
        text = text.trim();
        int jsonStart = text.indexOf('[');
        if (jsonStart > 0) {
            text = text.substring(jsonStart);
        }
        int jsonEnd = text.lastIndexOf(']');
        if (jsonEnd >= 0) {
            text = text.substring(0, jsonEnd + 1);
        }
        return text;
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

    private String getStringValue(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText("");
        }
        return "";
    }
} 