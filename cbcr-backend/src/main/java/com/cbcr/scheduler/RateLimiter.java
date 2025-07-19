package com.cbcr.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RateLimiter {
    
    private final SystemMetricsCollector metricsCollector;
    private final AtomicInteger currentConcurrentJobs = new AtomicInteger(0);
    private final AtomicLong lastBackoffTime = new AtomicLong(0);
    private final ReentrantLock rateLimitLock = new ReentrantLock();
    
    // Default values in case @Value is not processed
    private int maxConcurrentJobs = 5;
    private double cpuThreshold = 80.0;
    private double memoryThreshold = 85.0;
    private long backoffDurationMs = 30000;
    
    @Autowired
    public RateLimiter(SystemMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    // Setter methods for @Value injection
    @Value("${rate.limiter.max.concurrent.jobs:5}")
    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }
    
    @Value("${rate.limiter.cpu.threshold:80.0}")
    public void setCpuThreshold(double cpuThreshold) {
        this.cpuThreshold = cpuThreshold;
    }
    
    @Value("${rate.limiter.memory.threshold:85.0}")
    public void setMemoryThreshold(double memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }
    
    @Value("${rate.limiter.backoff.duration.ms:30000}")
    public void setBackoffDurationMs(long backoffDurationMs) {
        this.backoffDurationMs = backoffDurationMs;
    }
    
    public boolean canExecuteBackup() {
        rateLimitLock.lock();
        try {
            // Check concurrent job limit first
            int currentJobs = currentConcurrentJobs.get();
            if (currentJobs >= maxConcurrentJobs) {
                return false;
            }
            
            // Check if we're in backoff period
            if (isInBackoffPeriod()) {
                return false;
            }
            
            SystemMetricsCollector.SystemMetrics metrics = metricsCollector.getCurrentMetrics();
            
            // If no metrics available, allow execution (fallback behavior)
            if (metrics == null) {
                return true;
            }
            
            // Check system resource constraints
            double cpuUsage = metrics.getCpuUsage();
            double memoryUsage = metrics.getMemoryUsage();
            
            if (cpuUsage > cpuThreshold || memoryUsage > memoryThreshold) {
                startBackoffPeriod();
                return false;
            }
            
            return true;
        } finally {
            rateLimitLock.unlock();
        }
    }
    
    public void jobStarted() {
        currentConcurrentJobs.incrementAndGet();
    }
    
    public void jobCompleted() {
        currentConcurrentJobs.decrementAndGet();
    }
    
    private boolean isInBackoffPeriod() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastBackoff = currentTime - lastBackoffTime.get();
        return timeSinceLastBackoff < backoffDurationMs;
    }
    
    private void startBackoffPeriod() {
        lastBackoffTime.set(System.currentTimeMillis());
    }
    
    public RateLimitStatus getRateLimitStatus() {
        SystemMetricsCollector.SystemMetrics metrics = metricsCollector.getCurrentMetrics();
        double cpuUsage = metrics != null ? metrics.getCpuUsage() : 0.0;
        double memoryUsage = metrics != null ? metrics.getMemoryUsage() : 0.0;
        
        return new RateLimitStatus(
            currentConcurrentJobs.get(),
            maxConcurrentJobs,
            cpuUsage,
            memoryUsage,
            isInBackoffPeriod()
        );
    }
    
    public static class RateLimitStatus {
        private final int currentJobs;
        private final int maxJobs;
        private final double cpuUsage;
        private final double memoryUsage;
        private final boolean inBackoff;
        
        public RateLimitStatus(int currentJobs, int maxJobs, double cpuUsage, 
                             double memoryUsage, boolean inBackoff) {
            this.currentJobs = currentJobs;
            this.maxJobs = maxJobs;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.inBackoff = inBackoff;
        }
        
        // Getters
        public int getCurrentJobs() { return currentJobs; }
        public int getMaxJobs() { return maxJobs; }
        public double getCpuUsage() { return cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public boolean isInBackoff() { return inBackoff; }
    }
} 