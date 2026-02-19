package com.hydra.core.entity;

import com.hydra.core.enums.WorkoutIntensity;
import com.hydra.core.enums.WorkoutModality;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "workouts")
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private TeamEntity team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false)
	private UserEntity createdBy;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private WorkoutModality modality;

	@Column(name = "scheduled_date")
	private LocalDate scheduledDate;

	@Column(name = "scheduled_time")
	private LocalTime scheduledTime;

	@Column(name = "duration_minutes")
	private Integer durationMinutes;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private WorkoutIntensity intensity;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orderIndex ASC")
	@Builder.Default
	private List<WorkoutExerciseEntity> exercises = new ArrayList<>();

	@OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orderIndex ASC")
	@Builder.Default
	private List<WorkoutRunningSegmentEntity> runningSegments = new ArrayList<>();

	@OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orderIndex ASC")
	@Builder.Default
	private List<WorkoutSwimmingSetEntity> swimmingSets = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	private void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

}