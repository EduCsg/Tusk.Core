package com.hydra.core.security;

import com.hydra.core.dtos.InviteTokenDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.exceptions.InvalidTokenException;
import com.hydra.core.exceptions.UnauthorizedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

	private static final String VALID_SECRET = "my-super-secret-key-for-testing-1234567890!!";
	private static final String BASE_URL = "https://test-url.com";

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(VALID_SECRET, BASE_URL);
	}

	@Nested
	class Constructor {

		@Test
		void whenSecretIsNull_throwsIllegalStateException() {
			assertThatThrownBy(() -> new JwtService(null, BASE_URL)).isInstanceOf(IllegalStateException.class)
																	.hasMessageContaining("JWT secret is required");
		}

		@Test
		void whenSecretIsEmpty_throwsIllegalStateException() {
			assertThatThrownBy(() -> new JwtService("", BASE_URL)).isInstanceOf(IllegalStateException.class)
																  .hasMessageContaining("JWT secret is required");
		}

		@Test
		void whenSecretIsBlank_throwsIllegalStateException() {
			assertThatThrownBy(() -> new JwtService("   ", BASE_URL)).isInstanceOf(IllegalStateException.class)
																	 .hasMessageContaining("JWT secret is required");
		}

		@Test
		void whenValidParams_createsInstanceSuccessfully() {
			assertThatNoException().isThrownBy(() -> new JwtService(VALID_SECRET, BASE_URL));
		}

	}

	@Nested
	class ExtractTokenFromHeader {

		@Test
		void whenHeaderIsNull_throwsUnauthorizedException() {
			assertThatThrownBy(() -> jwtService.extractTokenFromHeader(null)).isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void whenHeaderIsEmpty_throwsUnauthorizedException() {
			assertThatThrownBy(() -> jwtService.extractTokenFromHeader("")).isInstanceOf(UnauthorizedException.class);
		}

		@Test
		void whenHeaderIsBlank_throwsUnauthorizedException() {
			assertThatThrownBy(() -> jwtService.extractTokenFromHeader("   ")).isInstanceOf(
					UnauthorizedException.class);
		}

		@Test
		void whenHeaderDoesNotStartWithBearer_throwsUnauthorizedException() {
			assertThatThrownBy(() -> jwtService.extractTokenFromHeader("Token abc123")).isInstanceOf(
					UnauthorizedException.class);
		}

		@Test
		void whenHeaderHasBearerPrefix_returnsToken() {
			String token = "abc123.def456.ghi789";
			String result = jwtService.extractTokenFromHeader("Bearer " + token);
			assertThat(result).isEqualTo(token);
		}

		@Test
		void whenHeaderHasBearerPrefixWithExtraWhitespace_returnsTrimmedToken() {
			String token = "abc123.def456.ghi789";
			String result = jwtService.extractTokenFromHeader("Bearer " + token + "  ");
			assertThat(result).isEqualTo(token);
		}

	}

	@Nested
	class GenerateToken {

		@Test
		void generatedToken_isValidatable() {
			String token = jwtService.generateToken("uid-1", "john", "john@example.com", "John Doe");
			assertThat(jwtService.validateToken(token)).isTrue();
		}

		@Test
		void generatedToken_parsesAllClaimsCorrectly() {
			String token = jwtService.generateToken("uid-1", "john", "john@example.com", "John Doe");
			UserDto user = jwtService.parseTokenToUser(token);

			assertThat(user.id()).isEqualTo("uid-1");
			assertThat(user.username()).isEqualTo("john");
			assertThat(user.email()).isEqualTo("john@example.com");
			assertThat(user.name()).isEqualTo("John Doe");
			assertThat(user.token()).isEqualTo(token);
		}

	}

	@Nested
	class ValidateToken {

		@Test
		void whenTokenIsNull_returnsFalse() {
			assertThat(jwtService.validateToken(null)).isFalse();
		}

		@Test
		void whenTokenIsEmpty_returnsFalse() {
			assertThat(jwtService.validateToken("")).isFalse();
		}

		@Test
		void whenTokenIsBlank_returnsFalse() {
			assertThat(jwtService.validateToken("   ")).isFalse();
		}

		@Test
		void whenTokenIsMalformed_returnsFalse() {
			assertThat(jwtService.validateToken("not.a.valid.token")).isFalse();
		}

		@Test
		void whenTokenIsSignedWithDifferentKey_returnsFalse() {
			String differentSecret = "another-secret-key-completely-different-one-xyz!!";
			JwtService otherService = new JwtService(differentSecret, BASE_URL);
			String token = otherService.generateToken("uid-1", "john", "john@example.com", "John Doe");

			assertThat(jwtService.validateToken(token)).isFalse();
		}

		@Test
		void whenTokenIsExpired_returnsFalse() {
			SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes());
			String expiredToken = Jwts.builder().subject("john").claim("userId", "uid-1").claim("username", "john")
									  .claim("email", "john@example.com").claim("name", "John Doe")
									  .expiration(Date.from(Instant.now().minus(Duration.ofHours(1)))).signWith(key)
									  .compact();

			assertThat(jwtService.validateToken(expiredToken)).isFalse();
		}

		@Test
		void whenTokenIsValid_returnsTrue() {
			String token = jwtService.generateToken("uid-1", "john", "john@example.com", "John Doe");
			assertThat(jwtService.validateToken(token)).isTrue();
		}

	}

	@Nested
	class ParseTokenToUser {

		@Test
		void whenTokenIsValid_returnsCorrectUserDto() {
			String token = jwtService.generateToken("uid-42", "jane", "jane@example.com", "Jane Doe");
			UserDto user = jwtService.parseTokenToUser(token);

			assertThat(user.id()).isEqualTo("uid-42");
			assertThat(user.username()).isEqualTo("jane");
			assertThat(user.email()).isEqualTo("jane@example.com");
			assertThat(user.name()).isEqualTo("Jane Doe");
			assertThat(user.token()).isEqualTo(token);
		}

		@Test
		void whenTokenIsInvalid_throwsInvalidTokenException() {
			assertThatThrownBy(() -> jwtService.parseTokenToUser("garbage")).isInstanceOf(InvalidTokenException.class);
		}

		@Test
		void whenUserIdClaimIsMissing_throwsInvalidTokenException() {
			SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes());
			// Build token without userId claim
			String tokenWithoutUserId = Jwts.builder().subject("john").claim("username", "john")
											.claim("email", "john@example.com").claim("name", "John Doe")
											.expiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
											.signWith(key).compact();

			assertThatThrownBy(() -> jwtService.parseTokenToUser(tokenWithoutUserId)).isInstanceOf(
					InvalidTokenException.class);
		}

		@Test
		void whenTokenIsExpired_throwsInvalidTokenException() {
			SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes());
			String expiredToken = Jwts.builder().subject("john").claim("userId", "uid-1").claim("username", "john")
									  .claim("email", "john@example.com").claim("name", "John Doe")
									  .expiration(Date.from(Instant.now().minus(Duration.ofHours(1)))).signWith(key)
									  .compact();

			assertThatThrownBy(() -> jwtService.parseTokenToUser(expiredToken)).isInstanceOf(
					InvalidTokenException.class);
		}

	}

	@Nested
	class GenerateTeamInviteUrl {

		@Test
		void withClaims_returnsUrlStartingWithBaseUrlAndPath() {
			String url = jwtService.generateTeamInviteUrl("team-1", "athlete-1", "coach-1", TeamRole.ATHLETE);

			assertThat(url).startsWith(BASE_URL + "/teams/invite?token=");
		}

		@Test
		void withClaims_tokenPartOfUrlIsParseable() {
			String url = jwtService.generateTeamInviteUrl("team-1", "athlete-1", "coach-1", TeamRole.ATHLETE);
			String token = url.substring((BASE_URL + "/teams/invite?token=").length());

			InviteTokenDto dto = jwtService.parseInviteToken(token);
			assertThat(dto.teamId()).isEqualTo("team-1");
			assertThat(dto.userId()).isEqualTo("athlete-1");
			assertThat(dto.invitedBy()).isEqualTo("coach-1");
			assertThat(dto.role()).isEqualTo(TeamRole.ATHLETE.toString());
		}

		@Test
		void withExistingToken_returnsUrlWithThatExactToken() {
			String existingToken = "my.existing.token";
			String url = jwtService.generateTeamInviteUrl(existingToken);

			assertThat(url).isEqualTo(BASE_URL + "/teams/invite?token=" + existingToken);
		}

	}

	@Nested
	class GenerateInviteToken {

		@Test
		void generatedToken_isNotBlank() {
			String token = jwtService.generateInviteToken("team-1", "athlete-1", "coach-1", TeamRole.COACH);
			assertThat(token).isNotBlank();
		}

		@Test
		void generatedToken_containsAllClaims() {
			String token = jwtService.generateInviteToken("team-99", "athlete-99", "coach-99", TeamRole.COACH);
			InviteTokenDto dto = jwtService.parseInviteToken(token);

			assertThat(dto.teamId()).isEqualTo("team-99");
			assertThat(dto.userId()).isEqualTo("athlete-99");
			assertThat(dto.invitedBy()).isEqualTo("coach-99");
			assertThat(dto.role()).isEqualTo(TeamRole.COACH.toString());
		}

	}

	@Nested
	class ParseInviteToken {

		@Test
		void whenTokenIsValid_returnsCorrectDto() {
			String token = jwtService.generateInviteToken("team-1", "athlete-1", "coach-1", TeamRole.ATHLETE);
			InviteTokenDto dto = jwtService.parseInviteToken(token);

			assertThat(dto.teamId()).isEqualTo("team-1");
			assertThat(dto.userId()).isEqualTo("athlete-1");
			assertThat(dto.invitedBy()).isEqualTo("coach-1");
			assertThat(dto.role()).isEqualTo(TeamRole.ATHLETE.toString());
		}

		@Test
		void whenTokenIsInvalid_throwsException() {
			assertThatThrownBy(() -> jwtService.parseInviteToken("invalid.token.value")).isInstanceOf(Exception.class);
		}

		@Test
		void whenTokenIsExpired_throwsException() {
			SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes());
			String expiredToken = Jwts.builder().claim("teamId", "team-1").claim("athleteId", "athlete-1")
									  .claim("coachId", "coach-1").claim("role", TeamRole.ATHLETE.toString())
									  .expiration(Date.from(Instant.now().minus(Duration.ofHours(1)))).signWith(key)
									  .compact();

			assertThatThrownBy(() -> jwtService.parseInviteToken(expiredToken)).isInstanceOf(Exception.class);
		}

		@Test
		void whenSignedWithDifferentKey_throwsException() {
			String differentSecret = "another-secret-key-completely-different-one-xyz!!";
			JwtService otherService = new JwtService(differentSecret, BASE_URL);
			String token = otherService.generateInviteToken("team-1", "athlete-1", "coach-1", TeamRole.ATHLETE);

			assertThatThrownBy(() -> jwtService.parseInviteToken(token)).isInstanceOf(Exception.class);
		}

	}

	@Nested
	class InviteTokenDtoRecord {

		@Test
		void accessors_returnCorrectValues() {
			InviteTokenDto dto = new InviteTokenDto("team-1", "user-1", "invited-by-1", "ATHLETE");

			assertThat(dto.teamId()).isEqualTo("team-1");
			assertThat(dto.userId()).isEqualTo("user-1");
			assertThat(dto.invitedBy()).isEqualTo("invited-by-1");
			assertThat(dto.role()).isEqualTo("ATHLETE");
		}

		@Test
		void equals_whenSameValues_returnsTrue() {
			InviteTokenDto dto1 = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");
			InviteTokenDto dto2 = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");

			assertThat(dto1).isEqualTo(dto2);
		}

		@Test
		void equals_whenDifferentValues_returnsFalse() {
			InviteTokenDto dto1 = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");
			InviteTokenDto dto2 = new InviteTokenDto("team-2", "user-1", "coach-1", "ATHLETE");

			assertThat(dto1).isNotEqualTo(dto2);
		}

		@Test
		void hashCode_whenSameValues_areEqual() {
			InviteTokenDto dto1 = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");
			InviteTokenDto dto2 = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");

			assertThat(dto1.hashCode()).hasSameHashCodeAs(dto2.hashCode());
		}

		@Test
		void toString_containsAllFields() {
			InviteTokenDto dto = new InviteTokenDto("team-1", "user-1", "coach-1", "ATHLETE");

			assertThat(dto.toString()).contains("team-1", "user-1", "coach-1", "ATHLETE");
		}

	}

}