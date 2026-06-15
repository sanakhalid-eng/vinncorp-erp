-- =============================================
-- Project-Level RBAC Schema
-- Database: project_management
-- =============================================

-- 1. Permissions table (already exists, ensure unique constraint)
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 2. Project Roles table (dedicated project-level roles)
CREATE TABLE IF NOT EXISTS project_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 3. Project Role Permissions join table (links project_roles to permissions)
CREATE TABLE IF NOT EXISTS project_role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY unique_role_permission (project_role_id, permission_id),
    FOREIGN KEY (project_role_id) REFERENCES project_roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 4. Project Members table (already exists, add project_role_id column)
-- Existing table: project_members (id, user_id, project_id, role_id, is_active, created_at)
ALTER TABLE project_members ADD COLUMN IF NOT EXISTS project_role_id BIGINT;
ALTER TABLE project_members ADD CONSTRAINT fk_project_member_role FOREIGN KEY (project_role_id) REFERENCES project_roles(id);

-- 5. Workflow Transition Rules (add required_permissions column)
-- Existing table: workflow_transition_rules
ALTER TABLE workflow_transition_rules ADD COLUMN IF NOT EXISTS required_permissions TEXT;

-- =============================================
-- Seed Data: Permissions
-- =============================================
INSERT IGNORE INTO permissions (name, description) VALUES
('VIEW_DASHBOARD', 'View Dashboard'),
('VIEW_PROJECT', 'View Project'),
('VIEW_ALL_PROJECTS', 'View All Projects'),
('CREATE_PROJECT', 'Create Project'),
('EDIT_PROJECT', 'Edit Project'),
('DELETE_PROJECT', 'Delete Project'),
('VIEW_TASK', 'View Task'),
('VIEW_ALL_TASKS', 'View All Tasks'),
('CREATE_TASK', 'Create Task'),
('EDIT_TASK', 'Edit Task'),
('EDIT_ANY_TASK', 'Edit Any Task'),
('EDIT_ASSIGNED_TASK', 'Edit Assigned Task'),
('DELETE_TASK', 'Delete Task'),
('MOVE_TASK', 'Move Task'),
('ASSIGN_TASK', 'Assign Task'),
('VIEW_MEMBERS', 'View Members'),
('ADD_MEMBER', 'Add Member'),
('REMOVE_MEMBER', 'Remove Member'),
('UPDATE_MEMBER_ROLE', 'Update Member Role'),
('VIEW_USERS', 'View Users'),
('CREATE_USER', 'Create User'),
('UPDATE_USER', 'Update User'),
('DELETE_USER', 'Delete User'),
('ASSIGN_SYSTEM_ROLE', 'Assign System Role'),
('VIEW_ROLES', 'View Roles'),
('CREATE_ROLE', 'Create Role'),
('UPDATE_ROLE', 'Update Role'),
('DELETE_ROLE', 'Delete Role'),
('MANAGE_WORKFLOW', 'Manage Workflow');

-- =============================================
-- Seed Data: Project Roles
-- =============================================
INSERT IGNORE INTO project_roles (name, description) VALUES
('PROJECT_MANAGER', 'Manages project tasks, members, and workflow'),
('TEAM_MEMBER', 'Project team member who can work on tasks');

-- =============================================
-- Map PROJECT_MANAGER → All project permissions
-- =============================================
INSERT INTO role_permissions (project_role_id, permission_id)
SELECT pr.id, p.id
FROM project_roles pr
CROSS JOIN permissions p
WHERE pr.name = 'PROJECT_MANAGER'
  AND p.name IN (
    'VIEW_TASK', 'VIEW_ALL_TASKS', 'CREATE_TASK', 'EDIT_TASK',
    'EDIT_ANY_TASK', 'DELETE_TASK', 'MOVE_TASK', 'ASSIGN_TASK',
    'VIEW_MEMBERS', 'ADD_MEMBER', 'REMOVE_MEMBER', 'UPDATE_MEMBER_ROLE',
    'VIEW_PROJECT', 'EDIT_PROJECT', 'MANAGE_WORKFLOW', 'VIEW_DASHBOARD'
  )
ON DUPLICATE KEY UPDATE id = id;

-- =============================================
-- Map TEAM_MEMBER → Limited permissions
-- =============================================
INSERT INTO role_permissions (project_role_id, permission_id)
SELECT pr.id, p.id
FROM project_roles pr
CROSS JOIN permissions p
WHERE pr.name = 'TEAM_MEMBER'
  AND p.name IN (
    'VIEW_TASK', 'CREATE_TASK', 'MOVE_TASK',
    'EDIT_ASSIGNED_TASK', 'VIEW_MEMBERS', 'VIEW_PROJECT', 'VIEW_DASHBOARD'
  )
ON DUPLICATE KEY UPDATE id = id;

-- =============================================
-- Migration: Map existing project_members.role_id → project_role_id
-- Copies the role reference from legacy Role table to new ProjectRole table
-- =============================================
UPDATE project_members pm
JOIN roles r ON pm.role_id = r.id
JOIN project_roles pr ON pr.name = r.name
SET pm.project_role_id = pr.id
WHERE pm.project_role_id IS NULL
  AND r.scope = 'PROJECT';
