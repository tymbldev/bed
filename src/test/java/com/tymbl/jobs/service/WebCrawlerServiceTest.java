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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebCrawlerServiceTest {

    @Mock
    private ExternalJobDetailsFromCompanyPortalRepository crawledContentRepository;

    @InjectMocks
    private WebCrawlerService webCrawlerService;

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
        verify(crawledContentRepository).findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS");
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
        verify(crawledContentRepository).findByExternalJobDetailIdAndCrawlStatus(externalJobDetailId, "SUCCESS");
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

    @Test
    void testGetCrawlStatistics_WhenNoCrawls_ReturnsZeroSuccessRate() {
        // Given
        when(crawledContentRepository.count()).thenReturn(0L);
        when(crawledContentRepository.countByCrawlStatus("SUCCESS")).thenReturn(0L);
        when(crawledContentRepository.countByCrawlStatus("FAILED")).thenReturn(0L);
        when(crawledContentRepository.countByCrawlStatus("PENDING")).thenReturn(0L);

        // When
        WebCrawlerService.CrawlStatistics statistics = webCrawlerService.getCrawlStatistics();

        // Then
        assertNotNull(statistics);
        assertEquals(0L, statistics.getTotalCrawls());
        assertEquals(0.0, statistics.getSuccessRate(), 0.1);
    }
}
