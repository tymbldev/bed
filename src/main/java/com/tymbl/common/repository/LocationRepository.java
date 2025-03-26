package com.tymbl.common.repository;

import com.tymbl.common.entity.City;
import com.tymbl.common.entity.Country;
import com.tymbl.common.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByCity(City city);
    List<Location> findByCountry(Country country);
    List<Location> findByIsRemote(boolean isRemote);
    Optional<Location> findByCityAndCountryAndZipCode(City city, Country country, String zipCode);
} 