package com.cbcr.scheduler;

import org.springframework.stereotype.Service;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class SchedulerService {
    private final Queue<BackupJob> jobQueue = new PriorityQueue<>();

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
            executeBackupJob(job);
        }
    }

    private void executeBackupJob(BackupJob job) {
        // TODO: Implement backup execution logic
        System.out.println("Executing: " + job);
    }
} 