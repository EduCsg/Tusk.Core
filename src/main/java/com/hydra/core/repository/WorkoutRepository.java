package com.hydra.core.repository;

import com.hydra.core.entity.WorkoutEntity;
import com.hydra.core.enums.WorkoutModality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, String> {

	List<WorkoutEntity> findByTeamIdOrderByScheduledDateDesc(String teamId);

	List<WorkoutEntity> findByTeamIdAndModalityOrderByScheduledDateDesc(String teamId, WorkoutModality modality);

	List<WorkoutEntity> findByCreatedById(String createdById);

	List<WorkoutEntity> findByTeamIdAndScheduledDateBetween(String teamId, LocalDate startDate, LocalDate endDate);

	List<WorkoutEntity> findByScheduledDate(LocalDate scheduledDate);

	boolean existsByIdAndTeamId(String id, String teamId);

}