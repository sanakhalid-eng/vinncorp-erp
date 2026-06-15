-- V40: Create project assignments table for HR-Projects integration

CREATE TABLE IF NOT EXISTS hr_project_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_name VARCHAR(200),
    role_in_project VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    allocation_percentage DECIMAL(5,2),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,

    CONSTRAINT fk_hr_project_assignments_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_hr_project_assignments_employee FOREIGN KEY (employee_id) REFERENCES hr_employees(id),
    CONSTRAINT uk_hr_project_assignments_emp_proj UNIQUE (employee_id, project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_hr_project_assignments_workspace ON hr_project_assignments(workspace_id);
CREATE INDEX idx_hr_project_assignments_employee ON hr_project_assignments(employee_id);
CREATE INDEX idx_hr_project_assignments_project ON hr_project_assignments(project_id);
CREATE INDEX idx_hr_project_assignments_status ON hr_project_assignments(workspace_id, status);
