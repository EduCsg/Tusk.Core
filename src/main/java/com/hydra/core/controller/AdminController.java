package com.hydra.core.controller;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UpdateGlobalRoleDto;
import com.hydra.core.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin")
public class AdminController {

	private final AdminService adminService;

	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@PutMapping("update-global-user-role")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResponseDto> updateGlobalRole(@RequestHeader String Authorization,
			@RequestBody UpdateGlobalRoleDto dto) {
		return adminService.updateGlobalRole(Authorization, dto);
	}

	@GetMapping("users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResponseDto> getAllUsers() {
		return adminService.getAllUsers();
	}

}