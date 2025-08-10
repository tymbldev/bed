package com.tymbl.common.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_dumper")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDumper {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operation_name", nullable = false)
    private String operationName;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(name = "prompt_token_count")
    private Integer promptTokenCount;
    
    @Column(name = "candidates_token_count")
    private Integer candidatesTokenCount;
    
    @Column(name = "total_token_count")
    private Integer totalTokenCount;
    
    @Column(name = "response_id")
    private String responseId;
    
    @Column(name = "finish_reason")
    private String finishReason;
    
    @Column(name = "avg_logprobs")
    private Double avgLogprobs;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
