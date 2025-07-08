package com.tymbl.registration.controller;

import com.tymbl.auth.service.JwtService;
import com.tymbl.common.entity.User;
import com.tymbl.common.entity.UserResume;
import com.tymbl.common.service.UserResumeService;
import com.tymbl.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
@Tag(name = "User Resumes", description = "Resume management endpoints")
public class UserResumeController {

    private static final Logger logger = LoggerFactory.getLogger("com.tymbl");
    
    private final UserResumeService userResumeService;
    private final JwtService jwtService;
    private final RegistrationService registrationService;

    @PostMapping("/upload")
    @Operation(
        summary = "Upload user resume",
        description = "Uploads a resume file for the authenticated user. Supports PDF, DOC, DOCX formats. Automatically updates the user's resume field with the download link."
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
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "500", description = "Server error during upload")
    })
    public ResponseEntity<?> uploadResume(
            @Parameter(description = "Resume file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Uploading resume for user: {}", user.getEmail());
            UserResume resume = userResumeService.uploadResume(user.getId(), file);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resume uploaded successfully");
            response.put("resumeId", resume.getId());
            response.put("fileName", resume.getFileName());
            response.put("uuid", resume.getUuid());
            response.put("downloadUrl", userResumeService.getDownloadUrl(resume.getUuid()));
            logger.info("Successfully uploaded resume for user: {}", user.getEmail());
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to upload resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            logger.error("Failed to upload resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/user")
    @Operation(
        summary = "Get all resumes for authenticated user",
        description = "Retrieves all resume files uploaded by the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User resumes retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserResume.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<UserResume>> getUserResumes(
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Retrieving resumes for user: {}", user.getEmail());
            List<UserResume> resumes = userResumeService.getUserResumes(user.getId());
            logger.info("Successfully retrieved {} resumes for user: {}", resumes.size(), user.getEmail());
        return ResponseEntity.ok(resumes);
        } catch (RuntimeException e) {
            logger.error("Failed to get user resumes. Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/latest")
    @Operation(
        summary = "Get latest resume for authenticated user",
        description = "Retrieves the most recently uploaded resume for the authenticated user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Latest resume retrieved successfully",
            content = @Content(
                schema = @Schema(implementation = UserResume.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "No resume found for user")
    })
    public ResponseEntity<UserResume> getLatestResume(
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Retrieving latest resume for user: {}", user.getEmail());
            UserResume resume = userResumeService.getLatestResume(user.getId());
        if (resume == null) {
                logger.info("No resume found for user: {}", user.getEmail());
            return ResponseEntity.notFound().build();
        }
            logger.info("Successfully retrieved latest resume for user: {}", user.getEmail());
        return ResponseEntity.ok(resume);
        } catch (RuntimeException e) {
            logger.error("Failed to get latest resume. Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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
        description = "Updates an existing resume file with a new file. Only the owner of the resume can update it."
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
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User can only update their own resumes"),
        @ApiResponse(responseCode = "404", description = "Resume not found"),
        @ApiResponse(responseCode = "500", description = "Server error during update")
    })
    public ResponseEntity<?> updateResume(
            @Parameter(description = "Resume ID", required = true)
            @PathVariable Long resumeId,
            @Parameter(description = "New resume file", required = true)
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Updating resume {} for user: {}", resumeId, user.getEmail());
            
            // Verify the resume belongs to the authenticated user
            UserResume existingResume = userResumeService.getResumeById(resumeId);
            if (existingResume == null) {
                logger.warn("Resume {} not found", resumeId);
                return ResponseEntity.notFound().build();
            }
            
            if (!existingResume.getUserId().equals(user.getId())) {
                logger.warn("User {} attempted to update resume {} which belongs to user {}", 
                    user.getEmail(), resumeId, existingResume.getUserId());
                Map<String, String> error = new HashMap<>();
                error.put("error", "You can only update your own resumes");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Delete existing resume
            userResumeService.deleteResume(resumeId);
            
            // Upload new resume
            UserResume newResume = userResumeService.uploadResume(user.getId(), file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Resume updated successfully");
            response.put("resumeId", newResume.getId());
            response.put("fileName", newResume.getFileName());
            response.put("uuid", newResume.getUuid());
            response.put("downloadUrl", userResumeService.getDownloadUrl(newResume.getUuid()));
            logger.info("Successfully updated resume {} for user: {}", resumeId, user.getEmail());
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to update resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            logger.error("Failed to update resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping
    @Operation(
        summary = "Delete user's resume",
        description = "Deletes the authenticated user's resume. Since a user can have only one resume, no identifier is needed."
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
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
        @ApiResponse(responseCode = "404", description = "No resume found for user"),
        @ApiResponse(responseCode = "500", description = "Server error during deletion")
    })
    public ResponseEntity<?> deleteResume(
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtService.extractUsername(token.substring(7));
            User user = registrationService.getUserByEmail(email);
            logger.info("Deleting resume for user: {}", user.getEmail());
            
            // Get the user's resume
            UserResume userResume = userResumeService.getLatestResume(user.getId());
            if (userResume == null) {
                logger.warn("No resume found for user: {}", user.getEmail());
            Map<String, String> error = new HashMap<>();
                error.put("error", "No resume found for user");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
            
            // Delete the resume
            userResumeService.deleteResume(userResume.getId());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Resume deleted successfully");
            logger.info("Successfully deleted resume for user: {}", user.getEmail());
            return ResponseEntity.ok().body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to delete resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Failed to delete resume. Error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete resume");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
} 