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

		if (ValidationUtils.isEmpty(userByToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		if (!userByToken.id().equals(request.coachId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		Optional<TeamEntity> team = teamRepository.findById(teamId);

		if (team.isEmpty()) {
			responseDto.setMessage("Time não encontrado");
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

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		InviteTokenDto inviteData = jwtService.parseInviteToken(inviteToken);

		if (ValidationUtils.isEmpty(inviteData)) {
			responseDto.setMessage("Token de convite inválido!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		// Valida que o token é para o usuário logado
		if (!userByToken.id().equals(inviteData.userId())) {
			responseDto.setMessage("Você não pode aceitar esse convite!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		// Valida se a role é válida
		TeamRole role;
		try {
			role = TeamRole.valueOf(inviteData.role());
		} catch (IllegalArgumentException e) {
			responseDto.setMessage("Papel inválido no convite!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		// Não permite convite para OWNER via token
		if (role == TeamRole.OWNER) {
			responseDto.setMessage("Não é possível aceitar convite como proprietário!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		// Verifica se já é membro do time
		Optional<TeamMemberEntity> alreadyExists = teamMemberRepository.findByTeamIdAndUserId(inviteData.teamId(),
				inviteData.userId());

		if (alreadyExists.isPresent()) {
			String teamName = alreadyExists.get().getTeam().getName();
			String currentRole = alreadyExists.get().getRole().getLabel();
			responseDto.setMessage("Você já faz parte da equipe " + teamName + " como " + currentRole + "!");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
		}

		// Busca as entidades necessárias
		TeamEntity team = teamRepository.findById(inviteData.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));

		UserEntity user = userRepository.findById(inviteData.userId())
										.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		UserEntity inviter = userRepository.findById(inviteData.invitedBy()).orElseThrow(
				() -> new EntityNotFoundException("Quem convidou não foi encontrado"));

		// Cria o novo membro
		TeamMemberEntity newMember = new TeamMemberEntity();
		newMember.setTeam(team);
		newMember.setUser(user);
		newMember.setRole(role);
		newMember.setInvitedBy(inviter);

		teamMemberRepository.save(newMember);

		responseDto.setSuccess(true);
		String roleMessage = role == TeamRole.COACH ? "treinador(a)" : "atleta";
		responseDto.setMessage(
				"Boas vindas a equipe! Agora você é um(a) " + roleMessage + " do time " + team.getName() + "!");

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
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		// Valida se a role é válida
		TeamRole role;
		try {
			role = TeamRole.valueOf(inviteData.role());
		} catch (IllegalArgumentException e) {
			responseDto.setMessage("Papel inválido no convite!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		// Não permite convite para OWNER via token
		if (role == TeamRole.OWNER) {
			responseDto.setMessage("Não é possível enviar convite como proprietário!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		// Verifica se já é membro do time
		Optional<TeamMemberEntity> alreadyExists = teamMemberRepository.findByTeamIdAndUserId(inviteData.teamId(),
				inviteData.userId());

		if (alreadyExists.isPresent()) {
			String teamName = alreadyExists.get().getTeam().getName();
			String currentRole = alreadyExists.get().getRole().getLabel();
			responseDto.setMessage("O usuário já faz parte da equipe " + teamName + " como " + currentRole + "!");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
		}

		// Busca as entidades necessárias
		UserEntity invitedUser = userRepository.findById(inviteData.userId()).orElseThrow(
				() -> new EntityNotFoundException("Usuário não encontrado"));

		if (ValidationUtils.isEmpty(invitedUser.getEmail())) {
			responseDto.setMessage("O usuário não possui um e-mail cadastrado!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		TeamEntity team = teamRepository.findById(inviteData.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));

		UserEntity inviter = userRepository.findById(inviteData.invitedBy()).orElseThrow(
				() -> new EntityNotFoundException("Quem convidou não foi encontrado"));

		// Gera URL do convite
		String inviteUrl = jwtService.generateTeamInviteUrl(inviteToken);

		// Carrega e personaliza o template
		String template = loadInviteTemplate();

		String roleText = role == TeamRole.COACH ? "treinador(a)" : "atleta";

		String html = template //
							   .replace("{{teamImageUrl}}", team.getImageUrl()) //
							   .replace("{{teamName}}", team.getName()) //
							   .replace("{{inviterName}}", inviter.getName()) //
							   .replace("{{inviterEmail}}", inviter.getEmail()) //
							   .replace("{{inviteUrl}}", inviteUrl) //
							   .replace("{{userName}}", invitedUser.getName());

		String subject = "Convite para o time " + team.getName();
		emailSender.sendHtmlMail(invitedUser.getEmail(), subject, html);

		responseDto.setSuccess(true);
		responseDto.setMessage("Convite enviado por e-mail com sucesso!");
		return ResponseEntity.ok(responseDto);
	}

	private String loadInviteTemplate() throws IOException {
		String templatePath = "/templates/email-invite.html";

		try (InputStream is = getClass().getResourceAsStream(templatePath)) {
			if (is == null) {
				throw new IOException("Template não encontrado: " + templatePath);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}