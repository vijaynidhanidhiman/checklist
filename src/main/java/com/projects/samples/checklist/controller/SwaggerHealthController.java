package com.projects.samples.checklist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
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
@Tag(name = "Health Check", description = "Health check endpoints for monitoring and Kubernetes probes")
public class SwaggerHealthController {

    private final MemoryMXBean memoryMXBean;
    private final OperatingSystemMXBean osMXBean;

    @Value("${info.app.name:checklist}")
    private String appName;

    @Value("${info.app.version:0.0.1-SNAPSHOT}")
    private String appVersion;

    @Value("${info.app.description:Checklist Application}")
    private String appDescription;

    public SwaggerHealthController() {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
    }

    @GetMapping("/live")
    @Operation(
        summary = "Liveness Probe",
        description = "Kubernetes liveness probe endpoint. Returns UP if the application is running."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is alive",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class)))
    })
    public ResponseEntity<HealthResponse> livenessProbe() {
        HealthResponse response = new HealthResponse(
            "UP",
            LocalDateTime.now().toString(),
            "liveness",
            null
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    @Operation(
        summary = "Readiness Probe",
        description = "Kubernetes readiness probe endpoint. Returns UP if the application is ready to serve traffic."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is ready",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class))),
        @ApiResponse(responseCode = "503", description = "Application is not ready",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class)))
    })
    public ResponseEntity<HealthResponse> readinessProbe() {
        Map<String, Object> details = new HashMap<>();
        details.put("heapMemoryUsed", memoryMXBean.getHeapMemoryUsage().getUsed());
        details.put("heapMemoryMax", memoryMXBean.getHeapMemoryUsage().getMax());
        details.put("nonHeapMemoryUsed", memoryMXBean.getNonHeapMemoryUsage().getUsed());
        details.put("availableProcessors", osMXBean.getAvailableProcessors());
        details.put("systemLoadAverage", osMXBean.getSystemLoadAverage());

        HealthResponse response = new HealthResponse(
            "UP",
            LocalDateTime.now().toString(),
            "readiness",
            details
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(
        summary = "Application Information",
        description = "Returns application metadata including name, version, and build information."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application information",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = InfoResponse.class)))
    })
    public ResponseEntity<InfoResponse> applicationInfo() {
        InfoResponse response = new InfoResponse(
            appName,
            appVersion,
            appDescription,
            LocalDateTime.now().toString(),
            System.getProperty("java.version"),
            System.getProperty("java.vendor"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            System.getProperty("os.arch")
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics")
    @Operation(
        summary = "Application Metrics",
        description = "Returns custom application metrics in JSON format including JVM, system, and application metrics."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application metrics",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MetricsResponse.class)))
    })
    public ResponseEntity<MetricsResponse> customMetrics() {
        MetricsResponse response = new MetricsResponse(
            LocalDateTime.now().toString(),
            getJvmMetrics(),
            getSystemMetrics(),
            getApplicationMetrics()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/startup")
    @Operation(
        summary = "Startup Probe",
        description = "Kubernetes startup probe endpoint. Returns UP if the application has started successfully."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application has started",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class)))
    })
    public ResponseEntity<HealthResponse> startupProbe() {
        HealthResponse response = new HealthResponse(
            "UP",
            LocalDateTime.now().toString(),
            "startup",
            Map.of("uptime", ManagementFactory.getRuntimeMXBean().getUptime())
        );
        return ResponseEntity.ok(response);
    }

    // Response DTOs for Swagger documentation
    @Schema(description = "Health check response")
    public static class HealthResponse {
        @Schema(description = "Health status", example = "UP")
        private String status;

        @Schema(description = "Timestamp of the health check", example = "2024-01-15T10:30:00")
        private String timestamp;

        @Schema(description = "Type of health check", example = "liveness")
        private String check;

        @Schema(description = "Additional details")
        private Map<String, Object> details;

        public HealthResponse(String status, String timestamp, String check, Map<String, Object> details) {
            this.status = status;
            this.timestamp = timestamp;
            this.check = check;
            this.details = details;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getCheck() { return check; }
        public void setCheck(String check) { this.check = check; }
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    @Schema(description = "Application information response")
    public static class InfoResponse {
        @Schema(description = "Application name", example = "checklist")
        private String name;

        @Schema(description = "Application version", example = "0.0.1-SNAPSHOT")
        private String version;

        @Schema(description = "Application description", example = "Checklist Application")
        private String description;

        @Schema(description = "Timestamp", example = "2024-01-15T10:30:00")
        private String timestamp;

        @Schema(description = "Java version", example = "17.0.1")
        private String javaVersion;

        @Schema(description = "Java vendor", example = "Eclipse Adoptium")
        private String javaVendor;

        @Schema(description = "OS name", example = "Windows 11")
        private String osName;

        @Schema(description = "OS version", example = "10.0")
        private String osVersion;

        @Schema(description = "OS architecture", example = "amd64")
        private String osArch;

        public InfoResponse(String name, String version, String description, String timestamp,
                           String javaVersion, String javaVendor, String osName, String osVersion, String osArch) {
            this.name = name;
            this.version = version;
            this.description = description;
            this.timestamp = timestamp;
            this.javaVersion = javaVersion;
            this.javaVendor = javaVendor;
            this.osName = osName;
            this.osVersion = osVersion;
            this.osArch = osArch;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
        public String getJavaVendor() { return javaVendor; }
        public void setJavaVendor(String javaVendor) { this.javaVendor = javaVendor; }
        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }
        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
        public String getOsArch() { return osArch; }
        public void setOsArch(String osArch) { this.osArch = osArch; }
    }

    @Schema(description = "Application metrics response")
    public static class MetricsResponse {
        @Schema(description = "Timestamp", example = "2024-01-15T10:30:00")
        private String timestamp;

        @Schema(description = "JVM metrics")
        private Map<String, Object> jvm;

        @Schema(description = "System metrics")
        private Map<String, Object> system;

        @Schema(description = "Application metrics")
        private Map<String, Object> application;

        public MetricsResponse(String timestamp, Map<String, Object> jvm, Map<String, Object> system, Map<String, Object> application) {
            this.timestamp = timestamp;
            this.jvm = jvm;
            this.system = system;
            this.application = application;
        }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getJvm() { return jvm; }
        public void setJvm(Map<String, Object> jvm) { this.jvm = jvm; }
        public Map<String, Object> getSystem() { return system; }
        public void setSystem(Map<String, Object> system) { this.system = system; }
        public Map<String, Object> getApplication() { return application; }
        public void setApplication(Map<String, Object> application) { this.application = application; }
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
        app.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        app.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
        return app;
    }
}