package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.TeamAthleteEntity;
import com.hydra.core.entity.TeamCoachEntity;
import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.entity.pk.TeamAthleteId;
import com.hydra.core.entity.pk.TeamCoachId;
import com.hydra.core.exceptions.UnauthorizedException;
import com.hydra.core.repository.TeamAthleteRepository;
import com.hydra.core.repository.TeamCoachRepository;
import com.hydra.core.repository.TeamRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.JwtUtils;
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
import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InviteService {

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final TeamAthleteRepository teamAthleteRepository;
	private final TeamCoachRepository teamCoachRepository;
	private final EmailSender emailSender;

	@Transactional
	public ResponseEntity<ResponseDto> createTeam(String authorization, CreateTeamDto dto) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isAnyEmpty(dto.name(), dto.city(), dto.uf(), dto.color())) {
			responseDto.setMessage("Preencha os campos obrigatórios corretamente!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (ValidationUtils.notEmpty(dto.imageUrl()) && !dto.imageUrl().startsWith("http")) {
			responseDto.setMessage("A URL da imagem é inválida!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token;
		try {
			token = JwtUtils.extractTokenFromHeader(authorization);
		} catch (UnauthorizedException ex) {
			responseDto.setMessage(ex.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		UserDto userByToken = JwtUtils.parseTokenToUser(token);
		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		UserEntity creator = userRepository.findById(userByToken.id())
										   .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		TeamEntity team = new TeamEntity();
		team.setName(dto.name());
		team.setDescription(dto.description());
		team.setCity(dto.city());
		team.setUf(dto.uf());
		team.setColor(dto.color());
		team.setImageUrl(dto.imageUrl());
		team.setCreatedBy(creator);
		team.setCoaches(new HashSet<>());
		team.setAthletes(new HashSet<>());
		teamRepository.save(team);

		TeamCoachId teamCoachId = new TeamCoachId(team.getTeamId(), creator.getId());
		TeamCoachEntity teamCoach = new TeamCoachEntity();
		teamCoach.setId(teamCoachId);
		teamCoach.setTeam(team);
		teamCoach.setCoach(creator);

		teamCoachRepository.save(teamCoach);

		responseDto.setSuccess(true);
		responseDto.setMessage("Time " + team.getName() + " criado com sucesso!");
		responseDto.setData(team.getTeamId());

		return ResponseEntity.ok(responseDto);
	}

	@Transactional
	public ResponseEntity<ResponseDto> createInviteToken(String authorization, String teamId,
			TeamInviteRequestDto request) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isEmpty(request.athleteIdentifier()) || ValidationUtils.isEmpty(request.coachId())) {
			responseDto.setMessage("Preencha os campos corretamente!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = JwtUtils.extractTokenFromHeader(authorization);
		UserDto userByToken = JwtUtils.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || !userByToken.id().equals(request.coachId())) {
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
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
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		Optional<UserEntity> athlete = userRepository.findByEmailOrUsername(request.athleteIdentifier(),
				request.athleteIdentifier());

		if (athlete.isEmpty()) {
			responseDto.setMessage("Nenhum usuário foi encontrado!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		String inviteUrl = JwtUtils.generateTeamInviteUrl(teamId, athlete.get().getId(), coach.get().getId());

		responseDto.setSuccess(true);
		responseDto.setData(inviteUrl);
		responseDto.setMessage("Link de convite gerado com sucesso");

		TeamAthleteEntity teamAthlete = new TeamAthleteEntity();

		teamAthlete.setTeam(team.get());
		teamAthlete.setAthlete(athlete.get());
		teamAthlete.setInvitedBy(coach.get());

		TeamAthleteId id = new TeamAthleteId(team.get().getTeamId(), athlete.get().getId());
		teamAthlete.setId(id);

		teamAthleteRepository.save(teamAthlete);

		return ResponseEntity.ok(responseDto);
	}

	@Transactional
	public ResponseEntity<ResponseDto> acceptInviteToken(String authorization, String inviteToken) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isEmpty(inviteToken)) {
			responseDto.setMessage("Token de convite é obrigatório!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = JwtUtils.extractTokenFromHeader(authorization);
		UserDto userByToken = JwtUtils.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		InviteTokenDto inviteData = JwtUtils.parseInviteToken(inviteToken);

		if (ValidationUtils.isEmpty(inviteData)) {
			responseDto.setMessage("Token de convite inválido!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		if (!userByToken.id().equals(inviteData.athleteId())) {
			responseDto.setMessage("Você não pode aceitar esse convite!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		Optional<TeamAthleteEntity> alreadyExists = teamAthleteRepository.findById(
				new TeamAthleteId(inviteData.teamId(), inviteData.athleteId()));

		if (alreadyExists.isPresent()) {
			String teamName = alreadyExists.get().getTeam().getName();
			responseDto.setMessage("Você já faz parte da equipe " + teamName + "!");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
		}

		TeamAthleteEntity newTeamAthlete = new TeamAthleteEntity();

		TeamEntity team = teamRepository.findById(inviteData.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));
		UserEntity athlete = userRepository.findById(inviteData.athleteId())
										   .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));
		UserEntity coach = userRepository.findById(inviteData.coachId())
										 .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado"));

		TeamAthleteId newTeamAthleteId = new TeamAthleteId(team.getTeamId(), athlete.getId());
		newTeamAthlete.setId(newTeamAthleteId);

		newTeamAthlete.setTeam(team);
		newTeamAthlete.setAthlete(athlete);
		newTeamAthlete.setInvitedBy(coach);

		teamAthleteRepository.save(newTeamAthlete);

		responseDto.setSuccess(true);
		responseDto.setMessage("Bem vindo(a) ao time " + team.getName() + "!");

		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> sendInviteTokenByEmail(String authorization, String inviteToken)
			throws IOException {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isEmpty(inviteToken)) {
			responseDto.setMessage("O token é obrigatório!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token = JwtUtils.extractTokenFromHeader(authorization);

		UserDto userByToken = JwtUtils.parseTokenToUser(token);
		InviteTokenDto inviteData = JwtUtils.parseInviteToken(inviteToken);

		if (ValidationUtils.isEmpty(userByToken.id()) || ValidationUtils.isEmpty(
				inviteData.coachId()) || !userByToken.id().equals(inviteData.coachId())) {
			responseDto.setMessage("Token inválido ou usuário não autorizado!");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		Optional<TeamAthleteEntity> alreadyExists = teamAthleteRepository.findById(
				new TeamAthleteId(inviteData.teamId(), inviteData.athleteId()));

		if (alreadyExists.isPresent()) {
			String teamName = alreadyExists.get().getTeam().getName();
			responseDto.setMessage("O atleta já faz parte da equipe " + teamName + "!");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
		}

		UserEntity athlete = userRepository.findById(inviteData.athleteId())
										   .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

		if (ValidationUtils.isEmpty(athlete.getEmail())) {
			responseDto.setMessage("O atleta não possui um e-mail cadastrado!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		TeamEntity team = teamRepository.findById(inviteData.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));
		UserEntity coach = userRepository.findById(inviteData.coachId())
										 .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado"));

		String inviteUrl = JwtUtils.generateTeamInviteUrl(inviteToken);

		String template = loadInviteTemplate();

		String html = template.replace("{{teamImageUrl}}", team.getImageUrl()) //
							  .replace("{{teamName}}", team.getName()) //
							  .replace("{{coachName}}", coach.getName()) //
							  .replace("{{coachEmail}}", coach.getEmail()) //
							  .replace("{{inviteUrl}}", inviteUrl) //
							  .replace("{{userName}}", athlete.getName());

		String subject = "Convite para o time " + team.getName();
		emailSender.sendHtmlMail(athlete.getEmail(), subject, html);

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