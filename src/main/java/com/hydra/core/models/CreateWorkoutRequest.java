package com.hydra.core.models;

import com.hydra.core.enums.WorkoutIntensity;
import com.hydra.core.enums.WorkoutModality;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateWorkoutRequest(String teamId, String userId, String title, String description,
								   WorkoutModality modality, LocalDate scheduledDate, LocalTime scheduledTime,
								   Integer duration, WorkoutIntensity intensity, String notes) {

}