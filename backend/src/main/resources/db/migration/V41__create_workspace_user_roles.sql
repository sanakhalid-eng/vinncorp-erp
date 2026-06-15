-- =====================================================
-- V41__create_workspace_user_roles.sql
-- =====================================================

CREATE TABLE IF NOT EXISTS workspace_user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT NULL,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,

    created_by BIGINT NULL,
    updated_by BIGINT NULL,

    deleted_at DATETIME NULL,
    deleted_by BIGINT NULL,

    INDEX idx_wur_workspace (workspace_id),
    INDEX idx_wur_user (user_id),
    INDEX idx_wur_role (role_id),
    INDEX idx_wur_ws_user (workspace_id, user_id),

    CONSTRAINT fk_wur_workspace
    FOREIGN KEY (workspace_id)
    REFERENCES workspaces(id),

    CONSTRAINT fk_wur_user
    FOREIGN KEY (user_id)
    REFERENCES users(id),

    CONSTRAINT fk_wur_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id),

    UNIQUE KEY uk_workspace_user_role
(workspace_id, user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- Migrate existing workspace members
-- =====================================================

INSERT IGNORE INTO workspace_user_roles (
    workspace_id,
    user_id,
    role_id,
    assigned_at,
    created_at
)
SELECT
    wm.workspace_id,
    wm.user_id,
    r.id,
    COALESCE(wm.joined_at, NOW()),
    COALESCE(wm.joined_at, NOW())
FROM workspace_members wm
         JOIN roles r ON (
    (wm.workspace_role = 'WORKSPACE_OWNER'
        AND r.name = 'ADMIN')
        OR (wm.workspace_role = 'WORKSPACE_ADMIN'
        AND r.name = 'ADMIN')
        OR (wm.workspace_role = 'WORKSPACE_MEMBER'
        AND r.name = 'USER')
        OR (wm.workspace_role = 'WORKSPACE_USER'
        AND r.name = 'USER')
    )
WHERE wm.active = TRUE;

-- =====================================================
-- Optional verification query
-- =====================================================

-- SELECT * FROM workspace_user_roles;
