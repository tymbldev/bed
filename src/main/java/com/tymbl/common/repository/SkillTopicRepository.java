package com.tymbl.common.repository;

import com.tymbl.common.entity.Skill;
import com.tymbl.common.entity.SkillTopic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillTopicRepository extends JpaRepository<SkillTopic, Long> {

  List<SkillTopic> findBySkill(Skill skill);

  void deleteBySkill(Skill skill);
} 