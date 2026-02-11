package com.hydra.core.entity;

import com.hydra.core.enums.Difficulty;
import com.hydra.core.enums.Equipment;
import com.hydra.core.enums.MuscleGroup;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exercises")
public class ExerciseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "muscle_group", length = 50)
	private MuscleGroup muscleGroup;

	@Column(name = "secondary_muscles", length = 200)
	private String secondaryMuscles; // separado por vírgula: "TRICEPS,SHOULDERS"

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private Equipment equipment;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private Difficulty difficulty;

	@Column(name = "video_url", length = 255)
	private String videoUrl;

	@Column(name = "image_url", length = 255)
	private String imageUrl;

	@Column(columnDefinition = "TEXT")
	private String instructions;

	@Column(name = "is_custom", nullable = false)
	private Boolean isCustom = false;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private UserEntity createdBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
	}

}