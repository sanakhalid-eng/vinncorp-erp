-- Phase 3A.4: Workflow Automation & Rule Engine

-- 1. Core automation rule tables
CREATE TABLE IF NOT EXISTS workflow_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    trigger_type VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    action_config JSON NULL,
    execution_order INT NOT NULL DEFAULT 0,
    cooldown_seconds INT NOT NULL DEFAULT 0,
    last_executed_at DATETIME NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    INDEX idx_wf_rules_workspace (workspace_id),
    INDEX idx_wf_rules_project (project_id),
    INDEX idx_wf_rules_trigger (trigger_type),
    INDEX idx_wf_rules_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS workflow_conditions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    operator VARCHAR(30) NOT NULL,
    comparison_value VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES workflow_rules(id) ON DELETE CASCADE,
    INDEX idx_wf_conditions_rule (rule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS workflow_execution_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time_ms BIGINT NOT NULL DEFAULT 0,
    error_message TEXT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rule_id) REFERENCES workflow_rules(id) ON DELETE CASCADE,
    INDEX idx_wf_logs_rule (rule_id),
    INDEX idx_wf_logs_status (status),
    INDEX idx_wf_logs_entity (entity_type, entity_id),
    INDEX idx_wf_logs_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. SLA tables
CREATE TABLE IF NOT EXISTS task_slas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    sla_type VARCHAR(20) NOT NULL COMMENT 'RESPONSE or COMPLETION',
    response_minutes INT NULL,
    completion_minutes INT NULL,
    warning_threshold_pct DOUBLE NOT NULL DEFAULT 80.0,
    breached_at DATETIME NULL,
    warned_at DATETIME NULL,
    resolved_at DATETIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    INDEX idx_sla_workspace (workspace_id),
    INDEX idx_sla_project (project_id),
    INDEX idx_sla_task (task_id),
    INDEX idx_sla_status (status),
    INDEX idx_sla_breached (breached_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Escalation rules
CREATE TABLE IF NOT EXISTS escalation_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    trigger_condition VARCHAR(50) NOT NULL COMMENT 'SLA_BREACH, OVERDUE, BLOCKED_FOR, etc.',
    threshold_minutes INT NOT NULL,
    escalate_to_role VARCHAR(50) NULL,
    escalate_to_user_id BIGINT NULL,
    notify_assignee BOOLEAN NOT NULL DEFAULT TRUE,
    notify_project_lead BOOLEAN NOT NULL DEFAULT FALSE,
    auto_assign BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    INDEX idx_esc_workspace (workspace_id),
    INDEX idx_esc_project (project_id),
    INDEX idx_esc_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Seed common automation templates
INSERT INTO workflow_rules (workspace_id, project_id, name, description, enabled, trigger_type, action_type, execution_order, created_by)
SELECT w.id, NULL, 'Auto-close completed subtasks', 'Automatically closes subtasks when parent is completed', TRUE, 'TASK_COMPLETED', 'UPDATE_STATUS', 1, 0
FROM workspaces w;

INSERT INTO workflow_rules (workspace_id, project_id, name, description, enabled, trigger_type, action_type, execution_order, created_by)
SELECT w.id, NULL, 'Escalate overdue critical tasks', 'Escalates overdue CRITICAL priority tasks', TRUE, 'TASK_OVERDUE', 'ESCALATE_TASK', 2, 0
FROM workspaces w;

INSERT INTO workflow_rules (workspace_id, project_id, name, description, enabled, trigger_type, action_type, execution_order, created_by)
SELECT w.id, NULL, 'Notify on blocked dependency', 'Sends notification when a task becomes blocked by dependency', TRUE, 'DEPENDENCY_BLOCKED', 'SEND_NOTIFICATION', 3, 0
FROM workspaces w;

INSERT INTO workflow_rules (workspace_id, project_id, name, description, enabled, trigger_type, action_type, execution_order, created_by)
SELECT w.id, NULL, 'Move completed tasks to QA', 'Auto-assigns completed task to QA reviewer', TRUE, 'TASK_COMPLETED', 'ASSIGN_USER', 4, 0
FROM workspaces w;

INSERT INTO workflow_rules (workspace_id, project_id, name, description, enabled, trigger_type, action_type, execution_order, created_by)
SELECT w.id, NULL, 'Alert sprint overload', 'Alerts when sprint exceeds capacity threshold', TRUE, 'SPRINT_STARTED', 'SEND_NOTIFICATION', 5, 0
FROM workspaces w;
