CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_app_users PRIMARY KEY (id),
    CONSTRAINT uk_app_users_email UNIQUE (email),
    CONSTRAINT ck_app_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    due_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by_id BIGINT NOT NULL,
    assignee_id BIGINT,
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_creator FOREIGN KEY (created_by_id) REFERENCES app_users (id),
    CONSTRAINT fk_tasks_assignee FOREIGN KEY (assignee_id) REFERENCES app_users (id),
    CONSTRAINT ck_tasks_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED')),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE TABLE task_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_task_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES app_users (id)
);

CREATE INDEX idx_tasks_status_created_at ON tasks (status, created_at);
CREATE INDEX idx_tasks_assignee_status ON tasks (assignee_id, status);
CREATE INDEX idx_tasks_creator_status ON tasks (created_by_id, status);
CREATE INDEX idx_comments_task_created_at ON task_comments (task_id, created_at);
