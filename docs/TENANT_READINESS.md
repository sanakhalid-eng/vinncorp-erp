# Tenant Readiness Audit

## Objective
Prepare every entity for multi-tenant isolation. This document catalogs each entity's ownership boundary, identifies cross-tenant leakage risks, and defines the migration strategy for introducing organization/workspace scoping at the database level.

## Entity Ownership Classification

### Workspace-Level Entities (Rooted to a single implicit workspace)
These entities do not currently have a `workspace_id` column. In a multi-tenant model, they would each gain `workspace_id` (or `organization_id`).

| Entity | Current Owner/Key | Isolation Risk | Strategy |
|--------|------------------|----------------|----------|
| `User` | `email` (unique, global) | Users are global — no workspace boundary | Introduce `workspace_members` join table; keep `users` global but scope membership |
| `Role` | Name (unique per scope) | Roles are system-scoped or global | Add `workspace_id` nullable; system roles remain global, custom roles become workspace-scoped |
| `Permission` | Name | Permissions are global | No change — permissions are definitional, not tenant-scoped |
| `UserRole` | `user_id` + `role_id` | Cross-workspace role assignment risk | Add `workspace_id` to scope role assignments |
| `RolePermission` | `role_id` + `permission_id` | No direct risk | Role-scoped by parent role; no direct change |
| `ProjectInvitation` | `token` (unique), `invited_by` | Invitations are per-workspace | Add `workspace_id` (or implicit via project) |
| `RefreshToken` | `token` (unique), `user_id` | User-bound, not workspace-bound | Add `workspace_id` or scope by user context |
| `TwoFactor` | `user_id` | User-bound | No change — user authentication is global |

### Project-Level Entities (Scoped by project)
These already have a `project_id` foreign key or are implicitly project-scoped. Multi-tenancy primarily affects how projects themselves are scoped.

| Entity | Current Scoping | Isolation Risk | Strategy |
|--------|----------------|----------------|----------|
| `Project` | `owner_id` (User) | Projects are user-owned, not workspace-owned | Add `workspace_id`; migrate `owner_id` to `workspace_id` for tenant boundary |
| `ProjectMember` | `project_id` + `user_id` | Scoped by project | When project gains `workspace_id`, member scope is transitive |
| `Board` | `project_id` | Scoped by project | Transitive via project |
| `BoardColumn` | `board_id` | Scoped by board | Transitive via board/project |
| `Task` | `project_id` | Scoped by project | Transitive via project |
| `TaskAttachment` | `task_id` | Scoped by task | Transitive via task/project |
| `TaskDependency` | `task_id` | Scoped by task | Transitive via task/project |
| `TaskLabel` | `task_id`, `label_id` | Scoped by task | Transitive via task/project |
| `TaskSprint` | `task_id`, `sprint_id` | Scoped by task/sprint | Transitive via task/sprint/project |
| `Sprint` | `project_id` | Scoped by project | Transitive via project |
| `SprintBurndown` | `sprint_id` | Scoped by sprint | Transitive via sprint/project |
| `TimeLog` | `task_id`, `user_id` | Scoped by task | Transitive via task/project |
| `TimesheetApproval` | `task_id`, `user_id` | Scoped by task | Transitive via task/project |
| `ActiveTimer` | `task_id`, `user_id` | Scoped by task | Transitive via task/project |
| `Comment` | `task_id` | Scoped by task | Transitive via task/project |
| `CommentEdit` | `comment_id` | Scoped by comment | Transitive via comment/task/project |
| `CommentMention` | `comment_id` | Scoped by comment | Transitive via comment/task/project |
| `CommentReaction` | `comment_id`, `user_id` | Scoped by comment | Transitive via comment/task/project |
| `Label` | `project_id` | Scoped by project | Transitive via project |
| `WorkflowStatus` | `project_id` | Scoped by project | Transitive via project |
| `WorkflowTransition` | `workflow_status_id` | Scoped by workflow status | Transitive via status/project |
| `WorkflowTransitionRule` | `workflow_transition_id` | Scoped by workflow transition | Transitive via transition/status/project |
| `Webhook` | `project_id` | Scoped by project | Transitive via project |
| `WebhookDelivery` | `webhook_id` | Scoped by webhook | Transitive via webhook/project |
| `WebhookDeliveryStatus` | `webhook_delivery_id` | Scoped by delivery | Transitive via delivery/webhook/project |
| `SlackIntegration` | `project_id` | Scoped by project | Transitive via project |
| `SlackUserMapping` | `slack_integration_id` | Scoped by integration | Transitive via integration/project |
| `ProjectRole` | `project_id` | Scoped by project | Transitive via project |

### Operational/Infrastructure Entities (Global or System-Scoped)
These entities are not user-owned and may remain global or gain soft-scoping.

| Entity | Current Scoping | Isolation Risk | Strategy |
|--------|----------------|----------------|----------|
| `ScheduledJob` | Global | No user data | Remain global |
| `ScheduledJobExecution` | `scheduled_job_id` | No user data | Remain global |
| `RetryQueue` | Global | No user data | Remain global |
| `EmailDelivery` | Global | Contains user emails | Add `workspace_id` nullable for audit trail |
| `EmailVerificationToken` | `user_id` | User-bound | No change — email verification is global |
| `Notification` | `user_id` (recipient), `actor_id` | User-bound notifications | Add `workspace_id` to scope notification visibility |
| `NotificationPreference` | `user_id` | User-bound | No change — preferences are personal |
| `BootstrapLock` | Global | Infrastructure lock | Remain global |
| `ActivityLog` | `user_id` (nullable for system actions) | Contains audit trail across entities | Add `workspace_id` nullable; migrate when entities are scoped |

## Migration Strategy

### Phase 1: Schema Preparation (No Code Changes)
- Add `workspace_id` columns (nullable) to all workspace-level and project-level tables
- Create `workspaces` table if not exists (migration from implicit single-workspace model)
- Add foreign key constraints (nullable initially)

### Phase 2: Backfill
- For single-tenant installations: backfill `workspace_id` to a default workspace ID
- Create migration scripts that assign all existing projects/users to the default workspace

### Phase 3: Enforce Non-Null
- After backfill, make `workspace_id` NOT NULL
- Add composite unique constraints: `(workspace_id, <entity-specific-unique-constraint>)`

### Phase 4: Application Layer
- Update all repositories to filter by `workspace_id`
- Update `MembershipResolver` to resolve organization-level membership
- Introduce `OrganizationMembershipResolver` alongside `SingleTenantMembershipResolver`

### Phase 5: API Scoping
- Add workspace context to all API endpoints
- Introduce workspace-switching mechanism in frontend
- Deprecate global queries

## Key Risks
1. **User identity is global** — email uniqueness must remain global; workspace scoping affects membership, not identity
2. **Activity log is entity-agnostic** — migration to workspace-scoped activity logs requires careful foreign key analysis
3. **System roles vs custom roles** — system roles (ADMIN, USER) must remain global; custom roles become workspace-scoped
4. **Notifications bridge workspaces** — a user may receive notifications across workspaces; filtering must happen at display layer
