package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.utils.BCrypt;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	private static UserRepository userRepository;
	ModelMapper mapper = new ModelMapper();

	{
		mapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
	}

	public UserService(UserRepository userRepository) {
		UserService.userRepository = userRepository;
	}

	public ResponseEntity<ResponseDto> registerUser(UserDto userDto) {
		ResponseDto responseDto = new ResponseDto();
		boolean alreadyExists = userRepository.findByEmailOrUsername(userDto.email(), userDto.username()).isPresent();

		if (alreadyExists) {
			responseDto.setMessage("User with given email or username already exists");
			responseDto.setSuccess(false);
			return ResponseEntity.badRequest().body(responseDto);
		}

		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setPassword(BCrypt.hashpw(userDto.password()));
		userRepository.save(userEntity);

		responseDto.setMessage("User registered successfully");
		responseDto.setSuccess(true);
		return ResponseEntity.ok(responseDto);
	}

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

		responseDto.setMessage("Login successful");
		responseDto.setSuccess(true);
		return ResponseEntity.ok(responseDto);
	}

}