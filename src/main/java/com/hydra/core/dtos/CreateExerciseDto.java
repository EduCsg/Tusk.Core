package com.hydra.core.dtos;

import com.hydra.core.enums.Difficulty;
import com.hydra.core.enums.Equipment;
import com.hydra.core.enums.MuscleGroup;

public record CreateExerciseDto(String name, String description, MuscleGroup muscleGroup, String secondaryMuscles,
								Equipment equipment, Difficulty difficulty, String videoUrl, String imageUrl,
								String instructions) {

}