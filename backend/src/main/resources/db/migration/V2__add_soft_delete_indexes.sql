-- Improve query performance on soft-delete filtered queries
-- Both tasks and projects use @SQLRestriction("deleted_at IS NULL")
-- which benefits significantly from an index on deleted_at.

CREATE INDEX idx_tasks_deleted_at ON tasks(deleted_at);
CREATE INDEX idx_projects_deleted_at ON projects(deleted_at);
