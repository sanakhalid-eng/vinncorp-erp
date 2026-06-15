-- Create missing tables that entities reference but were never created by migrations

CREATE TABLE IF NOT EXISTS retry_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    payload TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 5,
    last_error VARCHAR(2000),
    next_retry_at DATETIME,
    last_retry_at DATETIME,
    completed_at DATETIME,
    error_history TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL,
    INDEX idx_retry_status (status),
    INDEX idx_retry_next_retry (next_retry_at),
    INDEX idx_retry_type_status (type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS email_deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    last_error VARCHAR(2000),
    sent_at DATETIME,
    bounced_at DATETIME,
    bounce_reason VARCHAR(2000),
    opened_at DATETIME,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL,
    INDEX idx_email_status (status),
    INDEX idx_email_recipient (recipient_email),
    INDEX idx_email_type_status (email_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    expiry_date DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
    deleted_by BIGINT DEFAULT NULL,
    INDEX idx_reset_token (token, is_used),
    INDEX idx_reset_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
