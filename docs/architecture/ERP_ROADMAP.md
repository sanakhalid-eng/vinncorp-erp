# ERP Roadmap — VinnCorp ERP

**Status:** Active
**Last updated:** 2026-06-05
**Companion to:** `MODULE_BOUNDARIES.md`

This is the build order. Each stage is independently shippable. Each stage ends with a green build and a deployable artifact. We do not move to the next stage until the current one is verified in production (or in a staging environment that mirrors production).

---

## Guiding Principles

1. **No big-bang refactors.** The system must keep compiling and running after every commit.
2. **Behavior changes are not allowed during moves.** We move code; we don't rewrite it.
3. **Each stage is its own PR (or small set of PRs).** Easier to review, easier to revert.
4. **Test what we have before we change it.** If a stage lacks tests, write them first.

---

## Stage 0 — Documentation (done in this iteration)

- [x] `docs/architecture/MODULE_BOUNDARIES.md` — entity ownership contract
- [x] `docs/architecture/DEPENDENCY_RULES.md` — what can depend on what
- [x] `docs/architecture/EVENT_CATALOG.md` — cross-module events
- [x] `docs/architecture/ERP_ROADMAP.md` — this file

**Status:** ✅ Complete

---

## Stage 1 — Package Skeleton (no logic changes)

Create the new package directories. No file moves, no package renames. Empty packages are fine.

```
backend/src/main/java/com/vinncorp/erp/
├── shared/
│   ├── security/
│   ├── storage/
│   ├── email/
│   ├── exception/
│   ├── filter/
│   ├── cache/
│   ├── tenant/
│   ├── mapper/
│   ├── config/
│   ├── scheduling/
│   ├── websocket/
│   └── util/
├── core/
│   ├── auth/
│   ├── user/
│   ├── workspace/
│   ├── notification/
│   ├── audit/
│   ├── integrations/
│   ├── automation/        ← future home for SLA/Escalation
│   └── shared/            ← convenience re-exports, used by modules
└── modules/
    ├── projects/
    │   ├── api/
    │   ├── domain/
    │   ├── dto/
    │   ├── service/
    │   ├── repository/
    │   ├── event/
    │   ├── listener/
    │   ├── automation/    ← current workflow engine
    │   ├── analytics/     ← current PM analytics
    │   ├── comment/
    │   └── time/
    ├── hr/
    │   ├── api/
    │   ├── domain/
    │   ├── dto/
    │   ├── service/
    │   ├── repository/
    │   └── event/
    ├── crm/               ← empty for now
    └── finance/           ← empty for now
```

**Verification:** project still compiles (the new packages are empty, so nothing breaks).

**Estimate:** 1 hour (just `mkdir`).

---

## Stage 2 — Core Extraction

Move the core-owned classes from the legacy root package into `com.vinncorp.erp.core.*`. The legacy root package is `com.projectmanager.projectmanagementbackend.*`.

### 2.1 Sub-steps

For each sub-package below, do this in its own commit:

| # | Sub-package | Classes to move |
|---|---|---|
| 2.1 | `core/auth` | `AuthController`, `TwoFactorController`, `AuthService(Impl)`, `TwoFactorService`, `PasswordResetService(Impl)`, `RefreshTokenService`, `EmailVerificationTokenRepository`, `PasswordResetTokenRepository`, `RefreshTokenRepository`, `EmailVerificationToken`, `PasswordResetToken`, `RefreshToken`, `UserTwoFactor`, `TwoFactorRepository` |
| 2.2 | `core/user` | `UserController`, `UserRoleController`, `UserService(Impl)`, `UserRoleService(Impl)`, `UserRepository`, `UserRoleRepository`, `User`, `UserRole` |
| 2.3 | `core/workspace` | `WorkspaceController`, `WorkspaceInvitationController`, `WorkspaceNoteController`, `WorkspaceService`, `WorkspaceInvitationService`, `WorkspaceNoteService(Impl)`, all `Workspace*` entities, repos |
| 2.4 | `core/notification` | `NotificationController`, `NotificationPreferenceController`, `NotificationService(Impl)`, `NotificationIntelligenceService(Impl)`, `Notification` entity, `NotificationPreference` entity, repos, `events/EventPublisher` |
| 2.5 | `core/audit` | `ActivityLogController`, `ActivityLogService(Impl)`, `ActivityIntelligence*`, `ActivityLog`, `ActivityIntelligenceSummary` |
| 2.6 | `core/integrations` | `Webhook*`, `Slack*`, `FeatureFlag*` |
| 2.7 | `shared/*` | All `security/`, `storage/`, `email/`, `exception/`, `filter/`, `cache/`, `tenant/`, `config/`, `scheduling/`, `websocket/`, generic `mapper/` classes |

### 2.2 Process

For each sub-step:

1. **Move** files (cut from old location, paste into new location with new package declaration).
2. **Update imports** in all consumer files across the codebase.
3. **Compile**: `mvn compile` must succeed.
4. **Test** (if tests exist for the moved classes).
5. **Commit** with a clear message: `refactor(core): move auth to core/`.
6. **Deploy** to staging and smoke-test.

### 2.3 Verification

- `mvn compile` succeeds
- `mvn test` (if tests exist) passes
- Backend boots, `/api/auth/login` works
- A user can log in, view a workspace, see a notification (smoke test)

**Estimate:** 2-3 days (mechanical but many files).

---

