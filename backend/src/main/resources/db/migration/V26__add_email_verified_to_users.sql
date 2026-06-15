-- Add email_verified column to users table for OTP verification flow
-- Uses safe procedure pattern to handle re-runs and existing columns

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

CALL add_col_if_missing('users', 'email_verified', 'BOOLEAN NOT NULL DEFAULT FALSE');

-- Mark existing users as verified (they were created before this column existed)
UPDATE users SET email_verified = TRUE WHERE email_verified = FALSE;

DROP PROCEDURE IF EXISTS add_col_if_missing;
