# Role System Enhancement - Summary

## Changes Made

### 1. Fixed RoleScope Enum Inconsistency
**File:** `src/main/java/com/projectmanager/projectmanagementbackend/entity/enums/RoleScope.java`

**Before:**
```java
public enum RoleScope {
    GLOBAL,
    SYSTEM,
    PROJECT
}
```

**After:**
```java
public enum RoleScope {
    SYSTEM,
    PROJECT
}
```

**Reason:** Removed `GLOBAL` to eliminate confusion. The system now uses `SYSTEM` consistently for system-level roles.

---

### 2. Enforced Single System Role Per User
**File:** `src/main/java/com/projectmanager/projectmanagementbackend/service/impl/UserRoleServiceImpl.java`

**New Logic:**
- Before assigning a SYSTEM role, check if user already has one
- If user already has a SYSTEM role, throw `BadRequestException`
- This ensures ONE system role per user

**Key Code:**
```java
List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
boolean hasSystemRole = existingRoles.stream()
        .anyMatch(ur -> ur.getRole().getScope() == RoleScope.SYSTEM);

if (hasSystemRole) {
    throw new BadRequestException("User already has a system role. Remove it first before assigning a new one.");
}
```

---

### 3. Updated Method Names (Global → System)
**Files Updated:**
- `UserRoleService.java` - Renamed `assignGlobalRole()` to `assignSystemRole()`
- `UserRoleServiceImpl.java` - Updated implementation method name

---

### 4. Added Methods to UserRoleService
**New Methods:**
```java
void assignSystemRole(Long userId, Long roleId);
void removeSystemRole(Long userId, Long roleId);
List<UserRole> getUserSystemRoles(Long userId);
```

These allow admins to:
- Assign a system role (with single-role enforcement)
- Remove a system role
- View user's system roles

---

### 5. Created UserRoleController
**File:** `src/main/java/com/projectmanager/projectmanagementbackend/controller/UserRoleController.java`

**Endpoints:**
- `POST /api/users/{userId}/system-role?roleId=X` - Assign system role
- `DELETE /api/users/{userId}/system-role?roleId=X` - Remove system role
- `GET /api/users/{userId}/system-roles` - Get user's system roles

**Security:** Requires `ASSIGN_SYSTEM_ROLE` and `VIEW_USERS` authorities.

---

### 6. Updated AuthServiceImpl
**File:** `src/main/java/com/projectmanager/projectmanagementbackend/service/impl/AuthServiceImpl.java`

**Changes:**
- Added check to prevent assigning multiple system roles during registration
- Injected `UserRoleRepository` to perform the check

---

### 7. Updated OAuth2SuccessHandler
**File:** `src/main/java/com/projectmanager/projectmanagementbackend/security/OAuth2SuccessHandler.java`

**Changes:**
- Added check to prevent assigning multiple system roles during OAuth login
- Injected `UserRoleRepository` to perform the check

---

## Current Role Assignment Behavior

### System Roles (1 per user enforced)
- Assigned via `UserRole` entity
- Stored in `user_roles` table
- **Enforced:** Maximum 1 SYSTEM role per user
- Assigned/removed via `UserRoleController` endpoints

### Project Roles (1 per project per user)
- Assigned via `ProjectMember` entity
- Stored in `project_members` table
- **Enforced by DB constraint:** Unique constraint on `(user_id, project_id)`
- User can have different roles in different projects
- Example: User A = PROJECT_MANAGER in Project A, TEAM_MEMBER in Project B ✓

---

## Build Status
✅ Compilation successful
✅ No syntax errors
✅ All references updated from GLOBAL to SYSTEM

---

## Next Steps (Optional Enhancements)
1. Add `ASSIGN_SYSTEM_ROLE` permission to ADMIN role
2. Create a service method to change system role (remove old + assign new in one transaction)
3. Add audit logging for system role changes
4. Add unit tests for the new enforcement logic
