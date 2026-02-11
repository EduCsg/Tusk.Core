package com.hydra.core.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hydra.core.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "users", indexes = { @Index(columnList = "email"), @Index(columnList = "username") })
@JsonIgnoreProperties({ "teamMemberships", "password" })
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, unique = true, length = 36, updatable = false)
	private String id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(nullable = false)
	private String password;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
	private List<TeamMemberEntity> teamMemberships;

	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		updatedAt = LocalDateTime.now();
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	private void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public List<TeamEntity> getTeamsAsCoach() {
		return teamMemberships.stream().filter(m -> m.getRole() == TeamRole.COACH || m.getRole() == TeamRole.OWNER)
							  .map(TeamMemberEntity::getTeam).toList();
	}

	public List<TeamEntity> getTeamsAsAthlete() {
		return teamMemberships.stream().filter(m -> m.getRole() == TeamRole.ATHLETE).map(TeamMemberEntity::getTeam)
							  .toList();
	}

}