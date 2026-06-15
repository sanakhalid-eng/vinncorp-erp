-- Slack integrations table
CREATE TABLE IF NOT EXISTS slack_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    workspace_id VARCHAR(100) NOT NULL,
    workspace_name VARCHAR(200),
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    token_expiry DATETIME,
    channel_id VARCHAR(100),
    channel_name VARCHAR(200),
    signing_secret VARCHAR(255) NOT NULL,
    bot_user_id VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE KEY unique_workspace (workspace_id),
    INDEX idx_project_id (project_id)
);

-- Slack user mappings table
CREATE TABLE IF NOT EXISTS slack_user_mappings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slack_integration_id BIGINT NOT NULL,
    slack_user_id VARCHAR(100) NOT NULL,
    slack_username VARCHAR(200),
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (slack_integration_id) REFERENCES slack_integrations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_slack_user (slack_integration_id, slack_user_id),
    INDEX idx_slack_integration_id (slack_integration_id)
);
