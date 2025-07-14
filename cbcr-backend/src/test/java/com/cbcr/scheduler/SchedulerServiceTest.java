package com.cbcr.scheduler;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class SchedulerServiceTest {
    @Test
    void testAddAndRemoveJob() {
        SchedulerService scheduler = new SchedulerService();
        BackupJob job1 = new BackupJob("db1", "MySQL", 1000, BackupJob.Priority.HIGH, LocalDateTime.now());
        BackupJob job2 = new BackupJob("db2", "Cassandra", 2000, BackupJob.Priority.LOW, LocalDateTime.now().plusMinutes(1));
        scheduler.addJob(job2);
        scheduler.addJob(job1);
        assertEquals(2, scheduler.getQueueSize());
        assertEquals(job1, scheduler.removeJob().get()); // High priority first
        assertEquals(job2, scheduler.removeJob().get());
        assertEquals(0, scheduler.getQueueSize());
    }
} 