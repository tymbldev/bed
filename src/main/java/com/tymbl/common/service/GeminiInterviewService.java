package com.tymbl.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiInterviewService {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AIRestService aiRestService;

  public List<Map<String, Object>> generateTopicsForDesignation(String designationName) {
    try {
      log.info("[Gemini] Generating topics for designation: {}", designationName);
      String prompt = buildTopicGenerationPrompt(designationName);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Topic Generation for " + designationName);
      List<Map<String, Object>> topics = parseTopicsResponse(response.getBody());
      log.info("[Gemini] Parsed {} topics for designation: {}", topics.size(), designationName);
      return topics;
    } catch (Exception e) {
      log.error("[Gemini] Error generating topics for designation: {}", designationName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateGeneralInterviewQuestions(String designation,
      String topicName, String difficultyLevel, int numQuestions) {
    try {
      log.info(
          "[Gemini] Generating general interview questions for designation: {}, topic: {}, difficulty: {}",
          designation, topicName, difficultyLevel);
      String prompt = buildGeneralQuestionPrompt(designation, topicName, difficultyLevel,
          numQuestions);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "General Questions for " + designation + " - " + topicName);
      List<Map<String, Object>> questions = parseQuestionsResponse(response.getBody());
      log.info("[Gemini] Parsed {} general interview questions for designation: {}, topic: {}",
          questions.size(), designation, topicName);
      return questions;
    } catch (Exception e) {
      log.error(
          "[Gemini] Error generating general interview questions for designation: {}, topic: {}",
          designation, topicName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateCompanySpecificQuestions(String companyName,
      String designation, String topicName, String difficultyLevel, int numQuestions) {
    try {
      log.info(
          "[Gemini] Generating company-specific questions for company: {}, designation: {}, topic: {}",
          companyName, designation, topicName);
      String prompt = buildCompanySpecificQuestionPrompt(companyName, designation, topicName,
          difficultyLevel, numQuestions);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Company Specific Questions for " + companyName + " - " + designation);
      List<Map<String, Object>> questions = parseQuestionsResponse(response.getBody());
      log.info(
          "[Gemini] Parsed {} company-specific questions for company: {}, designation: {}, topic: {}",
          questions.size(), companyName, designation, topicName);
      return questions;
    } catch (Exception e) {
      log.error(
          "[Gemini] Error generating company-specific questions for company: {}, designation: {}, topic: {}",
          companyName, designation, topicName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateDesignationsForDepartment(String departmentName) {
    try {
      log.info("[Gemini] Generating designations for department: {}", departmentName);
      String prompt = buildDesignationGenerationPrompt(departmentName);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Designations for Department - " + departmentName);
      List<Map<String, Object>> designations = parseDesignationsResponse(response.getBody());
      log.info("[Gemini] Parsed {} designations for department: {}", designations.size(),
          departmentName);
      return designations;
    } catch (Exception e) {
      log.error("[Gemini] Error generating designations for department: {}", departmentName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateComprehensiveInterviewQuestions(String skillName,
      int numQuestions) {
    try {
      log.info("[Gemini] Generating comprehensive interview questions for skill: {}", skillName);
      String prompt = buildComprehensiveQuestionPrompt(skillName, numQuestions);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Comprehensive Questions for " + skillName);
      List<Map<String, Object>> questions = parseComprehensiveQuestionsResponse(response.getBody());
      log.info("[Gemini] Parsed {} comprehensive interview questions for skill: {}",
          questions.size(), skillName);
      return questions;
    } catch (Exception e) {
      log.error("[Gemini] Error generating comprehensive questions for skill: {}", skillName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateDetailedQuestionContent(String skillName,
      String questionSummary) {
    try {
      log.info("[Gemini] Generating detailed content for question. Skill: {}", skillName);
      String prompt = buildDetailedContentPrompt(skillName, questionSummary);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Detailed Content for " + skillName);
      List<Map<String, Object>> content = parseDetailedContentResponse(response.getBody());
      log.info("[Gemini] Parsed {} detailed content items for skill: {}", content.size(),
          skillName);
      return content;
    } catch (Exception e) {
      log.error("[Gemini] Error generating detailed content for skill: {}", skillName, e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateComprehensiveTechSkills() {
    try {
      log.info("[Gemini] Generating comprehensive list of tech skills using Gemini");
      String prompt = buildComprehensiveTechSkillsPrompt();
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Comprehensive Tech Skills");
      List<Map<String, Object>> skills = parseSkillsResponse(response.getBody());
      log.info("[Gemini] Parsed {} tech skills", skills.size());
      return skills;
    } catch (Exception e) {
      log.error("[Gemini] Error generating tech skills", e);
      return new ArrayList<>();
    }
  }

  public List<Map<String, Object>> generateTopicsForSkill(String skillName) {
    try {
      log.info("[Gemini] Generating topics for skill: {}", skillName);
      String prompt =
          "Generate a list of 8-12 important topics for the technical skill '" + skillName + "'. " +
              "Exclude any interpersonal or soft skills such as Leadership, Communication, Teamwork, Collaboration, Problem Solving, Critical Thinking, Creativity, Adaptability, Work Ethic, Time Management, Conflict Resolution, Empathy, Emotional Intelligence, Negotiation, Decision Making, Motivation, Responsibility, Interpersonal Skills, Presentation Skills, Active Listening, etc. "
              +
              "Focus only on technical topics relevant to the skill.\n" +
              "\n" +
              "INSTRUCTIONS:\n" +
              "- Do NOT include any soft skills, people skills, or behavioral skills.\n" +
              "- Only include topics that are technical, conceptual, or practical aspects of the skill.\n"
              +
              "- Example of what NOT to include: Leadership, Communication, Teamwork, Empathy, Time Management, etc.\n"
              +
              "- Example of what TO include for Java: Collections, Multithreading, Java 8 Features, Exception Handling, Streams API, JVM Internals, etc.\n"
              +
              "\n" +
              "Return as a JSON array of objects with 'topic' and 'description' fields. Example: [{\"topic\":\"Collections\",\"description\":\"Data structures in Java\"}, ...]";
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Topics for Skill - " + skillName);

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

  public List<Map<String, Object>> generateQuestionsForSkillAndTopic(String skillName,
      String topicName, int numQuestions) {
    try {
      log.info(
          "[Gemini] Generating summary interview question headings for skill: {} and topic: {} (custom separator mode)",
          skillName, topicName);
      StringBuilder prompt = new StringBuilder();
      prompt.append("Generate ").append(numQuestions)
          .append(" short summary interview question headings for the skill '")
          .append(skillName).append("' on the topic '").append(topicName).append("'.\n\n");
      prompt.append("REQUIREMENTS:\n");
      prompt.append(
          "1. Each question should be a short, clear heading (1-2 lines) suitable for use as a prompt to generate detailed content later.\n");
      prompt.append("2. Do NOT include answers, explanations, or code examples.\n");
      prompt.append("3. Vary difficulty levels (BEGINNER, INTERMEDIATE, ADVANCED)\n");
      prompt.append(
          "4. Include different question types (THEORETICAL, PRACTICAL, BEHAVIORAL, PROBLEM_SOLVING, SYSTEM_DESIGN)\n");
      prompt.append(
          "5. Each question should be engaging and suitable for technical interviews.\n\n");
      prompt.append(
          "OUTPUT FORMAT: Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion.\n");
      prompt.append("Fields (in order): question, difficulty_level, question_type, tags.\n");
      prompt.append(
          "Example: What is a Java interface?|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||INTERMEDIATE|||||THEORETICAL|||||java,oop\n");
      prompt.append(
          "Do NOT return JSON or markdown. Only output the questions in the specified format.\n");
      prompt.append(
          "IMPORTANT: Do NOT add any remarks, comments, PS, notes, explanations, or any extra text before, after, or inside the output. Only output the questions in the strict format specified above.\n");
      prompt.append(
          "These headings will be used as input to a separate prompt for detailed content generation.");
      String promptStr = prompt.toString();
      log.info("[Gemini] Prompt (first 200 chars): {}",
          promptStr.length() > 200 ? promptStr.substring(0, 200) + "..." : promptStr);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(promptStr);
      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Questions for Skill and Topic - " + skillName + " - " + topicName);
      String responseBody = response.getBody();
      List<Map<String, Object>> questions = parseQuestionsResponseCustomSeparator(responseBody);
      log.info(
          "[Gemini] Parsed {} summary question headings for skill: {} and topic: {} (custom separator mode)",
          questions.size(), skillName, topicName);
      return questions;
    } catch (Exception e) {
      log.error(
          "[Gemini] Error generating summary question headings for skill and topic: {} / {} (custom separator mode)",
          skillName, topicName, e);
    }
    return new ArrayList<>();
  }

  // Helper methods
  private String buildTopicGenerationPrompt(String designationName) {
    return "Generate top 10 interview topics for the designation: " + designationName +
        ". Return as JSON array of objects with 'topicName' and 'description' fields.";
  }

  private String buildGeneralQuestionPrompt(String designation, String topicName,
      String difficultyLevel, int numQuestions) {
    return "Generate " + numQuestions + " " + difficultyLevel + " level interview questions for "
        + designation +
        " on topic: " + topicName
        + ". Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion. Do NOT return JSON or markdown. Only output the questions in the specified format. Example: What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||<div>Polymorphism is ...</div>|||||INTERMEDIATE|||||THEORETICAL|||||java,oop";
  }

  private String buildCompanySpecificQuestionPrompt(String companyName, String designation,
      String topicName, String difficultyLevel, int numQuestions) {
    return "Generate " + numQuestions + " " + difficultyLevel
        + " level company-specific interview questions for " +
        companyName + " for " + designation + " role on topic: " + topicName +
        ". Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion. Do NOT return JSON or markdown. Only output the questions in the specified format. Example: What is a Java interface?|||||<div>In Java, an interface is ...</div>|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||<div>Polymorphism is ...</div>|||||INTERMEDIATE|||||THEORETICAL|||||java,oop";
  }

  private String buildDesignationGenerationPrompt(String departmentName) {
    return "Generate common job designations for the department: " + departmentName +
        ". Return as JSON array of objects with 'name' and 'description' fields.";
  }

  private String buildComprehensiveQuestionPrompt(String skillName, int numQuestions) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("Generate ").append(numQuestions)
        .append(" summary interview questions for the skill: ").append(skillName).append("\n\n");
    prompt.append("REQUIREMENTS:\n");
    prompt.append(
        "1. Each question should be a short, clear summary (1-2 lines) suitable for use as a prompt to generate detailed content later.\n");
    prompt.append("2. Do NOT include answers or explanations.\n");
    prompt.append("3. Vary difficulty levels (BEGINNER, INTERMEDIATE, ADVANCED)\n");
    prompt.append(
        "4. Include different question types (THEORETICAL, PRACTICAL, BEHAVIORAL, PROBLEM_SOLVING, SYSTEM_DESIGN)\n");
    prompt.append("5. Each question should be engaging and suitable for technical interviews.\n\n");
    prompt.append(
        "OUTPUT FORMAT: Output all questions as a single string, each question's fields separated by ||||| (five pipes), and each question block separated by tymblQuestion.\n");
    prompt.append("Fields (in order): question, difficulty_level, question_type, tags.\n");
    prompt.append(
        "Example: What is a Java interface?|||||BEGINNER|||||THEORETICAL|||||java,interface,ooptymblQuestionWhat is polymorphism?|||||INTERMEDIATE|||||THEORETICAL|||||java,oop\n");
    prompt.append(
        "Do NOT return JSON or markdown. Only output the questions in the specified format.\n");
    prompt.append("Make the questions concise, engaging, and suitable for technical interviews. ");
    prompt.append("Focus on real-world scenarios and practical applications of ").append(skillName)
        .append(".");
    return prompt.toString();
  }

  private String buildDetailedContentPrompt(String skillName, String questionSummary) {
    StringBuilder prompt = new StringBuilder();
    prompt.append(
        "Given the following short interview question heading, generate a detailed, well-phrased interview question and a comprehensive answer.\n\n");
    prompt.append("SKILL: ").append(skillName).append("\n");
    prompt.append("QUESTION HEADING: ").append(questionSummary).append("\n\n");
    prompt.append("REQUIREMENTS:\n");
    prompt.append(
        "1. Expand the heading into a full, detailed interview question suitable for a technical interview. The question should be clear, specific, and context-rich.\n");
    prompt.append(
        "2. Provide a step-by-step, detailed answer in HTML format using <h2>, <h3>, <p>, <ul>, <li>, <code>, <pre> tags.\n");
    prompt.append("3. Include code examples where applicable (especially for DSA).\n");
    prompt.append("4. Include practical examples and real-world scenarios.\n");
    prompt.append("5. Explain concepts thoroughly with examples.\n");
    prompt.append("6. Make it engaging and easy to understand.\n");
    prompt.append("7. Include best practices and common pitfalls.\n");
    prompt.append(
        "8. IMPORTANT : If any field (e.g., detailed_answer) is not present, return it as blank (empty string). Do NOT skip any field. Always output all fields in order, even if some are empty.\n\n");
    prompt.append(
        "OUTPUT FORMAT: Output all fields as a single string, separated by exactly ||||| (five pipes).\n");
    prompt.append(
        "Fields (in order): detailed_question, detailed_answer, code_examples, html_content, tags.\n");
    prompt.append(
        "Example: <h2>Detailed Question</h2><p>Write a program to reverse a singly linked list. Explain your approach and provide an example.</p>|||||<h2>Detailed Answer</h2><p>To reverse a linked list...</p>|||||<h3>Code Examples</h3><pre><code>// Code here</code></pre>|||||<div>Complete HTML formatted answer</div>|||||linked list,reverse,algorithm\n");
    prompt.append(
        "IMPORTANT : Do NOT return JSON, markdown, or any extra text. Only output the fields in the specified format, separated by pipes.");
    return prompt.toString();
  }

  private String buildComprehensiveTechSkillsPrompt() {
    return
        "Generate a comprehensive, up-to-date, and diverse list of technology skills relevant for the tech industry. "
            +
            "Include programming languages, frameworks, libraries, cloud platforms, devops tools, AI/ML, data engineering, security, frontend, backend, mobile, testing, and emerging technologies. "
            +
            "For each skill, provide: 'name', 'category' (e.g. Programming Language, Framework, Cloud, DevOps, AI/ML, Data, Security, Frontend, Backend, Mobile, Testing, Emerging Tech), and a one-line 'description'. "
            +
            "Output as a JSON array of objects. Example: [{\"name\": \"Python\", \"category\": \"Programming Language\", \"description\": \"A versatile high-level programming language used for web, data, and AI.\"}, ...]. "
            +
            "Include at least 50 skills, covering both popular and niche areas. Use correct spelling and avoid duplicates.";
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
    List<Map<String, Object>> contentList = new ArrayList<>();
    try {
      log.info(
          "[Gemini] Parsing detailed content response (pipe separator, single block, 5 fields). Raw response length: {}",
          responseBody != null ? responseBody.length() : 0);
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.info("[Gemini] Extracted generated text (first 200 chars): {}",
                generatedText != null && generatedText.length() > 200 ?
                    generatedText.substring(0, 200) + "..." : generatedText);
            String[] fields = generatedText.split("\\|\\|\\|\\|\\|");
            if (fields.length < 5) {
              log.warn("[Gemini] Malformed detailed content (expected 5 fields): {}",
                  generatedText);
            } else {
              Map<String, Object> contentMap = new HashMap<>();
              contentMap.put("detailed_question", fields[0].trim());
              contentMap.put("detailed_answer", fields[1].trim());
              contentMap.put("code_examples", fields[2].trim());
              contentMap.put("html_content", fields[3].trim());
              contentMap.put("tags", fields[4].trim());
              contentList.add(contentMap);
            }
            log.info("[Gemini] Parsed detailed content from pipe separator response (5 fields)");
            return contentList;
          }
        }
      }
      log.warn(
          "[Gemini] No valid candidates/content/parts found in detailed content response (pipe separator, 5 fields)");
      return contentList;
    } catch (Exception e) {
      log.error("[Gemini] Error parsing detailed content response (pipe separator, 5 fields)", e);
      return contentList;
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
          log.info("[extractJsonFromText] Extracted JSON array: {}",
              jsonArray.length() > 300 ? jsonArray.substring(0, 300) + "..." : jsonArray);
          return jsonArray;
        }
      }

      // If no array, try to extract a JSON object
      int objStart = text.indexOf('{');
      if (objStart >= 0) {
        int objEnd = findMatchingBracket(text, objStart, '{', '}');
        if (objEnd > objStart) {
          String jsonObject = text.substring(objStart, objEnd + 1).trim();
          log.info("[extractJsonFromText] Extracted JSON object: {}",
              jsonObject.length() > 300 ? jsonObject.substring(0, 300) + "..." : jsonObject);
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

  private int findMatchingBracket(String text, int startIndex, char openBracket,
      char closeBracket) {
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
      log.info(
          "[Gemini] Parsing questions response (custom separator mode, tymblQuestion). Raw response length: {}",
          responseBody != null ? responseBody.length() : 0);
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            log.info("[Gemini] Extracted generated text (first 200 chars): {}",
                generatedText != null && generatedText.length() > 200 ?
                    generatedText.substring(0, 200) + "..." : generatedText);
            String[] blocks = generatedText.split("tymblQuestion");
            for (String block : blocks) {
              String trimmed = block.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
              String[] fields = trimmed.split("\\|\\|\\|\\|");
              if (fields.length < 4) {
                log.warn("[Gemini] Malformed question block (expected 4 fields): {}", trimmed);
                continue;
              }
              Map<String, Object> q = new HashMap<>();
              q.put("question", fields[0].trim());
              q.put("difficulty_level", fields[1].trim());
              q.put("question_type", fields[2].trim());
              q.put("tags", fields[3].trim());
              questions.add(q);
            }
            log.info(
                "[Gemini] Parsed {} summary questions from custom separator response (tymblQuestion)",
                questions.size());
            return questions;
          }
        }
      }
      log.warn(
          "[Gemini] No valid candidates/content/parts found in custom separator response (tymblQuestion)");
      return questions;
    } catch (Exception e) {
      log.error("[Gemini] Error parsing questions response (custom separator mode, tymblQuestion)",
          e);
      return questions;
    }
  }

  public String shortenContent(String content, String contentType) {
    try {
      log.info("[Gemini] Shortening {} content (length: {})", contentType, content.length());
      String prompt = buildContentShorteningPrompt(content, contentType);
      log.info("[Gemini] Prompt (first 200 chars): {}",
          prompt.length() > 200 ? prompt.substring(0, 200) + "..." : prompt);
      Map<String, Object> requestBody = aiRestService.buildRequestBody(prompt);

      ResponseEntity<String> response = aiRestService.callGeminiAPI(requestBody,
          "Content Shortening for " + contentType);
      String shortenedContent = parseShortenedContentResponse(response.getBody());
      log.info("[Gemini] Successfully shortened {} content to {} characters", contentType,
          shortenedContent.length());
      return shortenedContent;
    } catch (Exception e) {
      log.error("[Gemini] Error shortening {} content", contentType, e);
      return content; // Return original content if there's an error
    }
  }

  private String buildContentShorteningPrompt(String content, String contentType) {
    return String.format(
        "You are a content summarizer. Please reduce the following %s content to a maximum of 5 key points. "
            +
            "IMPORTANT: Do NOT shorten individual sentences. Instead, reduce the number of sentences by combining related information or removing redundant points. "
            +
            "If the content already has 5 or fewer sentences, return it unchanged. " +
            "Format the response as follows:\n" +
            "- If there are multiple sentences/points, start each with a hyphen (-) and put each on a new line\n"
            +
            "- If there's only one sentence/point, do NOT use a hyphen\n" +
            "- Focus on maintaining the core message while reducing sentence count\n" +
            "- Return only the reduced content with no additional text, comments, or formatting\n\n"
            +
            "Example format for multiple points:\n" +
            "- First key point about the company\n" +
            "- Second key point about the company\n" +
            "- Third key point about the company\n\n" +
            "Content to reduce:\n%s",
        contentType, content
    );
  }

  private String parseShortenedContentResponse(String responseBody) {
    try {
      JsonNode responseNode = objectMapper.readTree(responseBody);
      JsonNode candidates = responseNode.get("candidates");
      if (candidates != null && candidates.isArray() && candidates.size() > 0) {
        JsonNode content = candidates.get(0).get("content");
        if (content != null) {
          JsonNode parts = content.get("parts");
          if (parts != null && parts.isArray() && parts.size() > 0) {
            String generatedText = parts.get(0).get("text").asText();
            return formatShortenedContent(generatedText.trim());
          }
        }
      }
      log.error("Unexpected Gemini API response structure for content shortening: {}",
          responseBody);
      return "";
    } catch (Exception e) {
      log.error("Error parsing Gemini response for content shortening", e);
      return "";
    }
  }

  private String formatShortenedContent(String content) {
    if (content == null || content.trim().isEmpty()) {
      return "";
    }

    // Split by common sentence endings and clean up
    String[] sentences = content.split("(?<=[.!?])\\s+");
    List<String> formattedSentences = new ArrayList<>();

    for (String sentence : sentences) {
      String trimmed = sentence.trim();
      if (!trimmed.isEmpty()) {
        // Remove any existing hyphens at the beginning
        if (trimmed.startsWith("-")) {
          trimmed = trimmed.substring(1).trim();
        }
        formattedSentences.add(trimmed);
      }
    }

    // If only one sentence, return as is
    if (formattedSentences.size() <= 1) {
      return content.trim();
    }

    // Format multiple sentences with hyphens and line breaks
    StringBuilder formattedContent = new StringBuilder();
    for (int i = 0; i < formattedSentences.size(); i++) {
      if (i > 0) {
        formattedContent.append("\n");
      }
      formattedContent.append("- ").append(formattedSentences.get(i));
    }

    return formattedContent.toString();
  }


} 