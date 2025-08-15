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
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
public class SkillController {

  private final SkillRepository skillRepository;

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
  @Cacheable(value = "skills")
  public ResponseEntity<List<Skill>> getAllSkills() {
    return ResponseEntity.ok(skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc());
  }
} 