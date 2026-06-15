-- Phase 3A.5-3A.8: Search, Knowledge, Executive Analytics, Productivity Enhancements
-- Idempotent migration with guarded index creation

-- 3A.5 Portfolio roadmap items
CREATE TABLE IF NOT EXISTS portfolio_roadmap_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    milestone_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.5 Monte Carlo forecast snapshots
CREATE TABLE IF NOT EXISTS monte_carlo_forecasts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT NOT NULL,
    iterations INT NOT NULL DEFAULT 1000,
    p50_completion_date DATE,
    p85_completion_date DATE,
    p95_completion_date DATE,
    mean_remaining_points DOUBLE NOT NULL DEFAULT 0,
    confidence_score DOUBLE NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.5 Capacity forecast snapshots
CREATE TABLE IF NOT EXISTS capacity_forecast_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    sprint_id BIGINT,
    predicted_utilization DOUBLE NOT NULL DEFAULT 0,
    predicted_overload_members INT NOT NULL DEFAULT 0,
    recommended_capacity_hours DOUBLE NOT NULL DEFAULT 0,
    forecast_horizon_days INT NOT NULL DEFAULT 14,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.6 Saved searches
CREATE TABLE IF NOT EXISTS saved_searches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    query_text VARCHAR(500) NOT NULL,
    filters_json TEXT,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.6 Workspace / project notes
CREATE TABLE IF NOT EXISTS workspace_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.6 Knowledge hub articles
CREATE TABLE IF NOT EXISTS knowledge_articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    markdown_content MEDIUMTEXT,
    tags_json TEXT,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.6 Activity intelligence summaries
CREATE TABLE IF NOT EXISTS activity_intelligence_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    summary_type VARCHAR(50) NOT NULL,
    highlights_json TEXT,
    metrics_json TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.7 Analytics snapshots
CREATE TABLE IF NOT EXISTS analytics_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT,
    sprint_id BIGINT,
    snapshot_type VARCHAR(50) NOT NULL,
    metrics_json MEDIUMTEXT NOT NULL,
    captured_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.7 Delivery predictability snapshots
CREATE TABLE IF NOT EXISTS delivery_predictability_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    predictability_score DOUBLE NOT NULL DEFAULT 0,
    on_time_delivery_rate DOUBLE NOT NULL DEFAULT 0,
    avg_delay_days DOUBLE NOT NULL DEFAULT 0,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    deleted_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3A.8 Command palette recent actions
CREATE TABLE IF NOT EXISTS command_palette_recents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action_key VARCHAR(100) NOT NULL,
    action_label VARCHAR(255) NOT NULL,
    target_url VARCHAR(500),
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Composite indexes (guarded)
DROP PROCEDURE IF EXISTS pmt_add_idx_if_missing;
DELIMITER //
CREATE PROCEDURE pmt_add_idx_if_missing(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = tbl AND index_name = idx
    ) THEN
        SET @s = ddl;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL pmt_add_idx_if_missing('portfolio_roadmap_items', 'idx_roadmap_ws_proj_created',
    'CREATE INDEX idx_roadmap_ws_proj_created ON portfolio_roadmap_items(workspace_id, project_id, created_at)');
CALL pmt_add_idx_if_missing('monte_carlo_forecasts', 'idx_mc_ws_sprint_created',
    'CREATE INDEX idx_mc_ws_sprint_created ON monte_carlo_forecasts(workspace_id, sprint_id, created_at)');
CALL pmt_add_idx_if_missing('capacity_forecast_snapshots', 'idx_capfc_ws_proj_sprint_created',
    'CREATE INDEX idx_capfc_ws_proj_sprint_created ON capacity_forecast_snapshots(workspace_id, project_id, sprint_id, created_at)');
CALL pmt_add_idx_if_missing('saved_searches', 'idx_savedsearch_ws_user_created',
    'CREATE INDEX idx_savedsearch_ws_user_created ON saved_searches(workspace_id, user_id, created_at)');
CALL pmt_add_idx_if_missing('workspace_notes', 'idx_notes_ws_proj_created',
    'CREATE INDEX idx_notes_ws_proj_created ON workspace_notes(workspace_id, project_id, created_at)');
CALL pmt_add_idx_if_missing('knowledge_articles', 'idx_knowledge_ws_slug',
    'CREATE INDEX idx_knowledge_ws_slug ON knowledge_articles(workspace_id, slug)');
CALL pmt_add_idx_if_missing('activity_intelligence_summaries', 'idx_actintel_ws_proj_created',
    'CREATE INDEX idx_actintel_ws_proj_created ON activity_intelligence_summaries(workspace_id, project_id, created_at)');
CALL pmt_add_idx_if_missing('analytics_snapshots', 'idx_analytics_ws_proj_sprint_created',
    'CREATE INDEX idx_analytics_ws_proj_sprint_created ON analytics_snapshots(workspace_id, project_id, sprint_id, created_at)');
CALL pmt_add_idx_if_missing('delivery_predictability_snapshots', 'idx_delpred_ws_proj_created',
    'CREATE INDEX idx_delpred_ws_proj_created ON delivery_predictability_snapshots(workspace_id, project_id, created_at)');
CALL pmt_add_idx_if_missing('command_palette_recents', 'idx_cmdpal_ws_user_used',
    'CREATE INDEX idx_cmdpal_ws_user_used ON command_palette_recents(workspace_id, user_id, used_at)');

DROP PROCEDURE IF EXISTS pmt_add_idx_if_missing;
