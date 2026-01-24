CREATE TABLE users
(
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6),
    updated_at TIMESTAMP(6)
);

CREATE INDEX idx_email ON users (email);
CREATE INDEX idx_username ON users (username);