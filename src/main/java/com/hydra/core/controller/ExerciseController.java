package com.hydra.core.controller;

import com.hydra.core.dtos.CreateExerciseDto;
import com.hydra.core.dtos.ExerciseDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.enums.MuscleGroup;
import com.hydra.core.security.JwtService;
import com.hydra.core.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercises")
public class ExerciseController {

	private static final String EXERCISES_FOUND_MESSAGE = "Exercícios encontrados com sucesso";

	private final ExerciseService exerciseService;
	private final JwtService jwtService;

	@GetMapping
	public ResponseEntity<ResponseDto> searchExercises(@RequestParam(required = false) String query,
			@RequestParam(required = false) MuscleGroup muscleGroup,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		List<ExerciseDto> exercises = exerciseService.searchExercises(query, muscleGroup, user.id());

		ResponseDto response = new ResponseDto(EXERCISES_FOUND_MESSAGE, exercises);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<ResponseDto> createCustomExercise(@RequestBody CreateExerciseDto dto,
			@RequestHeader("Authorization") String authorization) {

		String token = jwtService.extractTokenFromHeader(authorization);
		UserDto user = jwtService.parseTokenToUser(token);

		ExerciseDto exercise = exerciseService.createCustomExercise(dto, user.id());

		ResponseDto response = new ResponseDto(EXERCISES_FOUND_MESSAGE, exercise);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{exerciseId}")
	public ResponseEntity<ResponseDto> getExercise(@PathVariable String exerciseId) {
		ExerciseDto exercise = exerciseService.getExerciseById(exerciseId);

		ResponseDto response = new ResponseDto(EXERCISES_FOUND_MESSAGE, exercise);
		return ResponseEntity.ok(response);
	}

}