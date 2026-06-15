-- V18: Gap-fill audit columns on Phase 3A tables (V16/V17)
--
-- BaseAuditableEntity requires: created_at, updated_at, created_by, updated_by, deleted_at, deleted_by
-- V16/V17 CREATE TABLE IF NOT EXISTS skipped columns on several tables already created in dev DBs.

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

-- V16: execution intelligence
CALL add_col_safe('estimation_snapshots',           'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('estimation_snapshots',           'updated_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('estimation_snapshots',           'deleted_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('estimation_snapshots',           'deleted_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('execution_risk_snapshots',       'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('execution_risk_snapshots',       'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('execution_risk_snapshots',       'updated_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('execution_risk_snapshots',       'deleted_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('execution_risk_snapshots',       'deleted_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('productivity_snapshots',         'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('productivity_snapshots',         'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('productivity_snapshots',         'updated_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('productivity_snapshots',         'deleted_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('productivity_snapshots',         'deleted_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('critical_path_snapshots',        'created_at',  'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_col_safe('critical_path_snapshots',        'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('critical_path_snapshots',        'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('critical_path_snapshots',        'updated_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('critical_path_snapshots',        'deleted_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('critical_path_snapshots',        'deleted_by',  'BIGINT DEFAULT NULL');

-- V17: planning, search, analytics, productivity
CALL add_col_safe('monte_carlo_forecasts',          'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('monte_carlo_forecasts',          'updated_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('capacity_forecast_snapshots',    'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('capacity_forecast_snapshots',    'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('capacity_forecast_snapshots',    'updated_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('activity_intelligence_summaries','updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('activity_intelligence_summaries','created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('activity_intelligence_summaries','updated_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('analytics_snapshots',            'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('analytics_snapshots',            'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('analytics_snapshots',            'updated_by',  'BIGINT DEFAULT NULL');

CALL add_col_safe('delivery_predictability_snapshots','updated_at','DATETIME DEFAULT NULL');
CALL add_col_safe('delivery_predictability_snapshots','created_by','BIGINT DEFAULT NULL');
CALL add_col_safe('delivery_predictability_snapshots','updated_by','BIGINT DEFAULT NULL');

CALL add_col_safe('command_palette_recents',        'created_at',  'DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP');
CALL add_col_safe('command_palette_recents',        'updated_at',  'DATETIME DEFAULT NULL');
CALL add_col_safe('command_palette_recents',        'created_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('command_palette_recents',        'updated_by',  'BIGINT DEFAULT NULL');
CALL add_col_safe('command_palette_recents',        'deleted_by',  'BIGINT DEFAULT NULL');

DROP PROCEDURE IF EXISTS add_col_safe;
