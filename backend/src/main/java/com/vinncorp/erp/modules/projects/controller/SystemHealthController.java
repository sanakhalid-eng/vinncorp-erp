package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/system-health")
@RequiredArgsConstructor
@Tag(name = "System Health", description = "Platform health checks for super admins")
public class SystemHealthController {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DataSource dataSource;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "System health", description = "Get overall system health status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("timestamp", Instant.now().toString());

        // Database health
        Map<String, Object> dbHealth = checkDatabaseHealth();
        health.put("database", dbHealth);

        // Redis health
        Map<String, Object> redisHealth = checkRedisHealth();
        health.put("redis", redisHealth);

        // Overall status
        boolean dbUp = "UP".equals(dbHealth.get("status"));
        boolean redisUp = "UP".equals(redisHealth.get("status"));
        health.put("status", dbUp && redisUp ? "UP" : "DEGRADED");

        return ResponseEntity.ok(new ApiResponse<>(true, "System health retrieved", health));
    }

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            result.put("status", "UP");
            result.put("database", meta.getDatabaseProductName());
            result.put("version", meta.getDatabaseProductVersion());
            result.put("url", meta.getURL());
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> checkRedisHealth() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (redisTemplate == null) {
            result.put("status", "DISABLED");
            result.put("message", "Redis not configured");
            return result;
        }
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            result.put("status", "UP".equals(pong) ? "UP" : "DOWN");
            result.put("response", pong);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
        }
        return result;
    }
}
