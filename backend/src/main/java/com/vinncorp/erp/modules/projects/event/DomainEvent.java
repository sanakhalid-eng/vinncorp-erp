package com.vinncorp.erp.modules.projects.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent {

    private String eventId;
    private Type type;
    private Long actorId;
    private Long targetUserId;
    private String entityType;
    private Long entityId;
    private Long projectId;
    private String projectName;
    private String message;
    private Map<String, Object> metadata;
    private long timestamp;

    public DomainEvent(Type type, Long actorId, Long targetUserId, String entityType, Long entityId) {
        this.type = type;
        this.actorId = actorId;
        this.targetUserId = targetUserId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = System.currentTimeMillis();
        this.eventId = type.name() + "-" + entityId + "-" + System.currentTimeMillis();
    }

    public enum Type {
        TASK_ASSIGNED,
        TASK_UNASSIGNED,
        TASK_STATUS_CHANGED,
        TASK_CREATED,
        TASK_UPDATED,
        TASK_DELETED,
        SUBTASK_CREATED,
        SUBTASK_UPDATED,
        LABEL_ADDED_TO_TASK,
        LABEL_REMOVED_FROM_TASK,
        COMMENT_CREATED,
        COMMENT_MENTIONED,
        FILE_UPLOADED,
        DUE_DATE_APPROACHING,
        DUE_DATE_OVERDUE,
        MEMBER_ADDED,
        MEMBER_REMOVED,
        TASK_DEPENDENCY_ADDED,
        TASK_DEPENDENCY_REMOVED,
        SPRINT_CREATED,
        SPRINT_STARTED,
        SPRINT_COMPLETED,
        TASK_ADDED_TO_SPRINT,
        TASK_REMOVED_FROM_SPRINT,
        TASK_DEPENDENCY_CROSS_SPRINT,
        TIME_LOG_CREATED,
        RECURRING_TASK_GENERATED,
        RECURRENCE_PAUSED,
        RECURRENCE_STOPPED,
        SPRINT_AT_RISK,
        MEMBER_OVER_CAPACITY,
        VELOCITY_DECLINING,
        FORECAST_DELAYED,
        AUTOMATION_RULE_TRIGGERED,
        AUTOMATION_ACTION_EXECUTED,
        AUTOMATION_FAILED,
        SLA_BREACHED,
        SLA_WARNING,
        TASK_ESCALATED,
        SMART_ASSIGNMENT_COMPLETED,
        RISK_LEVEL_CRITICAL,
        DELIVERY_FORECAST_DELAYED,
        SPRINT_OVER_CAPACITY,
        CRITICAL_PATH_CHANGED,
        ESTIMATION_DRIFT_DETECTED
    }
}



