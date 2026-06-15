-- Seed default workspace plans
-- Required for workspace creation (assignFreePlanOnWorkspaceCreation)

INSERT INTO workspace_plans (code, name, description, monthly_price, yearly_price, max_projects, max_members, max_storage_gb, max_webhooks, max_api_requests_per_month, supports_slack, supports_webhooks, supports_advanced_analytics, supports_audit_logs, supports_custom_roles, supports_sso, supports_priority_support, is_active)
VALUES
('FREE', 'Free', 'Get started with basic project management features', 0.00, 0.00, 3, 5, 1, 0, 1000, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, TRUE),
('PRO', 'Pro', 'Unlock advanced features for growing teams', 29.00, 290.00, 50, 25, 10, 10, 50000, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, TRUE, TRUE),
('ENTERPRISE', 'Enterprise', 'Full access with priority support and custom limits', 99.00, 990.00, 999999, 999999, 500, 999999, 999999999, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE);
