-- V5: Workspace Core Foundation
-- Phase 2A: Multi-tenant foundation before full isolation

-- =============================================
-- WORKSPACES
-- =============================================
CREATE TABLE IF NOT EXISTS workspaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(500),
    settings_json TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit columns (BaseAuditableEntity)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT,

    INDEX idx_workspaces_slug (slug),
    INDEX idx_workspaces_active (active),
    INDEX idx_workspaces_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================
-- WORKSPACE MEMBERS
-- =============================================
CREATE TABLE IF NOT EXISTS workspace_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    workspace_role VARCHAR(50) NOT NULL DEFAULT 'WORKSPACE_MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    invited_by BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    UNIQUE KEY uq_workspace_user (workspace_id, user_id),
    INDEX idx_workspace_members_workspace (workspace_id),
    INDEX idx_workspace_members_user (user_id),
    INDEX idx_workspace_members_active (active),

    CONSTRAINT fk_workspace_members_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_workspace_members_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_workspace_members_invited_by
        FOREIGN KEY (invited_by) REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================
-- WORKSPACE ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS workspace_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    workspace_id BIGINT,
    system_managed BOOLEAN NOT NULL DEFAULT FALSE,
    permissions_json TEXT,

    -- audit columns
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT,

    INDEX idx_workspace_roles_workspace (workspace_id),
    INDEX idx_workspace_roles_name (name),
    INDEX idx_workspace_roles_deleted_at (deleted_at),

    CONSTRAINT fk_workspace_roles_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================
-- WORKSPACE INVITATIONS
-- =============================================
CREATE TABLE IF NOT EXISTS workspace_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    invited_by BIGINT NOT NULL,
    workspace_role VARCHAR(50),
    token VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at DATETIME NOT NULL,
    accepted_at DATETIME,

    -- audit columns
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_at DATETIME,
    deleted_by BIGINT,

    INDEX idx_workspace_invitation_token (token),
    INDEX idx_workspace_invitation_email (email),
    INDEX idx_workspace_invitation_workspace (workspace_id),
    INDEX idx_workspace_invitation_status (status),
    INDEX idx_workspace_invitation_deleted_at (deleted_at),

    CONSTRAINT fk_workspace_invitations_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_workspace_invitations_invited_by
        FOREIGN KEY (invited_by) REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
