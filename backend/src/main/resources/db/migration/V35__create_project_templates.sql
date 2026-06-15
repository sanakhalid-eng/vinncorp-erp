-- Project Templates table
CREATE TABLE IF NOT EXISTS project_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) DEFAULT NULL,
    category VARCHAR(100) DEFAULT NULL,
    icon VARCHAR(50) DEFAULT NULL,
    has_sprints BOOLEAN NOT NULL DEFAULT FALSE,
    default_labels TEXT DEFAULT NULL,
    default_columns TEXT DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by BIGINT DEFAULT NULL,
    created_by BIGINT DEFAULT NULL,
    updated_by BIGINT DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed default templates
INSERT INTO project_templates (name, description, category, icon, has_sprints, default_labels, default_columns, is_active) VALUES
('Scrum Software Team', 'Classic Scrum with sprints, backlog, and story points', 'Software Development', 'Trophy', TRUE,
 '[\\"bug\\", \\"feature\\", \\"improvement\\", \\"technical-debt\\", \\"spike\\"]',
 '[\\"TODO\\", \\"IN_PROGRESS\\", \\"REVIEW\\", \\"DONE\\"]', TRUE),
('Kanban Team', 'Continuous delivery with WIP limits and flow management', 'Software Development', 'LayoutGrid', FALSE,
 '[\\"bug\\", \\"feature\\", \\"improvement\\", \\"blocked\\", \\"high-priority\\"]',
 '[\\"BACKLOG\\", \\"READY\\", \\"IN_PROGRESS\\", \\"REVIEW\\", \\"DONE\\"]', TRUE),
('Marketing Campaign', 'Plan and track campaigns, content, and deliverables', 'Marketing', 'Megaphone', FALSE,
 '[\\"content\\", \\"design\\", \\"social\\", \\"email\\", \\"analytics\\"]',
 '[\\"IDEATION\\", \\"IN_PROGRESS\\", \\"REVIEW\\", \\"LIVE\\", \\"DONE\\"]', TRUE),
('Product Roadmap', 'Strategic product planning with quarterly goals', 'Product', 'Map', TRUE,
 '[\\"now\\", \\"next\\", \\"later\\", \\"epic\\", \\"initiative\\"]',
 '[\\"BACKLOG\\", \\"THIS_QUARTER\\", \\"NEXT_QUARTER\\", \\"IN_PROGRESS\\", \\"SHIPPED\\"]', TRUE);
