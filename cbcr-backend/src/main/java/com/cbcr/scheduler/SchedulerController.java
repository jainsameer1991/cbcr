package com.cbcr.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    private final SchedulerService schedulerService;
    private final SystemMetricsCollector metricsCollector;

    @Autowired
    public SchedulerController(SchedulerService schedulerService, SystemMetricsCollector metricsCollector) {
        this.schedulerService = schedulerService;
        this.metricsCollector = metricsCollector;
    }

    @PostMapping("/job")
    public ResponseEntity<String> addJob(@RequestBody BackupJobRequest request) {
        BackupJob job = new BackupJob(
                request.dbInstanceId,
                request.techType,
                request.sizeBytes,
                BackupJob.Priority.valueOf(request.priority.toUpperCase()),
                LocalDateTime.now()
        );
        schedulerService.addJob(job);
        return ResponseEntity.ok("Job added successfully");
    }

    @DeleteMapping("/job")
    public ResponseEntity<BackupJob> removeJob() {
        Optional<BackupJob> job = schedulerService.removeJob();
        return job.map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<BackupJob>> getAllJobs() {
        List<BackupJob> jobs = schedulerService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processQueue() {
        schedulerService.processQueue();
        return ResponseEntity.ok("Queue processing completed");
    }
    
    @GetMapping("/rate-limit-status")
    public ResponseEntity<RateLimiter.RateLimitStatus> getRateLimitStatus() {
        RateLimiter.RateLimitStatus status = schedulerService.getRateLimitStatus();
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<SystemMetricsCollector.SystemMetrics> getSystemMetrics() {
        metricsCollector.updateMetrics();
        SystemMetricsCollector.SystemMetrics metrics = metricsCollector.getCurrentMetrics();
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/queue-size")
    public ResponseEntity<Integer> getQueueSize() {
        int size = schedulerService.getQueueSize();
        return ResponseEntity.ok(size);
    }

    public static class BackupJobRequest {
        public String dbInstanceId;
        public String techType;
        public long sizeBytes;
        public String priority;
    }
} 