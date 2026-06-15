package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_execution_logs", indexes = {
        @Index(name = "idx_wf_logs_rule", columnList = "rule_id"),
        @Index(name = "idx_wf_logs_status", columnList = "status"),
        @Index(name = "idx_wf_logs_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_wf_logs_created", columnList = "created_at")
})
@Getter
@Setter
public class WorkflowExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExecutionStatus status;

    @Column(name = "execution_time_ms", nullable = false)
    private long executionTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}



