package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExerciseEntity, String> {

	List<WorkoutExerciseEntity> findByWorkoutIdOrderByOrderIndexAsc(String workoutId);

	List<WorkoutExerciseEntity> findByExerciseId(String exerciseId);

	boolean existsByWorkoutIdAndExerciseId(String workoutId, String exerciseId);

	void deleteByWorkoutId(String workoutId);

}