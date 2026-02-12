package com.hydra.core.controller;

import com.hydra.core.dtos.*;
import com.hydra.core.security.JwtService;
import com.hydra.core.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workouts")
public class WorkoutController {

	private final WorkoutService workoutService;
	private final JwtService jwtService;

	@PostMapping("/weightlifting")
	public ResponseEntity<ResponseDto> createWeightliftingWorkout(@RequestBody CreateWeightliftingWorkoutDto dto,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		WorkoutDto workout = workoutService.createWeightliftingWorkout(dto, user.id());

		ResponseDto response = new ResponseDto("Treino de musculação criado com sucesso!", workout);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/running")
	public ResponseEntity<ResponseDto> createRunningWorkout(@RequestBody CreateRunningWorkoutDto dto,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		WorkoutDto workout = workoutService.createRunningWorkout(dto, user.id());

		ResponseDto response = new ResponseDto("Treino de corrida criado com sucesso!", workout);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/swimming")
	public ResponseEntity<ResponseDto> createSwimmingWorkout(@RequestBody CreateSwimmingWorkoutDto dto,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		WorkoutDto workout = workoutService.createSwimmingWorkout(dto, user.id());

		ResponseDto response = new ResponseDto("Treino de natação criado com sucesso!", workout);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/team/{teamId}")
	public ResponseEntity<ResponseDto> getTeamWorkouts(@PathVariable String teamId,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		List<WorkoutDto> workouts = workoutService.getTeamWorkouts(teamId, user.id());

		ResponseDto response = new ResponseDto("Treinos do time encontrados com sucesso!", workouts);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{workoutId}")
	public ResponseEntity<ResponseDto> getWorkout(@PathVariable String workoutId,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		WorkoutDto workout = workoutService.getWorkoutById(workoutId, user.id());

		ResponseDto response = new ResponseDto("Treino encontrado com sucesso!", workout);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{workoutId}")
	public ResponseEntity<ResponseDto> deleteWorkout(@PathVariable String workoutId,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		workoutService.deleteWorkout(workoutId, user.id());

		ResponseDto response = new ResponseDto("Treino deletado com sucesso!", true);
		return ResponseEntity.ok(response);
	}

}