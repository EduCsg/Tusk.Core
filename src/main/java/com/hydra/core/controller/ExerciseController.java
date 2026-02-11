package com.hydra.core.controller;

import com.hydra.core.dtos.CreateExerciseDto;
import com.hydra.core.dtos.ExerciseDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.enums.MuscleGroup;
import com.hydra.core.service.ExerciseService;
import com.hydra.core.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

	@Autowired
	private ExerciseService exerciseService;

	@GetMapping
	public ResponseEntity<ResponseDto> searchExercises(@RequestParam(required = false) String query,
			@RequestParam(required = false) MuscleGroup muscleGroup,
			@RequestHeader("Authorization") String authorization) {

		String token = JwtUtils.extractTokenFromHeader(authorization);
		UserDto user = JwtUtils.parseTokenToUser(token);

		List<ExerciseDto> exercises = exerciseService.searchExercises(query, muscleGroup, user.id());

		ResponseDto response = new ResponseDto("Exercícios encontrados com sucesso", exercises);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<ResponseDto> createCustomExercise(@RequestBody CreateExerciseDto dto,
			@RequestHeader("Authorization") String authorization) {

		String token = JwtUtils.extractTokenFromHeader(authorization);
		UserDto user = JwtUtils.parseTokenToUser(token);

		ExerciseDto exercise = exerciseService.createCustomExercise(dto, user.id());

		ResponseDto response = new ResponseDto("Exercícios encontrados com sucesso", exercise);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{exerciseId}")
	public ResponseEntity<ResponseDto> getExercise(@PathVariable String exerciseId) {
		ExerciseDto exercise = exerciseService.getExerciseById(exerciseId);

		ResponseDto response = new ResponseDto("Exercícios encontrados com sucesso", exercise);
		return ResponseEntity.ok(response);
	}

}