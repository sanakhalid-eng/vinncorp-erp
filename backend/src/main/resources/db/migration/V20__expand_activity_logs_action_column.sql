-- V20: Expand activity_logs.action (and entity_type) for extended ActionType / EntityType enums
--
-- Fixes: Data truncated for column 'action' at row 1
--   e.g. WORKSPACE_CREATED, WORKSPACE_INVITATION_ACCEPTED, RECURRING_TEMPLATE_*
-- Older databases may have ENUM or VARCHAR(50) that does not accept new enum names.

DROP PROCEDURE IF EXISTS pmt_modify_col_if_needed;
DELIMITER $$
CREATE PROCEDURE pmt_modify_col_if_needed(
    IN tbl VARCHAR(64), IN col VARCHAR(64), IN col_def VARCHAR(500))
BEGIN
    DECLARE current_type VARCHAR(500);
    SELECT COLUMN_TYPE INTO current_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col;
    IF current_type IS NULL OR LOWER(current_type) NOT LIKE 'varchar(64)%' THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' MODIFY COLUMN ', col, ' ', col_def);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL pmt_modify_col_if_needed('activity_logs', 'action', 'VARCHAR(64) NOT NULL');
CALL pmt_modify_col_if_needed('activity_logs', 'entity_type', 'VARCHAR(64) NOT NULL');

DROP PROCEDURE IF EXISTS pmt_modify_col_if_needed;
