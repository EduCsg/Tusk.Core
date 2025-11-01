package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.RoleEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.RoleEnums;
import com.hydra.core.repository.RoleRepository;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.BCrypt;
import com.hydra.core.utils.JwtUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	ModelMapper mapper = new ModelMapper();

	{
		mapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
	}

	public AuthService(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	@Transactional
	public ResponseEntity<ResponseDto> registerUser(UserDto userDto) {
		ResponseDto responseDto = new ResponseDto();
		Optional<UserEntity> existingUser = userRepository.findByEmailOrUsername(userDto.email(), userDto.username());

		if (existingUser.isPresent()) {
			String message = existingUser.get().getEmail().equals(userDto.email())
					? "O email já está em uso!"
					: "O nome de usuário já está em uso!";

			responseDto.setMessage(message);
			responseDto.setSuccess(false);
			return ResponseEntity.badRequest().body(responseDto);
		}

		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setPassword(BCrypt.hashpw(userDto.password()));

		RoleEntity defaultRoleEntity = roleRepository.findById(RoleEnums.ATHLETE.getId())
													 .orElseThrow(() -> new RuntimeException("Default role not found"));
		userEntity.setRoles(Set.of(defaultRoleEntity));

		userRepository.save(userEntity);

		responseDto.setMessage("User registered successfully");
		responseDto.setSuccess(true);
		return ResponseEntity.ok(responseDto);
	}

	@Transactional
	public ResponseEntity<ResponseDto> loginUser(UserDto userDto) {
		ResponseDto responseDto = new ResponseDto();
		var userOpt = userRepository.findByEmailOrUsername(userDto.email(), userDto.username());

		if (userOpt.isEmpty()) {
			responseDto.setMessage("Usuário ou senha inválidos");
			responseDto.setSuccess(false);
			return ResponseEntity.status(401).body(responseDto);
		}

		UserEntity userEntity = userOpt.get();
		if (!BCrypt.checkpw(userDto.password(), userEntity.getPassword())) {
			responseDto.setMessage("Usuário ou senha inválidos");
			responseDto.setSuccess(false);
			return ResponseEntity.status(401).body(responseDto);
		}

		List<String> roles = userEntity.getRoles().stream().map(RoleEntity::getName).toList();

		String jwtToken = JwtUtils.generateToken(userEntity.getId(), userEntity.getUsername(), userEntity.getEmail(),
				userEntity.getName(), roles);

		responseDto.setMessage("Login successful");
		responseDto.setData(jwtToken);
		responseDto.setSuccess(true);

		return ResponseEntity.ok(responseDto);
	}

}