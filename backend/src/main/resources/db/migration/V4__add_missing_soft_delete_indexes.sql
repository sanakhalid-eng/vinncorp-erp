-- Add deleted_at indexes for all remaining soft-delete entities.
-- These tables extend BaseAuditableEntity and either already use
-- @SQLRestriction("deleted_at IS NULL") or will in the future.
-- Without these indexes, soft-delete filtered queries degrade at scale.

CREATE INDEX idx_roles_deleted_at ON roles(deleted_at);
CREATE INDEX idx_project_invitations_deleted_at ON project_invitations(deleted_at);
CREATE INDEX idx_notifications_deleted_at ON notifications(deleted_at);
CREATE INDEX idx_activity_logs_deleted_at ON activity_logs(deleted_at);
CREATE INDEX idx_webhooks_deleted_at ON webhooks(deleted_at);
CREATE INDEX idx_comments_deleted_at ON comments(deleted_at);
CREATE INDEX idx_task_attachments_deleted_at ON task_attachments(deleted_at);
