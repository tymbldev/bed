package com.tymbl.common.service;

import com.tymbl.common.entity.UserResume;
import com.tymbl.common.repository.UserResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UserResumeService {

    @Autowired
    private UserResumeRepository userResumeRepository;

    public UserResume uploadResume(Long userId, MultipartFile file) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && 
            !contentType.equals("text/plain") && 
            !contentType.equals("application/msword"))) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, TXT, and DOC files are allowed.");
        }

        // Validate file size (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit.");
        }

        UserResume resume = new UserResume();
        resume.setUserId(userId);
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(contentType);
        resume.setFileSize(file.getSize());
        resume.setResumeData(file.getBytes());

        return userResumeRepository.save(resume);
    }

    public List<UserResume> getUserResumes(Long userId) {
        return userResumeRepository.findByUserId(userId);
    }

    public UserResume getLatestResume(Long userId) {
        return userResumeRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);
    }

    public void deleteResume(Long resumeId) {
        userResumeRepository.deleteById(resumeId);
    }
} 