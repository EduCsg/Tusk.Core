package com.hydra.core.exceptions;

public class UserAlreadyInTeamException extends RuntimeException {

	public UserAlreadyInTeamException(String teamName, String role) {
		super("Usuário já faz parte da equipe " + teamName + " como " + role + "!");
	}

}