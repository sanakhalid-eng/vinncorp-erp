-- V44: CRM Core Tables
-- Contacts, Customers, Customer-Contact join, Leads, Pipelines, PipelineStages, Opportunities

-- ── Contacts (standalone, no user account required) ─────────────────────────
CREATE TABLE IF NOT EXISTS crm_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(32),
    company VARCHAR(200),
    job_title VARCHAR(150),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_crm_contacts_workspace_email (workspace_id, email),
    KEY idx_crm_contacts_workspace (workspace_id),
    KEY idx_crm_contacts_name (workspace_id, last_name, first_name),
    CONSTRAINT fk_crm_contacts_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Customers ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    industry VARCHAR(100),
    website VARCHAR(500),
    phone VARCHAR(32),
    email VARCHAR(200),
    address TEXT,
    notes TEXT,
    contact_owner_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_crm_customers_workspace_name (workspace_id, name),
    KEY idx_crm_customers_workspace (workspace_id),
    KEY idx_crm_customers_owner (contact_owner_id),
    CONSTRAINT fk_crm_customers_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_crm_customers_owner FOREIGN KEY (contact_owner_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Customer-Contact join ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_customer_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_crm_customer_contacts (customer_id, contact_id),
    KEY idx_crm_customer_contacts_customer (customer_id),
    KEY idx_crm_customer_contacts_contact (contact_id),
    CONSTRAINT fk_crm_cc_customer FOREIGN KEY (customer_id) REFERENCES crm_customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_crm_cc_contact FOREIGN KEY (contact_id) REFERENCES crm_contacts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Leads ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_leads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(32),
    company VARCHAR(200),
    job_title VARCHAR(150),
    source VARCHAR(32) NOT NULL DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    owner_id BIGINT,
    converted_customer_id BIGINT,
    converted_by BIGINT,
    converted_at TIMESTAMP NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_crm_leads_workspace (workspace_id),
    KEY idx_crm_leads_status (workspace_id, status),
    KEY idx_crm_leads_owner (owner_id),
    CONSTRAINT fk_crm_leads_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_crm_leads_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_crm_leads_converted_customer FOREIGN KEY (converted_customer_id) REFERENCES crm_customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_crm_leads_converted_by FOREIGN KEY (converted_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Pipelines ───────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_pipelines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_default TINYINT(1) NOT NULL DEFAULT 0,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    UNIQUE KEY uk_crm_pipelines_workspace_name (workspace_id, name),
    KEY idx_crm_pipelines_workspace (workspace_id),
    CONSTRAINT fk_crm_pipelines_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Pipeline Stages ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_pipeline_stages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pipeline_id BIGINT NOT NULL,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    probability_pct INT NOT NULL DEFAULT 0,
    is_won TINYINT(1) NOT NULL DEFAULT 0,
    is_lost TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_crm_stages_pipeline (pipeline_id),
    KEY idx_crm_stages_workspace (workspace_id),
    CONSTRAINT fk_crm_stages_pipeline FOREIGN KEY (pipeline_id) REFERENCES crm_pipelines(id) ON DELETE CASCADE,
    CONSTRAINT fk_crm_stages_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Opportunities (Deals) ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS crm_opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    value DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    stage_id BIGINT NOT NULL,
    customer_id BIGINT,
    lead_id BIGINT,
    owner_id BIGINT,
    expected_close_date DATE,
    actual_close_date DATE,
    probability_pct INT DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_crm_opps_workspace (workspace_id),
    KEY idx_crm_opps_stage (stage_id),
    KEY idx_crm_opps_owner (owner_id),
    KEY idx_crm_opps_customer (customer_id),
    KEY idx_crm_opps_lead (lead_id),
    CONSTRAINT fk_crm_opps_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_crm_opps_stage FOREIGN KEY (stage_id) REFERENCES crm_pipeline_stages(id) ON DELETE RESTRICT,
    CONSTRAINT fk_crm_opps_customer FOREIGN KEY (customer_id) REFERENCES crm_customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_crm_opps_lead FOREIGN KEY (lead_id) REFERENCES crm_leads(id) ON DELETE SET NULL,
    CONSTRAINT fk_crm_opps_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
