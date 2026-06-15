-- Subscription & Billing Foundation
-- Phase 2E: Subscription Domain Model

CREATE TABLE IF NOT EXISTS workspace_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    yearly_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    max_projects INTEGER NOT NULL DEFAULT 3,
    max_members INTEGER NOT NULL DEFAULT 5,
    max_storage_gb INTEGER NOT NULL DEFAULT 1,
    max_webhooks INTEGER NOT NULL DEFAULT 0,
    max_api_requests_per_month INTEGER NOT NULL DEFAULT 1000,
    supports_slack BOOLEAN NOT NULL DEFAULT FALSE,
    supports_webhooks BOOLEAN NOT NULL DEFAULT FALSE,
    supports_advanced_analytics BOOLEAN NOT NULL DEFAULT FALSE,
    supports_audit_logs BOOLEAN NOT NULL DEFAULT FALSE,
    supports_custom_roles BOOLEAN NOT NULL DEFAULT FALSE,
    supports_sso BOOLEAN NOT NULL DEFAULT FALSE,
    supports_priority_support BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS workspace_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    billing_cycle VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL,
    canceled_at TIMESTAMP NULL,
    trial_ends_at TIMESTAMP NULL,
    external_subscription_id VARCHAR(255),
    external_customer_id VARCHAR(255),
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    FOREIGN KEY (plan_id) REFERENCES workspace_plans(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_subscription_workspace ON workspace_subscriptions(workspace_id);
CREATE INDEX idx_subscription_plan ON workspace_subscriptions(plan_id);
CREATE INDEX idx_subscription_status ON workspace_subscriptions(status);

CREATE TABLE IF NOT EXISTS workspace_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL UNIQUE,
    project_count INTEGER NOT NULL DEFAULT 0,
    member_count INTEGER NOT NULL DEFAULT 0,
    storage_used_bytes BIGINT NOT NULL DEFAULT 0,
    webhook_count INTEGER NOT NULL DEFAULT 0,
    api_requests_this_month INTEGER NOT NULL DEFAULT 0,
    last_calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_usage_workspace ON workspace_usage(workspace_id);
