package com.hydra.core.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

	private static final String JWT_ENV = "JWT_SECRET_KEY";
	private static final SecretKey jwtSecret;

	static {
		String key = System.getenv(JWT_ENV);
		if (ValidationUtils.isEmpty(key))
			throw new IllegalStateException("Environment variable " + JWT_ENV + " is required for JWT signing");

		jwtSecret = Keys.hmacShaKeyFor(key.getBytes());
	}

	public static String generateToken(String userId, String username, String email, String name) {
		return Jwts.builder() //
				   .subject(username) //
				   .claim("userId", userId) //
				   .claim("username", username) //
				   .claim("email", email) //
				   .claim("name", name) //
				   .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6)) // 6 hours
				   .signWith(jwtSecret) //
				   .compact();
	}

	public static boolean validateToken(String token) {
		if (token == null || token.isBlank())
			return false;

		try {
			Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
			return true;
		} catch (JwtException ex) {
			return false;
		}
	}

}