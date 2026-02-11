package com.hydra.core.dtos;

import com.hydra.core.enums.Difficulty;
import com.hydra.core.enums.Equipment;
import com.hydra.core.enums.MuscleGroup;

import java.time.LocalDateTime;

public record ExerciseDto(
	String id,
	String name,
	String description,
	MuscleGroup muscleGroup,
	String secondaryMuscles,
	Equipment equipment,
	Difficulty difficulty,
	String videoUrl,
	String imageUrl,
	String instructions,
	Boolean isCustom,
	String createdByName,
	LocalDateTime createdAt
) {
}
