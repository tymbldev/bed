package com.tymbl.common.repository;

import com.tymbl.common.entity.SkillTopic;
import com.tymbl.common.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillTopicRepository extends JpaRepository<SkillTopic, Long> {
    List<SkillTopic> findBySkill(Skill skill);
    void deleteBySkill(Skill skill);
} 