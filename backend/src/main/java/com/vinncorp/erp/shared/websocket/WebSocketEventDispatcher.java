package com.vinncorp.erp.shared.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import com.vinncorp.erp.shared.cache.RedisPubSubListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventDispatcher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private ChannelTopic workspaceTopic;

    @Autowired(required = false)
    private ChannelTopic notificationTopic;

    public <T> void broadcastToWorkspace(Long workspaceId, WebSocketEvent<T> event) {
        String destination = "/topic/workspace/" + workspaceId;
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Broadcast to {}: event={}", destination, event.getEvent());
    }

    public <T> void broadcastToWorkspace(Long workspaceId, String entityType, Long entityId, WebSocketEvent<T> event) {
        String destination = "/topic/workspace/" + workspaceId + "/" + entityType + "/" + entityId;
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Broadcast to {}: event={}", destination, event.getEvent());
    }

    public <T> void sendToUser(String userId, WebSocketEvent<T> event) {
        String destination = "/queue/user/" + userId;
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", event);
        log.debug("Sent to user {}: event={}", userId, event.getEvent());
    }

    public <T> void broadcastToWorkspaceExcept(Long workspaceId, String excludeUserId, WebSocketEvent<T> event) {
        broadcastToWorkspace(workspaceId, event);
    }

    public void sendPresenceUpdate(Long workspaceId, Object presenceData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("presence", "presence_update", workspaceId, presenceData);
        broadcastToWorkspace(workspaceId, event);
    }

    public void sendTypingIndicator(Long workspaceId, String entityType, Long entityId, Object typingData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("typing", "typing_indicator", workspaceId, entityType, entityId, typingData);
        broadcastToWorkspace(workspaceId, entityType, entityId, event);
    }

    public void sendTaskUpdate(Long workspaceId, Long taskId, Object taskData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("entity_update", "task_updated", workspaceId, "task", taskId, taskData);
        broadcastToWorkspace(workspaceId, "task", taskId, event);
    }

    public void sendBoardUpdate(Long workspaceId, Long boardId, Object boardData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("entity_update", "board_updated", workspaceId, "board", boardId, boardData);
        broadcastToWorkspace(workspaceId, "board", boardId, event);
    }

    public void sendSprintUpdate(Long workspaceId, Long sprintId, Object sprintData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("entity_update", "sprint_updated", workspaceId, "sprint", sprintId, sprintData);
        broadcastToWorkspace(workspaceId, "sprint", sprintId, event);
    }

    public void sendNotification(Long userId, Object notificationData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("notification", "new_notification", null, notificationData);
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", event);
        log.debug("Sent to user {}: event={}", userId, event.getEvent());

        publishToRedisNotification(userId, event);
    }

    public void sendActivityStream(Long workspaceId, Object activityData) {
        WebSocketEvent<Object> event = WebSocketEvent.of("activity", "activity_update", workspaceId, activityData);
        broadcastToWorkspace(workspaceId, event);
    }

    public void publishToRedisWorkspace(Long workspaceId, String entityType, Long entityId, String eventType, Object data) {
        if (redisTemplate != null && workspaceTopic != null) {
            try {
                String payload = objectMapper.writeValueAsString(
                        new RedisPubSubListener.WorkspaceEvent(workspaceId, entityType, entityId, eventType, data)
                );
                redisTemplate.convertAndSend(workspaceTopic.getTopic(), payload);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize Redis pub/sub message", e);
            }
        }
    }

    private void publishToRedisNotification(Long userId, Object data) {
        if (redisTemplate != null && notificationTopic != null) {
            try {
                String payload = objectMapper.writeValueAsString(
                        new RedisPubSubListener.NotificationEvent(userId, "notification", data)
                );
                redisTemplate.convertAndSend(notificationTopic.getTopic(), payload);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize Redis notification message", e);
            }
        }
    }
}


