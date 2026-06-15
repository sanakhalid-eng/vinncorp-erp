package com.vinncorp.erp.modules.projects.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Store attempt counts per IP address
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    // Configuration: 10 attempts per 15 minutes for auth endpoints
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MINUTES = 15;

    public boolean tryConsume(String ipAddress) {
        Instant now = Instant.now();
        AttemptInfo info = attempts.computeIfAbsent(ipAddress, k -> new AttemptInfo());

        // Clean up old attempts outside the window
        if (info.windowStart.plus(WINDOW_MINUTES, ChronoUnit.MINUTES).isBefore(now)) {
            info.count = 0;
            info.windowStart = now;
        }

        if (info.count >= MAX_ATTEMPTS) {
            return false; // Rate limit exceeded
        }

        info.count++;
        return true;
    }

    public long getRemainingAttempts(String ipAddress) {
        AttemptInfo info = attempts.get(ipAddress);
        if (info == null) {
            return MAX_ATTEMPTS;
        }

        Instant now = Instant.now();
        if (info.windowStart.plus(WINDOW_MINUTES, ChronoUnit.MINUTES).isBefore(now)) {
            return MAX_ATTEMPTS; // Window expired
        }

        return Math.max(0, MAX_ATTEMPTS - info.count);
    }

    // Helper class to track attempt info
    private static class AttemptInfo {
        int count = 0;
        Instant windowStart = Instant.now();
    }
}


