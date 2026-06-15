# Flyway migration templates

Copy `TEMPLATE__tenant_auditable_table.sql` into `db/migration/V{n}__your_feature.sql` and replace placeholders.

## Checklist

- Full `BaseAuditableEntity` columns on every new table
- `workspace_id BIGINT NOT NULL` for `BaseTenantEntity` children
- `ENGINE=InnoDB` + `utf8mb4_unicode_ci`
- Use `pmt_add_col_if_missing` / `pmt_add_idx_if_missing` for idempotency
- Add composite index `(workspace_id, project_id, created_at)` (or `sprint_id`) for analytics
- Create indexes on FK columns before adding constraints
- Never use unrestricted `findAll()` in repositories — scope by `workspace_id` and `deleted_at IS NULL`
