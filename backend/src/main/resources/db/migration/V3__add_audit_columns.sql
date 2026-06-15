-- Conditionally add audit columns missing from existing (pre-Flyway) database.
-- On a fresh DB built by V1, these columns already exist — the procedure skips them.

DROP PROCEDURE IF EXISTS add_audit_col;
DELIMITER $$
CREATE PROCEDURE add_audit_col(tbl VARCHAR(100), col VARCHAR(100), col_def VARCHAR(500))
BEGIN
    DECLARE tbl_cnt INT DEFAULT 0;
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO tbl_cnt
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl;
    IF tbl_cnt = 0 THEN
        SELECT CONCAT('Skipping ', tbl, ': table does not exist') AS info;
    ELSE
        SELECT COUNT(*) INTO cnt
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col;
        IF cnt = 0 THEN
            SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', col_def);
            PREPARE stmt FROM @s;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
    END IF;
END$$
DELIMITER ;

-- ============================================================
-- Users
-- ============================================================
CALL add_audit_col('users', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('users', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('users', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Roles
-- ============================================================
CALL add_audit_col('roles', 'created_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('roles', 'updated_at', 'DATETIME DEFAULT NULL');

-- ============================================================
-- Feature Flags
-- ============================================================
CALL add_audit_col('feature_flags', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('feature_flags', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Email Verification Tokens
-- ============================================================
CALL add_audit_col('email_verification_tokens', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('email_verification_tokens', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('email_verification_tokens', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Scheduled Jobs
-- ============================================================
CALL add_audit_col('scheduled_jobs', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('scheduled_jobs', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Retry Queue
-- ============================================================
CALL add_audit_col('retry_queue', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('retry_queue', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Email Deliveries
-- ============================================================
CALL add_audit_col('email_deliveries', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('email_deliveries', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- User Two Factor
-- ============================================================
CALL add_audit_col('user_two_factor', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('user_two_factor', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Notification Preferences
-- ============================================================
CALL add_audit_col('notification_preferences', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('notification_preferences', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Project Members
-- ============================================================
CALL add_audit_col('project_members', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('project_members', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('project_members', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Role Audit Logs
-- ============================================================
CALL add_audit_col('role_audit_logs', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('role_audit_logs', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('role_audit_logs', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Boards
-- ============================================================
CALL add_audit_col('boards', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('boards', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('boards', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Labels (missing deleted_by)
-- ============================================================
CALL add_audit_col('labels', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Sprints
-- ============================================================
CALL add_audit_col('sprints', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('sprints', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Sprint Burndown
-- ============================================================
CALL add_audit_col('sprint_burndown', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('sprint_burndown', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Task Labels
-- ============================================================
CALL add_audit_col('task_labels', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('task_labels', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('task_labels', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Task Dependencies
-- ============================================================
CALL add_audit_col('task_dependencies', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('task_dependencies', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('task_dependencies', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Task Attachments (missing updated_at, deleted_by)
-- ============================================================
CALL add_audit_col('task_attachments', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('task_attachments', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Comments (missing deleted_by)
-- ============================================================
CALL add_audit_col('comments', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Comment Mentions
-- ============================================================
CALL add_audit_col('comment_mentions', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('comment_mentions', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('comment_mentions', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Comment Reactions
-- ============================================================
CALL add_audit_col('comment_reactions', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('comment_reactions', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('comment_reactions', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Time Logs
-- ============================================================
CALL add_audit_col('time_logs', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('time_logs', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('time_logs', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Notifications
-- ============================================================
CALL add_audit_col('notifications', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('notifications', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('notifications', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Activity Logs
-- ============================================================
CALL add_audit_col('activity_logs', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('activity_logs', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('activity_logs', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Slack Integrations
-- ============================================================
CALL add_audit_col('slack_integrations', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('slack_integrations', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('slack_integrations', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Slack User Mappings
-- ============================================================
CALL add_audit_col('slack_user_mappings', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('slack_user_mappings', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('slack_user_mappings', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Webhooks
-- ============================================================
CALL add_audit_col('webhooks', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('webhooks', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('webhooks', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Webhook Deliveries
-- ============================================================
CALL add_audit_col('webhook_deliveries', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('webhook_deliveries', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('webhook_deliveries', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Projects (missing updated_at)
-- ============================================================
CALL add_audit_col('projects', 'updated_at', 'DATETIME DEFAULT NULL');

-- ============================================================
-- Project Invitations (missing updated_at)
-- ============================================================
CALL add_audit_col('project_invitations', 'updated_at', 'DATETIME DEFAULT NULL');

-- ============================================================
-- Scheduled Job Executions
-- ============================================================
CALL add_audit_col('scheduled_job_executions', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('scheduled_job_executions', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('scheduled_job_executions', 'deleted_by', 'BIGINT DEFAULT NULL');

-- ============================================================
-- Email Verification
-- ============================================================
CALL add_audit_col('email_verification_tokens', 'updated_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('email_verification_tokens', 'deleted_at', 'DATETIME DEFAULT NULL');
CALL add_audit_col('email_verification_tokens', 'deleted_by', 'BIGINT DEFAULT NULL');

DROP PROCEDURE IF EXISTS add_audit_col;
