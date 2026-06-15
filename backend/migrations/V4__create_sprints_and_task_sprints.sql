-- Migration: Create sprints and task_sprints tables
-- Date: 2026-05-04

CREATE TABLE IF NOT EXISTS sprints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    goal VARCHAR(2000),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS task_sprints (
    task_id BIGINT NOT NULL,
    sprint_id BIGINT NOT NULL,

    CONSTRAINT fk_task_sprint_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_sprint_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id) ON DELETE CASCADE,
    CONSTRAINT uk_task_sprint UNIQUE (task_id, sprint_id)
);

CREATE INDEX IF NOT EXISTS idx_sprint_project_id ON sprints(project_id);
CREATE INDEX IF NOT EXISTS idx_sprint_status ON sprints(status);
CREATE INDEX IF NOT EXISTS idx_task_sprint_sprint_id ON task_sprints(sprint_id);
CREATE INDEX IF NOT EXISTS idx_task_sprint_task_id ON task_sprints(task_id);
