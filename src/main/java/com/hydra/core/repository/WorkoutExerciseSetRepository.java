package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutExerciseSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutExerciseSetRepository extends JpaRepository<WorkoutExerciseSetEntity, String> {

	List<WorkoutExerciseSetEntity> findByWorkoutExerciseIdOrderBySetNumberAsc(String workoutExerciseId);

	void deleteByWorkoutExerciseId(String workoutExerciseId);

}