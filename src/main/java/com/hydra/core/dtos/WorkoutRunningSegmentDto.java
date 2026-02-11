package com.hydra.core.dtos;

import com.hydra.core.enums.RunningIntensity;
import com.hydra.core.enums.RunningSegmentType;

public record WorkoutRunningSegmentDto(
	String id,
	Integer orderIndex,
	RunningSegmentType segmentType,
	Integer distanceMeters,
	Integer durationSeconds,
	String targetPace,
	Integer targetPaceSeconds,
	RunningIntensity intensity,
	String notes
) {
}
