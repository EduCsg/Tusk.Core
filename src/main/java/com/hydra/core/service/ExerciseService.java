package com.hydra.core.service;

import com.hydra.core.dtos.CreateExerciseDto;
import com.hydra.core.dtos.ExerciseDto;
import com.hydra.core.entity.ExerciseEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.MuscleGroup;
import com.hydra.core.repository.ExerciseRepository;
import com.hydra.core.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

	private final ExerciseRepository exerciseRepository;
	private final UserRepository userRepository;

	// Busca exercícios (globais + customizados do usuário)
	public List<ExerciseDto> searchExercises(String query, MuscleGroup muscleGroup, String userId) {
		List<ExerciseEntity> exercises;

		if (query != null && !query.isBlank()) {
			exercises = exerciseRepository.findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById(query, userId);
		} else if (muscleGroup != null) {
			exercises = exerciseRepository.findByMuscleGroupAndIsCustomFalseOrCreatedById(muscleGroup, userId);
		} else {
			exercises = exerciseRepository.findByIsCustomFalseOrCreatedById(userId);
		}

		return exercises.stream().map(this::mapToDto).collect(Collectors.toList());
	}

	// Criar exercício customizado
	@Transactional
	public ExerciseDto createCustomExercise(CreateExerciseDto dto, String userId) {
		UserEntity user = userRepository.findById(userId)
										.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		ExerciseEntity exercise = new ExerciseEntity();
		exercise.setName(dto.name());
		exercise.setDescription(dto.description());
		exercise.setMuscleGroup(dto.muscleGroup());
		exercise.setSecondaryMuscles(dto.secondaryMuscles());
		exercise.setEquipment(dto.equipment());
		exercise.setDifficulty(dto.difficulty());
		exercise.setVideoUrl(dto.videoUrl());
		exercise.setImageUrl(dto.imageUrl());
		exercise.setInstructions(dto.instructions());
		exercise.setIsCustom(true);
		exercise.setCreatedBy(user);

		ExerciseEntity saved = exerciseRepository.save(exercise);
		return mapToDto(saved);
	}

	public ExerciseDto getExerciseById(String exerciseId) {
		ExerciseEntity exercise = exerciseRepository.findById(exerciseId).orElseThrow(
				() -> new EntityNotFoundException("Exercício não encontrado"));
		return mapToDto(exercise);
	}

	private ExerciseDto mapToDto(ExerciseEntity entity) {
		return new ExerciseDto(entity.getId(), entity.getName(), entity.getDescription(), entity.getMuscleGroup(),
				entity.getSecondaryMuscles(), entity.getEquipment(), entity.getDifficulty(), entity.getVideoUrl(),
				entity.getImageUrl(), entity.getInstructions(), entity.getIsCustom(),
				entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null, entity.getCreatedAt());
	}

}