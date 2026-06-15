# Dependency Rules — VinnCorp ERP

**Status:** Active
**Last updated:** 2026-06-05
**Companion to:** `MODULE_BOUNDARIES.md`

This document defines which modules may depend on which. Violations are caught in code review and (later) by an ArchUnit test suite.

---

## 1. Dependency Direction

```
            ┌──────────────────────┐
            │       shared/        │  (no business logic, only infra)
            └──────────┬───────────┘
                       │
            ┌──────────▼───────────┐
            │        core/         │  (auth, user, workspace, notifications,
            └──────────┬───────────┘   audit, integrations, feature flags)
                       │
       ┌───────────────┼───────────────┐
       │               │               │
┌──────▼─────┐  ┌──────▼─────┐  ┌──────▼─────┐
│  projects  │  │     hr     │  │    crm     │   (future)
└──────┬─────┘  └──────┬─────┘  └────────────┘
       │               │
       └───────────────┤
                       │
              ┌────────▼────────┐
              │    finance      │   (last, depends on all above)
              └─────────────────┘
```

**Rule:** dependencies flow downward only. A module can depend on any module "above" it, never on one "below" or at the same level without going through a public service interface.

---

## 2. Allowed Dependencies

| From | To | Notes |
|---|---|---|
| `shared` | (nothing) | Leaf. No imports from `core/` or `modules/`. |
| `core` | `shared` | Always allowed. |
| `modules/projects` | `core`, `shared` | Always allowed. |
| `modules/hr` | `core`, `shared` | Always allowed. |
| `modules/crm` | `core`, `shared` | Always allowed. |
| `modules/finance` | `core`, `shared`, `modules/projects`, `modules/hr`, `modules/crm` | Last in the build order. |
| Cross-module (same level) | **Public service interface only** | Never direct entity or repository access. |

---

## 3. Forbidden Dependencies

| From | To | Why |
|---|---|---|
| `core` | `modules/*` | Core must not know about business modules. |
| `shared` | `core` or `modules/*` | Shared is leaf infra. |
| `modules/projects` | `modules/hr`, `modules/crm`, `modules/finance` | PM doesn't depend on HR/CRM/Finance. |
| `modules/hr` | `modules/projects`, `modules/crm`, `modules/finance` | HR doesn't depend on PM/CRM/Finance. |
| `modules/crm` | `modules/projects`, `modules/hr`, `modules/finance` | CRM doesn't depend on PM/HR/Finance. |
| Any module | Another module's `repository/` | Repositories are private to their module. |
| Any module | Another module's `entity/` (except via DTOs/mappers) | Entities are private. Always convert to DTO at the boundary. |
| Any module | Another module's `internal/` package | Internal is package-private by convention. |

---

## 4. Cross-Module Communication Patterns

When module A needs to read data from module B, or react to a change in module B, use one of these patterns. **Never** reach into B's internals.

### 4.1 Synchronous read (request/response)

Module A calls a public method on a service interface exposed by module B.

```java
// In modules/crm/ (caller)
@Service
public class CustomerOnboardingService {
    private final UserService userService;   // from core/

    public void onCustomerSigned(Customer c) {
        UserSummary user = userService.getSummary(c.getUserId());
        // ...
    }
}
```

**Rules:**
- The interface lives in module B.
- The caller depends on the **interface**, not the implementation.
- The interface returns **DTOs** (or primitives), never entities.
- DTOs are defined in module B's `dto/` package (or a shared `dto/` package if used by both).

### 4.2 Asynchronous event (publish/subscribe)

Module A publishes a domain event. Module B subscribes. Neither knows the other exists.

```java
// In modules/projects/ (publisher)
@Service
public class ProjectService {
    private final EventPublisher events;

    @Transactional
    public Project create(...) {
        Project saved = projectRepository.save(...);
        events.publish(new ProjectCreatedEvent(saved.getId(), saved.getWorkspaceId()));
        return saved;
    }
}
```

```java
// In modules/notifications or core/ (subscriber) — separate module, no import
@EventListener
@Async
public void onProjectCreated(ProjectCreatedEvent e) {
    notificationService.broadcastToWorkspace(e.workspaceId(), "New project: " + e.projectId());
}
```

**Rules:**
- Events are immutable POJOs.
- Events live in a `events/` package — owned by the **publisher**.
- Subscribers live in the **subscriber's** module.
- Subscribers must not throw exceptions that roll back the publisher's transaction. Use `@Async` or transactional outbox pattern.

### 4.3 Shared read model (last resort)

When the same data is genuinely needed by many modules, the owner module exposes a **read model DTO** (not the entity) and other modules read through a service call.

```
core.User  ─── UserSummary DTO ───►  available to all modules via UserService
```

**This is not the same as a shared entity.** The DTO is the contract; the entity stays private.

---

## 5. Frontend Mirror Rules

The frontend has the same module structure as the backend. The same dependency rules apply, with one addition:

| From (FE) | To (FE) | Allowed? |
|---|---|---|
| `modules/projects` | `core` | ✅ |
| `modules/projects` | `modules/hr` | ❌ |
| `shared/ui` | anything in `modules/` | ❌ — `shared/ui` is leaf. |
| `modules/*` | `shared/ui` | ✅ |

**Cross-module UI composition:** the only place a module can compose UI from another module is in the `app/` shell (router/layout). A module's own pages must not import another module's pages.

---

## 6. Enforcement

### 6.1 Code review

- Every PR must declare which module(s) it touches.
- Any cross-module import must be justified in the PR description.
- A reviewer can block a PR for violating the rules.

### 6.2 Automated (planned v2)

- [ArchUnit](https://www.archunit.org/) test in the backend that enforces the package dependency graph.
- ESLint rule (custom) in the frontend that flags `modules/*` importing from another `modules/*`.

---

## 7. Change Log

| Date | Change | Author |
|---|---|---|
| 2026-06-05 | Initial draft of dependency rules | refactor |
