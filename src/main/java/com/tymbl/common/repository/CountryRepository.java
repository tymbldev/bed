package com.tymbl.common.repository;

import com.tymbl.common.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByName(String name);
    Optional<Country> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
} 