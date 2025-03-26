package com.tymbl.common.repository;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndCountry(String name, Country country);
    List<City> findByCountryOrderByNameAsc(Country country);
    List<City> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
} 