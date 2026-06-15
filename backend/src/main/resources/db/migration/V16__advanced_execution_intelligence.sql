-- Phase 3A.5: Advanced Execution Intelligence
-- Migration V16: Create advanced execution intelligence tables

-- 1. Estimation Snapshots
CREATE TABLE IF NOT EXISTS estimation_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    estimated_points INT,
    actual_points INT,
    confidence_score DOUBLE,
    estimation_drift DOUBLE,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS add_idx_est_workspace;
DELIMITER //
CREATE PROCEDURE add_idx_est_workspace()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='estimation_snapshots' AND index_name='idx_est_workspace') THEN
        CREATE INDEX idx_est_workspace ON estimation_snapshots(workspace_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_est_workspace();
DROP PROCEDURE IF EXISTS add_idx_est_workspace;

DROP PROCEDURE IF EXISTS add_idx_est_project;
DELIMITER //
CREATE PROCEDURE add_idx_est_project()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='estimation_snapshots' AND index_name='idx_est_project') THEN
        CREATE INDEX idx_est_project ON estimation_snapshots(project_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_est_project();
DROP PROCEDURE IF EXISTS add_idx_est_project;

DROP PROCEDURE IF EXISTS add_idx_est_task;
DELIMITER //
CREATE PROCEDURE add_idx_est_task()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='estimation_snapshots' AND index_name='idx_est_task') THEN
        CREATE INDEX idx_est_task ON estimation_snapshots(task_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_est_task();
DROP PROCEDURE IF EXISTS add_idx_est_task;

DROP PROCEDURE IF EXISTS add_idx_est_created;
DELIMITER //
CREATE PROCEDURE add_idx_est_created()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='estimation_snapshots' AND index_name='idx_est_created') THEN
        CREATE INDEX idx_est_created ON estimation_snapshots(created_at);
    END IF;
END //
DELIMITER ;
CALL add_idx_est_created();
DROP PROCEDURE IF EXISTS add_idx_est_created;

-- 2. Execution Risk Snapshots
CREATE TABLE IF NOT EXISTS execution_risk_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    risk_score DOUBLE NOT NULL DEFAULT 0,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    delayed_task_count INT NOT NULL DEFAULT 0,
    blocked_task_count INT NOT NULL DEFAULT 0,
    overloaded_member_count INT NOT NULL DEFAULT 0,
    velocity_decline_percent DOUBLE NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS add_idx_risk_workspace;
DELIMITER //
CREATE PROCEDURE add_idx_risk_workspace()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='execution_risk_snapshots' AND index_name='idx_risk_workspace') THEN
        CREATE INDEX idx_risk_workspace ON execution_risk_snapshots(workspace_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_risk_workspace();
DROP PROCEDURE IF EXISTS add_idx_risk_workspace;

DROP PROCEDURE IF EXISTS add_idx_risk_project;
DELIMITER //
CREATE PROCEDURE add_idx_risk_project()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='execution_risk_snapshots' AND index_name='idx_risk_project') THEN
        CREATE INDEX idx_risk_project ON execution_risk_snapshots(project_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_risk_project();
DROP PROCEDURE IF EXISTS add_idx_risk_project;

DROP PROCEDURE IF EXISTS add_idx_risk_sprint;
DELIMITER //
CREATE PROCEDURE add_idx_risk_sprint()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='execution_risk_snapshots' AND index_name='idx_risk_sprint') THEN
        CREATE INDEX idx_risk_sprint ON execution_risk_snapshots(sprint_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_risk_sprint();
DROP PROCEDURE IF EXISTS add_idx_risk_sprint;

DROP PROCEDURE IF EXISTS add_idx_risk_created;
DELIMITER //
CREATE PROCEDURE add_idx_risk_created()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='execution_risk_snapshots' AND index_name='idx_risk_created') THEN
        CREATE INDEX idx_risk_created ON execution_risk_snapshots(created_at);
    END IF;
END //
DELIMITER ;
CALL add_idx_risk_created();
DROP PROCEDURE IF EXISTS add_idx_risk_created;

