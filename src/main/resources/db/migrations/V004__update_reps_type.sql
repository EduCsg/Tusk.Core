ALTER TABLE workout_exercise_sets
    ALTER COLUMN reps TYPE INT
    USING reps::integer;