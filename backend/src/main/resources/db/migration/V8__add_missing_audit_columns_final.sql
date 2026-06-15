-- V8: Final audit column gap-fill
--
-- Problem:
--   BaseAuditableEntity expects 6 columns on every entity:
--     created_at, updated_at, created_by, updated_by, deleted_at, deleted_by
--
--   V1+V3 added most columns to most tables but NEVER added:
--     - created_by (missing on 22 tables — only activity_logs/tasks/workspace*
--       have it from V1/V5/V6)
--     - updated_by (missing on 23 tables — only activity_logs and 3 workspace
--       tables have it from V5/V7)
--
--   These gaps cause Hibernate ddl-auto=validate to fail on startup.
--
-- Strategy:
--   Reuse the same stored-procedure pattern from V3 so every ADD COLUMN
--   is guarded by an information_schema check — fully idempotent.

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

-- ============================================================
-- Add missing created_by (BIGINT NULL) to 22 tables
-- (activity_logs handled by V6, tasks has it from V1,
--  workspace tables have it from V5)
-- ============================================================
CALL add_col_safe('boards',                    'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('comments',                  'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('email_verification_tokens',  'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('feature_flags',              'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('labels',                     'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('notifications',              'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('notification_preferences',   'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('projects',                   'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('project_invitations',        'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('project_members',            'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('roles',                      'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('role_audit_logs',            'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('slack_integrations',         'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('slack_user_mappings',        'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('sprints',                    'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('sprint_burndown',            'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('task_attachments',           'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('time_logs',                  'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('users',                      'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('user_two_factor',            'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('webhooks',                   'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('webhook_deliveries',         'created_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Add missing updated_by (BIGINT NULL) to 23 tables
-- (activity_logs handled by V7, workspace tables from V5)
-- ============================================================
CALL add_col_safe('boards',                    'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('comments',                  'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('email_verification_tokens',  'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('feature_flags',              'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('labels',                     'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('notifications',              'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('notification_preferences',   'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('projects',                   'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('project_invitations',        'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('project_members',            'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('roles',                      'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('role_audit_logs',            'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('slack_integrations',         'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('slack_user_mappings',        'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('sprints',                    'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('sprint_burndown',            'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('tasks',                      'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('task_attachments',           'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('time_logs',                  'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('users',                      'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('user_two_factor',            'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('webhooks',                   'updated_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('webhook_deliveries',         'updated_by', 'BIGINT DEFAULT NULL');

DROP PROCEDURE IF EXISTS add_col_safe;
