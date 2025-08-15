package com.tymbl.common.service;

import com.tymbl.common.entity.Institution;
import com.tymbl.common.repository.InstitutionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InstitutionService {

  private final InstitutionRepository institutionRepository;

  public List<Institution> getAllInstitutions() {
    return institutionRepository.findAll();
  }

  @Transactional
  public Institution createInstitution(Institution institution) {
    if (institutionRepository.existsByName(institution.getName())) {
      throw new RuntimeException(
          "Institution with name '" + institution.getName() + "' already exists");
    }
    return institutionRepository.save(institution);
  }

  public Institution getInstitutionById(Long id) {
    return institutionRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Institution not found with id: " + id));
  }

  @Transactional
  public Institution updateInstitution(Long id, Institution institution) {
    Institution existingInstitution = getInstitutionById(id);

    if (!existingInstitution.getName().equals(institution.getName()) &&
        institutionRepository.existsByName(institution.getName())) {
      throw new RuntimeException(
          "Institution with name '" + institution.getName() + "' already exists");
    }

    existingInstitution.setName(institution.getName());
    existingInstitution.setDescription(institution.getDescription());
    existingInstitution.setLocation(institution.getLocation());
    existingInstitution.setWebsite(institution.getWebsite());

    return institutionRepository.save(existingInstitution);
  }

  @Transactional
  public void deleteInstitution(Long id) {
    if (!institutionRepository.existsById(id)) {
      throw new RuntimeException("Institution not found with id: " + id);
    }
    institutionRepository.deleteById(id);
  }

  public List<Institution> searchInstitutions(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return getAllInstitutions();
    }
    return institutionRepository.findByNameContainingIgnoreCase(keyword.trim());
  }
} 