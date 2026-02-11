package com.hydra.core.dtos;

import com.hydra.core.enums.WorkoutIntensity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateRunningWorkoutDto(String teamId, String title, String description, LocalDate scheduledDate,
									  LocalTime scheduledTime, Integer durationMinutes, WorkoutIntensity intensity,
									  String notes, List<CreateRunningSegmentDto> segments) {

}