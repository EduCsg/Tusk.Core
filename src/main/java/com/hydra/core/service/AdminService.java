package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UpdateGlobalRoleDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.RoleEnums;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.JwtUtils;
import com.hydra.core.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

	private final UserRepository userRepository;

	public AdminService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public ResponseEntity<ResponseDto> updateGlobalRole(String headers, UpdateGlobalRoleDto dto) {

		if (ValidationUtils.isAnyEmpty(dto.userId(), dto.role()))
			return ResponseEntity.badRequest().body(new ResponseDto(
					"É necessário informar o ID do usuário e as roles a serem atribuídas."));

		if (headers == null || !headers.startsWith("Bearer "))
			return ResponseEntity.status(401)
								 .body(new ResponseDto("Você precisa estar logado para realizar esta ação"));

		String jwtToken = headers.substring(7);

		if (!JwtUtils.validateToken(jwtToken))
			return ResponseEntity.status(403)
								 .body(new ResponseDto("Sua sessão expirou ou é inválida. Faça login novamente"));

		UserDto userRequesterFromToken = JwtUtils.parseTokenToUser(jwtToken);

		UserEntity requestingUser = userRepository.findById(userRequesterFromToken.id()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado"));

		if (!requestingUser.getRole().equals(RoleEnums.ROLE_ADMIN.name())) {
			return ResponseEntity.status(403).body(new ResponseDto(
					"Você não tem permissão para atualizar as roles de outros usuários"));
		}

		UserEntity targetUser = userRepository.findById(dto.userId()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

		boolean isSelfUpdate = targetUser.getId().equals(requestingUser.getId());
		boolean targetIsAdmin = targetUser.getRole().equals(RoleEnums.ROLE_ADMIN.name());

		if (targetIsAdmin && !isSelfUpdate) {
			return ResponseEntity.badRequest()
								 .body(new ResponseDto("Você não pode modificar as roles de outro administrador"));
		}

		try {
			RoleEnums.valueOf(dto.role());
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ResponseDto("A role informada é inválida"));
		}

		targetUser.setRole(dto.role());
		userRepository.save(targetUser);

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		responseDto.setMessage("A role global do usuário foi atualizada com sucesso!");

		return ResponseEntity.ok(responseDto);
	}

}