package com.tymbl.common.controller;

import com.tymbl.common.entity.UserResume;
import com.tymbl.common.service.UserResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/resumes")
public class UserResumeController {

    @Autowired
    private UserResumeService userResumeService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            UserResume resume = userResumeService.uploadResume(userId, file);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resume uploaded successfully");
            response.put("resumeId", resume.getId());
            response.put("fileName", resume.getFileName());
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserResume>> getUserResumes(@PathVariable Long userId) {
        List<UserResume> resumes = userResumeService.getUserResumes(userId);
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<UserResume> getLatestResume(@PathVariable Long userId) {
        UserResume resume = userResumeService.getLatestResume(userId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resume);
    }

    @GetMapping("/{resumeId}/download")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long resumeId) {
        UserResume resume = userResumeService.getLatestResume(resumeId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(resume.getFileType()));
        headers.setContentDispositionFormData("attachment", resume.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resume.getResumeData());
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<?> deleteResume(@PathVariable Long resumeId) {
        try {
            userResumeService.deleteResume(resumeId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Resume deleted successfully");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 