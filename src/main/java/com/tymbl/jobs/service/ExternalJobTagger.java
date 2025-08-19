package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tymbl.jobs.service.SkillTaggerService;
import com.tymbl.jobs.service.JobTagTaggerService;
import com.tymbl.jobs.service.CompanyTaggerService;
import com.tymbl.jobs.service.DesignationTaggerService;
import com.tymbl.jobs.service.CityTaggerService;
import com.tymbl.jobs.service.CountryTaggerService;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalJobTagger {

  private final SkillTaggerService skillTaggerService;
  private final JobTagTaggerService jobTagTaggerService;
  private final CompanyTaggerService companyTaggerService;
  private final DesignationTaggerService designationTaggerService;
  private final CityTaggerService cityTaggerService;
  private final CountryTaggerService countryTaggerService;


  /**
   * Tag external job detail with company, designation, city, and country
   */
  @Transactional
  public TaggingResult tagExternalJob(ExternalJobDetail externalJob) {
    TaggingResult result = new TaggingResult();
    
    try {
      // Tag company
      CompanyTaggerService.CompanyTaggingResult companyResult = companyTaggerService.tagCompany(externalJob.getCompanyName(), externalJob.getId(), externalJob.getPortalName());
      result.setCompanyId(companyResult.getCompanyId());
      result.setCompanyName(companyResult.getCompanyName());
      result.setCompanyConfidence(companyResult.getConfidence());
      
      // Tag designation
      DesignationTaggerService.DesignationTaggingResult designationResult = designationTaggerService.tagDesignation(externalJob.getJobTitle(), externalJob.getId(), externalJob.getPortalName());
      result.setDesignationId(designationResult.getDesignationId());
      result.setDesignationName(designationResult.getDesignationName());
      result.setDesignationConfidence(designationResult.getConfidence());
      
      // Tag city
      CityTaggerService.CityTaggingResult cityResult = cityTaggerService.tagCity(externalJob.getCityName(), externalJob.getId(), externalJob.getPortalName());
      result.setCityId(cityResult.getCityId());
      result.setCityName(cityResult.getCityName());
      result.setCityConfidence(cityResult.getConfidence());
      
      // Tag country
      CountryTaggerService.CountryTaggingResult countryResult = countryTaggerService.tagCountry(externalJob.getCountryName(), externalJob.getId(), externalJob.getPortalName());
      result.setCountryId(countryResult.getCountryId());
      result.setCountryName(countryResult.getCountryName());
      result.setCountryConfidence(countryResult.getConfidence());
      
      // Tag skills
      SkillTaggerService.SkillTaggingResult skillResult = skillTaggerService.tagSkills(externalJob.getSkillsJson(), externalJob.getId(), externalJob.getPortalName());
      result.setSkillIds(skillResult.getSkillIds());
      result.setSkillNames(skillResult.getSkillNames());
      result.setSkillConfidence(skillResult.getConfidence());
      
      // Tag job tags
      JobTagTaggerService.JobTagTaggingResult jobTagResult = jobTagTaggerService.tagJobTags(externalJob.getJobTagsJson(), externalJob.getId(), externalJob.getPortalName());
      result.setJobTags(jobTagResult.getJobTags());
      result.setJobTagConfidence(jobTagResult.getConfidence());
      
      log.info("Tagged external job {}: Company={} (ID: {}, Confidence: {}), Designation={} (ID: {}, Confidence: {}), City={} (ID: {}, Confidence: {}), Country={} (ID: {}, Confidence: {}), Skills={} (IDs: {}, Confidence: {}), JobTags={} (Confidence: {})",
          externalJob.getId(),
          result.getCompanyName(), result.getCompanyId(), result.getCompanyConfidence(),
          result.getDesignationName(), result.getDesignationId(), result.getDesignationConfidence(),
          result.getCityName(), result.getCityId(), result.getCityConfidence(),
          result.getCountryName(), result.getCountryId(), result.getCountryConfidence(),
          result.getSkillNames(), result.getSkillIds(), result.getSkillConfidence(),
          result.getJobTags(), result.getJobTagConfidence());
      
    } catch (Exception e) {
      log.error("Error tagging external job {}: {}", externalJob.getId(), e.getMessage(), e);
      result.setError(e.getMessage());
    }
    
    return result;
  }

  // Result classes
  public static class TaggingResult {
    private Long companyId;
    private String companyName;
    private Double companyConfidence;
    private Long designationId;
    private String designationName;
    private Double designationConfidence;
    private Long cityId;
    private String cityName;
    private Double cityConfidence;
    private Long countryId;
    private String countryName;
    private Double countryConfidence;
    private String error;
    private List<Long> skillIds;
    private List<String> skillNames;
    private Double skillConfidence;
    private List<String> jobTags;
    private Double jobTagConfidence;

    // Getters and setters
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public Double getCompanyConfidence() { return companyConfidence; }
    public void setCompanyConfidence(Double companyConfidence) { this.companyConfidence = companyConfidence; }
    
    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }
    
    public String getDesignationName() { return designationName; }
    public void setDesignationName(String designationName) { this.designationName = designationName; }
    
    public Double getDesignationConfidence() { return designationConfidence; }
    public void setDesignationConfidence(Double designationConfidence) { this.designationConfidence = designationConfidence; }
    
    public Long getCityId() { return cityId; }
    public void setCityId(Long cityId) { this.cityId = cityId; }
    
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    
    public Double getCityConfidence() { return cityConfidence; }
    public void setCityConfidence(Double cityConfidence) { this.cityConfidence = cityConfidence; }
    
    public Long getCountryId() { return countryId; }
    public void setCountryId(Long countryId) { this.countryId = countryId; }
    
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    
    public Double getCountryConfidence() { return countryConfidence; }
    public void setCountryConfidence(Double countryConfidence) { this.countryConfidence = countryConfidence; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public List<Long> getSkillIds() { return skillIds; }
    public void setSkillIds(List<Long> skillIds) { this.skillIds = skillIds; }
    
    public List<String> getSkillNames() { return skillNames; }
    public void setSkillNames(List<String> skillNames) { this.skillNames = skillNames; }
    
    public Double getSkillConfidence() { return skillConfidence; }
    public void setSkillConfidence(Double skillConfidence) { this.skillConfidence = skillConfidence; }
    
    public List<String> getJobTags() { return jobTags; }
    public void setJobTags(List<String> jobTags) { this.jobTags = jobTags; }
    
    public Double getJobTagConfidence() { return jobTagConfidence; }
    public void setJobTagConfidence(Double jobTagConfidence) { this.jobTagConfidence = jobTagConfidence; }
  }
}
