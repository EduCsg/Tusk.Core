package com.hydra.core.entity;

import com.hydra.core.enums.ExerciseTechnique;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "workout_exercises")
public class WorkoutExerciseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workout_id", nullable = false)
	private WorkoutEntity workout;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercise_id", nullable = false)
	private ExerciseEntity exercise;

	@Column(name = "order_index", nullable = false)
	private Integer orderIndex;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private ExerciseTechnique technique;

	@Column(name = "rest_between_sets_seconds")
	private Integer restBetweenSetsSeconds;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("setNumber ASC")
	private List<WorkoutExerciseSetEntity> sets = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
	}

}