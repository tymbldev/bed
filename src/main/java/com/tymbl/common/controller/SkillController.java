package com.tymbl.common.controller;

import com.tymbl.common.entity.Skill;
import com.tymbl.common.repository.SkillRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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
        @ApiResponse(responseCode = "200", description = "Successfully retrieved skills list"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @Cacheable(value = "skillsList", key = "'all'")
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillRepository.findByEnabledTrueOrderByUsageCountDescNameAsc());
    }
} 