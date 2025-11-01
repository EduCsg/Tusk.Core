package com.hydra.core.utils;

import com.hydra.core.dtos.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

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

	public static String generateToken(String userId, String username, String email, String name, List<String> roles) {
		return Jwts.builder() //
				   .subject(username) //
				   .claim("userId", userId) //
				   .claim("username", username) //
				   .claim("email", email) //
				   .claim("name", name) //
				   .claim("roles", roles) //
				   .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 6)) // 6 hours
				   .signWith(jwtSecret) //
				   .compact();
	}

	public static boolean validateToken(String token) {
		if (ValidationUtils.isEmpty(token))
			return false;

		try {
			Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
			return true;
		} catch (JwtException ex) {
			return false;
		}
	}

	public static UserDto parseTokenToUser(String token) {
		Claims payload = Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();

		String userId = payload.get("userId").toString();
		String username = payload.get("username").toString();
		String email = payload.get("email").toString();
		String name = payload.get("name").toString();
		String roles = payload.get("roles").toString();

		System.out.println(payload);

		return new UserDto(userId, token, username, email, name, roles);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getRolesByToken(String token) {
		Claims payload = Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
		return payload.get("roles", List.class);
	}

}