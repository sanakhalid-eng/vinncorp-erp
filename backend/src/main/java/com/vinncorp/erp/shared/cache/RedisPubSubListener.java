package com.vinncorp.erp.shared.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.event.WebSocketEvent;
import com.vinncorp.erp.shared.websocket.WebSocketEventDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(RedisMessageListenerContainer.class)
public class RedisPubSubListener implements MessageListener {

    private final RedisMessageListenerContainer listenerContainer;
    private final ChannelTopic workspaceTopic;
    private final ChannelTopic notificationTopic;
    private final WebSocketEventDispatcher eventDispatcher;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        listenerContainer.addMessageListener(this, workspaceTopic);
        listenerContainer.addMessageListener(this, notificationTopic);
        log.info("Subscribed to Redis channels: {}, {}", workspaceTopic.getTopic(), notificationTopic.getTopic());
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            log.debug("Received Redis pub/sub message on channel={}", channel);

            if (channel.equals(workspaceTopic.getTopic())) {
                WorkspaceEvent event = objectMapper.readValue(body, WorkspaceEvent.class);
                WebSocketEvent<Object> wsEvent = WebSocketEvent.of(event.eventType(), event.eventType(), event.workspaceId(), event.entityType(), event.entityId(), event.data());
                eventDispatcher.broadcastToWorkspace(event.workspaceId(), wsEvent);
            } else if (channel.equals(notificationTopic.getTopic())) {
                NotificationEvent event = objectMapper.readValue(body, NotificationEvent.class);
                eventDispatcher.sendNotification(event.userId(), event.data());
            }
        } catch (Exception e) {
            log.error("Failed to process Redis pub/sub message", e);
        }
    }

    public record WorkspaceEvent(Long workspaceId, String entityType, Long entityId, String eventType, Object data) {}
    public record NotificationEvent(Long userId, String eventType, Object data) {}
}


