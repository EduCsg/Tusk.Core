package com.hydra.core.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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

	@Column(nullable = false)
	private String password;

	@Column(nullable = false, length = 20)
	private String role;

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

}