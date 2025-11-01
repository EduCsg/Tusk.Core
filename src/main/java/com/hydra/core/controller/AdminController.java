package com.hydra.core.controller;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UpdateUserRolesDto;
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

	@PutMapping("update-user-roles")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResponseDto> updateUserRoles(@RequestHeader String Authorization,
			@RequestBody UpdateUserRolesDto dto) {
		return adminService.updateUserRoles(Authorization, dto);
	}

}