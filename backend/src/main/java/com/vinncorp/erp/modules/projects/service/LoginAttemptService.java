package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.shared.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptService {

    private final CacheService cacheService;

    @Value("${api.rate-limit.login-attempts:5}")
    private int maxAttempts;

    @Value("${api.rate-limit.login-lockout-minutes:15}")
    private int lockoutMinutes;

    public void recordFailedAttempt(String identifier) {
        String key = "login_attempts:" + identifier.toLowerCase();
        Long attempts = cacheService.get(key, Long.class).orElse(0L);
        attempts++;
        cacheService.put(key, attempts, lockoutMinutes * 60000L);
        log.warn("Failed login attempt {} for identifier={}", attempts, identifier);
    }

    public void recordSuccessfulLogin(String identifier) {
        String key = "login_attempts:" + identifier.toLowerCase();
        cacheService.evict(key);
    }

    public boolean isLockedOut(String identifier) {
        String key = "login_attempts:" + identifier.toLowerCase();
        Long attempts = cacheService.get(key, Long.class).orElse(0L);
        return attempts >= maxAttempts;
    }

    public int getRemainingAttempts(String identifier) {
        String key = "login_attempts:" + identifier.toLowerCase();
        Long attempts = cacheService.get(key, Long.class).orElse(0L);
        return Math.max(0, maxAttempts - attempts.intValue());
    }
}



