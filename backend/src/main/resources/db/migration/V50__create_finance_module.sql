-- V50: Finance Module Core Tables
-- Expenses, Invoices, Invoice Items, and Payments

-- ── Expenses ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS finance_expenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    expense_date DATETIME NOT NULL,
    attachment_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_finance_expenses_workspace (workspace_id),
    KEY idx_finance_expenses_status (workspace_id, status),
    KEY idx_finance_expenses_category (workspace_id, category),
    CONSTRAINT fk_finance_expenses_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_finance_expenses_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Invoices ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS finance_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    invoice_number VARCHAR(64) NOT NULL,
    customer_id BIGINT NOT NULL,
    project_id BIGINT,
    opportunity_id BIGINT,
    issue_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0,
    amount_paid DECIMAL(19,2) NOT NULL DEFAULT 0,
    balance_due DECIMAL(19,2) NOT NULL DEFAULT 0,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sent_at DATETIME,
    paid_at DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_finance_invoices_workspace (workspace_id),
    KEY idx_finance_invoices_status (workspace_id, status),
    KEY idx_finance_invoices_customer (workspace_id, customer_id),
    KEY idx_finance_invoices_due_date (workspace_id, due_date),
    CONSTRAINT fk_finance_invoices_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Invoice Items ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS finance_invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(19,2) NOT NULL DEFAULT 0,
    total_price DECIMAL(19,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_finance_invoice_items_invoice (invoice_id),
    CONSTRAINT fk_finance_inv_items_invoice FOREIGN KEY (invoice_id) REFERENCES finance_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── Payments ─────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS finance_payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    reference_number VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NULL,
    updated_by BIGINT,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT,
    KEY idx_finance_payments_workspace (workspace_id),
    KEY idx_finance_payments_invoice (workspace_id, invoice_id),
    CONSTRAINT fk_finance_payments_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_finance_payments_invoice FOREIGN KEY (invoice_id) REFERENCES finance_invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
