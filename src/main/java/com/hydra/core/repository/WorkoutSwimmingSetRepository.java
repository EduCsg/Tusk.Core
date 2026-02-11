package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutSwimmingSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutSwimmingSetRepository extends JpaRepository<WorkoutSwimmingSetEntity, String> {

	List<WorkoutSwimmingSetEntity> findByWorkoutIdOrderByOrderIndexAsc(String workoutId);

	void deleteByWorkoutId(String workoutId);

}