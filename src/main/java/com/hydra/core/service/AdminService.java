package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UpdateUserRolesDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.RoleEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.RoleEnums;
import com.hydra.core.repository.RoleRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.JwtUtils;
import com.hydra.core.utils.ValidationUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public AdminService(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	@Transactional
	public ResponseEntity<ResponseDto> updateUserRoles(String headers, UpdateUserRolesDto dto) {

		List<String> roleIds = dto.roleIds();
		boolean isRolesEmpty = ValidationUtils.isEmpty(roleIds) || roleIds.stream().anyMatch(ValidationUtils::isEmpty);

		if (ValidationUtils.isEmpty(dto.userId()) || isRolesEmpty)
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

		boolean isAdmin = requestingUser.getRoles().stream().map(RoleEntity::getId)
										.anyMatch(role -> role.equals(RoleEnums.ADMIN.getId()));

		if (!isAdmin) {
			return ResponseEntity.status(403).body(new ResponseDto(
					"Você não tem permissão para atualizar as roles de outros usuários"));
		}

		UserEntity targetUser = userRepository.findById(dto.userId()).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

		boolean isSelfUpdate = targetUser.getId().equals(requestingUser.getId());
		boolean targetIsAdmin = targetUser.getRoles().stream().map(RoleEntity::getId)
										  .anyMatch(role -> role.equals(RoleEnums.ADMIN.getId()));

		if (targetIsAdmin && !isSelfUpdate) {
			return ResponseEntity.badRequest()
								 .body(new ResponseDto("Você não pode modificar as roles de outro administrador"));
		}

		List<RoleEntity> newRoles = roleRepository.findAllById(dto.roleIds());
		if (ValidationUtils.isEmpty(newRoles))
			return ResponseEntity.badRequest()
								 .body(new ResponseDto("Nenhuma role válida foi encontrada para atribuir ao usuário"));

		targetUser.getRoles().clear();
		targetUser.getRoles().addAll(newRoles);
		userRepository.save(targetUser);

		ResponseDto responseDto = new ResponseDto();
		responseDto.setSuccess(true);
		responseDto.setMessage("As roles do usuário foram atualizadas com sucesso!");
		responseDto.setData(targetUser.getRoles().stream().map(RoleEntity::getName).toList());

		return ResponseEntity.ok(responseDto);
	}

}