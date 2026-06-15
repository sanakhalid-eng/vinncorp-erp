# PMT-SK Architecture

## System Context

```
+------------------+     +------------------+     +------------------+
|   Browser (SPA)  |     |   Mobile/API     |     |   External       |
|   React 19       +----->   Clients        +----->   Services       |
|   Tailwind CSS   |     |                  |     |   Slack/Webhooks |
+--------+---------+     +--------+---------+     +--------+---------+
         |                          |                        |
         |         +----------------v--------+               |
         +--------->   PMT-SK Backend        <---------------+
                   |   Spring Boot 4.0       |
                   |   Java 21               |
                   +----------+--------------+
                              |
              +---------------+---------------+
              |               |               |
         +----v----+    +----v----+    +-----v------+
         |PostgreSQL|    |  Redis   |    | Filesystem |
         | (JPA)    |    | (Cache)  |    | (Uploads)  |
         +---------+    +---------+    +------------+
```

## Container Diagram

```
+==============================================================+
|                    PMT-SK Backend Container                  |
|                                                              |
|  +------------------+  +------------------+  +------------+ |
|  |  Controllers     |  |  WebSocket       |  |  Filters   | |
|  |  (REST API)      |  |  (Notifications) |  |  (MDC,     | |
|  |  32 controllers  |  |                  |  |   Rate,    | |
|  +--------+---------+  +--------+---------+  |   Timing)  | |
|           |                      |           +------------+ |
|           v                      v                           |
|  +------------------+  +------------------+                  |
|  |  Service Layer   |  |  Security Layer  |                  |
|  |  (43 services)   |  |  Auth, JWT,      |                  |
|  |  + impl/ (21)    |  |  Permissions,    |                  |
|  +--------+---------+  |  RBAC            |                  |
|           |            +------------------+                  |
|           v                                                  |
|  +------------------+  +------------------+                  |
|  |  Repository      |  |  Events/Jobs     |                  |
|  |  (JPA, 45 repos) |  |  Domain Events,  |                  |
|  |  Specifications  |  |  Scheduled Jobs,  |                  |
|  +--------+---------+  |  Retry Queue     |                  |
|           |            +------------------+                  |
|           v                                                  |
|  +------------------+                                        |
|  |  Entity Layer    |                                        |
|  |  (47 entities)   |                                        |
|  |  + enums/ (9)    |                                        |
|  +------------------+                                        |
+==============================================================+
```

## Component Diagram — Security Layer

```
+===========================================================+
|                  Security Layer                            |
|                                                           |
|  +====================+  +============================+   |
|  | Authentication     |  | Authorization              |   |
|  | + JwtAuthFilter    |  | + PermissionSecurity       |   |
|  | + JwtUtil          |  | + PermissionResolver       |   |
|  | + OAuth2Handler    |  | + ProjectSecurity          |   |
|  | + TwoFactorService |  | + TaskSecurity             |   |
|  +====================+  | + WorkspaceOwnerSecurity   |   |
|                          | + MembershipResolver  (I)  |   |
|                          | + SingleTenantMembership   |   |
|                          | + PermissionService   (I)  |   |
|                          +============================+   |
|                                                           |
|  +====================================================+   |
|  | Abstractions (Multi-Tenant Readiness)               |   |
|  | + MembershipResolver (interface)                    |   |
|  | + StorageProvider (interface)                       |   |
|  | + PermissionResolver (unified engine)               |   |
|  +====================================================+   |
+===========================================================+
```

## Component Diagram — Service Layer

```
+====================================================================+
|                        Service Layer                               |
|                                                                    |
|  +-------------------+  +-------------------+  +-----------------+ |
|  | AuthService       |  | UserService       |  | RoleService     | |
|  | + Registration    |  | + CRUD            |  | + System Roles  | |
|  | + Login           |  | + Avatar Upload   |  | + Immutability  | |
|  | + Email Verify    |  | + 2FA             |  | + Permission    | |
|  +-------------------+  +-------------------+  +-----------------+ |
|                                                                    |
|  +-------------------+  +-------------------+  +-----------------+ |
|  | ProjectService    |  | TaskService       |  | SprintService   | |
|  | + CRUD            |  | + CRUD            |  | + CRUD          | |
|  | + Members         |  | + Dependencies    |  | + Burndown      | |
|  | + Workflow        |  | + Attachments     |  | + Velocity      | |
|  +-------------------+  +-------------------+  +-----------------+ |
|                                                                    |
|  +-------------------+  +-------------------+  +-----------------+ |
|  | Integration       |  | Operational       |  | Analytics       | |
|  | SlackService      |  | JobTrackerService  |  | AnalyticsSvc   | |
|  | WebhookService    |  | RetryService      |  | DashboardSvc   | |
|  | WebhookRetrySvc   |  | EmailService      |  | BurndownSvc    | |
|  +-------------------+  +-------------------+  +-----------------+ |
|                                                                    |
|  +-------------------+  +-------------------+                       |
|  | Infrastructure    |  | Config            |                       |
|  | ActivityLogSvc    |  | SystemConfigSvc   |                       |
|  | NotificationSvc   |  | RateLimitService  |                       |
|  | AttachmentSvc     |  | ColorValidator    |                       |
|  +-------------------+  +-------------------+                       |
+====================================================================+
```

