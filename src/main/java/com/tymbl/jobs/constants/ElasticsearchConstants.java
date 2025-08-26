package com.tymbl.jobs.constants;

/**
 * Constants for Elasticsearch indices and operations
 */
public final class ElasticsearchConstants {

  // Index names
  public static final String JOBS_INDEX = "jobs";
  public static final String COMPANIES_INDEX = "companies";
  public static final String DESIGNATIONS_INDEX = "designations";
  public static final String CITIES_INDEX = "cities";
  public static final String SKILLS_INDEX = "skills";
  public static final String INDUSTRIES_INDEX = "industries";

  // Field names
  public static final String FIELD_ID = "id";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_DESCRIPTION = "description";
  public static final String FIELD_ACTIVE = "active";
  public static final String FIELD_PRIMARY_INDUSTRY_ID = "primaryIndustryId";
  public static final String FIELD_PRIMARY_INDUSTRY_NAME = "primaryIndustryName";
  public static final String FIELD_PRIMARY_INDUSTRY_NAME_KEYWORD = "primaryIndustryName.keyword";
  public static final String FIELD_SECONDARY_INDUSTRIES = "secondaryIndustries";
  public static final String FIELD_COMPANY_ID = "companyId";
  public static final String FIELD_COMPANY_NAME = "companyName";
  public static final String FIELD_COMPANY_NAME_KEYWORD = "companyName.keyword";
  public static final String FIELD_DESIGNATION_NAME = "designationName";
  public static final String FIELD_SEARCHABLE_TEXT = "searchableText";
  public static final String FIELD_CITY_ID = "cityId";
  public static final String FIELD_COUNTRY_ID = "countryId";
  public static final String FIELD_DESIGNATION_ID = "designationId";
  public static final String FIELD_CREATED_AT = "createdAt";
  public static final String FIELD_UPDATED_AT = "updatedAt";
  public static final String FIELD_LOGO_URL = "logoUrl";
  public static final String FIELD_WEBSITE = "website";
  public static final String FIELD_CAREER_PAGE_URL = "careerPageUrl";
  public static final String FIELD_LINKEDIN_URL = "linkedinUrl";
  public static final String FIELD_HEADQUARTERS = "headquarters";
  public static final String FIELD_COMPANY_SIZE = "companySize";
  public static final String FIELD_SPECIALTIES = "specialties";
  public static final String FIELD_ABOUT_US = "aboutUs";
  public static final String FIELD_VISION = "vision";
  public static final String FIELD_MISSION = "mission";
  public static final String FIELD_CULTURE = "culture";

  // Aggregation names
  public static final String AGG_INDUSTRIES_WITH_JOBS = "industries_with_jobs";
  public static final String AGG_COMPANIES_WITH_JOBS = "companies_with_jobs";
  public static final String AGG_TOP_COMPANIES = "top_companies";
  public static final String AGG_COMPANY_DETAILS = "company_details";
  public static final String AGG_ALL_INDUSTRIES = "all_industries";

  // Sorting and pagination defaults
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final int MAX_PAGE_SIZE = 100;
  public static final int MAX_SEARCH_SIZE = 1000;
  public static final int MAX_AGGREGATION_SIZE = 1000;
  public static final int TOP_COMPANIES_LIMIT = 5;

  // Query constants
  public static final String COUNT_FIELD = "_count";
  public static final float DEFAULT_BOOST = 1.0f;
  public static final float TITLE_BOOST = 2.0f;
  public static final float COMPANY_BOOST = 1.5f;
  public static final float DESIGNATION_BOOST = 1.5f;

  private ElasticsearchConstants() {
    // Private constructor to prevent instantiation
  }
}
