package com.hydra.core.controller;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("admin")
public class AdminController {

	private final AdminService adminService;

	@GetMapping("users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResponseDto> getAllUsers() {
		return adminService.getAllUsers();
	}

}