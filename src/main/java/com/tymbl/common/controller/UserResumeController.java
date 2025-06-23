package com.tymbl.common.controller;

import com.tymbl.common.entity.UserResume;
import com.tymbl.common.service.UserResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Resumes", description = "Resume management endpoints")
public class UserResumeController {

    @Autowired
    private UserResumeService userResumeService;

    @PostMapping("/upload")
    @Operation(
        summary = "Upload user resume",
        description = "Uploads a resume file for a specific user. Supports various file formats like PDF, DOC, DOCX."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resume uploaded successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Resume uploaded successfully\",\n" +
                          "  \"resumeId\": 1,\n" +
                          "  \"fileName\": \"resume.pdf\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file or user ID"),
        @ApiResponse(responseCode = "500", description = "Server error during upload")
    })
    public ResponseEntity<?> uploadResume(
            @Parameter(description = "Resume file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "User ID", required = true)
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
    @Operation(
        summary = "Get all resumes for a user",
        description = "Retrieves all resume files uploaded by a specific user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User resumes retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserResume.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<UserResume>> getUserResumes(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        List<UserResume> resumes = userResumeService.getUserResumes(userId);
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/user/{userId}/latest")
    @Operation(
        summary = "Get latest resume for a user",
        description = "Retrieves the most recently uploaded resume for a specific user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Latest resume retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserResume.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No resume found for user")
    })
    public ResponseEntity<UserResume> getLatestResume(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        UserResume resume = userResumeService.getLatestResume(userId);
        if (resume == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resume);
    }

    @GetMapping("/{resumeId}/download")
    @Operation(
        summary = "Download resume file",
        description = "Downloads a specific resume file by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resume file downloaded successfully",
            content = @Content(
                mediaType = "application/octet-stream"
            )
        ),
        @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    public ResponseEntity<byte[]> downloadResume(
            @Parameter(description = "Resume ID", required = true)
            @PathVariable Long resumeId) {
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
    @Operation(
        summary = "Delete resume",
        description = "Deletes a specific resume file by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resume deleted successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Resume deleted successfully\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Resume not found"),
        @ApiResponse(responseCode = "500", description = "Server error during deletion")
    })
    public ResponseEntity<?> deleteResume(
            @Parameter(description = "Resume ID", required = true)
            @PathVariable Long resumeId) {
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