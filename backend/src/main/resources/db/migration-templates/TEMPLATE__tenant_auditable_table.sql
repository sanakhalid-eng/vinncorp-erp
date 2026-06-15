-- =============================================================================
-- Flyway migration template — tenant-scoped auditable table (Phase 3A+)
-- Copy to: db/migration/V{next}__{description}.sql
-- =============================================================================
-- Standards:
--   * ENGINE=InnoDB, CHARSET=utf8mb4, COLLATE=utf8mb4_unicode_ci
--   * BaseTenantEntity: workspace_id + BaseAuditableEntity audit columns
--   * Idempotent: guarded CREATE TABLE / ADD COLUMN / CREATE INDEX
--   * FK indexes on referencing columns BEFORE optional FK constraints
--   * Composite analytics index: (workspace_id, project_id, created_at) or similar
-- =============================================================================

-- Step 1: Helper — guarded column add (reuse in every migration)
DROP PROCEDURE IF EXISTS pmt_add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE pmt_add_col_if_missing(IN tbl VARCHAR(64), IN col VARCHAR(64), IN col_def VARCHAR(500))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @s = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', col, ' ', col_def);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- Step 2: Helper — guarded index add
DROP PROCEDURE IF EXISTS pmt_add_idx_if_missing;
DELIMITER //
CREATE PROCEDURE pmt_add_idx_if_missing(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = tbl AND index_name = idx
    ) THEN
        SET @s = ddl;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

-- Step 3: Create table (IF NOT EXISTS) with full audit + tenant columns
-- Replace: your_table_name, domain columns, optional project_id / sprint_id
CREATE TABLE IF NOT EXISTS your_table_name (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
  -- project_id BIGINT,          -- optional tenant scope
  -- sprint_id BIGINT,           -- optional
    -- ... business columns ...
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 4: Gap-fill audit columns if table existed from partial migration
CALL pmt_add_col_if_missing('your_table_name', 'updated_at',  'DATETIME DEFAULT NULL');
CALL pmt_add_col_if_missing('your_table_name', 'created_by',  'BIGINT DEFAULT NULL');
CALL pmt_add_col_if_missing('your_table_name', 'updated_by',  'BIGINT DEFAULT NULL');
CALL pmt_add_col_if_missing('your_table_name', 'deleted_at',  'DATETIME DEFAULT NULL');
CALL pmt_add_col_if_missing('your_table_name', 'deleted_by',  'BIGINT DEFAULT NULL');

-- Step 5: Indexes (workspace isolation + analytics queries)
CALL pmt_add_idx_if_missing('your_table_name', 'idx_your_ws_proj_created',
    'CREATE INDEX idx_your_ws_proj_created ON your_table_name(workspace_id, project_id, created_at)');
CALL pmt_add_idx_if_missing('your_table_name', 'idx_your_workspace_id',
    'CREATE INDEX idx_your_workspace_id ON your_table_name(workspace_id)');

-- Step 6: Optional FK (after indexes exist)
-- ALTER TABLE your_table_name ADD CONSTRAINT fk_your_workspace
--     FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

DROP PROCEDURE IF EXISTS pmt_add_idx_if_missing;
DROP PROCEDURE IF EXISTS pmt_add_col_if_missing;
