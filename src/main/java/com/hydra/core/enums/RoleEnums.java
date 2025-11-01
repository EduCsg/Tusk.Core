package com.hydra.core.enums;

import lombok.Getter;

@Getter
public enum RoleEnums {
	ADMIN("f7ece7f5-aa85-453a-b27b-3d7ef3b4e828", "ADMIN"), //
	COACH("0fc2c5e2-5184-47e1-acb9-b7ac8852f4f2", "COACH"), //
	ATHLETE("09d62575-0c5b-439d-ad99-95e4d0e50375", "ATHLETE");

	private final String id;
	private final String label;

	RoleEnums(String id, String label) {
		this.id = id;
		this.label = label;
	}
}