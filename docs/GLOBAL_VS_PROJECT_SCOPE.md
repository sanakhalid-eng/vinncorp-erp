# Global vs Project Scope: Ownership Boundaries

## Objective
Clearly define which entities are global (system-level / cross-project) and which are scoped within a project. This document serves as the authority for permission checks, data isolation, and future multi-tenant migration.

## Global Entities (System-Level)
These entities exist outside any single project boundary. They are managed at the workspace/organization level.

| Entity | Owner Key | Rationale |
|--------|-----------|-----------|
| `User` | `id` (PK), `email` (unique) | User identity is universal across projects |
| `Role` | `id` (PK), `name` + `scope` (unique) | System roles apply across projects; custom roles may become workspace-scoped |
| `Permission` | `id` (PK), `name` (unique) | Permission definitions are universal |
| `UserRole` | `user_id` + `role_id` | Role assignment is global; may become workspace-scoped |
| `RolePermission` | `role_id` + `permission_id` | Scoped by parent role |
| `RefreshToken` | `token` (unique) | Authentication is global |
| `UserTwoFactor` | `user_id` | 2FA configuration is user-global |
| `EmailVerificationToken` | `token` (unique) | Email verification is global |
| `ScheduledJob` | `id` (PK) | Infrastructure — no user data |
| `ScheduledJobExecution` | `id` (PK) | Infrastructure — no user data |
| `RetryQueue` | `id` (PK) | Infrastructure — no user data |
| `BootstrapLock` | `id` (PK) | Infrastructure — no user data |
| `EmailDelivery` | `id` (PK) | Operational — may gain soft workspace scoping |
| `NotificationPreference` | `user_id` | Personal preference — user-global |
| `ActivityLog` | `id` (PK) | Audit trail — spans all entities; may gain workspace scoping |

## Project-Scoped Entities
These entities are owned by a specific project and should never be accessible outside that project context.

| Entity | Parent Project FK | Cascade Depth |
|--------|-------------------|---------------|
| `Project` | Self (via `owner_id`) | Root — all project entities cascade from here |
| `ProjectMember` | `project_id` (direct) | Direct |
| `ProjectRole` | `project_id` (direct) | Direct |
| `ProjectInvitation` | `project_id` (via project or direct) | Direct |
| `Board` | `project_id` (direct) | Direct |
| `BoardColumn` | `board_id` -> `project_id` | Depth 2 |
| `Sprint` | `project_id` (direct) | Direct |
| `SprintBurndown` | `sprint_id` -> `project_id` | Depth 2 |
| `Task` | `project_id` (direct) | Direct |
| `TaskAttachment` | `task_id` -> `project_id` | Depth 2 |
| `TaskDependency` | `task_id` -> `project_id` | Depth 2 |
| `TaskLabel` | `task_id` + `label_id` -> `project_id` | Depth 2 |
| `TaskSprint` | `task_id` + `sprint_id` -> `project_id` | Depth 2 |
| `TimeLog` | `task_id` -> `project_id` | Depth 2 |
| `TimesheetApproval` | `task_id` -> `project_id` | Depth 2 |
| `ActiveTimer` | `task_id` -> `project_id` | Depth 2 |
| `Comment` | `task_id` -> `project_id` | Depth 2 |
| `CommentEdit` | `comment_id` -> `task_id` -> `project_id` | Depth 3 |
| `CommentMention` | `comment_id` -> `task_id` -> `project_id` | Depth 3 |
| `CommentReaction` | `comment_id` + `user_id` -> `task_id` -> `project_id` | Depth 3 |
| `Label` | `project_id` (direct) | Direct |
| `WorkflowStatus` | `project_id` (direct) | Direct |
| `WorkflowTransition` | `workflow_status_id` -> `project_id` | Depth 2 |
| `WorkflowTransitionRule` | `workflow_transition_id` -> `project_id` | Depth 3 |
| `Webhook` | `project_id` (direct) | Direct |
| `WebhookDelivery` | `webhook_id` -> `project_id` | Depth 2 |
| `WebhookDeliveryStatus` | `webhook_delivery_id` -> `project_id` | Depth 3 |
| `SlackIntegration` | `project_id` (direct) | Direct |
| `SlackUserMapping` | `slack_integration_id` -> `project_id` | Depth 2 |

## Cross-Boundary Entities
These entities reference both global and project-scoped data, requiring careful isolation.

| Entity | Global References | Project References | Isolation Strategy |
|--------|------------------|--------------------|--------------------|
| `ProjectMember` | `user_id` (User) | `project_id` (Project) | Filter queries by `project_id`; user identity is shared |
| `TimeLog` | `user_id` (User) | `task_id` (Task -> Project) | Filter by task -> project membership of user |
| `ActiveTimer` | `user_id` (User) | `task_id` (Task -> Project) | Filter by task -> project membership of user |
| `CommentMention` | `user_id` (User) | `comment_id` (Comment -> Task -> Project) | Filter by comment -> task -> project |
| `CommentReaction` | `user_id` (User) | `comment_id` (Comment -> Task -> Project) | Filter by comment -> task -> project |
| `ActivityLog` | `user_id` (User, nullable) | `entity_id` (polymorphic) | Entity-based filtering with type discriminator |

## Permission Boundary Rules

1. **Global entities** require system-level permissions (e.g., `MANAGE_SYSTEM`, `VIEW_USERS`)
2. **Project-scoped entities** require project-level permissions (e.g., `VIEW_PROJECT`, `EDIT_TASK`)
3. **Admin role** bypasses all project-level permission checks but does not bypass entity-level visibility filters
4. **Project membership** is the minimum requirement for accessing any project-scoped entity
5. **Cross-boundary reads** (e.g., listing all tasks across projects) require either:
   - Admin role, OR
   - Explicit cross-project permission

## Migration Notes

When introducing multi-tenancy:
- Global entities remain at the organization/workspace level
- Project-scoped entities gain `workspace_id` (or `organization_id`) as an additional filter
- Cross-boundary queries become workspace-filtered views
- `MembershipResolver` switches from single-tenant to organization-based membership
- Permission checks route through `PermissionResolver` which adds workspace context
