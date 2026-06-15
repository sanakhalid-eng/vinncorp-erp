CREATE TABLE IF NOT EXISTS sprint_velocity_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    committed_points INT NOT NULL DEFAULT 0,
    completed_points INT NOT NULL DEFAULT 0,
    spillover_points INT NOT NULL DEFAULT 0,
    completion_rate DOUBLE NOT NULL DEFAULT 0,
    velocity_score DOUBLE NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_velocity_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    CONSTRAINT fk_velocity_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_velocity_project FOREIGN KEY (project_id) REFERENCES projects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_velocity_sprint ON sprint_velocity_snapshots (sprint_id);
CREATE INDEX idx_velocity_workspace ON sprint_velocity_snapshots (workspace_id);
CREATE INDEX idx_velocity_project ON sprint_velocity_snapshots (project_id);

CREATE TABLE IF NOT EXISTS sprint_capacities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    workspace_id BIGINT NOT NULL,
    available_hours DOUBLE NOT NULL DEFAULT 0,
    allocated_hours DOUBLE NOT NULL DEFAULT 0,
    utilization_percent DOUBLE NOT NULL DEFAULT 0,
    pto_days INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    CONSTRAINT fk_capacity_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    CONSTRAINT fk_capacity_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_capacity_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_capacity_sprint ON sprint_capacities (sprint_id);
CREATE INDEX idx_capacity_user ON sprint_capacities (user_id);
CREATE INDEX idx_capacity_workspace ON sprint_capacities (workspace_id);

CREATE TABLE IF NOT EXISTS sprint_metric_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sprint_id BIGINT NOT NULL,
    workspace_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    remaining_tasks INT NOT NULL DEFAULT 0,
    remaining_points INT NOT NULL DEFAULT 0,
    completed_tasks INT NOT NULL DEFAULT 0,
    completed_points INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_metric_sprint FOREIGN KEY (sprint_id) REFERENCES sprints(id),
    CONSTRAINT fk_metric_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    UNIQUE KEY uk_metric_sprint_date (sprint_id, snapshot_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_metric_sprint ON sprint_metric_snapshots (sprint_id);
CREATE INDEX idx_metric_snapshot_date ON sprint_metric_snapshots (snapshot_date);
CREATE INDEX idx_metric_workspace ON sprint_metric_snapshots (workspace_id);
