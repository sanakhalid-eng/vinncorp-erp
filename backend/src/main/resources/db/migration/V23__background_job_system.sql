-- Phase 3: Background Job System

CREATE TABLE IF NOT EXISTS background_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_type VARCHAR(100) NOT NULL,
    payload TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP NULL DEFAULT NULL,
    error_message TEXT,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    started_at TIMESTAMP NULL DEFAULT NULL,
    workspace_id BIGINT,
    dead_letter BOOLEAN NOT NULL DEFAULT FALSE,
    dead_letter_reason TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    deleted_by BIGINT,
    INDEX idx_job_status_retry (status, next_retry_at),
    INDEX idx_job_workspace (workspace_id),
    INDEX idx_job_dead_letter (dead_letter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
