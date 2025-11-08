package com.hydra.core.entity;

import com.hydra.core.entity.pk.TeamCoachId;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "teams_coaches")
public class TeamCoachEntity {

	@EmbeddedId
	private TeamCoachId id;

	@ManyToOne
	@MapsId("teamId")
	@JoinColumn(name = "team_id")
	private TeamEntity team;

	@ManyToOne
	@MapsId("coachId")
	private UserEntity coach;

	@Column(name = "joined_at", nullable = false)
	private LocalDateTime joinedAt;

	@PrePersist
	private void prePersist() {
		this.joinedAt = LocalDateTime.now();
	}

}