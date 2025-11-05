CREATE TABLE teams
(
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    color      VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);


CREATE TABLE teams_athletes
(
    athlete_id VARCHAR(36) NOT NULL,
    team_id    VARCHAR(36) NOT NULL,
    invited_by VARCHAR(36),
    joined_at  TIMESTAMP,
    CONSTRAINT fk_teams_athletes_athlete FOREIGN KEY (athlete_id) REFERENCES users (id),
    CONSTRAINT fk_teams_athletes_invited_by FOREIGN KEY (invited_by) REFERENCES users (id),
    CONSTRAINT fk_teams_athletes_team FOREIGN KEY (team_id) REFERENCES teams (id),
    PRIMARY KEY (athlete_id, team_id)
);


CREATE TABLE teams_coaches
(
    team_id  VARCHAR(36) NOT NULL,
    coach_id VARCHAR(36) NOT NULL,
    CONSTRAINT fk_teams_coaches_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_teams_coaches_coach FOREIGN KEY (coach_id) REFERENCES users (id),
    PRIMARY KEY (team_id, coach_id)
);