## Key Flows

### Authentication Flow
```
Browser -> POST /api/auth/login
  -> JwtAuthFilter (skip for /auth/**)
  -> AuthService.authenticate()
  -> UserRepository.findByEmail()
  -> PasswordEncoder.matches()
  -> JwtUtil.generateToken()
  <- ApiResponse<LoginResponse>(token, user)
```

### Permission Check Flow
```
Browser -> GET /api/projects/1
  -> JwtAuthFilter.extractToken()
  -> SecurityContextHolder.setAuthentication()
  -> @PreAuthorize("hasAuthority('VIEW_PROJECT')")
    -> PermissionResolver.hasSystemPermission(email, 'VIEW_PROJECT')
      -> UserRepository.findByEmailWithRoles()
      -> MembershipResolver.isAdmin() — short circuit if ADMIN
      -> permissionService.hasPermission(userId, null, permission)
  -> ProjectController.getProject()
  <- ApiResponse<ProjectResponse>
```

### Scheduled Job Flow
```
ScheduledJob (cron trigger)
  -> JobTrackerService.executeJob(job)
    -> Creates ScheduledJobExecution (RUNNING)
    -> Executes job logic
    -> On success: mark COMPLETED
    -> On failure: mark FAILED + RetryService.enqueue()
      -> RetryQueue item with exponential backoff
      -> RetryService.processQueue() on timer
        -> Max retries exceeded -> DEAD_LETTER
  <- EmailDelivery or WebhookDelivery updated
```

### Notification Flow
```
Service -> NotificationService.createNotification()
  -> NotificationRepository.save()
  -> NotificationWebSocketHandler.sendToUser(userId)
  -> Browser receives via WebSocket
  -> Sonner toast displayed
  -> NotificationDropdown badge updated
```

## Architecture Decisions

1. **Controller → Service → Repository → Entity** — Standard layered architecture
2. **Interface + Impl pattern** — All services have interfaces in `service/`, implementations in `service/impl/`
3. **Security abstractions** — `MembershipResolver`, `StorageProvider`, and `PermissionResolver` provide extension points for multi-tenancy without changing existing flows
4. **Event-driven** — Domain events for cross-cutting concerns (webhooks, activity logs, notifications)
5. **Flyway migrations** — Database schema versioning with sequential SQL migrations
6. **SLF4J MDC** — Correlation ID propagated through all service calls for distributed tracing
7. **API envelope** — All responses wrapped in `ApiResponse<T>`, errors include correlation ID
8. **Feature-based frontend** — Pages organized by feature domain under `features/`

## Package Structure

```
com.projectmanager.projectmanagementbackend
+-- config/           — Spring configuration (Security, Async, WebSocket, etc.)
+-- constants/        — Permission constants
+-- controller/       — REST controllers (32)
+-- dto/
|   +-- request/      — Request DTOs
|   +-- response/     — Response DTOs (45)
+-- entity/
|   +-- enums/        — Enum types (9)
+-- events/           — Domain events and processors
+-- exception/        — Custom exceptions + GlobalExceptionHandler
+-- filter/           — Servlet filters (CorrelationId, RateLimit, Timing)
+-- jobs/             — Scheduled job definitions
+-- listener/         — Event listeners
+-- mapper/           — Entity <-> DTO mappers
+-- notifications/    — Notification infrastructure
+-- repository/       — JPA repositories (45)
+-- scheduling/       — Scheduling configuration
+-- security/         — Auth, JWT, RBAC, abstractions
+-- service/
|   +-- impl/         — Service implementations (21)
+-- specification/    — JPA specifications
+-- websocket/        — WebSocket handlers
+-- workflow/         — Workflow engine
```

## Frontend Structure

```
frontend/src/
+-- api/              — Axios client + API functions
+-- components/       — Shared UI components (42)
|   +-- attachments/
|   +-- comments/
|   +-- labels/
|   +-- members/
|   +-- notifications/
|   +-- statuses/
|   +-- subtasks/
|   +-- tasks/
|   +-- ui/
+-- context/          — React contexts (Auth, Permission, Theme)
+-- features/         — Feature-based page directories
|   +-- auth/
|   +-- projects/
|   +-- tasks/
|   +-- sprints/
|   +-- analytics/
|   +-- integrations/
|   +-- notifications/
|   +-- settings/
|   +-- system/
+-- hooks/            — Custom React hooks
+-- layouts/          — Layout components (AppLayout, AuthLayout)
+-- pages/            — Page components (42)
+-- types/            — Type definitions
+-- utils/            — Utility functions
```
