package com.tymbl.common.repository;

import com.tymbl.common.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByCountryIdOrderByNameAsc(Long countryId);
    List<City> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
} 