# Event Catalog — VinnCorp ERP

**Status:** Draft
**Last updated:** 2026-06-05
**Companion to:** `MODULE_BOUNDARIES.md`, `DEPENDENCY_RULES.md`

This catalog lists every cross-module domain event. New events are added here **before** they are implemented, and updated here when their payload changes.

> **Convention:** an event name is always past tense — it states what **happened**, not what to do. Subscribers decide what to do.

---

## 1. Event Format

Every event:
- Is an immutable POJO (final fields, no setters)
- Lives in the **publisher's** `event/` package
- Has a `LocalDateTime occurredAt` field
- Has a unique `eventId` (UUID)
- Carries IDs and primitive data, **never entity references**

```java
public record ProjectCreatedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        Long projectId,
        Long workspaceId,
        Long createdByUserId
) implements DomainEvent { }
```

---

## 2. Current Events (legacy)

These events already exist in the codebase and will be migrated to the new module homes during the refactor. Their payload will not change in v1 of the refactor.

### From `core/auth` (after refactor)

| Event | Trigger | Subscribers |
|---|---|---|
| `UserRegisteredEvent` | New user signs up | `notification/`, `audit/` |
| `UserEmailVerifiedEvent` | User verifies email | `notification/`, `audit/` |
| `UserLoggedInEvent` | Successful login | `audit/` |
| `PasswordResetRequestedEvent` | Reset requested | `notification/` (email send) |
| `PasswordChangedEvent` | Password updated | `audit/` |

### From `core/workspace`

| Event | Trigger | Subscribers |
|---|---|---|
| `WorkspaceCreatedEvent` | New workspace | `notification/`, `audit/` |
| `WorkspaceMemberAddedEvent` | User joined | `notification/`, `audit/`, `projects/` (default project template?) |
| `WorkspaceMemberRemovedEvent` | User left/removed | `audit/`, `projects/`, `hr/` (deactivate employee) |
| `WorkspaceOwnerChangedEvent` | Ownership transfer | `audit/` |

### From `modules/projects`

| Event | Trigger | Subscribers |
|---|---|---|
| `ProjectCreatedEvent` | New project | `notification/`, `audit/`, `analytics/` |
| `ProjectDeletedEvent` | Project deleted | `audit/`, `analytics/` |
| `ProjectMemberAddedEvent` | User added to project | `notification/`, `audit/` |
| `ProjectMemberRemovedEvent` | User removed | `notification/`, `audit/` |
| `TaskCreatedEvent` | New task | `notification/`, `audit/`, `analytics/` |
| `TaskAssignedEvent` | Task assignee set | `notification/`, `audit/` |
| `TaskStatusChangedEvent` | Status transition | `notification/`, `audit/`, `analytics/` |
| `TaskCompletedEvent` | Task marked done | `notification/`, `audit/`, `analytics/` |
| `TaskDeletedEvent` | Task deleted | `audit/`, `analytics/` |
| `CommentAddedEvent` | Comment posted | `notification/` (mentions, replies), `audit/` |
| `TimeLoggedEvent` | Time log created | `audit/`, `analytics/`, `finance/` (billable) |
| `WorkflowRuleTriggeredEvent` | Workflow fired | `notification/`, `audit/` |

### From `modules/hr` (planned)

| Event | Trigger | Subscribers |
|---|---|---|
| `EmployeeCreatedEvent` | New employee | `core/notification/` (welcome email), `core/audit/` |
| `EmployeeTerminatedEvent` | Employee left | `projects/` (reassign tasks), `finance/` (final payroll), `audit/` |
| `LeaveRequestedEvent` | Leave request submitted | `notification/` (to approver), `audit/` |
| `LeaveApprovedEvent` | Leave approved | `notification/`, `audit/` |

### From `modules/finance` (planned)

| Event | Trigger | Subscribers |
|---|---|---|
| `InvoiceIssuedEvent` | Invoice created | `notification/`, `crm/` (customer record), `audit/` |
| `PaymentReceivedEvent` | Payment recorded | `notification/`, `crm/`, `audit/`, `analytics/` |
| `ExpenseSubmittedEvent` | Expense submitted | `notification/`, `audit/` |

---

## 3. Adding a New Event

1. Define the event class in the publisher's `event/` package.
2. Add a row to the table above (this file).
3. Publish via `EventPublisher` (Spring `ApplicationEventPublisher`).
4. Subscribers annotate with `@EventListener` and `@Async`.
5. **Update this catalog** if the payload changes.

---

## 4. Anti-Patterns

- ❌ A subscriber that mutates the publisher's entity (use a service call instead).
- ❌ A subscriber that throws an unchecked exception and rolls back the publisher's transaction (use `@Async` or catch+log).
- ❌ An event that carries the full entity (carry IDs, not objects).
- ❌ A bi-directional event (A publishes `X`, B subscribes and publishes `Y` which A subscribes to) — this is a hidden loop.
- ❌ Synchronous event listeners on the publisher's critical path. If you need a sync response, call a service.

---

## 5. Change Log

| Date | Change | Author |
|---|---|---|
| 2026-06-05 | Initial catalog draft with legacy + planned events | refactor |
