package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.TeamMemberEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.exceptions.InvalidRoleException;
import com.hydra.core.exceptions.OwnerInviteNotAllowedException;
import com.hydra.core.exceptions.UserAlreadyInTeamException;
import com.hydra.core.repository.TeamMemberRepository;
import com.hydra.core.repository.TeamRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.security.JwtService;
import com.hydra.core.utils.ValidationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteService {

	static final String INVALID_TOKEN_MESSAGE = "Token inválido ou usuário não autorizado!";
	static final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado!";
	static final String TEAM_NOT_FOUND_MESSAGE = "Time não encontrado!";

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final EmailSender emailSender;
	private final JwtService jwtService;

	@Transactional
	public ResponseEntity<ResponseDto> createInviteToken(String authorization, String teamId,
			TeamInviteRequestDto request) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isAnyEmpty(request.athleteIdentifier(), request.coachId())) {
			responseDto.setMessage("Preencha os campos corretamente!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		TeamRole teamRole = TeamRole.fromString(request.role());
		if (teamRole != TeamRole.ATHLETE && teamRole != TeamRole.COACH) {
			responseDto.setMessage("Tipo de usuário inválido!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		if (!userByToken.id().equals(request.coachId())) {
			responseDto.setMessage("Você não pode aceitar o próprio convite!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		Optional<TeamEntity> team = teamRepository.findById(teamId);

		if (team.isEmpty()) {
			responseDto.setMessage(TEAM_NOT_FOUND_MESSAGE);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		Optional<UserEntity> coach = userRepository.findById(request.coachId());

		if (coach.isEmpty()) {
			responseDto.setMessage("Professor não encontrado");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		boolean coachInTeam = team.get().getCoaches().stream().anyMatch(c -> c.getId().equals(coach.get().getId()));

		if (!coachInTeam) {
			responseDto.setMessage("Professor não autorizado a convidar para essa equipe!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		Optional<UserEntity> athlete = userRepository.findByEmailOrUsername(request.athleteIdentifier(),
				request.athleteIdentifier());

		if (athlete.isEmpty()) {
			responseDto.setMessage("Nenhum usuário foi encontrado!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		String athleteId = athlete.get().getId();
		if (team.get().getAthletes().stream().anyMatch(a -> a.getId().equals(athleteId))) {
			responseDto.setMessage("O usuário já é membro dessa equipe!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		String inviteUrl = jwtService.generateTeamInviteUrl(teamId, athlete.get().getId(), coach.get().getId(),
				teamRole);

		responseDto.setSuccess(true);
		responseDto.setData(inviteUrl);
		responseDto.setMessage("Link de convite gerado com sucesso");

		return ResponseEntity.ok(responseDto);
	}

	@Transactional
	public ResponseEntity<ResponseDto> acceptInviteToken(String authorization, String inviteToken) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isEmpty(inviteToken)) {
			responseDto.setMessage("Token de convite é obrigatório!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		InviteTokenDto inviteData = jwtService.parseInviteToken(inviteToken);

		if (inviteData == null || ValidationUtils.isAnyEmpty(inviteData.teamId(), inviteData.userId(),
				inviteData.role(), inviteData.invitedBy())) {
			responseDto.setMessage("Token de convite inválido!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		// Valida que o token é para o usuário logado
		if (!userByToken.id().equals(inviteData.userId())) {
			responseDto.setMessage("Você não pode aceitar esse convite!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		InviteValidationContext context = validateInvite(inviteData);

		// Cria o novo membro
		TeamMemberEntity newMember = new TeamMemberEntity();
		newMember.setTeam(context.team());
		newMember.setUser(context.invitedUser());
		newMember.setRole(context.role());
		newMember.setInvitedBy(context.inviter());

		teamMemberRepository.save(newMember);

		responseDto.setSuccess(true);
		String roleMessage = context.role() == TeamRole.COACH ? "treinador(a)" : "atleta";
		responseDto.setMessage("Boas vindas a equipe! Agora você é um(a) " + roleMessage + " do time " + context.team()
																												.getName() + "!");

		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> sendInviteTokenByEmail(String authorization, String inviteToken)
			throws IOException {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isEmpty(inviteToken)) {
			responseDto.setMessage("O token é obrigatório!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = jwtService.extractTokenFromHeader(authorization);

		UserDto userByToken = jwtService.parseTokenToUser(token);
		InviteTokenDto inviteData = jwtService.parseInviteToken(inviteToken);

		// Valida que quem está enviando é quem criou o convite
		if (ValidationUtils.isAnyEmpty(userByToken.id(), inviteData.invitedBy()) || !userByToken.id()
																								.equals(inviteData.invitedBy())) {
			responseDto.setMessage(INVALID_TOKEN_MESSAGE);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		InviteValidationContext context = validateInvite(inviteData);

		if (ValidationUtils.isEmpty(context.invitedUser().getEmail())) {
			responseDto.setMessage("O usuário não possui um e-mail cadastrado!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		UserEntity inviter = userRepository.findById(inviteData.invitedBy()).orElseThrow(
				() -> new EntityNotFoundException("Quem convidou não foi encontrado"));

		// Gera URL do convite
		String inviteUrl = jwtService.generateTeamInviteUrl(inviteToken);

		// Carrega e personaliza o template
		String template = loadInviteTemplate("/templates/email-invite.html");

		String html = template //
							   .replace("{{teamImageUrl}}", context.team().getImageUrl()) //
							   .replace("{{teamName}}", context.team().getName()) //
							   .replace("{{inviterName}}", inviter.getName()) //
							   .replace("{{inviterEmail}}", inviter.getEmail()) //
							   .replace("{{inviteUrl}}", inviteUrl) //
							   .replace("{{userName}}", context.invitedUser().getName());

		String subject = "Convite para o time " + context.team().getName();
		emailSender.sendHtmlMail(context.invitedUser().getEmail(), subject, html);

		responseDto.setSuccess(true);
		responseDto.setMessage("Convite enviado por e-mail com sucesso!");
		return ResponseEntity.ok(responseDto);
	}

	String loadInviteTemplate(String templatePath) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(templatePath)) {
			if (is == null) {
				throw new IOException("Template não encontrado: " + templatePath);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private InviteValidationContext validateInvite(InviteTokenDto inviteData) {

		TeamRole role;
		try {
			role = TeamRole.valueOf(inviteData.role());
		} catch (IllegalArgumentException _) {
			throw new InvalidRoleException();
		}

		if (role == TeamRole.OWNER) {
			throw new OwnerInviteNotAllowedException();
		}

		Optional<TeamMemberEntity> alreadyExists = teamMemberRepository.findByTeamIdAndUserId(inviteData.teamId(),
				inviteData.userId());

		if (alreadyExists.isPresent()) {
			String teamName = alreadyExists.get().getTeam().getName();
			String currentRole = alreadyExists.get().getRole().getLabel();
			throw new UserAlreadyInTeamException(teamName, currentRole);
		}

		TeamEntity team = teamRepository.findById(inviteData.teamId())
										.orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND_MESSAGE));

		UserEntity invitedUser = userRepository.findById(inviteData.userId())
											   .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

		UserEntity inviter = userRepository.findById(inviteData.invitedBy()).orElseThrow(
				() -> new EntityNotFoundException("Quem convidou não foi encontrado"));

		return new InviteValidationContext(role, team, invitedUser, inviter);
	}

}