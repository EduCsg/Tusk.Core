package com.hydra.core.dtos;

import com.hydra.core.enums.WorkoutIntensity;
import com.hydra.core.enums.WorkoutModality;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record WorkoutDto(
	String id,
	String teamId,
	String teamName,
	String createdById,
	String createdByName,
	String title,
	String description,
	WorkoutModality modality,
	LocalDate scheduledDate,
	LocalTime scheduledTime,
	Integer durationMinutes,
	WorkoutIntensity intensity,
	String notes,
	List<WorkoutExerciseDto> exercises,
	List<WorkoutRunningSegmentDto> runningSegments,
	List<WorkoutSwimmingSetDto> swimmingSets,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
