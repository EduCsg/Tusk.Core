package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.mappers.UserMapper;
import com.hydra.core.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

	private final UserRepository userRepository;

	public AdminService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public ResponseEntity<ResponseDto> getAllUsers() {
		ResponseDto responseDto = new ResponseDto();

		List<UserEntity> users = userRepository.findAll().stream().toList();

		responseDto.setSuccess(true);
		responseDto.setData(UserMapper.entitiesToDtos(users));
		return ResponseEntity.ok(responseDto);
	}

}