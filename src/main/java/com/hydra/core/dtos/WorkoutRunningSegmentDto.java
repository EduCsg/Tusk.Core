package com.hydra.core.dtos;

import com.hydra.core.enums.RunningSegmentType;
import com.hydra.core.enums.WorkoutIntensity;

public record WorkoutRunningSegmentDto(String id, Integer orderIndex, RunningSegmentType segmentType,
									   Integer distanceMeters, Integer durationSeconds, String targetPace,
									   Integer targetPaceSeconds, WorkoutIntensity intensity, String notes) {

}