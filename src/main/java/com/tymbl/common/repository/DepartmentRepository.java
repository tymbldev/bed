package com.tymbl.common.repository;

import com.tymbl.common.entity.Department;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

  Optional<Department> findByName(String name);

  boolean existsByName(String name);

  // Find all departments ordered by rank (ascending)
  List<Department> findAllByOrderByRankAsc();
} 