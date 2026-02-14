package com.hydra.core.mappers;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.*;
import org.springframework.stereotype.Component;

@Component
public class WorkoutMapper {

	public WorkoutDto toDto(WorkoutEntity entity) {
		if (entity == null)
			return null;

		return new WorkoutDto(entity.getId(), entity.getTeam() != null ? entity.getTeam().getId() : null,
				entity.getTeam() != null ? entity.getTeam().getName() : null,
				entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
				entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null, entity.getTitle(),
				entity.getDescription(), entity.getModality(), entity.getScheduledDate(), entity.getScheduledTime(),
				entity.getDurationMinutes(), entity.getIntensity(), entity.getNotes(),
				entity.getExercises().stream().map(this::toExerciseDto).toList(),
				entity.getRunningSegments().stream().map(this::toRunningSegmentDto).toList(),
				entity.getSwimmingSets().stream().map(this::toSwimmingSetDto).toList(), entity.getCreatedAt(),
				entity.getUpdatedAt());
	}

	private WorkoutExerciseDto toExerciseDto(WorkoutExerciseEntity entity) {
		return new WorkoutExerciseDto(entity.getId(),
				entity.getExercise() != null ? entity.getExercise().getId() : null,
				entity.getExercise() != null ? entity.getExercise().getName() : null, entity.getOrderIndex(),
				entity.getTechnique(), entity.getRestBetweenSetsSeconds(), entity.getNotes(),
				entity.getSets().stream().map(this::toExerciseSetDto).toList());
	}

	private WorkoutExerciseSetDto toExerciseSetDto(WorkoutExerciseSetEntity entity) {
		return new WorkoutExerciseSetDto(entity.getId(), entity.getSetNumber(), entity.getReps(), entity.getWeight(),
				entity.getRpe(), entity.getRestSeconds(), entity.getNotes());
	}

	private WorkoutRunningSegmentDto toRunningSegmentDto(WorkoutRunningSegmentEntity entity) {
		return new WorkoutRunningSegmentDto(entity.getId(), entity.getOrderIndex(), entity.getSegmentType(),
				entity.getDistanceMeters(), entity.getDurationSeconds(), entity.getTargetPace(),
				entity.getTargetPaceSeconds(), entity.getIntensity(), entity.getNotes());
	}

	private WorkoutSwimmingSetDto toSwimmingSetDto(WorkoutSwimmingSetEntity entity) {
		return new WorkoutSwimmingSetDto(entity.getId(), entity.getOrderIndex(), entity.getStroke(),
				entity.getDistanceMeters(), entity.getRepetitions(), entity.getTargetTime(),
				entity.getTargetPaceSeconds(), entity.getRestSeconds(), entity.getEquipment(), entity.getNotes());
	}

}