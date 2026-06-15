package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.shared.cache.CacheService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final CacheService cacheService;

    private static final long SESSION_TTL = 30L * 24 * 60 * 60 * 1000;

    @Data
    @Builder
    public static class SessionInfo {
        private String sessionId;
        private String deviceName;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime lastActive;
        private boolean isActive;
    }

    public void createSession(Long userId, String sessionId, String deviceName, String ipAddress, String userAgent) {
        SessionInfo session = SessionInfo.builder()
                .sessionId(sessionId)
                .deviceName(deviceName)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastActive(LocalDateTime.now())
                .isActive(true)
                .build();

        String key = "session:" + userId + ":" + sessionId;
        cacheService.put(key, session, SESSION_TTL);

        String userSessionsKey = "user_sessions:" + userId;
        Optional<Object> sessionsOpt = cacheService.get(userSessionsKey, Object.class);
        if (sessionsOpt.isPresent()) {
            @SuppressWarnings("unchecked")
            List<String> sessionList = (List<String>) sessionsOpt.get();
            sessionList.add(sessionId);
            cacheService.put(userSessionsKey, sessionList, SESSION_TTL);
        } else {
            cacheService.put(userSessionsKey, List.of(sessionId), SESSION_TTL);
        }

        log.info("Created session {} for user {}", sessionId, userId);
    }

    public void updateSessionActivity(Long userId, String sessionId) {
        String key = "session:" + userId + ":" + sessionId;
        Optional<SessionInfo> sessionOpt = cacheService.get(key, SessionInfo.class);
        if (sessionOpt.isPresent()) {
            SessionInfo session = sessionOpt.get();
            session.setLastActive(LocalDateTime.now());
            cacheService.put(key, session, SESSION_TTL);
        }
    }

    public void revokeSession(Long userId, String sessionId) {
        String key = "session:" + userId + ":" + sessionId;
        cacheService.evict(key);

        String userSessionsKey = "user_sessions:" + userId;
        Optional<Object> sessionsOpt = cacheService.get(userSessionsKey, Object.class);
        if (sessionsOpt.isPresent()) {
            @SuppressWarnings("unchecked")
            List<String> sessionList = (List<String>) sessionsOpt.get();
            sessionList.remove(sessionId);
            cacheService.put(userSessionsKey, sessionList, SESSION_TTL);
        }

        log.info("Revoked session {} for user {}", sessionId, userId);
    }

    public void revokeAllSessions(Long userId) {
        String userSessionsKey = "user_sessions:" + userId;
        Optional<Object> sessionsOpt = cacheService.get(userSessionsKey, Object.class);
        if (sessionsOpt.isPresent()) {
            @SuppressWarnings("unchecked")
            List<String> sessionList = (List<String>) sessionsOpt.get();
            sessionList.forEach(sid -> cacheService.evict("session:" + userId + ":" + sid));
            cacheService.evict(userSessionsKey);
        }

        log.info("Revoked all sessions for user {}", userId);
    }

    public List<SessionInfo> getUserSessions(Long userId) {
        String userSessionsKey = "user_sessions:" + userId;
        Optional<Object> sessionIdsOpt = cacheService.get(userSessionsKey, Object.class);
        if (sessionIdsOpt.isEmpty()) return List.of();

        @SuppressWarnings("unchecked")
        List<String> sessionIds = (List<String>) sessionIdsOpt.get();

        return sessionIds.stream()
                .map(sessionId -> cacheService.get("session:" + userId + ":" + sessionId, SessionInfo.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public boolean isSessionValid(Long userId, String sessionId) {
        String key = "session:" + userId + ":" + sessionId;
        return cacheService.exists(key);
    }
}



