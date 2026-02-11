-- 1. Treino base
CREATE TABLE workouts
(
    id               VARCHAR(36) PRIMARY KEY,
    team_id          VARCHAR(36)  NOT NULL REFERENCES teams ON DELETE CASCADE,
    created_by       VARCHAR(36)  NOT NULL REFERENCES users,
    title            VARCHAR(100) NOT NULL,
    description      TEXT,
    modality         VARCHAR(20)  NOT NULL, -- 'WEIGHTLIFTING', 'RUNNING', 'SWIMMING'
    scheduled_date   DATE,
    scheduled_time   TIME,
    duration_minutes INT,
    intensity        VARCHAR(20),           -- 'LOW', 'MODERATE', 'HIGH'
    notes            TEXT,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP,

    CONSTRAINT check_modality CHECK (modality IN ('WEIGHTLIFTING', 'RUNNING', 'SWIMMING'))
);

-- 2. Catálogo global de exercícios (para musculação)
CREATE TABLE exercises
(
    id                VARCHAR(36) PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    description       TEXT,
    muscle_group      VARCHAR(50),                                    -- 'CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE', 'GLUTES'
    secondary_muscles VARCHAR(200),                                   -- separado por vírgula: "TRICEPS,SHOULDERS"
    equipment         VARCHAR(50),                                    -- 'BARBELL', 'DUMBBELL', 'CABLE', 'MACHINE', 'BODYWEIGHT', 'KETTLEBELL'
    difficulty        VARCHAR(20),                                    -- 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'
    video_url         VARCHAR(255),
    image_url         VARCHAR(255),
    instructions      TEXT,
    is_custom         BOOLEAN DEFAULT FALSE,                          -- exercício criado por usuário
    created_by        VARCHAR(36) REFERENCES users ON DELETE CASCADE, -- null se for do catálogo global
    created_at        TIMESTAMP    NOT NULL,

    CONSTRAINT check_global_or_custom CHECK (
        (is_custom = FALSE AND created_by IS NULL) OR
        (is_custom = TRUE AND created_by IS NOT NULL)
        ),
    CONSTRAINT check_muscle_group CHECK (muscle_group IN
                                         ('CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE', 'GLUTES', 'CARDIO')),
    CONSTRAINT check_equipment CHECK (equipment IN
                                      ('BARBELL', 'DUMBBELL', 'CABLE', 'MACHINE', 'BODYWEIGHT', 'KETTLEBELL',
                                       'RESISTANCE_BAND', 'OTHER')),
    CONSTRAINT check_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

-- 3. Exercícios DO treino de musculação (liga treino -> exercício do catálogo)
CREATE TABLE workout_exercises
(
    id                        VARCHAR(36) PRIMARY KEY,
    workout_id                VARCHAR(36) NOT NULL REFERENCES workouts ON DELETE CASCADE,
    exercise_id               VARCHAR(36) NOT NULL REFERENCES exercises ON DELETE RESTRICT,
    order_index               INT         NOT NULL,
    technique                 VARCHAR(50), -- 'NORMAL', 'DROP_SET', 'SUPER_SET', 'PYRAMID', 'REST_PAUSE', 'CLUSTER_SET'
    rest_between_sets_seconds INT,
    notes                     TEXT,
    created_at                TIMESTAMP   NOT NULL,

    CONSTRAINT unique_workout_exercise_order UNIQUE (workout_id, order_index),
    CONSTRAINT check_technique CHECK (technique IN
                                      ('NORMAL', 'DROP_SET', 'SUPER_SET', 'PYRAMID', 'REST_PAUSE', 'CLUSTER_SET',
                                       'MTOR'))
);

-- 4. Séries específicas de cada exercício
CREATE TABLE workout_exercise_sets
(
    id                  VARCHAR(36) PRIMARY KEY,
    workout_exercise_id VARCHAR(36) NOT NULL REFERENCES workout_exercises ON DELETE CASCADE,
    set_number          INT         NOT NULL,
    reps                VARCHAR(20) NOT NULL, -- "12" ou "10-12" ou "até falha"
    weight              DECIMAL(6, 2),        -- peso em kg (null = peso corporal)
    rpe                 DECIMAL(3, 1),        -- Rate of Perceived Exertion 1-10
    rest_seconds        INT,                  -- sobrescreve o padrão se necessário
    notes               TEXT,

    CONSTRAINT unique_exercise_set UNIQUE (workout_exercise_id, set_number),
    CONSTRAINT check_rpe CHECK (rpe >= 1 AND rpe <= 10)
);

-- 5. Segmentos de treino de corrida
CREATE TABLE workout_running_segments
(
    id                  VARCHAR(36) PRIMARY KEY,
    workout_id          VARCHAR(36) NOT NULL REFERENCES workouts ON DELETE CASCADE,
    order_index         INT         NOT NULL,
    segment_type        VARCHAR(20) NOT NULL, -- 'WARMUP', 'INTERVAL', 'COOLDOWN', 'CONTINUOUS'
    distance_meters     INT,
    duration_seconds    INT,
    target_pace         VARCHAR(10),          -- "5:30" (min/km)
    target_pace_seconds INT,                  -- facilita o cálculo interno
    intensity           VARCHAR(20),          -- 'LIGHT', 'MODERATE', 'HIGH', 'SPRINT'
    notes               TEXT,

    CONSTRAINT unique_running_order UNIQUE (workout_id, order_index),
    CONSTRAINT check_segment_type CHECK (segment_type IN ('WARMUP', 'INTERVAL', 'COOLDOWN', 'CONTINUOUS')),
    CONSTRAINT check_running_intensity CHECK (intensity IN ('LIGHT', 'MODERATE', 'HIGH', 'SPRINT'))
);

-- 6. Sets de treino de natação
CREATE TABLE workout_swimming_sets
(
    id                  VARCHAR(36) PRIMARY KEY,
    workout_id          VARCHAR(36) NOT NULL REFERENCES workouts ON DELETE CASCADE,
    order_index         INT         NOT NULL,
    stroke              VARCHAR(20) NOT NULL, -- 'FREESTYLE', 'BACKSTROKE', 'BREASTSTROKE', 'BUTTERFLY', 'MEDLEY'
    distance_meters     INT         NOT NULL,
    repetitions         INT         NOT NULL,
    target_time         VARCHAR(10),          -- "1:30"
    target_pace_seconds INT,                  -- facilita o cálculo interno
    rest_seconds        INT,
    equipment           VARCHAR(50),          -- 'PULLBUOY', 'FINS', 'PADDLES', 'KICKBOARD', null
    notes               TEXT,

    CONSTRAINT unique_swimming_order UNIQUE (workout_id, order_index),
    CONSTRAINT check_stroke CHECK (stroke IN ('FREESTYLE', 'BACKSTROKE', 'BREASTSTROKE', 'BUTTERFLY', 'MEDLEY')),
    CONSTRAINT check_swimming_equipment CHECK (equipment IN
                                               ('PULLBUOY', 'FINS', 'PADDLES', 'KICKBOARD', 'SNORKEL', 'NONE') OR
                                               equipment IS NULL)
);

-- Índices para performance
CREATE INDEX idx_workouts_team ON workouts (team_id);
CREATE INDEX idx_workouts_created_by ON workouts (created_by);
CREATE INDEX idx_workouts_modality ON workouts (team_id, modality);
CREATE INDEX idx_workouts_scheduled ON workouts (scheduled_date, scheduled_time);

CREATE INDEX idx_exercises_name ON exercises (name);
CREATE INDEX idx_exercises_muscle_group ON exercises (muscle_group);
CREATE INDEX idx_exercises_custom ON exercises (is_custom, created_by);

CREATE INDEX idx_workout_exercises_workout ON workout_exercises (workout_id);
CREATE INDEX idx_workout_exercises_exercise ON workout_exercises (exercise_id);

CREATE INDEX idx_workout_sets_exercise ON workout_exercise_sets (workout_exercise_id);

CREATE INDEX idx_workout_running_workout ON workout_running_segments (workout_id);

CREATE INDEX idx_workout_swimming_workout ON workout_swimming_sets (workout_id);