package com.cbcr.scheduler;

import java.time.LocalDateTime;

public class BackupJob implements Comparable<BackupJob> {
    public enum Priority { HIGH, MEDIUM, LOW }
    private final String dbInstanceId;
    private final String techType;
    private final long sizeBytes;
    private final Priority priority;
    private final LocalDateTime scheduledTime;

    public BackupJob(String dbInstanceId, String techType, long sizeBytes, Priority priority, LocalDateTime scheduledTime) {
        this.dbInstanceId = dbInstanceId;
        this.techType = techType;
        this.sizeBytes = sizeBytes;
        this.priority = priority;
        this.scheduledTime = scheduledTime;
    }

    public String getDbInstanceId() { return dbInstanceId; }
    public String getTechType() { return techType; }
    public long getSizeBytes() { return sizeBytes; }
    public Priority getPriority() { return priority; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }

    @Override
    public int compareTo(BackupJob other) {
        // Lower ordinal = higher priority (HIGH=0, LOW=2)
        int cmp = Integer.compare(this.priority.ordinal(), other.priority.ordinal());
        if (cmp == 0) {
            return this.scheduledTime.compareTo(other.scheduledTime);
        }
        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BackupJob job = (BackupJob) o;
        return sizeBytes == job.sizeBytes &&
                dbInstanceId.equals(job.dbInstanceId) &&
                techType.equals(job.techType) &&
                priority == job.priority &&
                scheduledTime.equals(job.scheduledTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dbInstanceId, techType, sizeBytes, priority, scheduledTime);
    }

    @Override
    public String toString() {
        return String.format("BackupJob{dbInstanceId='%s', techType='%s', sizeBytes=%d, priority=%s, scheduledTime=%s}",
                dbInstanceId, techType, sizeBytes, priority, scheduledTime);
    }
} 