package com.hydra.core.exceptions;

public class InvalidTokenException extends RuntimeException {

	public InvalidTokenException() {
		super("Token inválido ou usuário não autorizado!");
	}

}