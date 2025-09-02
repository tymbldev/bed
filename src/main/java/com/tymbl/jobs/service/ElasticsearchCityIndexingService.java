package com.tymbl.jobs.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.tymbl.common.entity.City;
import com.tymbl.common.repository.CityRepository;
import com.tymbl.common.service.DropdownService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for indexing cities to Elasticsearch
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchCityIndexingService {

  private final ElasticsearchClient elasticsearchClient;
  private final CityRepository cityRepository;
  private final DropdownService dropdownService;

  private static final String CITIES_INDEX = "cities";

  /**
   * Index all cities to Elasticsearch
   */
  @Transactional(readOnly = true)
  public Map<String, Object> indexAllCities() {
    log.info("🚀 Starting to index all cities to Elasticsearch");

    try {
      List<City> cities = cityRepository.findAll();
      log.info("📋 Found {} cities to index", cities.size());

      BulkRequest.Builder bulkRequest = new BulkRequest.Builder();
      log.info("🏗️ Building bulk request for {} cities", cities.size());

      for (City city : cities) {
        log.info("📄 Building document for city: {} (ID: {})", city.getName(), city.getId());
        Map<String, Object> cityDoc = buildCityDocument(city);
        log.info(
            "✅ City document built successfully for: {} - Document size: {} fields, Country ID: {}",
            city.getName(), cityDoc.size(), cityDoc.get("countryId"));

        bulkRequest.operations(op -> op
            .index(idx -> idx
                .index(CITIES_INDEX)
                .id(city.getId().toString())
                .document(cityDoc)
            )
        );
      }

      log.info("🚀 Executing bulk request to Elasticsearch for {} cities", cities.size());
      BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());
      log.info("📊 Bulk request completed. Response received: {} items, Errors: {}",
          response.items().size(), response.errors());

      int successCount = 0;
      int failureCount = 0;

      if (response.errors()) {
        failureCount = response.items().size();
        log.error("Failed to index cities: {}",
            response.items().stream().map(item -> item.error().reason())
                .collect(Collectors.joining(", ")));
      } else {
        successCount = cities.size();
        log.info("✅ All {} cities indexed successfully", successCount);
      }

      Map<String, Object> result = new HashMap<>();
      result.put("totalCities", cities.size());
      result.put("indexedSuccessfully", successCount);
      result.put("failedToIndex", failureCount);
      result.put("message", "City indexing completed");

      log.info("🎉 City indexing completed - Success: {}, Failures: {}", successCount, failureCount);

      return result;

    } catch (Exception e) {
      log.error("Error indexing cities to Elasticsearch", e);
      Map<String, Object> error = new HashMap<>();
      error.put("error", "Failed to index cities: " + e.getMessage());
      return error;
    }
  }

  /**
   * Sync a city to Elasticsearch (save or update)
   */
  public void syncCityToElasticsearch(City city) {
    try {
      log.info("🔄 Starting to sync city to Elasticsearch: {} (ID: {})", city.getName(), city.getId());

      Map<String, Object> cityDocument = buildCityDocument(city);
      log.info("✅ City document built successfully for: {} - Document size: {} fields",
          city.getName(), cityDocument.size());

      IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(i -> i
          .index(CITIES_INDEX)
          .id(city.getId().toString())
          .document(cityDocument)
      );
      log.info("📝 Index request prepared for city: {} to index: {}", city.getName(), CITIES_INDEX);

      log.info("🚀 Executing index request to Elasticsearch for city: {}", city.getName());
      IndexResponse response = elasticsearchClient.index(indexRequest);
      log.info("📊 Index request completed for city: {} - Result: {}, Document ID: {}",
          city.getName(), response.result().name(), response.id());

      log.info("✅ Successfully synced city {} to Elasticsearch with result: {}",
          city.getId(), response.result().name());

    } catch (Exception e) {
      log.error("Failed to sync city {} to Elasticsearch. Error: {}", city.getId(),
          e.getMessage(), e);
      // Don't throw exception - let main transaction continue
    }
  }

  /**
   * Build city document for Elasticsearch
   */
  private Map<String, Object> buildCityDocument(City city) {
    log.info("🏗️ Building Elasticsearch document for city: {} (ID: {})", city.getName(), city.getId());

    Map<String, Object> doc = new HashMap<>();
    doc.put("id", city.getId());
    doc.put("name", city.getName());
    doc.put("countryId", city.getCountryId());
    doc.put("zipCode", city.getZipCode());
    doc.put("processedName", city.getProcessedName());
    doc.put("processedNameGenerated", city.isProcessedNameGenerated());

          log.info("📋 Basic city fields added for: {} - Fields: {}", city.getName(), doc.size());

    // Add country name if available
    if (city.getCountryId() != null) {
      try {
        String countryName = dropdownService.getCountryNameById(city.getCountryId());
        doc.put("countryName", countryName);
        log.info("🌍 Country name added for city: {} - Country: {}", city.getName(), countryName);
      } catch (Exception e) {
        log.warn("Error fetching country name for city ID {}: {}", city.getId(), e.getMessage());
      }
    }

    // Build searchable text
    StringBuilder searchableText = new StringBuilder();
          log.info("🔍 Building searchable text for city: {}", city.getName());

    if (city.getName() != null) {
      searchableText.append(city.getName()).append(" ");
    }
    if (city.getProcessedName() != null) {
      searchableText.append(city.getProcessedName()).append(" ");
    }
    if (city.getZipCode() != null) {
      searchableText.append(city.getZipCode()).append(" ");
    }
    if (city.getCountryId() != null) {
      try {
        String countryName = dropdownService.getCountryNameById(city.getCountryId());
        if (countryName != null) {
          searchableText.append(countryName).append(" ");
        }
      } catch (Exception e) {
        log.warn("Error adding country name to searchable text for city {}: {}", city.getName(),
            e.getMessage());
      }
    }

    doc.put("searchableText", searchableText.toString().trim());
          log.info("📝 Searchable text built for city: {} - Length: {} characters",
          city.getName(), searchableText.length());

          log.info("✅ City document built successfully for: {} - Total fields: {}", city.getName(),
          doc.size());
    return doc;
  }
}
