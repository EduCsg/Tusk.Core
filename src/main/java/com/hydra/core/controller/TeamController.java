package com.hydra.core.controller;

import com.hydra.core.dtos.CreateTeamDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.TeamInviteRequestDto;
import com.hydra.core.service.InviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("teams")
public class TeamController {

	private final InviteService inviteService;

	public TeamController(InviteService inviteService) {
		this.inviteService = inviteService;
	}

	@PostMapping("create")
	@PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
	public ResponseEntity<ResponseDto> createTeam(@RequestHeader("Authorization") String authorization,
			@RequestBody CreateTeamDto dto) {
		return inviteService.createTeam(authorization, dto);
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

	@PostMapping("invite/send-email/{inviteToken}")
	@PreAuthorize("hasAnyRole('COACH')")
	public ResponseEntity<ResponseDto> sendInviteTokenByEmail(@RequestHeader("Authorization") String authorization,
			@PathVariable String inviteToken) throws IOException {
		return inviteService.sendInviteTokenByEmail(authorization, inviteToken);
	}

}