-- Migration: Add sprint summary fields and backlog support
-- Date: 2026-05-04

ALTER TABLE sprints
    ADD COLUMN IF NOT EXISTS summary_total_tasks INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS summary_completed_tasks INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS summary_carried_forward INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP NULL;
