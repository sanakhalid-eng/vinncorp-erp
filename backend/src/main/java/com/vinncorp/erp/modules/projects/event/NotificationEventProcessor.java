package com.vinncorp.erp.modules.projects.event;

import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.enums.NotificationType;
import com.vinncorp.erp.modules.projects.service.NotificationService;
import com.vinncorp.erp.shared.websocket.WebSocketEventDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Component("eventNotificationProcessor")
public class NotificationEventProcessor {

    private final NotificationService notificationService;
    private final WebSocketEventDispatcher webSocketEventDispatcher;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_ASSIGNED) return;
        if (event.getTargetUserId() == null || event.getActorId() == null) return;

        createAndSendNotification(
                event.getTargetUserId(), event.getActorId(),
                NotificationType.TASK_ASSIGNED,
                event.getMessage(),
                event.getEntityId(), event.getEntityType(),
                event.getProjectId(), event.getProjectName(),
                "/tasks?taskId=" + event.getEntityId(),
                event.getEventId(), "TASK:" + event.getProjectId(), "MEDIUM"
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.COMMENT_CREATED) return;
        if (event.getTargetUserId() == null || event.getActorId() == null) return;

        createAndSendNotification(
                event.getTargetUserId(), event.getActorId(),
                NotificationType.COMMENT_MENTION,
                event.getMessage(),
                event.getEntityId(), event.getEntityType(),
                event.getProjectId(), event.getProjectName(),
                "/tasks?taskId=" + (event.getMetadata() != null ? event.getMetadata().get("taskId") : event.getEntityId()),
                event.getEventId(), "COMMENT:" + event.getProjectId(), "MEDIUM"
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChanged(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_STATUS_CHANGED) return;
        if (event.getTargetUserId() == null) return;

        createAndSendNotification(
                event.getTargetUserId(), event.getActorId(),
                NotificationType.STATUS_CHANGED,
                event.getMessage(),
                event.getEntityId(), event.getEntityType(),
                event.getProjectId(), event.getProjectName(),
                "/tasks?taskId=" + event.getEntityId(),
                event.getEventId(), "TASK:" + event.getProjectId(), "LOW"
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_UPDATED) return;
        if (event.getTargetUserId() == null || event.getActorId() == null) return;
        if (event.getTargetUserId().equals(event.getActorId())) return;

        createAndSendNotification(
                event.getTargetUserId(), event.getActorId(),
                NotificationType.TASK_ASSIGNED,
                event.getMessage(),
                event.getEntityId(), event.getEntityType(),
                event.getProjectId(), event.getProjectName(),
                "/tasks?taskId=" + event.getEntityId(),
                event.getEventId(), "TASK:" + event.getProjectId(), "LOW"
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMentionInComment(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.COMMENT_MENTIONED) return;
        if (event.getTargetUserId() == null || event.getActorId() == null) return;

        createAndSendNotification(
                event.getTargetUserId(), event.getActorId(),
                NotificationType.COMMENT_MENTION,
                event.getMessage(),
                event.getEntityId(), event.getEntityType(),
                event.getProjectId(), event.getProjectName(),
                "/tasks?taskId=" + event.getEntityId(),
                event.getEventId(), "COMMENT:" + event.getProjectId(), "HIGH"
        );
    }

    private void createAndSendNotification(
            Long userId, Long actorId,
            NotificationType type, String message,
            Long entityId, String entityType,
            Long projectId, String projectName,
            String actionUrl, String eventId, String groupKey, String priority
    ) {
        try {
            notificationService.createNotification(
                    userId, actorId, type, message,
                    entityId, entityType, projectId, projectName,
                    actionUrl, eventId, groupKey, priority
            );

            User receiver = userRepository.findById(userId).orElse(null);
            if (receiver != null) {
                Map<String, Object> payload = Map.of(
                        "type", type.name(),
                        "message", message,
                        "entityId", entityId,
                        "entityType", entityType,
                        "projectId", projectId,
                        "projectName", projectName != null ? projectName : "",
                        "actionUrl", actionUrl != null ? actionUrl : "",
                        "actorId", actorId,
                        "userId", userId
                );
                WebSocketEvent<Object> wsEvent = WebSocketEvent.of(
                        "notification", type.name().toLowerCase(),
                        projectId, "notification", entityId, payload
                );
                webSocketEventDispatcher.sendToUser(String.valueOf(userId), wsEvent);
            }
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage());
        }
    }
}



