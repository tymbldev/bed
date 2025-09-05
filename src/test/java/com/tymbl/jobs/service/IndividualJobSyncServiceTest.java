package com.tymbl.jobs.service;

import com.tymbl.common.entity.Job;
import com.tymbl.jobs.entity.ExternalJobDetail;
import com.tymbl.jobs.repository.ExternalJobDetailRepository;
import com.tymbl.jobs.repository.JobRepository;
import com.tymbl.jobs.service.ExternalJobTagger.TaggingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndividualJobSyncServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ExternalJobDetailRepository externalJobDetailRepository;

    @Mock
    private ExternalJobTagger externalJobTagger;

    @InjectMocks
    private IndividualJobSyncService individualJobSyncService;

    @Test
    void testSyncIndividualJob_WhenDuplicateExists_ShouldUpdateOpeningCount() {
        // Given
        Long externalJobId = 1L;
        ExternalJobDetail externalJob = createExternalJobDetail(externalJobId);
        ExternalJobDetail managedExternalJob = createExternalJobDetail(externalJobId);
        
        Job existingJob = createJob(1L, "Software Engineer", "Tech Corp", "New York", 2);
        List<Job> existingJobs = Arrays.asList(existingJob);
        
        TaggingResult taggingResult = createTaggingResult();

        when(externalJobDetailRepository.findById(externalJobId)).thenReturn(Optional.of(managedExternalJob));
        when(jobRepository.findByPortalJobId(anyString())).thenReturn(Optional.empty());
        when(externalJobTagger.tagExternalJob(managedExternalJob)).thenReturn(taggingResult);
        when(jobRepository.findExistingJobsByDesignationCompanyAndLocation(
                anyString(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(existingJobs);
        when(jobRepository.save(any(Job.class))).thenReturn(existingJob);
        when(externalJobDetailRepository.save(any(ExternalJobDetail.class))).thenReturn(managedExternalJob);

        // When
        IndividualJobSyncService.SyncResult result = individualJobSyncService.syncIndividualJob(externalJob);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Updated opening count for existing job", result.getMessage());
        assertEquals(1L, result.getJobId());
        
        // Verify opening count was incremented
        verify(jobRepository).save(argThat(job -> job.getOpeningCount() == 3));
        verify(externalJobDetailRepository).save(argThat(job -> job.getIsSyncedToJobTable()));
    }

    @Test
    void testSyncIndividualJob_WhenNoDuplicateExists_ShouldCreateNewJob() {
        // Given
        Long externalJobId = 1L;
        ExternalJobDetail externalJob = createExternalJobDetail(externalJobId);
        ExternalJobDetail managedExternalJob = createExternalJobDetail(externalJobId);
        
        Job newJob = createJob(2L, "Software Engineer", "Tech Corp", "New York", 1);
        TaggingResult taggingResult = createTaggingResult();

        when(externalJobDetailRepository.findById(externalJobId)).thenReturn(Optional.of(managedExternalJob));
        when(jobRepository.findByPortalJobId(anyString())).thenReturn(Optional.empty());
        when(externalJobTagger.tagExternalJob(managedExternalJob)).thenReturn(taggingResult);
        when(jobRepository.findExistingJobsByDesignationCompanyAndLocation(
                anyString(), anyString(), anyString(), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList()); // No existing jobs
        when(jobRepository.save(any(Job.class))).thenReturn(newJob);
        when(externalJobDetailRepository.save(any(ExternalJobDetail.class))).thenReturn(managedExternalJob);

        // When
        IndividualJobSyncService.SyncResult result = individualJobSyncService.syncIndividualJob(externalJob);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Job synced successfully", result.getMessage());
        assertEquals(2L, result.getJobId());
        
        // Verify new job was created
        verify(jobRepository).save(any(Job.class));
        verify(externalJobDetailRepository).save(argThat(job -> job.getIsSyncedToJobTable()));
    }

    @Test
    void testSyncIndividualJob_WhenJobAlreadyExists_ShouldSkip() {
        // Given
        Long externalJobId = 1L;
        ExternalJobDetail externalJob = createExternalJobDetail(externalJobId);
        ExternalJobDetail managedExternalJob = createExternalJobDetail(externalJobId);
        
        Job existingJob = createJob(1L, "Software Engineer", "Tech Corp", "New York", 1);

        when(externalJobDetailRepository.findById(externalJobId)).thenReturn(Optional.of(managedExternalJob));
        when(jobRepository.findByPortalJobId(anyString())).thenReturn(Optional.of(existingJob));
        when(externalJobDetailRepository.save(any(ExternalJobDetail.class))).thenReturn(managedExternalJob);

        // When
        IndividualJobSyncService.SyncResult result = individualJobSyncService.syncIndividualJob(externalJob);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Job already exists, marked as synced", result.getMessage());
        
        // Verify no new job was created and external job was marked as synced
        verify(jobRepository, never()).save(any(Job.class));
        verify(externalJobDetailRepository).save(argThat(job -> job.getIsSyncedToJobTable()));
    }

    private ExternalJobDetail createExternalJobDetail(Long id) {
        ExternalJobDetail job = new ExternalJobDetail();
        job.setId(id);
        job.setPortalJobId("PORTAL_" + id);
        job.setJobTitle("Software Engineer");
        job.setCompanyName("Tech Corp");
        job.setCityName("New York");
        job.setIsSyncedToJobTable(false);
        return job;
    }

    private Job createJob(Long id, String title, String company, String city, Integer openingCount) {
        Job job = new Job();
        job.setId(id);
        job.setTitle(title);
        job.setCompany(company);
        job.setCityName(city);
        job.setOpeningCount(openingCount);
        job.setActive(true);
        job.setCreatedAt(LocalDateTime.now().minusDays(10));
        return job;
    }

    private TaggingResult createTaggingResult() {
        TaggingResult result = new TaggingResult();
        result.setCompanyId(1L);
        result.setDesignationId(1L);
        result.setError(null);
        return result;
    }
}
