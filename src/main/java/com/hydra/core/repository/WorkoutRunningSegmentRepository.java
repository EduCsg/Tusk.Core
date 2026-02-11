package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutRunningSegmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRunningSegmentRepository extends JpaRepository<WorkoutRunningSegmentEntity, String> {

	List<WorkoutRunningSegmentEntity> findByWorkoutIdOrderByOrderIndexAsc(String workoutId);

	void deleteByWorkoutId(String workoutId);

}