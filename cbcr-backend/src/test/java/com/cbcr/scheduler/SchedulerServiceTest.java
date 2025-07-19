package com.cbcr.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerServiceTest {
    
    @Mock
    private RateLimiter rateLimiter;
    
    @Test
    void testAddAndRemoveJob() {
        when(rateLimiter.canExecuteBackup()).thenReturn(true);
        SchedulerService scheduler = new SchedulerService(rateLimiter);
        BackupJob job1 = new BackupJob("db1", "MySQL", 1000, BackupJob.Priority.HIGH, LocalDateTime.now());
        BackupJob job2 = new BackupJob("db2", "Cassandra", 2000, BackupJob.Priority.LOW, LocalDateTime.now().plusMinutes(1));
        
        scheduler.addJob(job2);
        scheduler.addJob(job1);
        assertEquals(2, scheduler.getQueueSize());
        
        // Process jobs
        scheduler.processQueue();
        
        // Verify rate limiter was called - each job calls canExecuteBackup twice (once in processQueue, once in executeBackupJobWithRateLimiting)
        verify(rateLimiter, times(4)).canExecuteBackup();
        verify(rateLimiter, times(2)).jobStarted();
        verify(rateLimiter, times(2)).jobCompleted();
    }
    
    @Test
    void testProcessQueueWithRateLimiting() {
        SchedulerService scheduler = new SchedulerService(rateLimiter);
        BackupJob job = new BackupJob("db1", "MySQL", 1000, BackupJob.Priority.HIGH, LocalDateTime.now());
        
        when(rateLimiter.canExecuteBackup()).thenReturn(true);
        
        scheduler.addJob(job);
        scheduler.processQueue();
        
        // Each job calls canExecuteBackup twice (once in processQueue, once in executeBackupJobWithRateLimiting)
        verify(rateLimiter, times(2)).canExecuteBackup();
        verify(rateLimiter, times(1)).jobStarted();
        verify(rateLimiter, times(1)).jobCompleted();
    }
} 