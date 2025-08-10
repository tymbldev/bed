package com.tymbl.common.repository;

import com.tymbl.common.entity.AiDumper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiDumperRepository extends JpaRepository<AiDumper, Long> {
}
