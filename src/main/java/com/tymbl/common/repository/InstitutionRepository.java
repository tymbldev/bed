package com.tymbl.common.repository;

import com.tymbl.common.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByName(String name);
    List<Institution> findByNameContainingIgnoreCase(String name);
    boolean existsByName(String name);
} 