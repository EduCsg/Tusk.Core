package com.hydra.core.utils;

import com.hydra.core.dtos.InviteTokenDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.exceptions.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

	private static final String JWT_ENV = "JWT_SECRET_KEY";
	private static final String BASE_URL = System.getenv("BASE_URL");
	private static final SecretKey jwtSecret;

	static {
		String key = System.getenv(JWT_ENV);
		if (ValidationUtils.isEmpty(key))
			throw new IllegalStateException("Environment variable " + JWT_ENV + " is required for JWT signing");

		jwtSecret = Keys.hmacShaKeyFor(key.getBytes());
	}

	public static String extractTokenFromHeader(String authorizationHeader) {
		if (ValidationUtils.isEmpty(authorizationHeader))
			throw new UnauthorizedException("Token ausente ou inválido");

		String prefix = "Bearer ";
		if (!authorizationHeader.startsWith(prefix))
			throw new UnauthorizedException("Token ausente ou inválido");

		return authorizationHeader.substring(prefix.length()).trim();
	}

	public static String generateToken(String userId, String username, String email, String name, String role) {
		return Jwts.builder() //
				   .subject(username) //
				   .claim("userId", userId) //
				   .claim("username", username) //
				   .claim("email", email) //
				   .claim("name", name) //
				   .claim("role", role) //
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
		String role = payload.get("role").toString();

		return new UserDto(userId, token, username, name, email, null, role);
	}

	public static String generateTeamInviteUrl(String teamId, String athleteId, String coachId) {
		String token = generateInviteToken(teamId, athleteId, coachId);
		return BASE_URL + "/teams/invite?token=" + token;
	}

	public static String generateInviteToken(String teamId, String athleteId, String coachId) {
		return Jwts.builder() //
				   .claim("teamId", teamId) //
				   .claim("athleteId", athleteId) //
				   .claim("coachId", coachId) //
				   .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
				   .signWith(jwtSecret) //
				   .compact();
	}

	public static InviteTokenDto parseInviteToken(String inviteToken) {
		Claims payload = Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(inviteToken).getPayload();

		String teamId = payload.get("teamId").toString();
		String athleteId = payload.get("athleteId").toString();
		String coachId = payload.get("coachId").toString();

		return new InviteTokenDto(teamId, athleteId, coachId);
	}

}