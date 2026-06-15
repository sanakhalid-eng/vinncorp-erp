package com.vinncorp.erp.modules.projects.engine;

import com.vinncorp.erp.modules.projects.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEventListenerImpl implements WorkflowEventListener {

    private final WorkflowTriggerDispatcher dispatcher;

    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        WorkflowTrigger trigger = mapEventTypeToTrigger(event.getType());
        if (trigger == null) {
            return;
        }

        Long workspaceId = extractWorkspaceId(event);
        if (workspaceId == null) {
            log.debug("No workspace context for event {}, skipping workflow dispatch", event.getEventId());
            return;
        }

        String entityType = event.getEntityType() != null ? event.getEntityType() : "TASK";
        Long entityId = event.getEntityId();
        Long projectId = event.getProjectId();

        Map<String, Object> context = new HashMap<>();
        if (event.getMetadata() != null) {
            context.putAll(event.getMetadata());
        }
        context.put("eventType", event.getType().name());
        context.put("actorId", event.getActorId());
        context.put("targetUserId", event.getTargetUserId());

        dispatcher.dispatch(trigger, workspaceId, projectId, entityType, entityId, context);
    }

    private WorkflowTrigger mapEventTypeToTrigger(DomainEvent.Type eventType) {
        return switch (eventType) {
            case TASK_CREATED, SUBTASK_CREATED -> WorkflowTrigger.TASK_CREATED;
            case TASK_UPDATED, SUBTASK_UPDATED -> WorkflowTrigger.TASK_UPDATED;
            case TASK_STATUS_CHANGED -> WorkflowTrigger.TASK_COMPLETED;
            case TASK_ASSIGNED -> WorkflowTrigger.TASK_ASSIGNED;
            case DUE_DATE_OVERDUE -> WorkflowTrigger.TASK_OVERDUE;
            case SPRINT_STARTED -> WorkflowTrigger.SPRINT_STARTED;
            case SPRINT_COMPLETED -> WorkflowTrigger.SPRINT_COMPLETED;
            case TASK_DEPENDENCY_ADDED -> WorkflowTrigger.DEPENDENCY_BLOCKED;
            case TASK_DEPENDENCY_REMOVED -> WorkflowTrigger.DEPENDENCY_UNBLOCKED;
            case COMMENT_CREATED, COMMENT_MENTIONED -> WorkflowTrigger.COMMENT_ADDED;
            case TIME_LOG_CREATED -> WorkflowTrigger.TIMELOG_CREATED;
            default -> null;
        };
    }

    private Long extractWorkspaceId(DomainEvent event) {
        if (event.getMetadata() != null && event.getMetadata().containsKey("workspaceId")) {
            Object wId = event.getMetadata().get("workspaceId");
            if (wId instanceof Number) {
                return ((Number) wId).longValue();
            }
        }
        return null;
    }
}



