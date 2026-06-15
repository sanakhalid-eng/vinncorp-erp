-- ============================================
-- V30: Add missing deleted_at columns to soft-delete entities
-- ============================================

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(tbl VARCHAR(100), col VARCHAR(100), col_def VARCHAR(500))
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

CALL add_col_if_missing('boards', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('board_columns', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('sprints', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('time_logs', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('comment_edits', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('comment_mentions', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('comment_reactions', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('slack_user_mappings', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('webhook_deliveries', 'deleted_at', 'DATETIME NULL');
CALL add_col_if_missing('user_roles', 'deleted_at', 'DATETIME NULL');

DROP PROCEDURE IF EXISTS add_col_if_missing;

-- Indexes for the newly added deleted_at columns
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

CALL add_index_if_missing('boards', 'idx_boards_deleted_at', '(deleted_at)');
CALL add_index_if_missing('board_columns', 'idx_board_columns_deleted_at', '(deleted_at)');
CALL add_index_if_missing('sprints', 'idx_sprints_deleted_at', '(deleted_at)');
CALL add_index_if_missing('time_logs', 'idx_time_logs_deleted_at', '(deleted_at)');
CALL add_index_if_missing('comment_edits', 'idx_comment_edits_deleted_at', '(deleted_at)');
CALL add_index_if_missing('comment_mentions', 'idx_comment_mentions_deleted_at', '(deleted_at)');
CALL add_index_if_missing('comment_reactions', 'idx_comment_reactions_deleted_at', '(deleted_at)');
CALL add_index_if_missing('slack_user_mappings', 'idx_slack_user_mappings_deleted_at', '(deleted_at)');
CALL add_index_if_missing('webhook_deliveries', 'idx_webhook_deliveries_deleted_at', '(deleted_at)');
CALL add_index_if_missing('user_roles', 'idx_user_roles_deleted_at', '(deleted_at)');

DROP PROCEDURE IF EXISTS add_index_if_missing;
