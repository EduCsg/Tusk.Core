package com.hydra.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "roles")
public class RoleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, unique = true, length = 36, updatable = false)
	private String id;

	@Column(name = "name", nullable = false, unique = true, length = 30)
	private String name;

}