package com.hydra.core.utils;

public class CryptoUtil {

	public static String hashPassword(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt(12));
	}

	public static boolean checkPassword(String password, String hashedPassword) {
		return BCrypt.checkpw(password, hashedPassword);
	}

}