package com.hydra.core.entity;

import com.hydra.core.enums.RunningSegmentType;
import com.hydra.core.enums.WorkoutIntensity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workout_running_segments")
public class WorkoutRunningSegmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_id", nullable = false)
	private WorkoutEntity workout;

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex;

	@Enumerated(EnumType.STRING)
	@Column(name = "segment_type", nullable = false, length = 20)
	private RunningSegmentType segmentType;

	@Column(name = "distance_meters")
	private Integer distanceMeters;

	@Column(name = "duration_seconds")
	private Integer durationSeconds;

	@Column(name = "target_pace", length = 10)
	private String targetPace; // "5:30" (min/km)

	@Column(name = "target_pace_seconds")
	private Integer targetPaceSeconds; // facilita cálculos

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private WorkoutIntensity intensity;

	@Column(columnDefinition = "TEXT")
	private String notes;

}