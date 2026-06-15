-- V10: Add workspace_id to notifications, activity_logs, and time_logs
-- Phase 2C: Workspace isolation for activity/notification/time entities
--
-- Strategy:
--   1. Add workspace_id to notifications (nullable, backfill via project_id)
--   2. Add FK + index on notifications.workspace_id
--   3. Add workspace_id to activity_logs (nullable, backfill via project_id)
--   4. Add FK + indexes on activity_logs.workspace_id
--   5. Add workspace_id to time_logs (nullable, backfill via task → project)
--   6. Add FK + index on time_logs.workspace_id
--   7. Add metadata prefix index on activity_logs for search performance
--
-- All column additions use the guarded stored-procedure pattern from V3/V8/V9
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
-- 1. Add workspace_id to notifications
-- =========================================================================
CALL add_col_safe('notifications', 'workspace_id', 'BIGINT DEFAULT NULL');

-- =========================================================================
-- 2. Backfill notifications.workspace_id from linked project
-- =========================================================================
UPDATE notifications n
INNER JOIN projects p ON p.id = n.project_id
SET n.workspace_id = p.workspace_id
WHERE n.workspace_id IS NULL;

-- =========================================================================
-- 3. FK constraint for notifications
-- =========================================================================
ALTER TABLE notifications
ADD CONSTRAINT fk_notification_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
ON DELETE CASCADE;

-- =========================================================================
-- 4. Index on notifications.workspace_id
-- =========================================================================
CREATE INDEX idx_notifications_workspace_id ON notifications(workspace_id);

-- =========================================================================
-- 5. Add workspace_id to activity_logs
-- =========================================================================
CALL add_col_safe('activity_logs', 'workspace_id', 'BIGINT DEFAULT NULL');

-- =========================================================================
-- 6. Backfill activity_logs.workspace_id from linked project
-- =========================================================================
UPDATE activity_logs al
INNER JOIN projects p ON p.id = al.project_id
SET al.workspace_id = p.workspace_id
WHERE al.workspace_id IS NULL;

-- =========================================================================
-- 7. FK constraint for activity_logs
-- =========================================================================
ALTER TABLE activity_logs
ADD CONSTRAINT fk_activity_log_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
ON DELETE CASCADE;

-- =========================================================================
-- 8. Indexes on activity_logs.workspace_id
-- =========================================================================
CREATE INDEX idx_activity_logs_workspace_id ON activity_logs(workspace_id);
CREATE INDEX idx_activity_logs_workspace_created ON activity_logs(workspace_id, created_at);

-- =========================================================================
-- 9. Add workspace_id to time_logs
-- =========================================================================
CALL add_col_safe('time_logs', 'workspace_id', 'BIGINT DEFAULT NULL');

-- =========================================================================
-- 10. Backfill time_logs.workspace_id via task → project chain
-- =========================================================================
UPDATE time_logs tl
INNER JOIN tasks t ON t.id = tl.task_id
INNER JOIN projects p ON p.id = t.project_id
SET tl.workspace_id = p.workspace_id
WHERE tl.workspace_id IS NULL;

-- =========================================================================
-- 11. FK constraint for time_logs
-- =========================================================================
ALTER TABLE time_logs
ADD CONSTRAINT fk_time_log_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
ON DELETE CASCADE;

-- =========================================================================
-- 12. Index on time_logs.workspace_id
-- =========================================================================
CREATE INDEX idx_time_logs_workspace_id ON time_logs(workspace_id);

-- =========================================================================
-- 13. Metadata prefix index for activity_logs search performance
-- =========================================================================
CREATE INDEX idx_activity_logs_metadata ON activity_logs(metadata(255));

-- =========================================================================
-- Cleanup
-- =========================================================================
DROP PROCEDURE IF EXISTS add_col_safe;
