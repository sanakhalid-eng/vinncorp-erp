-- V12: Enhance task_dependencies with type, description, and audit columns
-- Uses guarded column-add + index-add patterns for full idempotency

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

DROP PROCEDURE IF EXISTS add_index_safe;
DELIMITER $$
CREATE PROCEDURE add_index_safe(tbl VARCHAR(100), idx VARCHAR(100), idx_cols VARCHAR(500))
BEGIN
    DECLARE cnt INT DEFAULT 0;
    SELECT COUNT(*) INTO cnt
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND INDEX_NAME = idx;
    IF cnt = 0 THEN
        SET @s = CONCAT('CREATE INDEX ', idx, ' ON ', tbl, ' (', idx_cols, ')');
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_col_safe('task_dependencies', 'dependency_type', 'VARCHAR(20) NOT NULL DEFAULT ''BLOCKED_BY''');
CALL add_col_safe('task_dependencies', 'description', 'VARCHAR(500) NULL');
CALL add_col_safe('task_dependencies', 'created_by', 'BIGINT NULL');
CALL add_col_safe('task_dependencies', 'updated_at', 'DATETIME NULL');
CALL add_col_safe('task_dependencies', 'updated_by', 'BIGINT NULL');
CALL add_col_safe('task_dependencies', 'deleted_at', 'DATETIME NULL');
CALL add_col_safe('task_dependencies', 'deleted_by', 'BIGINT NULL');

CALL add_index_safe('task_dependencies', 'idx_dependency_type', 'dependency_type');
CALL add_index_safe('task_dependencies', 'idx_task_dep_type', 'task_id, dependency_type');
CALL add_index_safe('task_dependencies', 'idx_depends_on_task_dep_type', 'depends_on_task_id, dependency_type');
CALL add_index_safe('task_dependencies', 'idx_task_dep_deleted_at', 'deleted_at');

DROP PROCEDURE IF EXISTS add_col_safe;
DROP PROCEDURE IF EXISTS add_index_safe;
