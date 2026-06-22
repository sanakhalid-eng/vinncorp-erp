package com.vinncorp.erp.shared.websocket;

import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final WebSocketEventDispatcher eventDispatcher;

    private final Map<Long, UserPresence> activeUsers = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> workspaceActiveUsers = new ConcurrentHashMap<>();

    public void userConnected(Long userId, Long workspaceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        UserPresence presence = UserPresence.builder()
                .userId(userId)
                .userName(user.getName())
                .userEmail(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .workspaceId(workspaceId)
                .lastActive(LocalDateTime.now())
                .online(true)
                .status("online")
                .build();

        activeUsers.put(userId, presence);
        workspaceActiveUsers.computeIfAbsent(workspaceId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        WebSocketEvent<UserPresence> event = WebSocketEvent.of("presence", "user_connected", workspaceId, presence);
        eventDispatcher.broadcastToWorkspace(workspaceId, event);

        log.info("User connected: userId={}, workspaceId={}", userId, workspaceId);
    }

    public void userDisconnected(Long userId, Long workspaceId) {
        activeUsers.remove(userId);

        if (workspaceId != null) {
            Set<Long> users = workspaceActiveUsers.get(workspaceId);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    workspaceActiveUsers.remove(workspaceId);
                }
            }

            WebSocketEvent<Map<String, Object>> event = WebSocketEvent.of("presence", "user_disconnected", workspaceId,
                    Map.of("userId", userId));
            eventDispatcher.broadcastToWorkspace(workspaceId, event);
        }

        log.info("User disconnected: userId={}, workspaceId={}", userId, workspaceId);
    }

    public void userActivity(Long userId, Long workspaceId) {
        UserPresence presence = activeUsers.get(userId);
        if (presence != null) {
            presence.setLastActive(LocalDateTime.now());
            presence.setOnline(true);
        }
    }

    public List<UserPresence> getWorkspaceActiveUsers(Long workspaceId) {
        Set<Long> userIds = workspaceActiveUsers.getOrDefault(workspaceId, Set.of());
        return userIds.stream()
                .map(activeUsers::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int getWorkspaceActiveUserCount(Long workspaceId) {
        return workspaceActiveUsers.getOrDefault(workspaceId, Set.of()).size();
    }

    public boolean isUserOnline(Long userId) {
        UserPresence presence = activeUsers.get(userId);
        return presence != null && presence.isOnline();
    }

    public Map<String, Object> getPresenceSummary(Long workspaceId) {
        List<UserPresence> users = getWorkspaceActiveUsers(workspaceId);
        return Map.of(
                "workspaceId", workspaceId,
                "activeUsers", users,
                "count", users.size()
        );
    }
}


