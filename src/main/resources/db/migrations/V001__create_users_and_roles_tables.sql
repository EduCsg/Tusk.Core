CREATE TABLE roles
(
    id   VARCHAR(36) PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id         VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6),
    name       VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,

    CONSTRAINT idx_users_username UNIQUE (username),
    CONSTRAINT idx_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email_index ON users (email);
CREATE INDEX idx_users_username_index ON users (username);

CREATE TABLE users_roles
(
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_users_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);