package com.hydra.core.enums;

import lombok.Getter;

@Getter
public enum TeamRole {

	OWNER("Dono"), COACH("Treinador"), ATHLETE("Atleta");

	private final String label;

	TeamRole(String label) {
		this.label = label;
	}

	public static TeamRole fromString(String role) {
		for (TeamRole teamRole : TeamRole.values()) {
			if (teamRole.name().equalsIgnoreCase(role)) {
				return teamRole;
			}
		}
		return null;
	}
}