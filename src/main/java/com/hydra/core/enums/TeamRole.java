package com.hydra.core.enums;

import lombok.Getter;

@Getter
public enum TeamRole {
	OWNER(1), COACH(2), ATHLETE(3);

	private final int order;

	TeamRole(int order) {
		this.order = order;
	}

	public static TeamRole fromString(String role) {
		for (TeamRole teamRole : TeamRole.values()) {
			if (teamRole.name().equalsIgnoreCase(role)) {
				return teamRole;
			}
		}
		throw new IllegalArgumentException("No enum constant for role: " + role);
	}
}