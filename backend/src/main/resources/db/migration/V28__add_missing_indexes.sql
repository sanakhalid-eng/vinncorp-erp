-- ============================================
-- V28: Add Missing Indexes for Performance
-- ============================================

DROP PROCEDURE IF EXISTS add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE add_index_if_missing(tbl VARCHAR(100), idx VARCHAR(100), cols VARCHAR(500))
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND INDEX_NAME = idx;
    IF cnt = 0 THEN
        SET @s = CONCAT('CREATE INDEX ', idx, ' ON ', tbl, ' ', cols);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- Task table: frequently filtered columns
CALL add_index_if_missing('tasks', 'idx_tasks_priority', '(priority)');
CALL add_index_if_missing('tasks', 'idx_tasks_due_date', '(due_date)');
CALL add_index_if_missing('tasks', 'idx_tasks_workflow_status', '(workflow_status_id)');

-- Project table
CALL add_index_if_missing('projects', 'idx_projects_owner', '(owner_id)');
CALL add_index_if_missing('projects', 'idx_projects_is_active', '(is_active)');

-- Comments: ordering by created_at within a task
CALL add_index_if_missing('comments', 'idx_comments_task_created', '(task_id, created_at)');

-- Notifications: ordering by created_at for a user
CALL add_index_if_missing('notifications', 'idx_notifications_user_created', '(user_id, created_at)');

-- Activity logs: entity timeline
CALL add_index_if_missing('activity_logs', 'idx_activity_logs_entity_timeline', '(entity_type, entity_id, created_at)');

-- Task labels: composite for lookup
CALL add_index_if_missing('task_labels', 'idx_task_labels_task_label', '(task_id, label_id)');

-- Workflow statuses: project lookup
CALL add_index_if_missing('workflow_status', 'idx_workflow_status_project', '(project_id)');

-- deleted_at indexes (labels already has deleted_at column; others added in V30)
CALL add_index_if_missing('labels', 'idx_label_project_deleted', '(project_id, deleted_at)');

DROP PROCEDURE IF EXISTS add_index_if_missing;
