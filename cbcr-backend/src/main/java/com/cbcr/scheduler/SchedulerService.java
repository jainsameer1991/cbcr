package com.cbcr.scheduler;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

@Service
public class SchedulerService {
    private final Queue<BackupJob> jobQueue = new PriorityQueue<>();
    private final RateLimiter rateLimiter;
    private final ExecutorService executorService;
    
    @Autowired
    public SchedulerService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        // Use virtual threads for efficient monitoring (Java 21 feature)
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }
    
    public void addJob(BackupJob job) {
        jobQueue.offer(job);
    }
    
    public Optional<BackupJob> removeJob() {
        return Optional.ofNullable(jobQueue.poll());
    }
    
    public int getQueueSize() {
        return jobQueue.size();
    }
    
    public List<BackupJob> getAllJobs() {
        return new ArrayList<>(jobQueue);
    }
    
    public void processQueue() {
        while (!jobQueue.isEmpty()) {
            BackupJob job = jobQueue.poll();
            
            // Check rate limiting before execution
            if (rateLimiter.canExecuteBackup()) {
                executeBackupJobWithRateLimiting(job);
            } else {
                // Re-queue the job with lower priority if rate limited
                job = new BackupJob(
                    job.getDbInstanceId(),
                    job.getTechType(),
                    job.getSizeBytes(),
                    BackupJob.Priority.LOW, // Lower priority when rate limited
                    job.getScheduledTime()
                );
                jobQueue.offer(job);
                break; // Stop processing to avoid infinite loop
            }
        }
    }
    
    private void executeBackupJobWithRateLimiting(BackupJob job) {
        if (rateLimiter.canExecuteBackup()) {
            rateLimiter.jobStarted();
            try {
                executeBackupJob(job);
            } finally {
                rateLimiter.jobCompleted();
            }
        } else {
            // Re-add job to queue if rate limited
            jobQueue.offer(job);
        }
    }
    
    private void executeBackupJob(BackupJob job) {
        // Simulate backup execution
        System.out.println("Executing backup job: " + job);
        try {
            Thread.sleep(1000); // Simulate backup time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Completed backup job: " + job.getDbInstanceId());
    }
    
    public RateLimiter.RateLimitStatus getRateLimitStatus() {
        return rateLimiter.getRateLimitStatus();
    }
} 