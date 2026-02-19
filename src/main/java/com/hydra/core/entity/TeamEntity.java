package com.hydra.core.entity;

import com.hydra.core.enums.TeamRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "teams")
public class TeamEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@Column(name = "uf", nullable = false, length = 2)
	private String uf;

	@Column(name = "color", nullable = false, length = 7)
	private String color;

	@Column(name = "image_url")
	private String imageUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", nullable = false, updatable = false)
	private UserEntity createdBy;

	@OneToMany(mappedBy = "team", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<TeamMemberEntity> members = new ArrayList<>();

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

	// Métodos helper úteis
	public List<UserEntity> getCoaches() {
		return members.stream().filter(m -> m.getRole() == TeamRole.COACH || m.getRole() == TeamRole.OWNER)
					  .map(TeamMemberEntity::getUser).toList();
	}

	public List<UserEntity> getAthletes() {
		return members.stream().filter(m -> m.getRole() == TeamRole.ATHLETE).map(TeamMemberEntity::getUser).toList();
	}

	public List<UserEntity> getOwners() {
		return members.stream().filter(m -> m.getRole() == TeamRole.OWNER).map(TeamMemberEntity::getUser).toList();
	}

	public boolean isOwner(UserEntity user) {
		return members.stream()
					  .anyMatch(m -> m.getUser().getId().equals(user.getId()) && m.getRole() == TeamRole.OWNER);
	}

	public boolean isCoach(UserEntity user) {
		return members.stream().anyMatch(m -> m.getUser().getId()
											   .equals(user.getId()) && (m.getRole() == TeamRole.COACH || m.getRole() == TeamRole.OWNER));
	}

	public boolean isMember(UserEntity user) {
		return members.stream().anyMatch(m -> m.getUser().getId().equals(user.getId()));
	}

}