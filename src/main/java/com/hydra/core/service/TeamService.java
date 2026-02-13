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
import com.hydra.core.utils.ValidationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

	private static final String INVALID_TOKEN_MESSAGE = "Token inválido ou usuário não autorizado!";
	private static final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado!";
	private static final String TEAM_NOT_FOUND_MESSAGE = "Time não encontrado!";

	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final JwtService jwtService;

	@Transactional
	public ResponseEntity<ResponseDto> createTeam(String authorization, CreateTeamDto dto) {
		ResponseDto responseDto = new ResponseDto();

		if (ValidationUtils.isAnyEmpty(dto.name(), dto.city(), dto.uf(), dto.color())) {
			responseDto.setMessage("Preencha os campos obrigatórios corretamente!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		// TODO: por enquanto, aceitar só URL de imagens já hospedadas, depois implementar upload para S3
		if (ValidationUtils.notEmpty(dto.imageUrl()) && !dto.imageUrl().startsWith("http")) {
			responseDto.setMessage("A URL da imagem é inválida!");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
		}

		String token;
		try {
			token = jwtService.extractTokenFromHeader(authorization);
		} catch (UnauthorizedException ex) {
			responseDto.setMessage(ex.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		UserDto userByToken = jwtService.parseTokenToUser(token);
		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage(INVALID_TOKEN_MESSAGE);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		UserEntity creator = userRepository.findById(userByToken.id())
										   .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

		TeamEntity team = new TeamEntity();
		team.setName(dto.name());
		team.setDescription(dto.description());
		team.setCity(dto.city());
		team.setUf(dto.uf());
		team.setColor(dto.color());
		team.setImageUrl(dto.imageUrl());
		team.setCreatedBy(creator);

		TeamMemberEntity ownerMember = new TeamMemberEntity();
		ownerMember.setTeam(team);
		ownerMember.setUser(creator);
		ownerMember.setRole(TeamRole.OWNER);
		ownerMember.setInvitedBy(null);

		teamRepository.save(team);
		teamMemberRepository.save(ownerMember);

		responseDto.setSuccess(true);
		responseDto.setMessage("Time " + team.getName() + " criado com sucesso!");
		responseDto.setData(team.getId());

		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> getTeamUsers(String authorization, String teamId) {
		// Valida autenticação
		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto(INVALID_TOKEN_MESSAGE));
		}

		// Verifica se o time existe
		TeamEntity team = teamRepository.findById(teamId)
										.orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND_MESSAGE));

		// Verifica se o usuário é membro do time
		Optional<TeamMemberEntity> userMembership = teamMemberRepository.findByTeamIdAndUserId(teamId,
				userByToken.id());

		if (userMembership.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
								 .body(new ResponseDto("Você não tem permissão para ver os membros deste time!"));
		}

		// Busca todos os membros
		List<TeamMemberEntity> members = teamMemberRepository.findAllByTeamOrderedByRole(team.getId());

		List<TeamMemberDto> membersDto = members.stream().map(member -> new TeamMemberDto(member.getId(),
				member.getUser().getId(), member.getUser().getName(), member.getUser().getEmail(),
				member.getUser().getUsername(), member.getRole().getLabel(),
				member.getInvitedBy() != null ? member.getInvitedBy().getName() : null, member.getJoinedAt(),
				member.getCreatedAt())).toList();

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		responseDto.setData(membersDto);
		responseDto.setMessage("Membros do time recuperados com sucesso!");

		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> getTeamDetails(String authorization, String teamId) {
		ResponseDto responseDto = new ResponseDto();

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage(INVALID_TOKEN_MESSAGE);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		Optional<TeamEntity> teamOpt = teamRepository.findById(teamId);
		if (teamOpt.isEmpty()) {
			responseDto.setMessage(TEAM_NOT_FOUND_MESSAGE);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		UserEntity userEntity = userRepository.findById(userByToken.id())
											  .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

		TeamMemberEntity teamMember = teamMemberRepository.findFirstByUserAndTeam(userEntity, teamOpt.get());

		if (teamMember == null) {
			responseDto.setMessage("Usuário não autorizado a acessar os detalhes deste time!");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
		}

		TeamEntity team = teamOpt.get();
		TeamDetailsDto teamDetailsDto = new TeamDetailsDto(team.getId(), team.getName(), team.getDescription(),
				team.getCity(), team.getUf(), team.getColor(), teamMember.getRole().getLabel(), team.getImageUrl(),
				team.getCreatedAt());

		responseDto.setSuccess(true);
		responseDto.setData(teamDetailsDto);
		responseDto.setMessage("Detalhes do time recuperados com sucesso!");

		return ResponseEntity.ok(responseDto);
	}

	public ResponseEntity<ResponseDto> getMainTeamOfUser(String authorization) {
		ResponseDto responseDto = new ResponseDto();

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto userByToken = jwtService.parseTokenToUser(token);

		if (ValidationUtils.isEmpty(userByToken) || ValidationUtils.isEmpty(userByToken.id())) {
			responseDto.setMessage(INVALID_TOKEN_MESSAGE);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
		}

		UserEntity userEntity = userRepository.findById(userByToken.id())
											  .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

		TeamMemberEntity teamMember = teamMemberRepository.findFirstByUserOrderByJoinedAtAsc(userEntity);

		if (teamMember == null) {
			responseDto.setMessage("Usuário não pertence a nenhum time!");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
		}

		TeamEntity team = teamMember.getTeam();
		TeamDetailsDto teamDetailsDto = new TeamDetailsDto(team.getId(), team.getName(), team.getDescription(),
				team.getCity(), team.getUf(), team.getColor(), teamMember.getRole().getLabel(), team.getImageUrl(),
				team.getCreatedAt());

		responseDto.setSuccess(true);
		responseDto.setData(teamDetailsDto);
		responseDto.setMessage("Time principal do usuário encontrado com sucesso!");
		return ResponseEntity.ok(responseDto);
	}

}