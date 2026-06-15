# Module Boundaries — VinnCorp ERP Architecture Contract

**Status:** Active
**Last updated:** 2026-06-05
**Scope:** Backend (Spring Boot) and Frontend (React) — applies to both

This document is the single source of truth for module ownership. Any new entity,
service, controller, or DTO must declare which module it belongs to. Any cross-module
read or write must use the patterns described in `DEPENDENCY_RULES.md`.

> If two modules need the same data, **one owns it** and exposes a public API for the other.

---

## 1. The Module Map

| # | Module | Root package (target) | Purpose |
|---|--------|----------------------|---------|
| 1 | `shared` | `com.vinncorp.erp.shared` | Cross-cutting plumbing. No business logic. |
| 2 | `core` | `com.vinncorp.erp.core` | Auth, identity, tenancy, notifications, audit, integrations. |
| 3 | `modules/projects` | `com.vinncorp.erp.modules.projects` | PM tool: projects, tasks, boards, sprints, time, workflow, analytics. |
| 4 | `modules/hr` | `com.vinncorp.erp.modules.hr` | Employees, departments, attendance, leave, payroll. |
| 5 | `modules/crm` | `com.vinncorp.erp.modules.crm` | Leads, customers, deals, activities. |
| 6 | `modules/finance` | `com.vinncorp.erp.modules.finance` | Invoices, payments, expenses, budgets. |

### What is NOT a module
- `reports` is not a module — reporting is cross-cutting and lives in `shared/reports` (read-only views over module events).
- `integrations` is not a module — webhooks, slack, etc. live in `core/integrations` because every module needs them.
- `workflow` is not a module — the current workflow engine is project-scoped. It lives in `modules/projects/workflow/`. HR and Finance will build their own simpler workflows first. A `core/automation` may emerge later.

---

## 2. Entity Ownership Table

Every entity belongs to exactly one module. Cross-module references go through a public service interface, never through direct entity import.

### 2.1 Core-owned (foundational)

| Entity | Notes |
|---|---|
| `User` | Auth identity (login, email, password, 2FA, avatar) |
| `UserRole` | RBAC mapping |
| `UserTwoFactor` | 2FA secret |
| `Role`, `Permission`, `RolePermission` | RBAC system |
| `Workspace` | Tenant root |
| `WorkspaceMember` | User ↔ Workspace membership |
| `WorkspaceRole` | Workspace-level role (OWNER, ADMIN, MEMBER) |
| `WorkspacePermissionMatrix` | Per-workspace permission overrides |
| `WorkspaceInvitation` | Pending invites to a workspace |
| `WorkspaceNote` | General workspace-level notes |
| `WorkspaceUsage` | Storage / usage tracking |
| `Notification` | In-app notification feed |
| `NotificationPreference` | Per-user notification settings |
| `EmailDelivery` | Outbound email audit log |
| `EmailVerificationToken` | Email verification flow |
| `PasswordResetToken` | Password reset flow |
| `RefreshToken` | JWT refresh flow |
| `ActivityLog` | General audit (cross-module) |
| `ActivityIntelligenceSummary` | AI summaries (cross-module) |
| `Webhook`, `WebhookDelivery` | Outbound webhook config + log |
| `SlackIntegration`, `SlackUserMapping` | Slack integration config |
| `FeatureFlag` | Runtime feature toggles |
| `SavedSearch` | Cross-module user searches |
| `CommandPaletteRecent` | UI personalization |
| `KnowledgeArticle` | KB docs (cross-module) |
| `ScheduledJob`, `ScheduledJobExecution` | Job runner config + log |
| `BackgroundJob`, `RetryQueue`, `BootstrapLock` | Infra plumbing |
| `SlaPolicy`, `EscalationRule`, `EscalationTarget`, `EscalationExecution` | Generic SLA + escalation (see §2.4) |

### 2.2 Projects module-owned

| Entity | Notes |
|---|---|
| `Project` | Project aggregate root |
| `ProjectMember`, `ProjectRole` | Per-project membership + roles |
| `ProjectInvitation` | Project-scoped invites |
| `ProjectTemplateEntity` | Reusable project blueprints |
| `PortfolioRoadmapItem` | High-level portfolio planning |
| `Task`, `TaskAttachment`, `TaskDependency`, `TaskLabel` | Core task model |
| `TaskSprint` | Task ↔ Sprint link |
| `Comment`, `CommentEdit`, `CommentMention`, `CommentReaction` | PM-scoped comments |
| `Label` | Project-scoped labels |
| `Board`, `BoardColumn` | Kanban boards |
| `Sprint`, `SprintBurndown`, `SprintCapacity`, `SprintMetricSnapshot`, `SprintVelocitySnapshot` | Sprint management |
| `RecurringTaskTemplate`, `RecurringTaskOccurrence` | Recurring tasks |
| `TimeLog`, `ActiveTimer`, `TimesheetApproval` | Time tracking |
| `WorkflowRule`, `WorkflowCondition`, `WorkflowStatus`, `WorkflowTransition`, `WorkflowExecutionLog` | PM workflow engine |
| `ApprovalWorkflow`, `ApprovalStep`, `ApprovalRequest` | PM approvals |
| All `*Snapshot` (CapacityForecast, CriticalPath, DeliveryPredictability, Estimation, ExecutionRisk, MonteCarlo, Productivity, Analytics) | PM-scoped analytics |

