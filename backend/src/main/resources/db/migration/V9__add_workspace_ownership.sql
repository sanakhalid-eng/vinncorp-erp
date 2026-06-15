-- V9: Add workspace ownership to projects and slack integrations
-- Phase 2B: Workspace isolation foundations
--
-- Strategy:
--   1. Add workspace_id to projects (nullable, backfill, then NOT NULL)
--   2. Add FK + indexes on projects.workspace_id
--   3. Add owning_workspace_id to slack_integrations (nullable, backfill)
--   4. Add FK + index on slack_integrations.owning_workspace_id
--   5. Add workspace-level preference columns to workspaces table
--
-- All column additions use the guarded stored-procedure pattern from V3/V8
-- so every statement is idempotent.

DROP PROCEDURE IF EXISTS add_col_safe;
DELIMITER $$
CREATE PROCEDURE add_col_safe(tbl VARCHAR(100), col VARCHAR(100), col_def VARCHAR(500))
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col;
    IF cnt = 0 THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', col_def);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- =========================================================================
-- 1. Add workspace_id to projects (nullable initially for backfill)
-- =========================================================================
CALL add_col_safe('projects', 'workspace_id', 'BIGINT DEFAULT NULL');

-- =========================================================================
-- 2. Backfill workspace_id for existing projects
--
-- Priority (per project):
--   a) The workspace where the project owner holds WORKSPACE_OWNER role
--   b) Any active, non-deleted workspace the project owner belongs to
--   c) The "Personal Workspace" (slug = 'personal-workspace')
-- =========================================================================
UPDATE projects p
SET p.workspace_id = (
    SELECT COALESCE(
        (SELECT wm1.workspace_id
         FROM workspace_members wm1
         INNER JOIN workspaces w1 ON w1.id = wm1.workspace_id AND w1.deleted_at IS NULL
         WHERE wm1.user_id = p.owner_id
           AND wm1.workspace_role = 'WORKSPACE_OWNER'
           AND (wm1.active = TRUE OR wm1.active IS NULL)
         LIMIT 1),
        (SELECT wm2.workspace_id
         FROM workspace_members wm2
         INNER JOIN workspaces w2 ON w2.id = wm2.workspace_id AND w2.deleted_at IS NULL
         WHERE wm2.user_id = p.owner_id
           AND (wm2.active = TRUE OR wm2.active IS NULL)
         ORDER BY wm2.workspace_id
         LIMIT 1),
        (SELECT id FROM workspaces WHERE slug = 'personal-workspace' AND deleted_at IS NULL)
    )
)
WHERE p.workspace_id IS NULL;

-- Safety net: ensure no NULLs remain before applying NOT NULL
UPDATE projects p
SET p.workspace_id = (SELECT id FROM workspaces WHERE slug = 'personal-workspace' AND deleted_at IS NULL)
WHERE p.workspace_id IS NULL;

-- =========================================================================
-- 3. Make workspace_id NOT NULL
-- =========================================================================
ALTER TABLE projects MODIFY COLUMN workspace_id BIGINT NOT NULL;

-- =========================================================================
-- 4. Add FK constraint: fk_project_workspace
-- =========================================================================
ALTER TABLE projects
ADD CONSTRAINT fk_project_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
ON DELETE CASCADE;

-- =========================================================================
-- 5. Add indexes on projects.workspace_id
-- =========================================================================
CREATE INDEX idx_projects_workspace_id ON projects(workspace_id);
CREATE INDEX idx_projects_workspace_deleted ON projects(workspace_id, deleted_at);

-- =========================================================================
-- 6. Add owning_workspace_id to slack_integrations (nullable)
-- =========================================================================
CALL add_col_safe('slack_integrations', 'owning_workspace_id', 'BIGINT DEFAULT NULL');

-- =========================================================================
-- 7. Backfill slack_integrations from the linked project's workspace
-- =========================================================================
UPDATE slack_integrations si
INNER JOIN projects p ON p.id = si.project_id
SET si.owning_workspace_id = p.workspace_id
WHERE si.owning_workspace_id IS NULL;

-- =========================================================================
-- 8. Add FK constraint + index for slack_integrations.owning_workspace_id
-- =========================================================================
ALTER TABLE slack_integrations
ADD CONSTRAINT fk_si_owning_workspace
FOREIGN KEY (owning_workspace_id) REFERENCES workspaces(id)
ON DELETE SET NULL;

CREATE INDEX idx_si_owning_workspace_id ON slack_integrations(owning_workspace_id);

-- =========================================================================
-- 9. Add workspace-level preference columns to workspaces table
-- =========================================================================
CALL add_col_safe('workspaces', 'avatar_url',             'VARCHAR(500) DEFAULT NULL');
CALL add_col_safe('workspaces', 'timezone',               'VARCHAR(50) DEFAULT ''UTC''');
CALL add_col_safe('workspaces', 'date_format',            'VARCHAR(20) DEFAULT ''YYYY-MM-DD''');
CALL add_col_safe('workspaces', 'default_sprint_duration', 'INT DEFAULT 14');
CALL add_col_safe('workspaces', 'default_task_statuses',   'TEXT');

-- =========================================================================
-- Cleanup
-- =========================================================================
DROP PROCEDURE IF EXISTS add_col_safe;
