package com.hydra.core.entity;

import com.hydra.core.entity.pk.TeamAthleteId;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teams_athletes")
public class TeamAthleteEntity {

	@EmbeddedId
	private TeamAthleteId id;

	@ManyToOne
	@MapsId("teamId")
	@JoinColumn(name = "team_id")
	private TeamEntity team;

	@ManyToOne
	@MapsId("athleteId")
	private UserEntity athlete;

	@ManyToOne
	@JoinColumn(name = "invited_by")
	private UserEntity invitedBy;

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt;

	@PrePersist
	private void prePersist() {
		this.joinedAt = LocalDateTime.now();
	}

}