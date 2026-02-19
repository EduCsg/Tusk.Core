package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, String> {

	List<WorkoutEntity> findByTeamIdOrderByScheduledDateDesc(String teamId);

}