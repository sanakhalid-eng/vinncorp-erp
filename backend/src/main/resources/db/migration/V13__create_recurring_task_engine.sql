CREATE TABLE IF NOT EXISTS recurring_task_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    template_task_id BIGINT NOT NULL,
    recurrence_type VARCHAR(20) NOT NULL,
    interval_value INT NOT NULL DEFAULT 1,
    days_of_week VARCHAR(50),
    day_of_month INT,
    next_run_at DATETIME NOT NULL,
    last_generated_at DATETIME,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    paused BOOLEAN NOT NULL DEFAULT FALSE,
    ends_at DATETIME,
    max_occurrences INT,
    generated_count INT NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    deleted_at DATETIME,
    deleted_by BIGINT,
    CONSTRAINT fk_recurring_template_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_recurring_template_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_recurring_template_task FOREIGN KEY (template_task_id) REFERENCES tasks(id),
    CONSTRAINT fk_recurring_template_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_recurring_template_workspace ON recurring_task_templates (workspace_id);
CREATE INDEX idx_recurring_template_next_run ON recurring_task_templates (next_run_at);
CREATE INDEX idx_recurring_template_active ON recurring_task_templates (active);
CREATE INDEX idx_recurring_template_project ON recurring_task_templates (project_id);

CREATE TABLE IF NOT EXISTS recurring_task_occurrences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recurring_template_id BIGINT NOT NULL,
    generated_task_id BIGINT NOT NULL,
    occurrence_date DATE NOT NULL,
    generation_status VARCHAR(20) NOT NULL DEFAULT 'GENERATED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recurring_occurrence_template FOREIGN KEY (recurring_template_id) REFERENCES recurring_task_templates(id),
    CONSTRAINT fk_recurring_occurrence_task FOREIGN KEY (generated_task_id) REFERENCES tasks(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_recurring_occurrence_template ON recurring_task_occurrences (recurring_template_id);
CREATE INDEX idx_recurring_occurrence_date ON recurring_task_occurrences (occurrence_date);
CREATE INDEX idx_recurring_occurrence_status ON recurring_task_occurrences (generation_status);
