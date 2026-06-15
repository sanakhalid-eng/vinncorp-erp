-- Billing Analytics Views
-- Phase 2F: Analytics Materialized Views

-- Monthly Recurring Revenue view
CREATE OR REPLACE VIEW v_mrr AS
SELECT
    DATE_FORMAT(s.started_at, '%Y-%m') AS month,
    SUM(CASE WHEN s.billing_cycle = 'MONTHLY' THEN p.monthly_price
             WHEN s.billing_cycle = 'YEARLY' THEN p.yearly_price / 12
        END) AS mrr,
    COUNT(DISTINCT s.workspace_id) AS active_subscriptions
FROM workspace_subscriptions s
JOIN workspace_plans p ON s.plan_id = p.id
WHERE s.status IN ('ACTIVE', 'TRIAL')
GROUP BY DATE_FORMAT(s.started_at, '%Y-%m')
ORDER BY month DESC;

-- Plan distribution view
CREATE OR REPLACE VIEW v_plan_distribution AS
SELECT
    p.code AS plan_code,
    p.name AS plan_name,
    COUNT(s.id) AS subscriber_count,
    SUM(CASE WHEN s.status = 'ACTIVE' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN s.status = 'TRIAL' THEN 1 ELSE 0 END) AS trial_count,
    SUM(CASE WHEN s.billing_cycle = 'MONTHLY' THEN 1 ELSE 0 END) AS monthly_count,
    SUM(CASE WHEN s.billing_cycle = 'YEARLY' THEN 1 ELSE 0 END) AS yearly_count
FROM workspace_plans p
LEFT JOIN workspace_subscriptions s ON p.id = s.plan_id
GROUP BY p.code, p.name
ORDER BY subscriber_count DESC;

-- Churn rate view
CREATE OR REPLACE VIEW v_churn_rate AS
SELECT
    DATE_FORMAT(canceled_at, '%Y-%m') AS month,
    COUNT(id) AS canceled_count,
    COUNT(id) * 100.0 / NULLIF(LAG(COUNT(id)) OVER (ORDER BY DATE_FORMAT(canceled_at, '%Y-%m')), 0) AS churn_rate
FROM workspace_subscriptions
WHERE canceled_at IS NOT NULL
GROUP BY DATE_FORMAT(canceled_at, '%Y-%m')
ORDER BY month DESC;

-- Revenue by plan view
CREATE OR REPLACE VIEW v_revenue_by_plan AS
SELECT
    p.code AS plan_code,
    p.name AS plan_name,
    SUM(i.amount) AS total_revenue,
    COUNT(i.id) AS invoice_count,
    SUM(CASE WHEN i.status = 'PAID' THEN i.amount ELSE 0 END) AS collected_revenue
FROM invoices i
JOIN workspace_plans p ON i.plan_code = p.code COLLATE utf8mb4_0900_ai_ci
WHERE i.deleted_at IS NULL
GROUP BY p.code, p.name
ORDER BY total_revenue DESC;
