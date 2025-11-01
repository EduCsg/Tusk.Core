package com.hydra.core.controller;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {

	private final AuthService userService;

	public AuthController(AuthService userService) {
		this.userService = userService;
	}

	@PostMapping("register")
	public ResponseEntity<ResponseDto> registerUser(@Valid @RequestBody UserDto userDto) {
		return userService.registerUser(userDto);
	}

	@PostMapping("login")
	public ResponseEntity<ResponseDto> loginUser(@RequestBody UserDto userDto) {
		return userService.loginUser(userDto);
	}

}