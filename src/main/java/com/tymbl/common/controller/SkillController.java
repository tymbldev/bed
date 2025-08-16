package com.tymbl.common.controller;

import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS,
        RequestMethod.PATCH
    }
)
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Tag(name = "Skills", description = "Skills suggester endpoints")
@Slf4j
public class SkillController {

  private final SkillRepository skillRepository;
  
  // JVM cache for skills
  private final ConcurrentMap<String, List<Skill>> skillsCache = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, Long> skillsCacheTimestamp = new ConcurrentHashMap<>();
  
  // Cache TTL in milliseconds (30 minutes)
  private static final long CACHE_TTL = 30 * 60 * 1000L;

  /**
   * Check if cache entry is expired
   */
  private boolean isCacheExpired(String cacheKey) {
    Long timestamp = skillsCacheTimestamp.get(cacheKey);
    return timestamp == null || (System.currentTimeMillis() - timestamp) > CACHE_TTL;
  }

  @GetMapping
  @Operation(
      summary = "Get all skills",
      description = "Returns a list of all available skills sorted by usage count (descending) and name (ascending). Results are cached for better performance."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully retrieved skills list",
          content = @Content(
              schema = @Schema(implementation = Skill.class),
              examples = @ExampleObject(
                  value = "[\n" +
                      "  {\n" +
                      "    \"id\": 1,\n" +
                      "    \"name\": \"Java\",\n" +
                      "    \"usageCount\": 1000,\n" +
                      "    \"category\": \"Programming Language\"\n" +
                      "  },\n" +
                      "  {\n" +
                      "    \"id\": 2,\n" +
                      "    \"name\": \"Spring Boot\",\n" +
                      "    \"usageCount\": 800,\n" +
                      "    \"category\": \"Framework\"\n" +
                      "  },\n" +
                      "  {\n" +
                      "    \"id\": 3,\n" +
                      "    \"name\": \"AWS\",\n" +
                      "    \"usageCount\": 600,\n" +
                      "    \"category\": \"Cloud Platform\"\n" +
                      "  }\n" +
                      "]"
              )
          )
      )
  })
  public ResponseEntity<List<Skill>> getAllSkills() {
    String cacheKey = "all_skills";
    
    // Check cache first
    if (!isCacheExpired(cacheKey)) {
      List<Skill> cachedSkills = skillsCache.get(cacheKey);
      if (cachedSkills != null) {
        log.debug("Returning skills from cache");
        return ResponseEntity.ok(cachedSkills);
      }
    }
    
    // Cache miss or expired, fetch from database
    log.debug("Cache miss for skills, fetching from database");
    List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
    
    // Update cache
    skillsCache.put(cacheKey, skills);
    skillsCacheTimestamp.put(cacheKey, System.currentTimeMillis());
    
    log.debug("Skills cache updated with {} skills", skills.size());
    return ResponseEntity.ok(skills);
  }
} 