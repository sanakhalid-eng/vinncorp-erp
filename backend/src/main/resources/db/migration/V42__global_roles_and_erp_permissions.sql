-- ============================================================
-- V42: Global Roles + ERP Permission Seeding
-- ============================================================

-- 1. Global Roles table (platform-level, not workspace-scoped)
CREATE TABLE IF NOT EXISTS global_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. User Global Roles join table
CREATE TABLE IF NOT EXISTS user_global_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    global_role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL,
    deleted_at TIMESTAMP DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL,
    CONSTRAINT fk_ugr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ugr_global_role FOREIGN KEY (global_role_id) REFERENCES global_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_ugr_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_global_roles UNIQUE (user_id, global_role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_user_global_roles_user ON user_global_roles(user_id);
CREATE INDEX idx_user_global_roles_role ON user_global_roles(global_role_id);

-- 3. Seed Global Roles
INSERT INTO global_roles (name, description, created_at) VALUES
('SUPER_ADMIN', 'Platform super administrator with full access to all workspaces and settings', NOW()),
('SUPPORT_ADMIN', 'Platform support administrator with read access and limited management capabilities', NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 4. Seed new ERP permissions
INSERT INTO permissions (name, description, permission_group) VALUES
-- Employee Management
('EMPLOYEE_VIEW', 'View employees', 'EMPLOYEE_MANAGEMENT'),
('EMPLOYEE_CREATE', 'Create employees', 'EMPLOYEE_MANAGEMENT'),
('EMPLOYEE_UPDATE', 'Update employees', 'EMPLOYEE_MANAGEMENT'),
('EMPLOYEE_DELETE', 'Delete employees', 'EMPLOYEE_MANAGEMENT'),
-- Department Management
('DEPARTMENT_VIEW', 'View departments', 'DEPARTMENT_MANAGEMENT'),
('DEPARTMENT_CREATE', 'Create departments', 'DEPARTMENT_MANAGEMENT'),
('DEPARTMENT_UPDATE', 'Update departments', 'DEPARTMENT_MANAGEMENT'),
('DEPARTMENT_DELETE', 'Delete departments', 'DEPARTMENT_MANAGEMENT'),
-- Designation Management
('DESIGNATION_VIEW', 'View designations', 'DESIGNATION_MANAGEMENT'),
('DESIGNATION_CREATE', 'Create designations', 'DESIGNATION_MANAGEMENT'),
('DESIGNATION_UPDATE', 'Update designations', 'DESIGNATION_MANAGEMENT'),
('DESIGNATION_DELETE', 'Delete designations', 'DESIGNATION_MANAGEMENT'),
-- Platform / Workspace Management
('WORKSPACE_MANAGE', 'Manage workspaces at platform level', 'PLATFORM_MANAGEMENT'),
('PLATFORM_MANAGE', 'Manage platform settings and configurations', 'PLATFORM_MANAGEMENT')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 5. Seed WORKSPACE_ADMIN workspace role with all workspace permissions
-- (Workspace roles use the existing `roles` table with scope = 'SYSTEM')
INSERT INTO roles (name, description, scope, is_system_role, is_editable, created_at, updated_at)
VALUES
('WORKSPACE_ADMIN', 'Workspace administrator with full workspace access', 'SYSTEM', false, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (name, description, scope, is_system_role, is_editable, created_at, updated_at)
VALUES
('HR_MANAGER', 'HR manager with employee and department management access', 'SYSTEM', false, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (name, description, scope, is_system_role, is_editable, created_at, updated_at)
VALUES
('FINANCE_MANAGER', 'Finance manager with financial data access', 'SYSTEM', false, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (name, description, scope, is_system_role, is_editable, created_at, updated_at)
VALUES
('TEAM_LEAD', 'Team lead with task and member management access', 'SYSTEM', false, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO roles (name, description, scope, is_system_role, is_editable, created_at, updated_at)
VALUES
('EMPLOYEE', 'Standard employee with basic access', 'SYSTEM', false, true, NOW(), NOW())
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 6. Assign permissions to WORKSPACE_ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'WORKSPACE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 7. Assign permissions to HR_MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'HR_MANAGER'
AND p.name IN (
    'EMPLOYEE_VIEW', 'EMPLOYEE_CREATE', 'EMPLOYEE_UPDATE', 'EMPLOYEE_DELETE',
    'DEPARTMENT_VIEW', 'DEPARTMENT_CREATE', 'DEPARTMENT_UPDATE', 'DEPARTMENT_DELETE',
    'DESIGNATION_VIEW', 'DESIGNATION_CREATE', 'DESIGNATION_UPDATE', 'DESIGNATION_DELETE',
    'VIEW_USERS', 'VIEW_DASHBOARD', 'VIEW_MEMBERS', 'VIEW_PROFILE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 8. Assign permissions to FINANCE_MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'FINANCE_MANAGER'
AND p.name IN (
    'VIEW_DASHBOARD', 'VIEW_PROFILE', 'VIEW_PROJECT', 'VIEW_TASK'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 9. Assign permissions to TEAM_LEAD role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'TEAM_LEAD'
AND p.name IN (
    'VIEW_DASHBOARD', 'VIEW_PROJECT', 'VIEW_ALL_PROJECTS',
    'VIEW_TASK', 'VIEW_ALL_TASKS', 'CREATE_TASK', 'EDIT_TASK', 'EDIT_ANY_TASK',
    'DELETE_TASK', 'MOVE_TASK', 'ASSIGN_TASK',
    'VIEW_MEMBERS', 'ADD_MEMBER', 'REMOVE_MEMBER',
    'VIEW_PROFILE', 'CREATE_LABEL', 'DELETE_LABEL', 'ASSIGN_LABEL'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- 10. Assign permissions to EMPLOYEE role (basic access)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'EMPLOYEE'
AND p.name IN (
    'VIEW_DASHBOARD', 'VIEW_PROJECT', 'VIEW_TASK', 'VIEW_MEMBERS', 'VIEW_PROFILE'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
