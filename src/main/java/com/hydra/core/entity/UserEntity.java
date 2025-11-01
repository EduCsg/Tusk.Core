package com.hydra.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "users", indexes = { @Index(columnList = "email"), @Index(columnList = "username") })
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

	@Column(nullable = false, length = 255)
	private String password;

	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles", //
			joinColumns = @JoinColumn(name = "user_id"), //
			inverseJoinColumns = @JoinColumn(name = "role_id") //
	)
	private Set<RoleEntity> roles;

	@PrePersist
	private void prePersist() {
		updatedAt = LocalDateTime.now();
		createdAt = LocalDateTime.now();
	}

	@PreUpdate
	private void preUpdate() {
		updatedAt = LocalDateTime.now();
	}

}