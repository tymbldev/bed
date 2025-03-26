package com.tymbl.common.repository;

import com.tymbl.common.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByTitle(String title);
    List<Designation> findByLevel(Integer level);
    boolean existsByTitle(String title);
} 