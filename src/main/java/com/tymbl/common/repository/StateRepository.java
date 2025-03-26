package com.tymbl.common.repository;

import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameAndCountry(String name, Country country);
    Optional<State> findByCodeAndCountry(String code, Country country);
    List<State> findByCountryOrderByNameAsc(Country country);
    boolean existsByNameAndCountry(String name, Country country);
} 