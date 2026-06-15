package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.projects.enums.WorkflowAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_rules", indexes = {
        @Index(name = "idx_wf_rules_workspace", columnList = "workspace_id"),
        @Index(name = "idx_wf_rules_project", columnList = "project_id"),
        @Index(name = "idx_wf_rules_trigger", columnList = "trigger_type"),
        @Index(name = "idx_wf_rules_enabled", columnList = "enabled")
})
@Getter
@Setter
public class WorkflowRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private WorkflowTrigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private WorkflowAction actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_config", columnDefinition = "JSON")
    private String actionConfig;

    @Column(name = "execution_order", nullable = false)
    private int executionOrder = 0;

    @Column(name = "cooldown_seconds", nullable = false)
    private int cooldownSeconds = 0;

    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkflowCondition> conditions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



