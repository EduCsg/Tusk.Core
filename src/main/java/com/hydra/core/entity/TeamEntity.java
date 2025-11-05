package com.hydra.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "teams")
public class TeamEntity {

	@Id
	@Column(name = "id", length = 36)
	private String teamId;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "color", nullable = false)
	private String color;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "teams_coaches", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "coach_id"))
	private Set<UserEntity> coaches;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "teams_athletes", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "athlete_id"))
	private Set<UserEntity> athletes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
	}

}