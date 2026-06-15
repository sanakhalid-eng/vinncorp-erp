-- V37: HR (Human Resources) module - Phase 4 of ERP roadmap
--
-- Three entities, all tenant-scoped via workspace_id, all using the
-- shared soft-delete columns (created_at/updated_at/created_by/updated_by/deleted_at/deleted_by)
-- from BaseAuditableEntity.
--
-- Naming convention: hr_ prefix for HR-owned tables (per MODULE_BOUNDARIES.md).
-- Future modules (crm_, fin_) follow the same prefix rule.

CREATE TABLE IF NOT EXISTS hr_departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(32),
    description VARCHAR(500),
    head_employee_id BIGINT,
    parent_department_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_hr_departments_workspace_name (workspace_id, name),
    KEY idx_hr_departments_workspace (workspace_id),
    KEY idx_hr_departments_head (head_employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS hr_designations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    code VARCHAR(32),
    description VARCHAR(500),
    level INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_hr_designations_workspace_title (workspace_id, title),
    KEY idx_hr_designations_workspace (workspace_id),
    KEY idx_hr_designations_level (workspace_id, level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS hr_employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    employee_code VARCHAR(32) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    work_email VARCHAR(200),
    personal_email VARCHAR(200),
    phone VARCHAR(32),
    employment_type VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    date_of_birth DATE,
    hire_date DATE NOT NULL,
    termination_date DATE,
    job_title VARCHAR(150),
    timezone VARCHAR(16),
    locale VARCHAR(8),
    manager_id BIGINT,
    user_id BIGINT,
    department_id BIGINT,
    designation_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_hr_employees_employee_code (workspace_id, employee_code),
    UNIQUE KEY uk_hr_employees_user (workspace_id, user_id),
    KEY idx_hr_employees_workspace (workspace_id),
    KEY idx_hr_employees_department (workspace_id, department_id),
    KEY idx_hr_employees_designation (workspace_id, designation_id),
    KEY idx_hr_employees_status (workspace_id, status),
    KEY idx_hr_employees_email (workspace_id, work_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Self-referential manager relationship (Employee.manager_id -> Employee.id)
ALTER TABLE hr_employees
    ADD CONSTRAINT fk_hr_employees_manager
    FOREIGN KEY (manager_id) REFERENCES hr_employees(id) ON DELETE SET NULL;

-- Optional link to the auth identity in core.user.User.
-- ON DELETE SET NULL: revoking the user account must not lose the HR record.
ALTER TABLE hr_employees
    ADD CONSTRAINT fk_hr_employees_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- A department head is also an employee. Same nullify-on-delete semantics.
ALTER TABLE hr_departments
    ADD CONSTRAINT fk_hr_departments_head_employee
    FOREIGN KEY (head_employee_id) REFERENCES hr_employees(id) ON DELETE SET NULL;

-- Departments may form a tree.
ALTER TABLE hr_departments
    ADD CONSTRAINT fk_hr_departments_parent
    FOREIGN KEY (parent_department_id) REFERENCES hr_departments(id) ON DELETE SET NULL;

-- Employees and designations.
ALTER TABLE hr_employees
    ADD CONSTRAINT fk_hr_employees_department
    FOREIGN KEY (department_id) REFERENCES hr_departments(id) ON DELETE SET NULL;

ALTER TABLE hr_employees
    ADD CONSTRAINT fk_hr_employees_designation
    FOREIGN KEY (designation_id) REFERENCES hr_designations(id) ON DELETE SET NULL;
