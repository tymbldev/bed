package com.tymbl.common.repository;

import com.tymbl.common.entity.Skill;
import java.util.List;
import java.util.Optional;
import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

  @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
  List<Skill> findByEnabledTrueOrderByUsageCountDescNameAsc();

  boolean existsByNameIgnoreCase(String name);

  // Methods from interview.repository.SkillRepository
  List<Skill> findByCategory(String category);

  List<Skill> findByNameContainingIgnoreCase(String name);

  Optional<Skill> findByNameIgnoreCase(String name);

  List<Skill> findByNameIn(List<String> names);

  // Find top skills by usage count for AI matching
  List<Skill> findTop20ByOrderByUsageCountDesc();
} 