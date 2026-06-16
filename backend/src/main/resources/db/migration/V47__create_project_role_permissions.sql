CREATE TABLE IF NOT EXISTS project_role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_permission (project_role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (project_role_id) REFERENCES project_roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
