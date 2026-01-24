package com.hydra.core.entity;

import com.hydra.core.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "team_members", uniqueConstraints = @UniqueConstraint(columnNames = { "team_id", "user_id" }))
public class TeamMemberEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, unique = true, length = 36, updatable = false)
	private String id;

	@ManyToOne
	@JoinColumn(name = "team_id", nullable = false)
	private TeamEntity team;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TeamRole role;

	@ManyToOne
	@JoinColumn(name = "invited_by")
	private UserEntity invitedBy;

	@Column(nullable = false)
	private LocalDateTime joinedAt;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void prePersist() {
		createdAt = LocalDateTime.now();
		joinedAt = LocalDateTime.now();
	}

}