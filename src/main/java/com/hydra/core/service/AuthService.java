package com.hydra.core.service;

import com.hydra.core.dtos.AuthResponseDto;
import com.hydra.core.dtos.LoginDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.BCrypt;
import com.hydra.core.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;

	ModelMapper mapper = new ModelMapper();

	{
		mapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
	}

	@Transactional
	public ResponseEntity<ResponseDto> registerUser(UserDto userDto) {
		ResponseDto responseDto = new ResponseDto();
		Optional<UserEntity> existingUser = userRepository.findByEmailOrUsernameIgnoreCase(userDto.email(),
				userDto.username());

		if (existingUser.isPresent()) {
			String message = existingUser.get().getEmail().equalsIgnoreCase(userDto.email())
					? "O email já está em uso!"
					: "O nome de usuário já está em uso!";

			responseDto.setMessage(message);
			responseDto.setSuccess(false);
			return ResponseEntity.badRequest().body(responseDto);
		}

		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setPassword(BCrypt.hashpw(userDto.password()));

		userRepository.save(userEntity);

		String jwtToken = JwtUtils.generateToken(userEntity.getId(), userEntity.getUsername(), userEntity.getEmail(),
				userEntity.getName());

		AuthResponseDto authResponseDto = new AuthResponseDto(userEntity.getId(), jwtToken);

		responseDto.setMessage("User registered successfully");
		responseDto.setData(authResponseDto);
		responseDto.setSuccess(true);

		return ResponseEntity.ok(responseDto);
	}

	@Transactional
	public ResponseEntity<ResponseDto> loginUser(LoginDto userDto) {
		ResponseDto responseDto = new ResponseDto();
		Optional<UserEntity> userOpt = userRepository.findByEmailOrUsername(userDto.login(), userDto.login());

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

		String jwtToken = JwtUtils.generateToken(userEntity.getId(), userEntity.getUsername(), userEntity.getEmail(),
				userEntity.getName());

		AuthResponseDto authResponseDto = new AuthResponseDto(userEntity.getId(), jwtToken);

		responseDto.setMessage("Login successful");
		responseDto.setData(authResponseDto);
		responseDto.setSuccess(true);

		return ResponseEntity.ok(responseDto);
	}

}