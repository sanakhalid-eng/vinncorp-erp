-- V43: Refresh Token Rotation + Security Enhancements

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token VARCHAR(512) NULL,
    token_family VARCHAR(64) NOT NULL,

    CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE INDEX idx_refresh_tokens_user
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expiry
    ON refresh_tokens (expiry_date);

CREATE INDEX idx_refresh_tokens_family
    ON refresh_tokens (token_family);