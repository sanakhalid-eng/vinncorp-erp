-- ============================================
-- V29: Add start_date and end_date to tasks for Gantt chart
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

CALL add_col_if_missing('tasks', 'start_date', 'DATETIME NULL AFTER due_date');
CALL add_col_if_missing('tasks', 'end_date', 'DATETIME NULL AFTER start_date');

DROP PROCEDURE IF EXISTS add_col_if_missing;

-- Indexes for new date columns
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

CALL add_index_if_missing('tasks', 'idx_tasks_start_date', '(start_date)');
CALL add_index_if_missing('tasks', 'idx_tasks_end_date', '(end_date)');

DROP PROCEDURE IF EXISTS add_index_if_missing;
