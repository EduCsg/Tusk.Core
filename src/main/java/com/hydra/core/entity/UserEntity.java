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
	private String id;

	private String name;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String username;

	private String password;
	private LocalDateTime updatedAt;
	private LocalDateTime createdAt;

	@ManyToMany(fetch = FetchType.EAGER)
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