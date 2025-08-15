package com.tymbl.common.service;

import com.tymbl.common.entity.UserResume;
import com.tymbl.common.repository.UserRepository;
import com.tymbl.common.repository.UserResumeRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserResumeService {

  @Autowired
  private UserResumeRepository userResumeRepository;

  @Autowired
  private UserRepository userRepository;

  @Value("${app.base-url:http://localhost:8085}")
  private String baseUrl;

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Transactional
  public UserResume uploadResume(Long userId, MultipartFile file) throws IOException {
    // Validate file type
    String contentType = file.getContentType();
    if (contentType == null || (!contentType.equals("application/pdf") &&
        !contentType.equals("application/msword") &&
        !contentType.equals(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
      throw new IllegalArgumentException(
          "Invalid file type. Only PDF, DOC, and DOCX files are allowed.");
    }

    // Validate file size (5MB)
    if (file.getSize() > 5 * 1024 * 1024) {
      throw new IllegalArgumentException("File size exceeds 5MB limit.");
    }

    // Check if user exists
    com.tymbl.common.entity.User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    UserResume resume = new UserResume();
    resume.setUserId(userId);
    resume.setFileName(file.getOriginalFilename());
    resume.setFileType(contentType);
    resume.setContentType(contentType);
    resume.setFileSize(file.getSize());
    resume.setResumeData(file.getBytes());

    resume = userResumeRepository.save(resume);

    // Update user's resume field with download link and content type
    String downloadUrl = baseUrl + contextPath + "/api/v1/resumes/download/" + resume.getUuid();
    user.setResume(downloadUrl);
    user.setResumeContentType(contentType);
    userRepository.save(user);

    return resume;
  }

  public List<UserResume> getUserResumes(Long userId) {
    return userResumeRepository.findByUserId(userId);
  }

  public UserResume getLatestResume(Long userId) {
    return userResumeRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
  }

  public Optional<UserResume> getResumeByUuid(String uuid) {
    return userResumeRepository.findByUuid(uuid);
  }

  public UserResume getResumeById(Long resumeId) {
    return userResumeRepository.findById(resumeId)
        .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
  }

  @Transactional
  public void deleteResume(Long resumeId) {
    UserResume resume = getResumeById(resumeId);

    // If this is the user's current resume, clear the User.resume field
    com.tymbl.common.entity.User user = userRepository.findById(resume.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getResume() != null && user.getResume().contains(resume.getUuid())) {
      user.setResume(null);
      user.setResumeContentType(null);
      userRepository.save(user);
    }

    userResumeRepository.deleteById(resumeId);
  }

  @Transactional
  public void deleteResumeByUuid(String uuid) {
    UserResume resume = getResumeByUuid(uuid)
        .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
    deleteResume(resume.getId());
  }

  public String getDownloadUrl(String uuid) {
    return baseUrl + contextPath + "/api/v1/resumes/download/" + uuid;
  }
} 