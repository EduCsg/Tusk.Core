package com.hydra.core.controller;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.TeamInviteRequestDto;
import com.hydra.core.service.InviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("teams")
public class TeamController {

	private final InviteService inviteService;

	public TeamController(InviteService inviteService) {
		this.inviteService = inviteService;
	}

	@PostMapping("{teamId}/invite")
	@PreAuthorize("hasRole('COACH')")
	public ResponseEntity<ResponseDto> generateInviteToken(@RequestHeader("Authorization") String authorization,
			@PathVariable String teamId, @RequestBody TeamInviteRequestDto request) {
		return inviteService.createInviteToken(authorization, teamId, request);
	}

	@PostMapping("invite/accept/{inviteToken}")
	public ResponseEntity<ResponseDto> acceptInviteToken(@RequestHeader("Authorization") String authorization,
			@PathVariable String inviteToken) {
		return inviteService.acceptInviteToken(authorization, inviteToken);
	}

}