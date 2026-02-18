package com.hydra.core.dtos;

import java.math.BigDecimal;

public record CreateWorkoutSetDto(Integer setNumber, int reps, BigDecimal weight, BigDecimal rpe, Integer restSeconds,
								  String notes) {

}