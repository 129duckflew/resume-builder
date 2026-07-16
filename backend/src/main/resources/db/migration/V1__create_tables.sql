CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    api_key VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS themes (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    css_content TEXT,
    is_built_in BOOLEAN NOT NULL DEFAULT false,
    sort_order INTEGER,
    variables_schema TEXT,
    layout VARCHAR(20) NOT NULL DEFAULT 'single',
    user_id BIGINT
);

CREATE TABLE IF NOT EXISTS resumes (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    theme_id VARCHAR(50) NOT NULL DEFAULT 'classic',
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20) DEFAULT 'normal',
    user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume_versions (
    id BIGSERIAL PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    version_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    theme_id VARCHAR(50) NOT NULL,
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_version_resume_id ON resume_versions(resume_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_version_resume_version ON resume_versions(resume_id, version_number);

CREATE TABLE IF NOT EXISTS resume_styles (
    id BIGSERIAL PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    theme_id VARCHAR(50) NOT NULL,
    font_size FLOAT,
    line_height FLOAT,
    section_spacing VARCHAR(20),
    custom_variables TEXT,
    UNIQUE(resume_id, theme_id)
);

CREATE TABLE IF NOT EXISTS share_links (
    id VARCHAR(36) PRIMARY KEY,
    resume_id VARCHAR(36) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    desensitize BOOLEAN NOT NULL DEFAULT false,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS section_templates (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    prompt TEXT NOT NULL,
    sort_order INTEGER
);

CREATE TABLE IF NOT EXISTS desensitize_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    pattern VARCHAR(500) NOT NULL,
    replacement VARCHAR(200) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER
);
