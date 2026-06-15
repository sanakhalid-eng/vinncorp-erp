-- V6: Add missing created_by column to activity_logs
-- V3 added updated_at/deleted_at/deleted_by to activity_logs but
-- never added created_by, which BaseAuditableEntity expects.

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

CALL add_col_if_missing('activity_logs', 'created_by', 'BIGINT DEFAULT NULL');

DROP PROCEDURE IF EXISTS add_col_if_missing;
