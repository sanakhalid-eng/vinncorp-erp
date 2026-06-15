-- Migration: Add constraints for task self-reference prevention and task-label uniqueness
-- Date: 2026-05-03

-- Prevent task from being its own parent (CHECK constraint)
ALTER TABLE tasks ADD CONSTRAINT chk_no_self_parent CHECK (id != parent_task_id);

-- Ensure unique task-label associations (UNIQUE constraint)
ALTER TABLE task_labels ADD CONSTRAINT uk_task_label UNIQUE (task_id, label_id);
