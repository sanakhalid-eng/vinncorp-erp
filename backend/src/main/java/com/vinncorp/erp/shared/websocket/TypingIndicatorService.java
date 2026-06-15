package com.vinncorp.erp.shared.websocket;

import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypingIndicatorService {

    private final WebSocketEventDispatcher eventDispatcher;

    private final Map<String, TypingSession> typingSessions = new ConcurrentHashMap<>();

    private static final long TYPING_TIMEOUT_MS = 5000;

    public void userTyping(Long userId, String userName, Long workspaceId, String entityType, Long entityId) {
        String key = workspaceId + ":" + entityType + ":" + entityId + ":" + userId;

        TypingSession session = TypingSession.builder()
                .userId(userId)
                .userName(userName)
                .workspaceId(workspaceId)
                .entityType(entityType)
                .entityId(entityId)
                .lastTyped(LocalDateTime.now())
                .build();

        typingSessions.put(key, session);

        TypingData data = TypingData.builder()
                .userId(userId)
                .userName(userName)
                .entityType(entityType)
                .entityId(entityId)
                .isTyping(true)
                .build();

        eventDispatcher.sendTypingIndicator(workspaceId, entityType, entityId, data);
    }

    public void userStoppedTyping(Long userId, Long workspaceId, String entityType, Long entityId) {
        String key = workspaceId + ":" + entityType + ":" + entityId + ":" + userId;
        typingSessions.remove(key);

        TypingData data = TypingData.builder()
                .userId(userId)
                .workspaceId(workspaceId)
                .entityType(entityType)
                .entityId(entityId)
                .isTyping(false)
                .build();

        eventDispatcher.sendTypingIndicator(workspaceId, entityType, entityId, data);
    }

    @Scheduled(fixedDelay = 3000)
    public void cleanupExpiredTypingSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusNanos(TYPING_TIMEOUT_MS * 1_000_000);

        typingSessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getLastTyped().isBefore(cutoff)) {
                TypingSession session = entry.getValue();
                TypingData data = TypingData.builder()
                        .userId(session.getUserId())
                        .workspaceId(session.getWorkspaceId())
                        .entityType(session.getEntityType())
                        .entityId(session.getEntityId())
                        .isTyping(false)
                        .build();

                eventDispatcher.sendTypingIndicator(
                        session.getWorkspaceId(),
                        session.getEntityType(),
                        session.getEntityId(),
                        data
                );
                return true;
            }
            return false;
        });
    }

    @Data
    @Builder
    public static class TypingSession {
        private Long userId;
        private String userName;
        private Long workspaceId;
        private String entityType;
        private Long entityId;
        private LocalDateTime lastTyped;
    }

    @Data
    @Builder
    public static class TypingData {
        private Long userId;
        private String userName;
        private Long workspaceId;
        private String entityType;
        private Long entityId;
        private boolean isTyping;
    }
}


