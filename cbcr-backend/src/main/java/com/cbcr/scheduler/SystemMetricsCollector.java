package com.cbcr.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SystemMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final AtomicReference<SystemMetrics> currentMetrics = new AtomicReference<>();
    
    @Autowired
    public SystemMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
        // Initialize with default metrics
        updateMetrics();
    }
    
    private void initializeMetrics() {
        // Register system metrics gauges
        Gauge.builder("system.cpu.usage", this, obj -> obj.getCpuUsage())
             .description("Current CPU usage percentage")
             .register(meterRegistry);
             
        Gauge.builder("system.memory.usage", this, obj -> obj.getMemoryUsage())
             .description("Current memory usage percentage")
             .register(meterRegistry);
             
        Gauge.builder("system.memory.available", this, obj -> obj.getAvailableMemory())
             .description("Available memory in bytes")
             .register(meterRegistry);
    }
    
    public SystemMetrics getCurrentMetrics() {
        SystemMetrics metrics = currentMetrics.get();
        if (metrics == null) {
            // Return default metrics if none available
            updateMetrics();
            metrics = currentMetrics.get();
        }
        return metrics;
    }
    
    public void updateMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        double cpuUsage = getCpuUsage();
        double memoryUsage = getMemoryUsage();
        long availableMemory = getAvailableMemory();
        int availableProcessors = osBean.getAvailableProcessors();
        
        SystemMetrics metrics = new SystemMetrics(
            cpuUsage,
            memoryUsage,
            availableMemory,
            availableProcessors,
            System.currentTimeMillis()
        );
        
        currentMetrics.set(metrics);
    }
    
    private double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemLoadAverage() * 100.0;
    }
    
    private double getMemoryUsage() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        return (double) usedMemory / maxMemory * 100.0;
    }
    
    private long getAvailableMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getMax() - memoryBean.getHeapMemoryUsage().getUsed();
    }
    
    public static class SystemMetrics {
        private final double cpuUsage;
        private final double memoryUsage;
        private final long availableMemory;
        private final int availableProcessors;
        private final long timestamp;
        
        public SystemMetrics(double cpuUsage, double memoryUsage, long availableMemory, 
                           int availableProcessors, long timestamp) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.availableMemory = availableMemory;
            this.availableProcessors = availableProcessors;
            this.timestamp = timestamp;
        }
        
        // Getters
        public double getCpuUsage() { return cpuUsage; }
        public double getMemoryUsage() { return memoryUsage; }
        public long getAvailableMemory() { return availableMemory; }
        public int getAvailableProcessors() { return availableProcessors; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("SystemMetrics{cpu=%.2f%%, memory=%.2f%%, available=%dMB, processors=%d}",
                cpuUsage, memoryUsage, availableMemory / (1024 * 1024), availableProcessors);
        }
    }
} 