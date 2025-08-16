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
import java.util.stream.Collectors;
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

  /**
   * DTO for skills with only id and name
   */
  public static class SkillDTO {
    private Long id;
    private String name;

    public SkillDTO(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
  }

  @GetMapping
  @Operation(
      summary = "Get all skills",
      description = "Returns a list of all available skills with only id and name fields, sorted by usage count (descending) and name (ascending). Results are cached for better performance."
  )
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Successfully retrieved skills list",
          content = @Content(
              schema = @Schema(implementation = SkillDTO.class),
              examples = @ExampleObject(
                  value = "[\n" +
                      "  {\n" +
                      "    \"id\": 1,\n" +
                      "    \"name\": \"Java\"\n" +
                      "  },\n" +
                      "  {\n" +
                      "    \"id\": 2,\n" +
                      "    \"name\": \"Spring Boot\"\n" +
                      "  },\n" +
                      "  {\n" +
                      "    \"id\": 3,\n" +
                      "    \"name\": \"AWS\"\n" +
                      "  }\n" +
                      "]"
              )
          )
      )
  })
  public ResponseEntity<List<SkillDTO>> getAllSkills() {
    String cacheKey = "all_skills";
    
    // Check cache first
    if (!isCacheExpired(cacheKey)) {
      List<Skill> cachedSkills = skillsCache.get(cacheKey);
      if (cachedSkills != null) {
        log.debug("Returning skills from cache");
        List<SkillDTO> skillDTOs = cachedSkills.stream()
            .map(skill -> new SkillDTO(skill.getId(), skill.getName()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(skillDTOs);
      }
    }
    
    // Cache miss or expired, fetch from database
    log.debug("Cache miss for skills, fetching from database");
    List<Skill> skills = skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc();
    
    // Update cache
    skillsCache.put(cacheKey, skills);
    skillsCacheTimestamp.put(cacheKey, System.currentTimeMillis());
    
    // Convert to DTOs
    List<SkillDTO> skillDTOs = skills.stream()
        .map(skill -> new SkillDTO(skill.getId(), skill.getName()))
        .collect(Collectors.toList());
    
    log.debug("Skills cache updated with {} skills", skills.size());
    return ResponseEntity.ok(skillDTOs);
  }
} 