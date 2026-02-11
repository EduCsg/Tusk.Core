package com.hydra.core.dtos;

import com.hydra.core.enums.ExerciseTechnique;

import java.util.List;

public record WorkoutExerciseDto(
	String id,
	String exerciseId,
	String exerciseName,
	Integer orderIndex,
	ExerciseTechnique technique,
	Integer restBetweenSetsSeconds,
	String notes,
	List<WorkoutExerciseSetDto> sets
) {
}
