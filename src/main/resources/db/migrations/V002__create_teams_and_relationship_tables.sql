CREATE TABLE teams
(
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    city        VARCHAR(100),
    uf          CHAR(2),
    color       VARCHAR(7)  NOT NULL,
    image_url   VARCHAR(255),
    updated_at  TIMESTAMP   NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(36) NOT NULL,
    CONSTRAINT fk_teams_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);


CREATE TABLE team_members
(
    id         VARCHAR(36) NOT NULL PRIMARY KEY,
    team_id    VARCHAR(36) NOT NULL
        CONSTRAINT fk_team_members_team REFERENCES teams ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL
        CONSTRAINT fk_team_members_user REFERENCES users ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL,
    invited_by VARCHAR(36)
        CONSTRAINT fk_team_members_invited_by REFERENCES users ON DELETE SET NULL,
    joined_at  TIMESTAMP   NOT NULL,
    created_at TIMESTAMP   NOT NULL,

    CONSTRAINT unique_team_user UNIQUE (team_id, user_id),
    CONSTRAINT check_valid_role CHECK (role IN ('OWNER', 'COACH', 'ATHLETE'))
);

CREATE INDEX idx_team_members_team ON team_members (team_id);
CREATE INDEX idx_team_members_user ON team_members (user_id);
CREATE INDEX idx_team_members_role ON team_members (team_id, role);