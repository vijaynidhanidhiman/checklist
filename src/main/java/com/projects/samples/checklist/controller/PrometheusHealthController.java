package com.projects.samples.checklist.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class PrometheusHealthController {

    private final MeterRegistry meterRegistry;
    private final Counter healthCheckCounter;
    private final MemoryMXBean memoryMXBean;
    private final OperatingSystemMXBean osMXBean;

    @Autowired
    public PrometheusHealthController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.healthCheckCounter = Counter.builder("health.check.total")
                .description("Total number of health checks performed")
                .register(meterRegistry);
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> livenessProbe() {
        healthCheckCounter.increment();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("check", "liveness");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readinessProbe() {
        healthCheckCounter.increment();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("check", "readiness");
        response.put("details", getSystemDetails());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> customMetrics() {
        healthCheckCounter.increment();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now().toString());
        metrics.put("jvm", getJvmMetrics());
        metrics.put("system", getSystemMetrics());
        metrics.put("application", getApplicationMetrics());
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/prometheus")
    public ResponseEntity<String> prometheusMetrics() {
        healthCheckCounter.increment();
        
        StringBuilder prometheusOutput = new StringBuilder();
        
        // JVM Memory Metrics
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryMXBean.getHeapMemoryUsage().getCommitted();
        long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax();
        long nonHeapCommitted = memoryMXBean.getNonHeapMemoryUsage().getCommitted();
        
        prometheusOutput.append("# HELP jvm_memory_heap_used_bytes Used heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_heap_used_bytes gauge\n");
        prometheusOutput.append("jvm_memory_heap_used_bytes ").append(heapUsed).append("\n");
        
        prometheusOutput.append("# HELP jvm_memory_heap_max_bytes Max heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_heap_max_bytes gauge\n");
        prometheusOutput.append("jvm_memory_heap_max_bytes ").append(heapMax).append("\n");
        
        prometheusOutput.append("# HELP jvm_memory_heap_committed_bytes Committed heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_heap_committed_bytes gauge\n");
        prometheusOutput.append("jvm_memory_heap_committed_bytes ").append(heapCommitted).append("\n");
        
        prometheusOutput.append("# HELP jvm_memory_nonheap_used_bytes Used non-heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_nonheap_used_bytes gauge\n");
        prometheusOutput.append("jvm_memory_nonheap_used_bytes ").append(nonHeapUsed).append("\n");
        
        prometheusOutput.append("# HELP jvm_memory_nonheap_max_bytes Max non-heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_nonheap_max_bytes gauge\n");
        prometheusOutput.append("jvm_memory_nonheap_max_bytes ").append(nonHeapMax).append("\n");
        
        prometheusOutput.append("# HELP jvm_memory_nonheap_committed_bytes Committed non-heap memory in bytes\n");
        prometheusOutput.append("# TYPE jvm_memory_nonheap_committed_bytes gauge\n");
        prometheusOutput.append("jvm_memory_nonheap_committed_bytes ").append(nonHeapCommitted).append("\n");
        
        // System Metrics
        double systemLoadAverage = osMXBean.getSystemLoadAverage();
        int availableProcessors = osMXBean.getAvailableProcessors();
        
        prometheusOutput.append("# HELP system_load_average_1m System load average for 1 minute\n");
        prometheusOutput.append("# TYPE system_load_average_1m gauge\n");
        prometheusOutput.append("system_load_average_1m ").append(systemLoadAverage).append("\n");
        
        prometheusOutput.append("# HELP system_cpu_count Number of available processors\n");
        prometheusOutput.append("# TYPE system_cpu_count gauge\n");
        prometheusOutput.append("system_cpu_count ").append(availableProcessors).append("\n");
        
        // Health Check Counter
        prometheusOutput.append("# HELP health_check_total Total number of health checks performed\n");
        prometheusOutput.append("# TYPE health_check_total counter\n");
        prometheusOutput.append("health_check_total ").append((long) healthCheckCounter.count()).append("\n");
        
        // Application Info
        prometheusOutput.append("# HELP application_info Application information\n");
        prometheusOutput.append("# TYPE application_info gauge\n");
        prometheusOutput.append("application_info{version=\"0.0.1-SNAPSHOT\",name=\"checklist\"} 1\n");
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
                .body(prometheusOutput.toString());
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> applicationInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "checklist");
        info.put("version", "0.0.1-SNAPSHOT");
        info.put("description", "Checklist Application with Prometheus Health Monitoring");
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("java.version", System.getProperty("java.version"));
        info.put("java.vendor", System.getProperty("java.vendor"));
        info.put("os.name", System.getProperty("os.name"));
        info.put("os.version", System.getProperty("os.version"));
        info.put("os.arch", System.getProperty("os.arch"));
        
        return ResponseEntity.ok(info);
    }

    private Map<String, Object> getSystemDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("heapMemoryUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        details.put("heapMemoryMax", memoryMXBean.getHeapMemoryUsage().getMax());
        details.put("nonHeapMemoryUsed", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        details.put("availableProcessors", osMXBean.getAvailableProcessors());
        details.put("systemLoadAverage", osMXBean.getSystemLoadAverage());
        return details;
    }

    private Map<String, Object> getJvmMetrics() {
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("heapUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        jvm.put("heapMax", memoryMXBean.getHeapMemoryUsage().getMax());
        jvm.put("heapCommitted", memoryMXBean.getHeapMemoryUsage().getCommitted());
        jvm.put("nonHeapUsed", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        jvm.put("nonHeapMax", memoryMXBean.getNonHeapMemoryUsage().getMax());
        jvm.put("nonHeapCommitted", memoryMXBean.getNonHeapMemoryUsage().getCommitted());
        jvm.put("threads", Thread.activeCount());
        jvm.put("classesLoaded", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
        return jvm;
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> system = new HashMap<>();
        system.put("loadAverage", osMXBean.getSystemLoadAverage());
        system.put("availableProcessors", osMXBean.getAvailableProcessors());
        system.put("freeMemory", Runtime.getRuntime().freeMemory());
        system.put("totalMemory", Runtime.getRuntime().totalMemory());
        system.put("maxMemory", Runtime.getRuntime().maxMemory());
        return system;
    }

    private Map<String, Object> getApplicationMetrics() {
        Map<String, Object> app = new HashMap<>();
        app.put("healthChecksTotal", (long) healthCheckCounter.count());
        app.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        return app;
    }
}