-- V36: Drop all billing/subscription tables, views, and columns
-- This migration removes the entire billing/subscription system.
-- workspace_usage is preserved (used by AttachmentServiceImpl for storage tracking).

-- Drop views first (depend on tables)
DROP VIEW IF EXISTS v_revenue_by_plan;
DROP VIEW IF EXISTS v_plan_distribution;
DROP VIEW IF EXISTS v_mrr_summary;
DROP VIEW IF EXISTS v_churn_summary;

-- Drop tables (order matters: child tables first)
DROP TABLE IF EXISTS billing_events;
DROP TABLE IF EXISTS workspace_subscription_addons;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS workspace_subscriptions;
DROP TABLE IF EXISTS workspace_plans;
DROP TABLE IF EXISTS workspace_addons;
