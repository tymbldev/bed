package com.tymbl.jobs.service;

import com.tymbl.jobs.entity.ExternalJobDetailsFromCompanyPortal;
import com.tymbl.jobs.repository.ExternalJobDetailsFromCompanyPortalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerServiceFallbackTest {

    @Mock
    private ExternalJobDetailsFromCompanyPortalRepository crawledContentRepository;

    @InjectMocks
    private WebCrawlerService webCrawlerService;

    @Test
    void testCrawlJobUrl_WhenWebDriverIsNull_ShouldUseFallback() {
        // Given
        Long externalJobDetailId = 1L;
        String redirectUrl = "https://example.com/job";
        
        // Mock repository to return no existing crawl
        when(crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS"))
                .thenReturn(Optional.empty());
        when(crawledContentRepository.save(any(ExternalJobDetailsFromCompanyPortal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - WebDriver is null (injected as null)
        ExternalJobDetailsFromCompanyPortal result = webCrawlerService.crawlJobUrl(externalJobDetailId, redirectUrl);

        // Then
        assertNotNull(result);
        assertEquals(externalJobDetailId, result.getExternalJobDetailId());
        assertEquals(redirectUrl, result.getRedirectUrl());
        
        // Should attempt to save the record (either success or failure)
        verify(crawledContentRepository).save(any(ExternalJobDetailsFromCompanyPortal.class));
    }

    @Test
    void testCrawlJobUrl_WhenAlreadyCrawled_ShouldReturnExisting() {
        // Given
        Long externalJobDetailId = 1L;
        String redirectUrl = "https://example.com/job";
        
        ExternalJobDetailsFromCompanyPortal existingCrawl = ExternalJobDetailsFromCompanyPortal.builder()
                .id(1L)
                .externalJobDetailId(externalJobDetailId)
                .redirectUrl(redirectUrl)
                .crawlStatus("SUCCESS")
                .build();

        when(crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS"))
                .thenReturn(Optional.of(existingCrawl));

        // When
        ExternalJobDetailsFromCompanyPortal result = webCrawlerService.crawlJobUrl(externalJobDetailId, redirectUrl);

        // Then
        assertNotNull(result);
        assertEquals(existingCrawl.getId(), result.getId());
        assertEquals("SUCCESS", result.getCrawlStatus());
        
        // Should not save anything new
        verify(crawledContentRepository, never()).save(any(ExternalJobDetailsFromCompanyPortal.class));
    }

    @Test
    void testIsContentCrawled_WhenContentExists_ReturnsTrue() {
        // Given
        Long externalJobDetailId = 1L;
        ExternalJobDetailsFromCompanyPortal existingCrawl = ExternalJobDetailsFromCompanyPortal.builder()
                .id(1L)
                .externalJobDetailId(externalJobDetailId)
                .crawlStatus("SUCCESS")
                .build();

        when(crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS"))
                .thenReturn(Optional.of(existingCrawl));

        // When
        boolean result = webCrawlerService.isContentCrawled(externalJobDetailId);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsContentCrawled_WhenContentDoesNotExist_ReturnsFalse() {
        // Given
        Long externalJobDetailId = 1L;

        when(crawledContentRepository.findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS"))
                .thenReturn(Optional.empty());

        // When
        boolean result = webCrawlerService.isContentCrawled(externalJobDetailId);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCrawlStatistics() {
        // Given
        when(crawledContentRepository.count()).thenReturn(100L);
        when(crawledContentRepository.countByCrawlStatus("SUCCESS")).thenReturn(80L);
        when(crawledContentRepository.countByCrawlStatus("FAILED")).thenReturn(15L);
        when(crawledContentRepository.countByCrawlStatus("PENDING")).thenReturn(5L);

        // When
        WebCrawlerService.CrawlStatistics statistics = webCrawlerService.getCrawlStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(100L, statistics.getTotalCrawls());
        assertEquals(80L, statistics.getSuccessfulCrawls());
        assertEquals(15L, statistics.getFailedCrawls());
        assertEquals(5L, statistics.getPendingCrawls());
        assertEquals(80.0, statistics.getSuccessRate(), 0.1);
    }
}

