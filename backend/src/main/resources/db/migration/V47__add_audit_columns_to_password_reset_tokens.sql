-- V47: Add missing audit columns to password_reset_tokens
--
-- Problem:
--   password_reset_tokens was created in V25 with created_at, updated_at,
--   deleted_at, and deleted_by, but was never given created_by or updated_by.
--   BaseAuditableEntity maps both columns, causing Hibernate schema validation
--   to fail on startup with:
--     "Schema validation: missing column [created_by] in table [password_reset_tokens]"
--
-- Fix:
--   Add created_by and updated_by (BIGINT NULL) using the same idempotent
--   add_col_safe stored-procedure pattern used throughout this migration set.

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

CALL add_col_safe('password_reset_tokens', 'created_by', 'BIGINT DEFAULT NULL');
CALL add_col_safe('password_reset_tokens', 'updated_by', 'BIGINT DEFAULT NULL');

DROP PROCEDURE IF EXISTS add_col_safe;
