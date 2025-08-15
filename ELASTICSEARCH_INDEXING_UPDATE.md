# Elasticsearch Indexing Update

## Overview

The `indexAllEntities` method has been exposed as a controller endpoint in `AIDropdownController` with enhanced functionality that first deletes all existing documents from Elasticsearch indices before re-indexing everything fresh.

## Implementation Details

### 1. **Enhanced ElasticsearchIndexingService**

#### New Methods Added:

- **`deleteAllDocumentsFromIndex(String indexName)`**: Deletes all documents from a specific index
- **`deleteAllDocumentsFromAllIndices()`**: Deletes all documents from all indices (companies, designations, cities)
- **Enhanced `indexAllEntities()`**: Now includes cleanup before re-indexing

#### Key Features:

- **Cleanup First**: Deletes all existing documents before re-indexing
- **Index Existence Check**: Verifies index exists before attempting deletion
- **Bulk Deletion**: Uses `deleteByQuery` with `match_all` for efficient deletion
- **Wait Period**: Includes 1-second wait after deletion to ensure completion
- **Comprehensive Logging**: Detailed logging for monitoring and debugging

### 2. **New Controller Endpoint**

#### Endpoint: `POST /api/v1/ai/dropdowns/elasticsearch/index-all-entities`

- **Location**: `AIDropdownController`
- **Purpose**: Re-index all entities to Elasticsearch with cleanup
- **Method**: POST
- **Authentication**: Not required (public endpoint)

#### Swagger Documentation:

- **Summary**: "Re-index all entities to Elasticsearch with cleanup"
- **Description**: "First deletes all existing documents from Elasticsearch indices (companies, designations, cities), then re-indexes all entities fresh. This ensures a clean, up-to-date search index."
- **Example Response**: Comprehensive example showing cleanup and indexing results

### 3. **Process Flow**

The enhanced indexing process follows this sequence:

1. **Cleanup Phase**:
   - Delete all documents from companies index
   - Delete all documents from designations index  
   - Delete all documents from cities index
   - Wait for deletion to complete

2. **Re-indexing Phase**:
   - Index all companies fresh
   - Index all designations fresh
   - Index all cities fresh

3. **Response**:
   - Cleanup results for each index
   - Indexing results for each entity type
   - Overall success/failure status

### 4. **Postman Collection Update**

#### New Endpoint Added:

- **Section**: "AI Dropdown Controller"
- **Name**: "Re-index All Entities to Elasticsearch"
- **Method**: POST
- **URL**: `{{base_url}}/api/v1/ai/dropdowns/elasticsearch/index-all-entities`
- **Example Response**: Complete sample response showing cleanup and indexing results

## Code Changes

### ElasticsearchIndexingService.java

```java
// New method: Delete all documents from a specific index
public Map<String, Object> deleteAllDocumentsFromIndex(String indexName)

// New method: Delete all documents from all indices
public Map<String, Object> deleteAllDocumentsFromAllIndices()

// Enhanced method: Index all entities with cleanup
public Map<String, Object> indexAllEntities()
```

### AIDropdownController.java

```java
// New dependency injection
private final ElasticsearchIndexingService elasticsearchIndexingService;

// New endpoint
@PostMapping("/elasticsearch/index-all-entities")
public ResponseEntity<Map<String, Object>> indexAllEntitiesToElasticsearch()
```

## Benefits

1. **Clean Index**: Ensures no stale or duplicate data in Elasticsearch
2. **Fresh Data**: All entities are re-indexed with latest information
3. **Atomic Operation**: Complete cleanup and re-indexing in one call
4. **Monitoring**: Detailed logging and response data for tracking
5. **Error Handling**: Comprehensive error handling and reporting
6. **Postman Ready**: Fully documented and testable via Postman

## Usage

### Via API Call

```bash
POST {{base_url}}/api/v1/ai/dropdowns/elasticsearch/index-all-entities
```

### Via Postman

1. Import the updated collection
2. Navigate to "AI Dropdown Controller" section
3. Use "Re-index All Entities to Elasticsearch" endpoint
4. Send POST request (no body required)

## Response Format

```json
{
  "cleanup": {
    "companies": {
      "indexName": "companies",
      "documentsDeleted": 150,
      "message": "All documents deleted from index: companies"
    },
    "designations": {
      "indexName": "designations", 
      "documentsDeleted": 80,
      "message": "All documents deleted from index: designations"
    },
    "cities": {
      "indexName": "cities",
      "documentsDeleted": 200,
      "message": "All documents deleted from index: cities"
    },
    "message": "All documents deleted from all indices"
  },
  "companies": {
    "totalCompanies": 150,
    "indexedSuccessfully": 150,
    "failedToIndex": 0,
    "message": "Company indexing completed"
  },
  "designations": {
    "totalDesignations": 80,
    "totalIndexed": 80,
    "failedToIndex": 0,
    "message": "Designation indexing completed"
  },
  "cities": {
    "totalCities": 200,
    "indexedSuccessfully": 200,
    "failedToIndex": 0,
    "message": "City indexing completed"
  },
  "message": "All entities re-indexed to Elasticsearch after cleanup"
}
```

## Next Steps

1. **Test the endpoint** using Postman
2. **Monitor logs** during indexing process
3. **Verify Elasticsearch** indices are clean and up-to-date
4. **Schedule regular re-indexing** if needed for data freshness
5. **Extend functionality** to other entity types as needed
