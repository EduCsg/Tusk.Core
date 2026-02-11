package com.hydra.core.dtos;

import com.hydra.core.enums.ExerciseTechnique;

import java.util.List;

public record CreateWorkoutExerciseDto(String exerciseId, ExerciseTechnique technique, Integer restBetweenSetsSeconds,
									   String notes, List<CreateWorkoutSetDto> sets) {

}