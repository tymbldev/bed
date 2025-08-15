package com.tymbl.location.service;

import com.tymbl.common.dto.CityDTO;
import com.tymbl.common.dto.CountryDTO;
import java.util.List;

public interface LocationService {

  /**
   * Returns a list of all countries
   *
   * @return List of CountryDTO objects
   */
  List<CountryDTO> getAllCountries();

  /**
   * Returns a list of cities for a specific country
   *
   * @param countryId ID of the country
   * @return List of CityDTO objects
   */
  List<CityDTO> getCitiesByCountry(Long countryId);

  /**
   * Returns a list of all cities
   *
   * @return List of CityDTO objects
   */
  List<CityDTO> getAllCities();

  /**
   * Returns a city by its ID
   *
   * @param cityId ID of the city
   * @return CityDTO object
   */
  CityDTO getCityById(Long cityId);

  /**
   * Searches for cities based on a query string
   *
   * @param query Search term for city name
   * @return List of CityDTO objects matching the query
   */
  List<CityDTO> searchCities(String query);
} 