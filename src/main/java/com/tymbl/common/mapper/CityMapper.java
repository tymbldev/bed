package com.tymbl.common.mapper;

import com.tymbl.common.dto.CityDTO;
import com.tymbl.common.entity.City;
import com.tymbl.common.repository.CountryRepository;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    
    private final CountryRepository countryRepository;
    
    public CityMapper(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }
    
    public CityDTO toDTO(City city) {
        String countryName = null;
        if (city.getCountryId() != null) {
            countryName = countryRepository.findById(city.getCountryId())
                .map(country -> country.getName())
                .orElse(null);
        }
        
        return CityDTO.builder()
            .id(city.getId())
            .name(city.getName())
            .zipCode(city.getZipCode())
            .countryId(city.getCountryId())
            .countryName(countryName)
            .build();
    }
} 