package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.TeamMemberEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.exceptions.UnauthorizedException;
import com.hydra.core.repository.TeamMemberRepository;
import com.hydra.core.repository.TeamRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

	private static final String AUTH_HEADER = "Bearer valid-token";
	private static final String TOKEN = "valid-token";
	private static final String USER_ID = "user-1";
	private static final String TEAM_ID = "team-1";
	@Mock
	private UserRepository userRepository;
	@Mock
	private TeamRepository teamRepository;
	@Mock
	private TeamMemberRepository teamMemberRepository;
	@Mock
	private JwtService jwtService;
	@InjectMocks
	private TeamService teamService;
	private UserDto userDto() {
		return new UserDto(USER_ID, TOKEN, "john", "John Doe", "john@example.com", null);
	}

	private UserEntity userEntity() {
		UserEntity u = new UserEntity();
		u.setId(USER_ID);
		u.setName("John Doe");
		u.setEmail("john@example.com");
		u.setUsername("john");
		return u;
	}

	private TeamEntity teamEntity() {
		TeamEntity t = new TeamEntity();
		t.setId(TEAM_ID);
		t.setName("Hydra FC");
		t.setDescription("Best team");
		t.setCity("São Paulo");
		t.setUf("SP");
		t.setColor("#FF0000");
		t.setImageUrl("https://img.url/logo.png");
		t.setCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0));
		return t;
	}

	private TeamMemberEntity memberEntity(UserEntity user, TeamEntity team, TeamRole role) {
		TeamMemberEntity m = new TeamMemberEntity();
		m.setId("member-1");
		m.setUser(user);
		m.setTeam(team);
		m.setRole(role);
		m.setInvitedBy(null);
		m.setJoinedAt(LocalDateTime.of(2024, 1, 2, 0, 0));
		m.setCreatedAt(LocalDateTime.of(2024, 1, 2, 0, 0));
		return m;
	}

	private CreateTeamDto validCreateDto() {
		return new CreateTeamDto("Hydra FC", "Best team", "São Paulo", "SP", "#FF0000", "https://img.url/logo.png");
	}

	@Nested
	class CreateTeam {

		@Test
		void whenRequiredFieldsAreMissing_returnsBadRequest() {
			CreateTeamDto dto = new CreateTeamDto("", "desc", "city", "SP", "#000", null);

			ResponseEntity<ResponseDto> response = teamService.createTeam(AUTH_HEADER, dto);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody().getMessage()).isEqualTo("Preencha os campos obrigatórios corretamente!");
			verifyNoInteractions(jwtService, userRepository, teamRepository, teamMemberRepository);
		}

		@Test
		void whenImageUrlIsInvalid_returnsBadRequest() {
			CreateTeamDto dto = new CreateTeamDto("Hydra FC", "desc", "São Paulo", "SP", "#000", "not-a-url");

			ResponseEntity<ResponseDto> response = teamService.createTeam(AUTH_HEADER, dto);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody().getMessage()).isEqualTo("A URL da imagem é inválida!");
		}

		@Test
		void whenImageUrlIsNull_imageValidationIsSkipped() {
			CreateTeamDto dto = new CreateTeamDto("Hydra FC", "desc", "São Paulo", "SP", "#000", null);
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));

			ResponseEntity<ResponseDto> response = teamService.createTeam(AUTH_HEADER, dto);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		}

		@Test
		void whenAuthorizationIsInvalid_returnsUnauthorized() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenThrow(
					new UnauthorizedException("Token ausente ou inválido"));

			ResponseEntity<ResponseDto> response = teamService.createTeam(AUTH_HEADER, validCreateDto());

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody().getMessage()).isEqualTo("Token ausente ou inválido");
		}

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

			CreateTeamDto dto = validCreateDto();

			assertThatThrownBy(() -> teamService.createTeam(AUTH_HEADER, dto)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado!");
		}

		@Test
		void whenValid_savesTeamAndOwnerMember() {
			UserEntity user = userEntity();
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

			teamService.createTeam(AUTH_HEADER, validCreateDto());

			verify(teamRepository).save(any(TeamEntity.class));
			verify(teamMemberRepository).save(any(TeamMemberEntity.class));
		}

		@Test
		void whenValid_ownerMemberHasCorrectRole() {
			UserEntity user = userEntity();
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

			teamService.createTeam(AUTH_HEADER, validCreateDto());

			ArgumentCaptor<TeamMemberEntity> captor = ArgumentCaptor.forClass(TeamMemberEntity.class);
			verify(teamMemberRepository).save(captor.capture());
			assertThat(captor.getValue().getRole()).isEqualTo(TeamRole.OWNER);
			assertThat(captor.getValue().getInvitedBy()).isNull();
			assertThat(captor.getValue().getUser()).isEqualTo(user);
		}

		@Test
		void whenValid_setsAllTeamFields() {
			UserEntity user = userEntity();
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

			teamService.createTeam(AUTH_HEADER, validCreateDto());

			ArgumentCaptor<TeamEntity> captor = ArgumentCaptor.forClass(TeamEntity.class);
			verify(teamRepository).save(captor.capture());
			TeamEntity saved = captor.getValue();
			assertThat(saved.getName()).isEqualTo("Hydra FC");
			assertThat(saved.getDescription()).isEqualTo("Best team");
			assertThat(saved.getCity()).isEqualTo("São Paulo");
			assertThat(saved.getUf()).isEqualTo("SP");
			assertThat(saved.getColor()).isEqualTo("#FF0000");
			assertThat(saved.getImageUrl()).isEqualTo("https://img.url/logo.png");
			assertThat(saved.getCreatedBy()).isEqualTo(user);
		}

		@Test
		void whenValid_returnsOkWithSuccessTrueAndTeamName() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));

			ResponseEntity<ResponseDto> response = teamService.createTeam(AUTH_HEADER, validCreateDto());

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).contains("Hydra FC");
		}

	}

	@Nested
	class GetTeamUsers {

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> teamService.getTeamUsers(AUTH_HEADER, TEAM_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado!");
		}

		@Test
		void whenUserIsNotMember_returnsForbidden() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = teamService.getTeamUsers(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(response.getBody().getMessage()).contains("permissão");
		}

		@Test
		void whenUserIsMember_returnsOkWithMembers() {
			UserEntity user = userEntity();
			TeamEntity team = teamEntity();
			TeamMemberEntity member = memberEntity(user, team, TeamRole.OWNER);

			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.of(member));
			when(teamMemberRepository.findAllByTeamOrderedByRole(TEAM_ID)).thenReturn(List.of(member));

			ResponseEntity<ResponseDto> response = teamService.getTeamUsers(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat((List<?>) response.getBody().getData()).hasSize(1);
		}

		@Test
		void whenMemberHasInvitedBy_mapsInvitedByName() {
			UserEntity inviter = new UserEntity();
			inviter.setName("Coach Ana");

			UserEntity user = userEntity();
			TeamEntity team = teamEntity();
			TeamMemberEntity member = memberEntity(user, team, TeamRole.ATHLETE);
			member.setInvitedBy(inviter);

			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.of(member));
			when(teamMemberRepository.findAllByTeamOrderedByRole(TEAM_ID)).thenReturn(List.of(member));

			ResponseEntity<ResponseDto> response = teamService.getTeamUsers(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			Object data = response.getBody().getData();

			if (data instanceof List<?> list) {
				List<TeamMemberDto> members = list.stream().map(e -> (TeamMemberDto) e).toList();
				assertThat(members.getFirst().invitedByName()).isEqualTo("Coach Ana");
			}
		}

		@Test
		void whenMemberHasNoInvitedBy_invitedByNameIsNull() {
			UserEntity user = userEntity();
			TeamEntity team = teamEntity();
			TeamMemberEntity member = memberEntity(user, team, TeamRole.OWNER);
			member.setInvitedBy(null);

			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.of(member));
			when(teamMemberRepository.findAllByTeamOrderedByRole(TEAM_ID)).thenReturn(List.of(member));

			ResponseEntity<ResponseDto> response = teamService.getTeamUsers(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			Object data = response.getBody().getData();

			if (data instanceof List<?> list) {
				List<TeamMemberDto> members = list.stream().map(e -> (TeamMemberDto) e).toList();
				assertThat(members.getFirst().invitedByName()).isNull();
			}
		}

	}

	@Nested
	class GetTeamDetails {

		@Test
		void whenTeamNotFound_returnsNotFound() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = teamService.getTeamDetails(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(response.getBody().getMessage()).isEqualTo("Time não encontrado!");
		}

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> teamService.getTeamDetails(AUTH_HEADER, TEAM_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado!");
		}

		@Test
		void whenUserIsNotMember_returnsForbidden() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findFirstByUserAndTeam(any(), any())).thenReturn(null);

			ResponseEntity<ResponseDto> response = teamService.getTeamDetails(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
			assertThat(response.getBody().getMessage()).contains("não autorizado");
		}

		@Test
		void whenValid_returnsOkWithTeamDetails() {
			UserEntity user = userEntity();
			TeamEntity team = teamEntity();
			TeamMemberEntity member = memberEntity(user, team, TeamRole.OWNER);

			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(teamMemberRepository.findFirstByUserAndTeam(user, team)).thenReturn(member);

			ResponseEntity<ResponseDto> response = teamService.getTeamDetails(AUTH_HEADER, TEAM_ID);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();

			TeamDetailsDto details = (TeamDetailsDto) response.getBody().getData();
			assertThat(details.id()).isEqualTo(TEAM_ID);
			assertThat(details.name()).isEqualTo("Hydra FC");
			assertThat(details.city()).isEqualTo("São Paulo");
			assertThat(details.uf()).isEqualTo("SP");
			assertThat(details.color()).isEqualTo("#FF0000");
			assertThat(details.imageUrl()).isEqualTo("https://img.url/logo.png");
			assertThat(details.role()).isEqualTo(TeamRole.OWNER.getLabel());
		}

	}

	@Nested
	class GetMainTeamOfUser {

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> teamService.getMainTeamOfUser(AUTH_HEADER)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado!");
		}

		@Test
		void whenUserHasNoTeam_returnsNotFound() {
			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findFirstByUserOrderByJoinedAtAsc(any())).thenReturn(null);

			ResponseEntity<ResponseDto> response = teamService.getMainTeamOfUser(AUTH_HEADER);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(response.getBody().getMessage()).contains("não pertence a nenhum time");
		}

		@Test
		void whenUserHasTeam_returnsOkWithTeamDetails() {
			UserEntity user = userEntity();
			TeamEntity team = teamEntity();
			TeamMemberEntity member = memberEntity(user, team, TeamRole.ATHLETE);

			when(jwtService.extractTokenFromHeader(AUTH_HEADER)).thenReturn(TOKEN);
			when(jwtService.parseTokenToUser(TOKEN)).thenReturn(userDto());
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(teamMemberRepository.findFirstByUserOrderByJoinedAtAsc(user)).thenReturn(member);

			ResponseEntity<ResponseDto> response = teamService.getMainTeamOfUser(AUTH_HEADER);

			Assertions.assertNotNull(response.getBody());
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).contains("encontrado com sucesso");

			TeamDetailsDto details = (TeamDetailsDto) response.getBody().getData();
			assertThat(details.id()).isEqualTo(TEAM_ID);
			assertThat(details.name()).isEqualTo("Hydra FC");
			assertThat(details.role()).isEqualTo(TeamRole.ATHLETE.getLabel());
		}

	}

}