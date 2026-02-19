package com.hydra.core.exceptions;

public class OwnerInviteNotAllowedException extends RuntimeException {

	public OwnerInviteNotAllowedException() {
		super("Não é possível usar OWNER via token!");
	}

}