package com.vinncorp.erp.modules.projects.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinncorp.erp.modules.projects.service.SlackService;
import com.vinncorp.erp.modules.projects.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookEventProcessor {
    private final WebhookService webhookService;
    private final SlackService slackService;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_CREATED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: TASK_CREATED for taskId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("TASK_CREATED", event.getProjectId(), data);
        
        // Send Slack notification
        try {
            slackService.getProjectIntegration(event.getProjectId())
                    .ifPresent(integration -> {
                        // Convert data to JSON for Slack
                        String taskTitle = event.getMetadata() != null && event.getMetadata().containsKey("title") 
                                ? event.getMetadata().get("title").toString() 
                                : "Untitled Task";
                        String priority = event.getMetadata() != null && event.getMetadata().containsKey("priority") 
                                ? event.getMetadata().get("priority").toString() 
                                : "MEDIUM";
                        
                        Map<String, Object> slackData = Map.of(
                                "title", taskTitle,
                                "priority", priority,
                                "taskId", event.getEntityId().toString(),
                                "projectId", event.getProjectId().toString()
                        );
                        // slackService.sendTaskCreatedNotification(integration.getWorkspaceId(), "general", objectMapper.valueToTree(slackData));
                    });
        } catch (Exception e) {
            log.error("Failed to send Slack notification for task created", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_UPDATED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: TASK_UPDATED for taskId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("TASK_UPDATED", event.getProjectId(), data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskStatusChanged(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TASK_STATUS_CHANGED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: TASK_STATUS_CHANGED for taskId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("TASK_STATUS_CHANGED", event.getProjectId(), data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.COMMENT_CREATED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: COMMENT_CREATED for taskId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("COMMENT_CREATED", event.getProjectId(), data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTimeLogCreated(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.TIME_LOG_CREATED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: TIME_LOG_CREATED for taskId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("TIME_LOG_CREATED", event.getProjectId(), data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintStarted(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_STARTED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: SPRINT_STARTED for sprintId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("sprintId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("SPRINT_STARTED", event.getProjectId(), data);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSprintCompleted(DomainEvent event) {
        if (event.getType() != DomainEvent.Type.SPRINT_COMPLETED) return;
        if (event.getProjectId() == null) return;

        log.debug("Webhook event: SPRINT_COMPLETED for sprintId={}", event.getEntityId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("sprintId", event.getEntityId());
        data.put("actorId", event.getActorId());
        if (event.getMetadata() != null) {
            data.putAll(event.getMetadata());
        }
        
        webhookService.publishEvent("SPRINT_COMPLETED", event.getProjectId(), data);
    }
}



