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

    @Autowired
    public SchedulerController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/job")
    public ResponseEntity<String> addJob(@RequestBody BackupJobRequest request) {
        BackupJob job = new BackupJob(
                request.dbInstanceId(),
                request.techType(),
                request.sizeBytes(),
                request.priority(),
                request.scheduledTime() != null ? request.scheduledTime() : LocalDateTime.now()
        );
        schedulerService.addJob(job);
        return ResponseEntity.ok("Job added");
    }

    @DeleteMapping("/job")
    public ResponseEntity<BackupJob> removeJob() {
        Optional<BackupJob> job = schedulerService.removeJob();
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<BackupJob>> getAllJobs() {
        List<BackupJob> jobs = schedulerService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processQueue() {
        schedulerService.processQueue();
        return ResponseEntity.ok("Queue processed");
    }

    // DTO for job creation
    public record BackupJobRequest(
            String dbInstanceId,
            String techType,
            long sizeBytes,
            BackupJob.Priority priority,
            LocalDateTime scheduledTime
    ) {}
} 