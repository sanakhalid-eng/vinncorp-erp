-- Billing & Subscription Enhancements
-- Phase 2F: Billing Events, Add-Ons, Grace Period, Multi-Currency

-- 1. Billing Events Timeline
CREATE TABLE IF NOT EXISTS billing_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    metadata_json TEXT DEFAULT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_billing_events_workspace (workspace_id),
    INDEX idx_billing_events_type (event_type),
    INDEX idx_billing_events_occurred (occurred_at),
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Workspace Add-Ons
CREATE TABLE IF NOT EXISTS workspace_addons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    yearly_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS workspace_subscription_addons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    addon_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMP NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (subscription_id) REFERENCES workspace_subscriptions(id),
    FOREIGN KEY (addon_id) REFERENCES workspace_addons(id),
    INDEX idx_sub_addon_subscription (subscription_id),
    INDEX idx_sub_addon_addon (addon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-6. Column additions using stored procedure (MySQL 8.4 dropped ADD COLUMN IF NOT EXISTS)
DROP PROCEDURE IF EXISTS add_col_if_missing_32;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing_32(tbl VARCHAR(100), col VARCHAR(100), col_def VARCHAR(500))
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

-- Grace Period and Billing Provider
CALL add_col_if_missing_32('workspace_subscriptions', 'grace_period_ends_at', 'TIMESTAMP NULL');
CALL add_col_if_missing_32('workspace_subscriptions', 'billing_provider', 'VARCHAR(50) NULL');

-- Multi-Currency PKR pricing
CALL add_col_if_missing_32('workspace_plans', 'monthly_price_pkr', 'DECIMAL(10,2) NULL');
CALL add_col_if_missing_32('workspace_plans', 'yearly_price_pkr', 'DECIMAL(10,2) NULL');
CALL add_col_if_missing_32('workspace_plans', 'currency', 'VARCHAR(3) NOT NULL DEFAULT \'USD\'');

-- Enhanced Invoice fields
CALL add_col_if_missing_32('invoices', 'payer_name', 'VARCHAR(255) NULL');
CALL add_col_if_missing_32('invoices', 'payer_phone', 'VARCHAR(50) NULL');
CALL add_col_if_missing_32('invoices', 'payment_date', 'DATE NULL');
CALL add_col_if_missing_32('invoices', 'billing_provider', 'VARCHAR(50) NULL');

-- Cancellation Feedback
CALL add_col_if_missing_32('workspace_subscriptions', 'cancellation_reason', 'VARCHAR(500) NULL');
CALL add_col_if_missing_32('workspace_subscriptions', 'cancellation_feedback', 'VARCHAR(1000) NULL');

DROP PROCEDURE IF EXISTS add_col_if_missing_32;

-- 7. Seed default add-ons
INSERT INTO workspace_addons (code, name, description, monthly_price, yearly_price, currency, is_active) VALUES
('EXTRA_PROJECTS', 'Extra Projects', 'Additional projects beyond plan limit', 9.99, 99.99, 'USD', TRUE),
('EXTRA_STORAGE', 'Extra Storage (10GB)', 'Additional storage space', 4.99, 49.99, 'USD', TRUE),
('EXTRA_API_REQUESTS', 'Extra API Requests (10K/mo)', 'Additional API request quota', 2.99, 29.99, 'USD', TRUE),
('PREMIUM_SUPPORT', 'Premium Support', 'Priority customer support access', 14.99, 149.99, 'USD', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);
