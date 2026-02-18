package com.hydra.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "workout_exercise_sets")
public class WorkoutExerciseSetEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_exercise_id", nullable = false)
	private WorkoutExerciseEntity workoutExercise;

	@Column(name = "set_number", nullable = false)
	private Integer setNumber;

	@Column(nullable = false, length = 20)
	private int reps; // "12", "10-12", "até falha"

	@Column(precision = 6, scale = 2)
	private BigDecimal weight; // em kg

	@Column(precision = 3, scale = 1)
	private BigDecimal rpe; // Rate of Perceived Exertion 1-10

	@Column(name = "rest_seconds")
	private Integer restSeconds;

	@Column(columnDefinition = "TEXT")
	private String notes;

}