-- 3. Productivity Snapshots
CREATE TABLE IF NOT EXISTS productivity_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    throughput INT NOT NULL DEFAULT 0,
    average_cycle_time DOUBLE NOT NULL DEFAULT 0,
    average_lead_time DOUBLE NOT NULL DEFAULT 0,
    blocked_time_hours DOUBLE NOT NULL DEFAULT 0,
    predictability_score DOUBLE NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS add_idx_prod_workspace;
DELIMITER //
CREATE PROCEDURE add_idx_prod_workspace()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='productivity_snapshots' AND index_name='idx_prod_workspace') THEN
        CREATE INDEX idx_prod_workspace ON productivity_snapshots(workspace_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_prod_workspace();
DROP PROCEDURE IF EXISTS add_idx_prod_workspace;

DROP PROCEDURE IF EXISTS add_idx_prod_project;
DELIMITER //
CREATE PROCEDURE add_idx_prod_project()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='productivity_snapshots' AND index_name='idx_prod_project') THEN
        CREATE INDEX idx_prod_project ON productivity_snapshots(project_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_prod_project();
DROP PROCEDURE IF EXISTS add_idx_prod_project;

DROP PROCEDURE IF EXISTS add_idx_prod_sprint;
DELIMITER //
CREATE PROCEDURE add_idx_prod_sprint()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='productivity_snapshots' AND index_name='idx_prod_sprint') THEN
        CREATE INDEX idx_prod_sprint ON productivity_snapshots(sprint_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_prod_sprint();
DROP PROCEDURE IF EXISTS add_idx_prod_sprint;

DROP PROCEDURE IF EXISTS add_idx_prod_created;
DELIMITER //
CREATE PROCEDURE add_idx_prod_created()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='productivity_snapshots' AND index_name='idx_prod_created') THEN
        CREATE INDEX idx_prod_created ON productivity_snapshots(created_at);
    END IF;
END //
DELIMITER ;
CALL add_idx_prod_created();
DROP PROCEDURE IF EXISTS add_idx_prod_created;

-- 4. Critical Path Snapshots
CREATE TABLE IF NOT EXISTS critical_path_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    dependency_depth INT NOT NULL DEFAULT 0,
    criticality_score DOUBLE NOT NULL DEFAULT 0,
    is_on_critical_path BOOLEAN NOT NULL DEFAULT FALSE,
    calculated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP PROCEDURE IF EXISTS add_idx_critpath_workspace;
DELIMITER //
CREATE PROCEDURE add_idx_critpath_workspace()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='critical_path_snapshots' AND index_name='idx_critpath_workspace') THEN
        CREATE INDEX idx_critpath_workspace ON critical_path_snapshots(workspace_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_critpath_workspace();
DROP PROCEDURE IF EXISTS add_idx_critpath_workspace;

DROP PROCEDURE IF EXISTS add_idx_critpath_project;
DELIMITER //
CREATE PROCEDURE add_idx_critpath_project()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='critical_path_snapshots' AND index_name='idx_critpath_project') THEN
        CREATE INDEX idx_critpath_project ON critical_path_snapshots(project_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_critpath_project();
DROP PROCEDURE IF EXISTS add_idx_critpath_project;

DROP PROCEDURE IF EXISTS add_idx_critpath_task;
DELIMITER //
CREATE PROCEDURE add_idx_critpath_task()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='critical_path_snapshots' AND index_name='idx_critpath_task') THEN
        CREATE INDEX idx_critpath_task ON critical_path_snapshots(task_id);
    END IF;
END //
DELIMITER ;
CALL add_idx_critpath_task();
DROP PROCEDURE IF EXISTS add_idx_critpath_task;

DROP PROCEDURE IF EXISTS add_idx_critpath_calculated;
DELIMITER //
CREATE PROCEDURE add_idx_critpath_calculated()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_name='critical_path_snapshots' AND index_name='idx_critpath_calculated') THEN
        CREATE INDEX idx_critpath_calculated ON critical_path_snapshots(calculated_at);
    END IF;
END //
DELIMITER ;
CALL add_idx_critpath_calculated();
DROP PROCEDURE IF EXISTS add_idx_critpath_calculated;
