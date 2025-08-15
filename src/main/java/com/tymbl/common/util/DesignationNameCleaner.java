package com.tymbl.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility class for cleaning designation names by removing various anomalies that can occur in
 * AI-generated responses.
 */
@Slf4j
@Component
public class DesignationNameCleaner {

  /**
   * Clean a designation name by removing various anomalies and unwanted characters.
   *
   * @param designationName The raw designation name to clean
   * @return The cleaned designation name
   */
  public static String cleanDesignationName(String designationName) {
    if (designationName == null || designationName.trim().isEmpty()) {
      return null;
    }

    String cleaned = designationName;

    // Step 1: Remove markdown formatting (```json, ```, etc.)
    cleaned = cleaned.replaceAll("```json", "");
    cleaned = cleaned.replaceAll("```", "");

    // Step 2: Remove separators (||)
    cleaned = cleaned.replaceAll("\\|\\|", "");

    // Step 3: Remove newlines, carriage returns, and tabs
    cleaned = cleaned.replaceAll("\\n", "");
    cleaned = cleaned.replaceAll("\\r", "");
    cleaned = cleaned.replaceAll("\\t", "");

    // Step 4: Handle bracket patterns (same logic as SQL queries)

    // Remove names starting with '["' (LEFT(name, 2) = '["')
    if (cleaned.startsWith("[\"")) {
      cleaned = cleaned.substring(3); // SUBSTRING(name, 3)
    }

    // Remove names starting with '[' (LEFT(name, 1) = '[')
    if (cleaned.startsWith("[")) {
      cleaned = cleaned.substring(2); // SUBSTRING(name, 2)
    }

    // Remove names ending with '"]' (RIGHT(name, 2) = '"]')
    if (cleaned.endsWith("\"]")) {
      cleaned = cleaned.substring(0, cleaned.length() - 2); // LEFT(name, LENGTH(name) - 2)
    }

    // Remove names ending with ']' (RIGHT(name, 1) = ']')
    if (cleaned.endsWith("]")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1); // LEFT(name, LENGTH(name) - 1)
    }

    // Step 5: Remove quotes from beginning and end (TRIM(BOTH '"' FROM name))
    cleaned = cleaned.replaceAll("^\"+", ""); // Remove quotes from start
    cleaned = cleaned.replaceAll("\"+$", ""); // Remove quotes from end

    // Step 6: Remove multiple spaces and final trim
    cleaned = cleaned.replaceAll("\\s+", " ");
    cleaned = cleaned.trim();

    // Log if significant cleaning was done
    if (!designationName.equals(cleaned)) {
      log.info("Cleaned designation name: '{}' -> '{}'", designationName, cleaned);
    }

    return cleaned;
  }

  /**
   * Check if a designation name is valid after cleaning.
   *
   * @param designationName The designation name to validate
   * @return true if the name is valid, false otherwise
   */
  public static boolean isValidDesignationName(String designationName) {
    if (designationName == null || designationName.trim().isEmpty()) {
      return false;
    }

    String cleaned = cleanDesignationName(designationName);

    // Check if cleaning resulted in an empty string
    if (cleaned == null || cleaned.trim().isEmpty()) {
      return false;
    }

    // Check if the name is too short (less than 2 characters)
    if (cleaned.length() < 2) {
      return false;
    }

    // Check if the name contains only special characters or numbers
    if (cleaned.matches("^[\\d\\s\\W]+$")) {
      return false;
    }

    return true;
  }

  /**
   * Clean and validate a designation name, returning null if invalid.
   *
   * @param designationName The raw designation name to clean and validate
   * @return The cleaned designation name if valid, null otherwise
   */
  public static String cleanAndValidateDesignationName(String designationName) {
    if (!isValidDesignationName(designationName)) {
      return null;
    }
    return cleanDesignationName(designationName);
  }

  /**
   * Get the normalized name for duplicate detection. This method applies comprehensive
   * normalization logic matching all the SQL queries.
   *
   * @param designationName The designation name to normalize
   * @return The normalized name for duplicate detection
   */
  public static String getNormalizedNameForDuplicateDetection(String designationName) {
    if (designationName == null || designationName.trim().isEmpty()) {
      return null;
    }

    String normalized = designationName;

    // Apply comprehensive normalization logic matching all SQL queries

    // Remove separators (||)
    normalized = normalized.replaceAll("\\|\\|", "");

    // Remove newlines, carriage returns, and tabs
    normalized = normalized.replaceAll("\\n", "");
    normalized = normalized.replaceAll("\\r", "");
    normalized = normalized.replaceAll("\\t", "");

    // Handle bracket patterns (same logic as SQL queries)

    // Remove names starting with '["' (LEFT(name, 2) = '["')
    if (normalized.startsWith("[\"")) {
      normalized = normalized.substring(3); // SUBSTRING(name, 3)
    }

    // Remove names starting with '[' (LEFT(name, 1) = '[')
    if (normalized.startsWith("[")) {
      normalized = normalized.substring(2); // SUBSTRING(name, 2)
    }

    // Remove names ending with '"]' (RIGHT(name, 2) = '"]')
    if (normalized.endsWith("\"]")) {
      normalized = normalized.substring(0, normalized.length() - 2); // LEFT(name, LENGTH(name) - 2)
    }

    // Remove names ending with ']' (RIGHT(name, 1) = ']')
    if (normalized.endsWith("]")) {
      normalized = normalized.substring(0, normalized.length() - 1); // LEFT(name, LENGTH(name) - 1)
    }

    // Remove quotes from beginning and end (TRIM(BOTH '"' FROM name))
    normalized = normalized.replaceAll("^\"+", "");
    normalized = normalized.replaceAll("\"+$", "");

    // Final trim
    normalized = normalized.trim();

    return normalized;
  }
} 