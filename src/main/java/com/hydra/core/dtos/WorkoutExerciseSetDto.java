package com.hydra.core.dtos;

import java.math.BigDecimal;

public record WorkoutExerciseSetDto(String id, Integer setNumber, int reps, BigDecimal weight, BigDecimal rpe,
									Integer restSeconds, String notes) {

}