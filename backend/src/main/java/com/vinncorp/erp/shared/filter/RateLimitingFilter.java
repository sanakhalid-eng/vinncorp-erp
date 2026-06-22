package com.vinncorp.erp.shared.filter;

import com.vinncorp.erp.shared.cache.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final CacheService cacheService;

    private final ConcurrentMap<String, long[]> localRateLimits = new ConcurrentHashMap<>();

    @Value("${api.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${api.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth")
                || path.startsWith("/ws")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String key = "rate_limit:" + clientIp + ":" + request.getRequestURI();

        try {
            Long count = cacheService.get(key, Long.class).orElse(0L);

            if (count >= requestsPerMinute) {
                log.warn("Rate limit exceeded for IP={}, URI={}", clientIp, request.getRequestURI());
                writeRateLimitResponse(response);
                return;
            }

            cacheService.put(key, count + 1, 60000);
            response.setHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(requestsPerMinute - count - 1));
        } catch (Exception e) {
            log.warn("Redis unavailable, falling back to local rate limiter for IP={}", clientIp);
            if (!tryLocalRateLimit(key)) {
                writeRateLimitResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryLocalRateLimit(String key) {
        long now = System.currentTimeMillis();
        long window = 60_000L;

        long[] entry = localRateLimits.putIfAbsent(key, new long[]{1L, now});
        if (entry == null) {
            return true;
        }

        synchronized (entry) {
            if (now - entry[1] > window) {
                entry[0] = 1L;
                entry[1] = now;
                return true;
            }
            if (entry[0] >= requestsPerMinute) {
                return false;
            }
            entry[0]++;
            return true;
        }
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"Rate limit exceeded. Try again later.\",\"status\":429}");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
