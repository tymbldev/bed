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

    public List<Map<String, Object>> generateComprehensiveInterviewQuestions(String skillName, int numQuestions) {
        try {
            log.info("Generating comprehensive interview questions for skill: {}", skillName);
            String prompt = buildComprehensiveQuestionPrompt(skillName, numQuestions);
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
                return parseComprehensiveQuestionsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating comprehensive questions for skill: {}", skillName, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateDetailedQuestionContent(String skillName, String questionSummary) {
        try {
            log.info("Generating detailed content for question: {}", questionSummary);
            String prompt = buildDetailedContentPrompt(skillName, questionSummary);
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
                return parseDetailedContentResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating detailed content for question: {}", questionSummary, e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> generateComprehensiveTechSkills() {
        try {
            log.info("Generating comprehensive list of tech skills using Gemini");
            String prompt = buildComprehensiveTechSkillsPrompt();
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
                return parseSkillsResponse(response.getBody());
            } else {
                log.error("Gemini API error: {} - {}", response.getStatusCodeValue(), response.getBody());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error generating tech skills", e);
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
        
        prompt.append("OUTPUT FORMAT (JSON array):\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"question\": \"Detailed question text\",\n");
        prompt.append("    \"difficulty_level\": \"BEGINNER|INTERMEDIATE|ADVANCED\",\n");
        prompt.append("    \"question_type\": \"THEORETICAL|PRACTICAL|BEHAVIORAL|PROBLEM_SOLVING|SYSTEM_DESIGN\",\n");
        prompt.append("    \"tags\": \"tag1,tag2,tag3\",\n");
        prompt.append("    \"summary_answer\": \"Brief summary answer\",\n");
        prompt.append("    \"applicable_designations\": [\"Software Engineer\", \"Data Scientist\", \"DevOps Engineer\"]\n");
        prompt.append("  }\n");
        prompt.append("]\n\n");
        
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

    private List<Map<String, Object>> parseComprehensiveQuestionsResponse(String responseBody) {
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
                        return mapJsonToComprehensiveQuestionsList(questionsData);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error parsing comprehensive questions response", e);
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
                        log.debug("Raw generated text: {}", generatedText);
                        
                        String jsonText = extractJsonFromText(generatedText);
                        log.debug("Extracted JSON text: {}", jsonText);
                        
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
            // First, try to clean up the text - only remove markdown code blocks
            text = text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            text = text.trim();
            
            // Look for JSON array or object patterns
            int jsonStart = -1;
            int jsonEnd = -1;
            
            // Try to find JSON array
            jsonStart = text.indexOf('[');
            if (jsonStart >= 0) {
                jsonEnd = findMatchingBracket(text, jsonStart, '[', ']');
            }
            
            // If no array found, try to find JSON object
            if (jsonStart == -1 || jsonEnd == -1) {
                jsonStart = text.indexOf('{');
                if (jsonStart >= 0) {
                    jsonEnd = findMatchingBracket(text, jsonStart, '{', '}');
                }
            }
            
            // If we found valid JSON boundaries
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String jsonText = text.substring(jsonStart, jsonEnd + 1);
                
                // Validate that it's actually JSON by trying to parse it
                try {
                    objectMapper.readTree(jsonText);
                    return jsonText;
                } catch (Exception e) {
                    log.warn("Extracted text is not valid JSON, trying alternative extraction");
                }
            }
            
            // Enhanced fallback: try to extract JSON using more sophisticated patterns
            // This handles cases where HTML content might interfere with simple regex
            String[] jsonPatterns = {
                // Pattern for complete JSON objects with nested structures
                "\\{[^{}]*(?:\\{[^{}]*\\}[^{}]*)*\\}",
                // Pattern for JSON arrays
                "\\[[^\\[\\]]*(?:\\[[^\\[\\]]*\\][^\\[\\]]*)*\\]",
                // Pattern for simple JSON objects (fallback)
                "\\{[^}]*\\}"
            };
            
            for (String pattern : jsonPatterns) {
                java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher matcher = regexPattern.matcher(text);
                
                while (matcher.find()) {
                    String candidate = matcher.group();
                    try {
                        // Try to parse as JSON
                        objectMapper.readTree(candidate);
                        return candidate;
                    } catch (Exception e) {
                        // Continue to next match
                    }
                }
            }
            
            // If all else fails, return the original text and let the caller handle it
            log.warn("Could not extract valid JSON from text, returning original");
            return text;
            
        } catch (Exception e) {
            log.error("Error extracting JSON from text", e);
            return text;
        }
    }
    
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
} 