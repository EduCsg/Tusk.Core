package com.hydra.core.entity;

import com.hydra.core.enums.SwimmingEquipment;
import com.hydra.core.enums.SwimmingStroke;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workout_swimming_sets")
public class WorkoutSwimmingSetEntity {

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
	@Column(nullable = false, length = 20)
	private SwimmingStroke stroke;

	@Column(name = "distance_meters", nullable = false)
	private Integer distanceMeters;

	@Column(nullable = false)
	private Integer repetitions;

	@Column(name = "target_time", length = 10)
	private String targetTime; // "1:30"

	@Column(name = "target_pace_seconds")
	private Integer targetPaceSeconds; // facilita cálculos

	@Column(name = "rest_seconds")
	private Integer restSeconds;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private SwimmingEquipment equipment;

	@Column(columnDefinition = "TEXT")
	private String notes;

}