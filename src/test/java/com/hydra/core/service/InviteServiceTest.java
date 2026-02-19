package com.hydra.core.service;

import com.hydra.core.dtos.InviteTokenDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.TeamInviteRequestDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.TeamMemberEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.repository.TeamMemberRepository;
import com.hydra.core.repository.TeamRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

	private static final String AUTH = "Bearer token";
	private static final String TOKEN = "token";
	private static final String TEAM_ID = "team-1";
	private static final String COACH_ID = "coach-1";
	private static final String ATHLETE_ID = "athlete-1";
	private static final String INVITE_TOKEN = "invite.token.value";

	@Mock
	private UserRepository userRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private TeamMemberRepository teamMemberRepository;

	@Mock
	private EmailSender emailSender;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private InviteService inviteService;

	private UserEntity coachEntity() {
		UserEntity u = new UserEntity();
		u.setId(COACH_ID);
		u.setName("Coach Ana");
		u.setEmail("coach@example.com");
		u.setUsername("coach");
		return u;
	}

	private UserEntity athleteEntity() {
		UserEntity u = new UserEntity();
		u.setId(ATHLETE_ID);
		u.setName("John Doe");
		u.setEmail("john@example.com");
		u.setUsername("john");
		return u;
	}

	private UserDto coachDto() {
		return new UserDto(COACH_ID, TOKEN, "coach", "Coach Ana", "coach@example.com", null);
	}

	private UserDto athleteDto() {
		return new UserDto(ATHLETE_ID, TOKEN, "john", "John Doe", "john@example.com", null);
	}

	/**
	 * Monta um TeamEntity com a lista de members corretamente populada.
	 * <p>
	 * getCoaches() filtra members por COACH ou OWNER → retorna [coach] getAthletes() filtra members por ATHLETE →
	 * retorna []
	 */
	private TeamEntity teamWithCoach() {
		TeamEntity t = new TeamEntity();
		t.setId(TEAM_ID);
		t.setName("Hydra FC");
		t.setImageUrl("https://img.url/logo.png");

		TeamMemberEntity coachMember = new TeamMemberEntity();
		coachMember.setTeam(t);
		coachMember.setUser(coachEntity());
		coachMember.setRole(TeamRole.COACH);

		t.setMembers(new ArrayList<>(List.of(coachMember)));
		return t;
	}

	/**
	 * Time com COACH + ATHLETE já adicionados. getAthletes() → retorna [athlete] — usado para testar conflito.
	 */
	private TeamEntity teamWithCoachAndAthlete() {
		TeamEntity t = new TeamEntity();
		t.setId(TEAM_ID);
		t.setName("Hydra FC");
		t.setImageUrl("https://img.url/logo.png");

		TeamMemberEntity coachMember = new TeamMemberEntity();
		coachMember.setTeam(t);
		coachMember.setUser(coachEntity());
		coachMember.setRole(TeamRole.COACH);

		TeamMemberEntity athleteMember = new TeamMemberEntity();
		athleteMember.setTeam(t);
		athleteMember.setUser(athleteEntity());
		athleteMember.setRole(TeamRole.ATHLETE);

		t.setMembers(new ArrayList<>(List.of(coachMember, athleteMember)));
		return t;
	}

	/**
	 * Time sem nenhum membro. getCoaches() → [] — usado para testar coach não autorizado.
	 */
	private TeamEntity teamWithNoMembers() {
		TeamEntity t = new TeamEntity();
		t.setId(TEAM_ID);
		t.setName("Hydra FC");
		t.setMembers(new ArrayList<>());
		return t;
	}

	private InviteTokenDto inviteTokenDto() {
		return new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "ATHLETE");
	}

	private TeamInviteRequestDto validAthleteRequest() {
		return new TeamInviteRequestDto(COACH_ID, "john@example.com", "ATHLETE");
	}

	private void mockCoachAuth() {
		when(jwtService.extractTokenFromHeader(AUTH)).thenReturn(TOKEN);
		when(jwtService.parseTokenToUser(TOKEN)).thenReturn(coachDto());
	}

	private void mockAthleteAuth() {
		when(jwtService.extractTokenFromHeader(AUTH)).thenReturn(TOKEN);
		when(jwtService.parseTokenToUser(TOKEN)).thenReturn(athleteDto());
	}

	@Nested
	class CreateInviteToken {

		@Test
		void whenFieldsAreEmpty_returnsBadRequest() {
			TeamInviteRequestDto req = new TeamInviteRequestDto("", "", "ATHLETE");

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID, req);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Preencha os campos corretamente!");
			verifyNoInteractions(jwtService, teamRepository, userRepository);
		}

		@Test
		void whenRoleIsOwner_returnsBadRequest() {
			TeamInviteRequestDto req = new TeamInviteRequestDto("john@example.com", COACH_ID, "OWNER");

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID, req);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Tipo de usuário inválido!");
		}

		@Test
		void whenRoleIsUnknown_returnsBadRequest() {
			TeamInviteRequestDto req = new TeamInviteRequestDto("john@example.com", COACH_ID, "XPTO");

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID, req);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Tipo de usuário inválido!");
		}

		@Test
		void whenTokenUserDoesNotMatchCoachId_returnsForbidden() {
			when(jwtService.extractTokenFromHeader(AUTH)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(
					new UserDto("other-id", TOKEN, "x", "X", "x@x.com", null));

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		void whenTeamNotFound_returnsNotFound() {
			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Time não encontrado!");
		}

		@Test
		void whenCoachNotFound_returnsNotFound() {
			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Professor não encontrado");
		}

		@Test
		void whenCoachNotInTeam_returnsForbidden() {
			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithNoMembers()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("não autorizado");
		}

		@Test
		void whenAthleteNotFound_returnsNotFound() {
			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));
			when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			System.out.println(response.getBody());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Nenhum usuário foi encontrado!");
		}

		@Test
		void whenAthleteAlreadyInTeam_returnsForbidden() {
			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoachAndAthlete()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));
			when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(
					Optional.of(athleteEntity()));

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("O usuário já é membro dessa equipe!");
		}

		@Test
		void whenValid_asAthlete_returnsOkWithInviteUrl() {
			String expectedUrl = "https://hydra.app/teams/invite?token=xxx";

			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));
			when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(
					Optional.of(athleteEntity()));
			when(jwtService.generateTeamInviteUrl(TEAM_ID, ATHLETE_ID, COACH_ID, TeamRole.ATHLETE)).thenReturn(
					expectedUrl);

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID,
					validAthleteRequest());

			assertNotNull(response.getBody());
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().getData()).isEqualTo(expectedUrl);
			assertThat(response.getBody().getMessage()).isEqualTo("Link de convite gerado com sucesso");
		}

		@Test
		void whenValid_asCoach_returnsOk() {
			TeamInviteRequestDto req = new TeamInviteRequestDto(COACH_ID, "john@example.com", "COACH");
			String expectedUrl = "https://hydra.app/teams/invite?token=xxx";

			mockCoachAuth();
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));
			when(userRepository.findByEmailOrUsername(anyString(), anyString())).thenReturn(
					Optional.of(athleteEntity()));
			when(jwtService.generateTeamInviteUrl(TEAM_ID, ATHLETE_ID, COACH_ID, TeamRole.COACH)).thenReturn(
					expectedUrl);

			ResponseEntity<ResponseDto> response = inviteService.createInviteToken(AUTH, TEAM_ID, req);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

	}

	@Nested
	class AcceptInviteToken {

		@Test
		void whenTokenIsNull_returnsBadRequest() {
			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, null);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Token de convite é obrigatório!");
		}

		@Test
		void whenTokenIsEmpty_returnsBadRequest() {
			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, "");

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Token de convite é obrigatório!");
		}

		@Test
		void whenInviteDataIsNull_returnsBadRequest() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(null);

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Token de convite inválido!");
		}

		@Test
		void whenInviteDataHasEmptyTeamId_returnsBadRequest() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto("", ATHLETE_ID, COACH_ID, "ATHLETE"));

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Token de convite inválido!");
		}

		@Test
		void whenLoggedUserDoesNotMatchInviteUserId_returnsForbidden() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(jwtService.extractTokenFromHeader(AUTH)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(new UserDto("other", TOKEN, "x", "X", "x@x.com", null));

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Você não pode aceitar esse convite!");
		}

		@Test
		void whenRoleIsInvalid_returnsBadRequest() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "INVALID"));
			mockAthleteAuth();

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Função inválida no convite!");
		}

		@Test
		void whenRoleIsOwner_returnsForbidden() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "OWNER"));
			mockAthleteAuth();

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("proprietário");
		}

		@Test
		void whenUserAlreadyInTeam_returnsConflictWithTeamAndRoleInMessage() {
			TeamEntity t = new TeamEntity();
			t.setId(TEAM_ID);
			t.setName("Hydra FC");
			t.setMembers(new ArrayList<>());

			TeamMemberEntity existing = new TeamMemberEntity();
			existing.setTeam(t);
			existing.setUser(athleteEntity());
			existing.setRole(TeamRole.ATHLETE);

			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.of(existing));

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("já faz parte").contains("Hydra FC")
													   .contains(TeamRole.ATHLETE.getLabel());
		}

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.acceptInviteToken(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado!");
		}

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.acceptInviteToken(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado!");
		}

		@Test
		void whenInviterNotFound_throwsEntityNotFoundException() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.acceptInviteToken(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Quem convidou não foi encontrado");
		}

		@Test
		void whenValid_asAthlete_savesAndReturnsAthleteMessage() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).contains("atleta").contains("Hydra FC");
			verify(teamMemberRepository).save(any(TeamMemberEntity.class));
		}

		@Test
		void whenValid_asCoach_returnsCoachMessage() {
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "COACH"));
			mockAthleteAuth();
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));

			ResponseEntity<ResponseDto> response = inviteService.acceptInviteToken(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("treinador(a)");
		}

	}

	@Nested
	class SendInviteTokenByEmail {

		@Test
		void whenTokenIsNull_returnsBadRequest() throws IOException {
			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, null);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("O token é obrigatório!");
		}

		@Test
		void whenTokenIsEmpty_returnsBadRequest() throws IOException {
			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, "");

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("O token é obrigatório!");
		}

		@Test
		void whenSenderDoesNotMatchInvitedBy_returnsUnauthorized() throws IOException {
			when(jwtService.extractTokenFromHeader(AUTH)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(new UserDto("other", TOKEN, "x", "X", "x@x.com", null));
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Token inválido ou usuário não autorizado!");
		}

		@Test
		void whenUserIdIsNull_shouldReturnUnauthorized() throws Exception {
			String authorization = "Bearer token";
			String inviteToken = "inviteToken";

			when(jwtService.extractTokenFromHeader(authorization)).thenReturn("token");

			// user com id null
			UserDto userDto = new UserDto(null, null, "username", "Name", "email@email.com", "password");

			InviteTokenDto inviteDto = new InviteTokenDto("teamId", "userId", "ATHLETE", "inviterId");

			when(jwtService.parseTokenToUser("token")).thenReturn(userDto);
			when(jwtService.parseInviteToken(inviteToken)).thenReturn(inviteDto);

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(authorization, inviteToken);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}

		@Test
		void whenRoleIsInvalid_returnsBadRequest() throws IOException {
			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "INVALID"));

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("Função inválida no convite!");
		}

		@Test
		void whenRoleIsOwner_returnsForbidden() throws IOException {
			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(
					new InviteTokenDto(TEAM_ID, ATHLETE_ID, COACH_ID, "OWNER"));

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("proprietário");
		}

		@Test
		void whenUserAlreadyInTeam_returnsConflict() throws IOException {
			TeamEntity t = new TeamEntity();
			t.setId(TEAM_ID);
			t.setName("Hydra FC");
			t.setMembers(new ArrayList<>());

			TeamMemberEntity existing = new TeamMemberEntity();
			existing.setTeam(t);
			existing.setUser(athleteEntity());
			existing.setRole(TeamRole.ATHLETE);

			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.of(existing));

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).contains("já faz parte");
		}

		@Test
		void whenInvitedUserNotFound_throwsEntityNotFoundException() {
			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado!");
		}

		@Test
		void whenInvitedUserHasNoEmail_returnsBadRequest() throws IOException {
			UserEntity noEmail = athleteEntity();
			noEmail.setEmail(null);

			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(noEmail));

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertNotNull(response.getBody());
			assertThat(response.getBody().getMessage()).isEqualTo("O usuário não possui um e-mail cadastrado!");
		}

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado");
		}

		@Test
		void whenInviterNotFound_throwsEntityNotFoundException() {
			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Quem convidou não foi encontrado");
		}

		@Test
		void whenValid_sendsEmailAndReturnsOk() throws IOException {
			String inviteUrl = "https://hydra.app/teams/invite?token=" + INVITE_TOKEN;

			mockCoachAuth();
			when(jwtService.parseInviteToken(INVITE_TOKEN)).thenReturn(inviteTokenDto());
			when(jwtService.generateTeamInviteUrl(INVITE_TOKEN)).thenReturn(inviteUrl);
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, ATHLETE_ID)).thenReturn(Optional.empty());
			when(userRepository.findById(ATHLETE_ID)).thenReturn(Optional.of(athleteEntity()));
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamWithCoach()));
			when(userRepository.findById(COACH_ID)).thenReturn(Optional.of(coachEntity()));

			ResponseEntity<ResponseDto> response = inviteService.sendInviteTokenByEmail(AUTH, INVITE_TOKEN);

			assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).isEqualTo("Convite enviado por e-mail com sucesso!");
			verify(emailSender, times(1)).sendHtmlMail(eq("john@example.com"), contains("Hydra FC"),
					contains("Hydra FC"));
		}

		@Test
		void whenTemplateNotFound_throwsIOException() {
			String invalidTemplatePath = "/templates/arquivo-inexistente.html";
			Throwable thrown = catchThrowable(() -> inviteService.loadInviteTemplate(invalidTemplatePath));
			assertThat(thrown).isInstanceOf(IOException.class)
							  .hasMessage("Template não encontrado: " + invalidTemplatePath);
		}

	}

}