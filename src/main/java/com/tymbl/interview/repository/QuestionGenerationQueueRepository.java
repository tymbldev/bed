package com.tymbl.interview.repository;

import com.tymbl.interview.entity.QuestionGenerationQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionGenerationQueueRepository extends JpaRepository<QuestionGenerationQueue, Long> {
    
    List<QuestionGenerationQueue> findByStatus(QuestionGenerationQueue.Status status);
    
    List<QuestionGenerationQueue> findByRequestTypeAndStatus(QuestionGenerationQueue.RequestType requestType, QuestionGenerationQueue.Status status);
    
    List<QuestionGenerationQueue> findByDesignationAndStatus(String designation, QuestionGenerationQueue.Status status);
    
    @Query("SELECT q FROM QuestionGenerationQueue q WHERE q.status = 'PENDING' ORDER BY q.createdAt ASC")
    List<QuestionGenerationQueue> findPendingRequests();
    
    @Query("SELECT q FROM QuestionGenerationQueue q WHERE q.status = 'IN_PROGRESS' ORDER BY q.updatedAt ASC")
    List<QuestionGenerationQueue> findInProgressRequests();
} 