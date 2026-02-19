package com.hydra.core.exceptions;

public class InvalidRoleException extends RuntimeException {

	public InvalidRoleException() {
		super("Função inválida no convite!");
	}

}