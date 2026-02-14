package com.hydra.core.security;

import com.hydra.core.dtos.InviteTokenDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.exceptions.InvalidTokenException;
import com.hydra.core.exceptions.UnauthorizedException;
import com.hydra.core.utils.ValidationUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtService {

	private final SecretKey jwtSecret;
	private final String baseUrl;
	private final JwtParser jwtParser;
	private final Clock clock;

	JwtService( //
			@Value("${jwt.secret}") String secret, //
			@Value("${app.base-url}") String baseUrl //
	) {
		if (ValidationUtils.isEmpty(secret)) {
			throw new IllegalStateException("JWT secret is required");
		}

		this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
		this.baseUrl = baseUrl;

		this.jwtParser = Jwts.parser().verifyWith(jwtSecret).build();
		this.clock = Clock.systemUTC();
	}

	public String extractTokenFromHeader(String authorizationHeader) {
		if (ValidationUtils.isEmpty(authorizationHeader))
			throw new UnauthorizedException("Token ausente ou inválido");

		String prefix = "Bearer ";
		if (!authorizationHeader.startsWith(prefix))
			throw new UnauthorizedException("Token ausente ou inválido");

		return authorizationHeader.substring(prefix.length()).trim();
	}

	public String generateToken(String userId, String username, String email, String name) {
		return Jwts.builder() //
				   .subject(username) //
				   .claim("userId", userId) //
				   .claim("username", username) //
				   .claim("email", email) //
				   .claim("name", name) //
				   .expiration(Date.from(clock.instant().plus(Duration.ofHours(6)))) //
				   .signWith(this.jwtSecret) //
				   .compact();
	}

	public boolean validateToken(String token) {
		if (ValidationUtils.isEmpty(token))
			return false;

		try {
			jwtParser.parseSignedClaims(token).getPayload();
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	public UserDto parseTokenToUser(String token) {
		try {
			Claims payload = jwtParser.parseSignedClaims(token).getPayload();

			String userId = payload.get("userId", String.class);

			if (ValidationUtils.isEmpty(userId))
				throw new InvalidTokenException();

			return new UserDto(userId, token, payload.get("username", String.class), payload.get("name", String.class),
					payload.get("email", String.class), null);
		} catch (Exception _) {
			throw new InvalidTokenException();
		}
	}

	public String generateTeamInviteUrl(String teamId, String athleteId, String coachId, TeamRole role) {
		String token = generateInviteToken(teamId, athleteId, coachId, role);
		return baseUrl + "/teams/invite?token=" + token;
	}

	public String generateTeamInviteUrl(String inviteToken) {
		return baseUrl + "/teams/invite?token=" + inviteToken;
	}

	public String generateInviteToken(String teamId, String athleteId, String coachId, TeamRole role) {
		return Jwts.builder() //
				   .claim("teamId", teamId) //
				   .claim("athleteId", athleteId) //
				   .claim("coachId", coachId) //
				   .claim("role", role.toString()) //
				   .expiration(Date.from(clock.instant().plus(Duration.ofHours(1)))) //
				   .signWith(jwtSecret) //
				   .compact();
	}

	public InviteTokenDto parseInviteToken(String inviteToken) {
		Claims payload = jwtParser.parseSignedClaims(inviteToken).getPayload();

		String teamId = payload.get("teamId").toString();
		String athleteId = payload.get("athleteId").toString();
		String coachId = payload.get("coachId").toString();
		String role = payload.get("role").toString();

		return new InviteTokenDto(teamId, athleteId, coachId, role);
	}

}