## Stage 3 — Projects Module Extraction

Move the projects-owned classes from the legacy root package into `com.vinncorp.erp.modules.projects.*`.

### 3.1 Sub-steps

| # | Sub-package | Classes to move |
|---|---|---|
| 3.1 | `projects/domain` | `Project`, `ProjectMember`, `ProjectRole`, `RolePermission` (project-scope), `ProjectInvitation`, `ProjectTemplateEntity`, `PortfolioRoadmapItem` |
| 3.2 | `projects/task` | `Task`, `TaskAttachment`, `TaskDependency`, `TaskLabel`, `TaskSprint`, all `Task*` repos + services + controllers + DTOs |
| 3.3 | `projects/board` | `Board`, `BoardColumn`, `BoardService(Impl)`, `BoardController` |
| 3.4 | `projects/sprint` | `Sprint`, all `Sprint*` snapshots, `Sprint*` services, `Sprint*` controllers |
| 3.5 | `projects/comment` | `Comment`, `CommentEdit`, `CommentMention`, `CommentReaction`, `Comment*` services, `CommentController` |
| 3.6 | `projects/time` | `TimeLog`, `ActiveTimer`, `TimesheetApproval`, `TimeTrackingService`, `TimeTrackingController`, `TimesheetApprovalController` |
| 3.7 | `projects/template` | `ProjectTemplateService`, `TemplateController`, `ProjectTemplateEntity` (moved from 3.1) |
| 3.8 | `projects/automation` | `Workflow*` entities, services, controllers, `EscalationService(Impl)`, `EscalationController`, `SLA*` |
| 3.9 | `projects/analytics` | All `*Snapshot` repos, `*Analytics*` services, `*Forecasting*`, `Velocity*`, `CriticalPath*`, `ExecutionRisk*`, `MonteCarlo*`, `Estimation*`, `Productivity*` |

### 3.2 Process

Same as Stage 2. Move, update imports, compile, test, commit, smoke-test.

**Tricky bits:**
- The `NotificationEventProcessor` (in `events/`) and a duplicate in `notifications/` — keep only one (the `core/notification/` version).
- `analytics/` will have lots of cross-cutting reads. After the move, verify none of them reach into other modules' entities directly — they should call services.
- The workflow engine uses `Task`, `Board`, `WorkflowStatus`. Make sure these are all in `projects/` before moving the engine.

### 3.3 Verification

- `mvn compile` succeeds
- `mvn test` passes
- A user can create a project, add tasks, drag on a board, log time
- WebSocket notifications still work
- All analytics endpoints respond

**Estimate:** 3-5 days.

---

## Stage 4 — First New Module: HR

Build the HR module from scratch. This is **new code**, not a refactor.

### 4.1 V1 scope (minimum lovable)

- `Employee` entity (linked to `User` via `userId`)
- `Department` entity (tree structure)
- `Designation` entity
- CRUD for all three
- A simple `EmployeeListPage` in the frontend
- A way to link an existing `User` to a new `Employee`

### 4.2 V2 scope (after v1 is solid)

- `AttendanceRecord` (check-in/out, daily summary)
- `LeaveRequest` + `LeaveBalance` + simple approval flow
- Integration with `projects/`: "show me all tasks assigned to employee X"
- Integration with `core/notification/`: notify manager on leave request

### 4.3 V3 scope (later)

- `PayrollRun` + `Payslip`
- Integration with `finance/`: post payroll to GL

### 4.4 Process

1. Create `modules/hr/` skeleton (already done in Stage 1).
2. Create database migration: `V37__create_hr_module.sql` (in `db/migration/modules/hr/`).
3. Create entities: `Employee`, `Department`, `Designation`.
4. Create repositories, services, DTOs, controllers.
5. Wire up the frontend module: `frontend/src/modules/hr/`.
6. Add lazy-loaded route in `app/routes.tsx`.
7. Add the "HR" entry in the app navigation.
8. Test, commit, deploy.

### 4.5 Verification

- A user can create a department, designation, employee.
- A user can view all employees in a list.
- A user can click an employee and see their detail page (initially just basic info).

**Estimate:** 1 week (v1).

---

## Stage 5 — CRM Module

Same pattern as Stage 4, but for CRM.

**V1 scope:** `Lead`, `Customer`, `Activity` (touchpoint log), `Deal` (basic pipeline).

**Estimate:** 1-2 weeks (v1).

---

## Stage 6 — Finance Module

Last in the build order. Can read from projects, hr, crm (for billable time, employee info, customer info).

**V1 scope:** `Invoice`, `Payment`, `Expense` (basic), `Budget` (per project, per department).

**Estimate:** 2-3 weeks (v1).

---

## Stage 7 — Cross-Module Automation Engine (optional v2)

Extract the current PM `Workflow` engine and the new `core/automation/SlaPolicy` + `EscalationRule` into a generic engine that all modules can use.

This happens only when we see real repetition across modules — not before.

---

## Open Questions

- [ ] Where do `AnalyticsSnapshot` and similar live — projects/analytics (current) or shared/reports (future)?
- [ ] When do we extract a shared `core/comment`? After HR's first need for it.
- [ ] When do we extract a shared `core/automation`? After HR + Finance both have workflows.
- [ ] Do we want a separate `core/file` module for attachments, or keep them per-module?

---

## Change Log

| Date | Change | Author |
|---|---|---|
| 2026-06-05 | Initial roadmap draft | refactor |
