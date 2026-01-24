package com.hydra.core.controller;

import com.hydra.core.dtos.CreateTeamDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.TeamInviteRequestDto;
import com.hydra.core.service.InviteService;
import com.hydra.core.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("teams")
public class TeamController {

	private final InviteService inviteService;
	private final TeamService teamService;

	@PostMapping("create")
	public ResponseEntity<ResponseDto> createTeam(@RequestHeader("Authorization") String authorization,
			@RequestBody CreateTeamDto dto) {
		return teamService.createTeam(authorization, dto);
	}

	@GetMapping("{teamId}/users")
	public ResponseEntity<ResponseDto> getTeamUsers(@RequestHeader("Authorization") String authorization,
			@PathVariable String teamId) {
		return teamService.getTeamUsers(authorization, teamId);
	}

	@GetMapping("/{teamId}")
	public ResponseEntity<ResponseDto> getTeamDetails(@RequestHeader("Authorization") String authorization,
			@PathVariable String teamId) {
		return teamService.getTeamDetails(authorization, teamId);
	}

	@GetMapping("main")
	public ResponseEntity<ResponseDto> getMainTeamOfUser(@RequestHeader("Authorization") String authorization) {
		return teamService.getMainTeamOfUser(authorization);
	}

	@PostMapping("{teamId}/invite")
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
	public ResponseEntity<ResponseDto> sendInviteTokenByEmail(@RequestHeader("Authorization") String authorization,
			@PathVariable String inviteToken) throws IOException {
		return inviteService.sendInviteTokenByEmail(authorization, inviteToken);
	}

}