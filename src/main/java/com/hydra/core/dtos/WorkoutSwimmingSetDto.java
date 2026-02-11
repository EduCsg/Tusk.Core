package com.hydra.core.dtos;

import com.hydra.core.enums.SwimmingEquipment;
import com.hydra.core.enums.SwimmingStroke;

public record WorkoutSwimmingSetDto(
	String id,
	Integer orderIndex,
	SwimmingStroke stroke,
	Integer distanceMeters,
	Integer repetitions,
	String targetTime,
	Integer targetPaceSeconds,
	Integer restSeconds,
	SwimmingEquipment equipment,
	String notes
) {
}
