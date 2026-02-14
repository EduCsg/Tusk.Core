package com.hydra.core.dtos;

import com.hydra.core.enums.WorkoutIntensity;
import com.hydra.core.enums.WorkoutModality;

import java.time.LocalDate;
import java.time.LocalTime;

public interface CreateWorkoutDtoBase {

	String teamId();
	String title();
	String description();
	LocalDate scheduledDate();
	LocalTime scheduledTime();
	Integer durationMinutes();
	WorkoutIntensity intensity();
	String notes();
	WorkoutModality modality();

}