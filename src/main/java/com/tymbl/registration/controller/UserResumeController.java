package com.tymbl.registration.controller;

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
        description = "Uploads a resume file for a specific user. Supports PDF, DOC, DOCX formats. Automatically updates the user's resume field with the download link."
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
                          "  \"fileName\": \"resume.pdf\",\n" +
                          "  \"uuid\": \"550e8400-e29b-41d4-a716-446655440000\",\n" +
                          "  \"downloadUrl\": \"http://localhost:8080/api/v1/resumes/download/550e8400-e29b-41d4-a716-446655440000\"\n" +
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
            response.put("uuid", resume.getUuid());
            response.put("downloadUrl", userResumeService.getDownloadUrl(resume.getUuid()));
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

    @GetMapping("/{resumeId}")
    @Operation(
        summary = "Get resume by ID",
        description = "Retrieves a specific resume by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resume retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserResume.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Resume not found")
    })
    public ResponseEntity<UserResume> getResumeById(
            @Parameter(description = "Resume ID", required = true)
            @PathVariable Long resumeId) {
        try {
            UserResume resume = userResumeService.getResumeById(resumeId);
            return ResponseEntity.ok(resume);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{uuid}")
    @Operation(
        summary = "Download resume file by UUID",
        description = "Downloads a specific resume file using its UUID. This is the primary download endpoint."
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
    public ResponseEntity<byte[]> downloadResumeByUuid(
            @Parameter(description = "Resume UUID", required = true)
            @PathVariable String uuid) {
        return userResumeService.getResumeByUuid(uuid)
            .map(resume -> {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(resume.getFileType()));
                headers.setContentDispositionFormData("attachment", resume.getFileName());
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resume.getResumeData());
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{resumeId}/download")
    @Operation(
        summary = "Download resume file by ID (Legacy)",
        description = "Downloads a specific resume file by its ID. This is a legacy endpoint for backward compatibility."
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
        try {
            UserResume resume = userResumeService.getResumeById(resumeId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(resume.getFileType()));
            headers.setContentDispositionFormData("attachment", resume.getFileName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resume.getResumeData());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{resumeId}")
    @Operation(
        summary = "Update resume file",
        description = "Updates an existing resume file with a new file."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resume updated successfully",
            content = @Content(
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = "{\n" +
                          "  \"message\": \"Resume updated successfully\",\n" +
                          "  \"resumeId\": 1,\n" +
                          "  \"fileName\": \"updated-resume.pdf\",\n" +
                          "  \"uuid\": \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                          "}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "404", description = "Resume not found"),
        @ApiResponse(responseCode = "500", description = "Server error during update")
    })
    public ResponseEntity<?> updateResume(
            @Parameter(description = "Resume ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "New resume file", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            // Delete existing resume
            userResumeService.deleteResume(resumeId);
            
            // Get the user ID from the deleted resume
            UserResume deletedResume = userResumeService.getResumeById(resumeId);
            Long userId = deletedResume.getUserId();
            
            // Upload new resume
            UserResume newResume = userResumeService.uploadResume(userId, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resume updated successfully");
            response.put("resumeId", newResume.getId());
            response.put("fileName", newResume.getFileName());
            response.put("uuid", newResume.getUuid());
            response.put("downloadUrl", userResumeService.getDownloadUrl(newResume.getUuid()));
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{resumeId}")
    @Operation(
        summary = "Delete resume by ID",
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
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/uuid/{uuid}")
    @Operation(
        summary = "Delete resume by UUID",
        description = "Deletes a specific resume file using its UUID."
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
    public ResponseEntity<?> deleteResumeByUuid(
            @Parameter(description = "Resume UUID", required = true)
            @PathVariable String uuid) {
        try {
            userResumeService.deleteResumeByUuid(uuid);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Resume deleted successfully");
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 