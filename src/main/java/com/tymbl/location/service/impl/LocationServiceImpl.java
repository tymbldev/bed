package com.tymbl.location.service.impl;

import com.tymbl.common.dto.CityDTO;
import com.tymbl.common.dto.CountryDTO;
import com.tymbl.common.entity.City;
import com.tymbl.common.mapper.CityMapper;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.repository.CountryRepository;
import com.tymbl.exception.ResourceNotFoundException;
import com.tymbl.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Override
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(country -> CountryDTO.builder()
                        .id(country.getId())
                        .name(country.getName())
                        .code(country.getCode())
                        .phoneCode(country.getPhoneCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDTO> getCitiesByCountry(Long countryId) {
        if (!countryRepository.existsById(countryId)) {
            throw new ResourceNotFoundException("Country not found with id: " + countryId);
        }
        
        return cityRepository.findByCountryIdOrderByNameAsc(countryId).stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CityDTO> getAllCities() {
        return cityRepository.findAll().stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CityDTO getCityById(Long cityId) {
        return cityRepository.findById(cityId)
                .map(cityMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));
    }

    @Override
    public List<CityDTO> searchCities(String query) {
        return cityRepository.findByNameContainingIgnoreCaseOrderByNameAsc(query).stream()
                .map(cityMapper::toDTO)
                .collect(Collectors.toList());
    }
} 