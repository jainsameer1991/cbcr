package com.cbcr.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.micrometer.core.instrument.MeterRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

    @Mock
    private SystemMetricsCollector metricsCollector;
    
    @Mock
    private MeterRegistry meterRegistry;
    
    private RateLimiter rateLimiter;
    private SystemMetricsCollector.SystemMetrics normalMetrics;
    private SystemMetricsCollector.SystemMetrics constrainedMetrics;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter(metricsCollector);
        normalMetrics = new SystemMetricsCollector.SystemMetrics(30.0, 50.0, 1024 * 1024 * 1024L, 4, System.currentTimeMillis());
        constrainedMetrics = new SystemMetricsCollector.SystemMetrics(90.0, 95.0, 50 * 1024 * 1024L, 4, System.currentTimeMillis());
    }
    
    @Test
    void testCanExecuteBackup_WhenResourcesNormal_ShouldReturnTrue() {
        when(metricsCollector.getCurrentMetrics()).thenReturn(normalMetrics);
        
        boolean result = rateLimiter.canExecuteBackup();
        
        assertTrue(result);
        verify(metricsCollector).getCurrentMetrics();
    }
    
    @Test
    void testCanExecuteBackup_WhenCpuHigh_ShouldReturnFalse() {
        SystemMetricsCollector.SystemMetrics highCpuMetrics = new SystemMetricsCollector.SystemMetrics(90.0, 50.0, 1024 * 1024 * 1024L, 4, System.currentTimeMillis());
        when(metricsCollector.getCurrentMetrics()).thenReturn(highCpuMetrics);
        
        boolean result = rateLimiter.canExecuteBackup();
        
        assertFalse(result);
        verify(metricsCollector).getCurrentMetrics();
    }
    
    @Test
    void testCanExecuteBackup_WhenMemoryHigh_ShouldReturnFalse() {
        SystemMetricsCollector.SystemMetrics highMemoryMetrics = new SystemMetricsCollector.SystemMetrics(50.0, 90.0, 1024 * 1024 * 1024L, 4, System.currentTimeMillis());
        when(metricsCollector.getCurrentMetrics()).thenReturn(highMemoryMetrics);
        
        boolean result = rateLimiter.canExecuteBackup();
        
        assertFalse(result);
        verify(metricsCollector).getCurrentMetrics();
    }
    
    @Test
    void testJobStartedAndCompleted_ShouldTrackConcurrentJobs() {
        when(metricsCollector.getCurrentMetrics()).thenReturn(normalMetrics);
        
        assertTrue(rateLimiter.canExecuteBackup());
        rateLimiter.jobStarted();
        
        // Should still be able to execute more jobs
        assertTrue(rateLimiter.canExecuteBackup());
        
        rateLimiter.jobCompleted();
        
        // Should still be able to execute jobs
        assertTrue(rateLimiter.canExecuteBackup());
    }
    
    @Test
    void testMaxConcurrentJobs_ShouldLimitExecution() {
        when(metricsCollector.getCurrentMetrics()).thenReturn(normalMetrics);
        
        // Start max jobs
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.canExecuteBackup());
            rateLimiter.jobStarted();
        }
        
        // Should not be able to start more
        assertFalse(rateLimiter.canExecuteBackup());
        
        // Complete one job
        rateLimiter.jobCompleted();
        
        // Should be able to start one more
        assertTrue(rateLimiter.canExecuteBackup());
    }
    
    @Test
    void testCanExecuteBackup_WhenMetricsNull_ShouldAllowExecution() {
        when(metricsCollector.getCurrentMetrics()).thenReturn(null);
        
        boolean result = rateLimiter.canExecuteBackup();
        
        assertTrue(result);
        verify(metricsCollector).getCurrentMetrics();
    }
} 