### 2.3 HR module-owned (planned)

| Entity | Notes |
|---|---|
| `Employee` | HR business identity (linked 1:1 to `User` via `userId` FK) |
| `Department` | Org tree |
| `Designation` | Job title |
| `AttendanceRecord` | Daily check-in/out (later) |
| `LeaveRequest`, `LeaveBalance` | Leave management (later) |
| `PayrollRun`, `Payslip` | Payroll (later) |

### 2.4 SLA and Escalation — moved to Core

The existing `TaskSLA` and `EscalationRule` entities will be renamed and re-homed as **generic patterns** in `core/automation/`:

| New name | Purpose |
|---|---|
| `SlaPolicy` | Defines a target time + breach rule. Reusable across modules. |
| `EscalationRule` | Triggers when an SLA is at risk or breached. |
| `EscalationTarget` | Who/what gets notified or actioned. |
| `EscalationExecution` | Audit log of an escalation firing. |

Concrete reuse:
- **Projects:** task response/completion SLAs
- **HR:** leave request approval SLA
- **Finance:** invoice approval SLA
- **CRM:** deal follow-up SLA

> This is a v2 change. For v1, leave the existing PM-scoped SLA entities in `modules/projects/` and add `core/automation/` as a parallel system. Migrate PM SLAs in a later phase.

### 2.5 Future modules

| Module | Future entities | Owner |
|---|---|---|
| `modules/crm` | `Lead`, `Customer`, `Deal`, `Activity` | CRM |
| `modules/finance` | `Invoice`, `Payment`, `Expense`, `Budget`, `Account` | Finance |

---

## 3. Cross-Module User / Employee Decision

**Decision:** `User` and `Employee` are separate entities.

- `User` (in `core/user`) is the auth identity. It has `email`, `password`, 2FA, avatar.
- `Employee` (in `modules/hr/employee`) is the HR business identity. It has `userId` FK, `employeeCode`, `departmentId`, `designationId`, `joiningDate`, `managerId`, `salary`, etc.

```
core.User
   │
   └── modules.hr.Employee   (one-to-one, optional)
```

**Why:**
- Authentication is a system concern; HR data is a business concern.
- External users (customers, vendors, portal users in the future) can be `User` without being `Employee`.
- This matches the long-term ERP direction (customer portal, vendor portal).

**Implementation note:** When inviting a person to the ERP, the flow is:
1. Create `User` (auth)
2. Optionally create `Employee` (HR record)
3. Add to `WorkspaceMember`
4. Add to relevant module memberships

---

## 4. Comments — PM-only in v1

Comments live in `modules/projects/comment/`. Do **not** build `core/comment` yet.

When HR adds Employee Notes / Leave Discussions, we'll extract a shared `core/collaboration/comment/` interface. That refactor happens when the second consumer appears, not before.

---

## 5. Workflow / Automation — PM-only in v1

Workflows live in `modules/projects/workflow/`. PM workflows are tied to tasks, boards, statuses, transitions. They are not general enough to move yet.

HR will build its own simpler workflows first (e.g. leave approval: 1-step, manager-only). Only when we see repetition across modules do we extract a `core/automation/` engine.

---

## 6. Database Strategy — Single Schema, Table Prefixes

**Decision:** one database schema, tables named with module prefixes.

| Module | Table prefix | Example |
|---|---|---|
| core | (no prefix) | `users`, `roles`, `workspaces`, `notifications` |
| projects | (no prefix, legacy) | `projects`, `tasks`, `comments` |
| hr | `hr_` | `hr_employees`, `hr_departments`, `hr_attendance` |
| crm | `crm_` | `crm_leads`, `crm_customers` |
| finance | `fin_` | `fin_invoices`, `fin_payments`, `fin_expenses` |
| shared | (no prefix) | `feature_flags`, `webhooks`, `email_deliveries` |

**Why:**
- Simpler Flyway migrations
- Easier ad-hoc reporting
- One team, one deploy, one DB
- Can be split into separate schemas later if scaling demands it

**Flyway migration locations** (after refactor):
```
db/migration/                   # core + shared
db/migration/modules/projects/
db/migration/modules/hr/
db/migration/modules/crm/
db/migration/modules/finance/
```

---

## 7. Duplicate Code

There is a duplicate `NotificationEventProcessor` — one in `events/`, one in `notifications/`. After verifying both do the same work, **keep only the `core/notification/` version** and delete the legacy one.

---

## 8. Change Log

| Date | Change | Author |
|---|---|---|
| 2026-06-05 | Initial draft of ERP module boundaries | refactor |
