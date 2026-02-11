package com.hydra.core.dtos;

import java.math.BigDecimal;

public record CreateWorkoutSetDto(Integer setNumber, String reps, BigDecimal weight, BigDecimal rpe,
								  Integer restSeconds, String notes) {

}