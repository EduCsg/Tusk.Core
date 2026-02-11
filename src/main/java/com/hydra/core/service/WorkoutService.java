package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.*;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.enums.WorkoutModality;
import com.hydra.core.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

	private final WorkoutRepository workoutRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final TeamMemberRepository teamMemberRepository;

	private static WorkoutRunningSegmentEntity getWorkoutRunningSegmentEntity(CreateRunningWorkoutDto dto, int i,
			WorkoutEntity workout) {
		CreateRunningSegmentDto segmentDto = dto.segments().get(i);

		WorkoutRunningSegmentEntity segment = new WorkoutRunningSegmentEntity();
		segment.setWorkout(workout);
		segment.setOrderIndex(i + 1);
		segment.setSegmentType(segmentDto.segmentType());
		segment.setDistanceMeters(segmentDto.distanceMeters());
		segment.setDurationSeconds(segmentDto.durationSeconds());
		segment.setTargetPace(segmentDto.targetPace());
		segment.setTargetPaceSeconds(segmentDto.targetPaceSeconds());
		segment.setIntensity(segmentDto.intensity());
		segment.setNotes(segmentDto.notes());
		return segment;
	}

	@Transactional
	public WorkoutDto createWeightliftingWorkout(CreateWeightliftingWorkoutDto dto, String userId) {
		// Valida se o usuário é coach/owner do time
		TeamEntity team = teamRepository.findById(dto.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));

		UserEntity user = userRepository.findById(userId)
										.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(dto.teamId(), userId).orElseThrow(
				() -> new RuntimeException("Você não é membro deste time"));

		if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.COACH) {
			throw new RuntimeException("Apenas coaches e owners podem criar treinos");
		}

		// Cria o treino
		WorkoutEntity workout = new WorkoutEntity();
		workout.setTeam(team);
		workout.setCreatedBy(user);
		workout.setTitle(dto.title());
		workout.setDescription(dto.description());
		workout.setModality(WorkoutModality.WEIGHTLIFTING);
		workout.setScheduledDate(dto.scheduledDate());
		workout.setScheduledTime(dto.scheduledTime());
		workout.setDurationMinutes(dto.durationMinutes());
		workout.setIntensity(dto.intensity());
		workout.setNotes(dto.notes());

		// Adiciona os exercícios
		for (int i = 0; i < dto.exercises().size(); i++) {
			CreateWorkoutExerciseDto exerciseDto = dto.exercises().get(i);

			ExerciseEntity exercise = exerciseRepository.findById(exerciseDto.exerciseId()).orElseThrow(
					() -> new EntityNotFoundException("Exercício não encontrado: " + exerciseDto.exerciseId()));

			WorkoutExerciseEntity workoutExercise = new WorkoutExerciseEntity();
			workoutExercise.setWorkout(workout);
			workoutExercise.setExercise(exercise);
			workoutExercise.setOrderIndex(i + 1);
			workoutExercise.setTechnique(exerciseDto.technique());
			workoutExercise.setRestBetweenSetsSeconds(exerciseDto.restBetweenSetsSeconds());
			workoutExercise.setNotes(exerciseDto.notes());

			// Adiciona as séries
			for (CreateWorkoutSetDto setDto : exerciseDto.sets()) {
				WorkoutExerciseSetEntity set = new WorkoutExerciseSetEntity();
				set.setWorkoutExercise(workoutExercise);
				set.setSetNumber(setDto.setNumber());
				set.setReps(setDto.reps());
				set.setWeight(setDto.weight());
				set.setRpe(setDto.rpe());
				set.setRestSeconds(setDto.restSeconds());
				set.setNotes(setDto.notes());

				workoutExercise.getSets().add(set);
			}

			workout.getExercises().add(workoutExercise);
		}

		WorkoutEntity saved = workoutRepository.save(workout);
		return mapToDto(saved);
	}

	@Transactional
	public WorkoutDto createRunningWorkout(CreateRunningWorkoutDto dto, String userId) {
		TeamEntity team = teamRepository.findById(dto.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));

		UserEntity user = userRepository.findById(userId)
										.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(dto.teamId(), userId).orElseThrow(
				() -> new RuntimeException("Você não é membro deste time"));

		if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.COACH) {
			throw new RuntimeException("Apenas coaches e owners podem criar treinos");
		}

		WorkoutEntity workout = new WorkoutEntity();
		workout.setTeam(team);
		workout.setCreatedBy(user);
		workout.setTitle(dto.title());
		workout.setDescription(dto.description());
		workout.setModality(WorkoutModality.RUNNING);
		workout.setScheduledDate(dto.scheduledDate());
		workout.setScheduledTime(dto.scheduledTime());
		workout.setDurationMinutes(dto.durationMinutes());
		workout.setIntensity(dto.intensity());
		workout.setNotes(dto.notes());

		for (int i = 0; i < dto.segments().size(); i++) {
			WorkoutRunningSegmentEntity segment = getWorkoutRunningSegmentEntity(dto, i, workout);

			workout.getRunningSegments().add(segment);
		}

		WorkoutEntity saved = workoutRepository.save(workout);
		return mapToDto(saved);
	}

	@Transactional
	public WorkoutDto createSwimmingWorkout(CreateSwimmingWorkoutDto dto, String userId) {
		TeamEntity team = teamRepository.findById(dto.teamId())
										.orElseThrow(() -> new EntityNotFoundException("Time não encontrado"));

		UserEntity user = userRepository.findById(userId)
										.orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(dto.teamId(), userId).orElseThrow(
				() -> new RuntimeException("Você não é membro deste time"));

		if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.COACH) {
			throw new RuntimeException("Apenas coaches e owners podem criar treinos");
		}

		WorkoutEntity workout = new WorkoutEntity();
		workout.setTeam(team);
		workout.setCreatedBy(user);
		workout.setTitle(dto.title());
		workout.setDescription(dto.description());
		workout.setModality(WorkoutModality.SWIMMING);
		workout.setScheduledDate(dto.scheduledDate());
		workout.setScheduledTime(dto.scheduledTime());
		workout.setDurationMinutes(dto.durationMinutes());
		workout.setIntensity(dto.intensity());
		workout.setNotes(dto.notes());

		for (int i = 0; i < dto.sets().size(); i++) {
			CreateSwimmingSetDto setDto = dto.sets().get(i);

			WorkoutSwimmingSetEntity swimmingSet = new WorkoutSwimmingSetEntity();
			swimmingSet.setWorkout(workout);
			swimmingSet.setOrderIndex(i + 1);
			swimmingSet.setStroke(setDto.stroke());
			swimmingSet.setDistanceMeters(setDto.distanceMeters());
			swimmingSet.setRepetitions(setDto.repetitions());
			swimmingSet.setTargetTime(setDto.targetTime());
			swimmingSet.setTargetPaceSeconds(setDto.targetPaceSeconds());
			swimmingSet.setRestSeconds(setDto.restSeconds());
			swimmingSet.setEquipment(setDto.equipment());
			swimmingSet.setNotes(setDto.notes());

			workout.getSwimmingSets().add(swimmingSet);
		}

		WorkoutEntity saved = workoutRepository.save(workout);
		return mapToDto(saved);
	}

	public List<WorkoutDto> getTeamWorkouts(String teamId, String userId) {
		// Valida se o usuário é membro do time
		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).orElseThrow(
				() -> new RuntimeException("Você não é membro deste time"));

		List<WorkoutEntity> workouts = workoutRepository.findByTeamIdOrderByScheduledDateDesc(teamId);
		return workouts.stream().map(this::mapToDto).collect(java.util.stream.Collectors.toList());
	}

	public WorkoutDto getWorkoutById(String workoutId, String userId) {
		WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow(
				() -> new EntityNotFoundException("Treino não encontrado"));

		// Valida se o usuário é membro do time
		teamMemberRepository.findByTeamIdAndUserId(workout.getTeam().getId(), userId)
							.orElseThrow(() -> new RuntimeException("Você não tem permissão para ver este treino"));

		return mapToDto(workout);
	}

	@Transactional
	public void deleteWorkout(String workoutId, String userId) {
		WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow(
				() -> new EntityNotFoundException("Treino não encontrado"));

		// Valida se o usuário é coach/owner do time
		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(workout.getTeam().getId(), userId)
														  .orElseThrow(() -> new RuntimeException(
																  "Você não é membro deste time"));

		if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.COACH) {
			throw new RuntimeException("Apenas coaches e owners podem deletar treinos");
		}

		workoutRepository.delete(workout);
	}

	private WorkoutDto mapToDto(WorkoutEntity entity) {
		return new WorkoutDto(entity.getId(), entity.getTeam() != null ? entity.getTeam().getId() : null,
				entity.getTeam() != null ? entity.getTeam().getName() : null,
				entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
				entity.getCreatedBy() != null ? entity.getCreatedBy().getName() : null, entity.getTitle(),
				entity.getDescription(), entity.getModality(), entity.getScheduledDate(), entity.getScheduledTime(),
				entity.getDurationMinutes(), entity.getIntensity(), entity.getNotes(),
				entity.getExercises().stream().map(this::mapWorkoutExerciseToDto)
					  .collect(java.util.stream.Collectors.toList()),
				entity.getRunningSegments().stream().map(this::mapRunningSegmentToDto)
					  .collect(java.util.stream.Collectors.toList()),
				entity.getSwimmingSets().stream().map(this::mapSwimmingSetToDto)
					  .collect(java.util.stream.Collectors.toList()), entity.getCreatedAt(), entity.getUpdatedAt());
	}

	private WorkoutExerciseDto mapWorkoutExerciseToDto(WorkoutExerciseEntity entity) {
		return new WorkoutExerciseDto(entity.getId(),
				entity.getExercise() != null ? entity.getExercise().getId() : null,
				entity.getExercise() != null ? entity.getExercise().getName() : null, entity.getOrderIndex(),
				entity.getTechnique(), entity.getRestBetweenSetsSeconds(), entity.getNotes(),
				entity.getSets().stream().map(this::mapExerciseSetToDto).collect(java.util.stream.Collectors.toList()));
	}

	private WorkoutExerciseSetDto mapExerciseSetToDto(WorkoutExerciseSetEntity entity) {
		return new WorkoutExerciseSetDto(entity.getId(), entity.getSetNumber(), entity.getReps(), entity.getWeight(),
				entity.getRpe(), entity.getRestSeconds(), entity.getNotes());
	}

	private WorkoutRunningSegmentDto mapRunningSegmentToDto(WorkoutRunningSegmentEntity entity) {
		return new WorkoutRunningSegmentDto(entity.getId(), entity.getOrderIndex(), entity.getSegmentType(),
				entity.getDistanceMeters(), entity.getDurationSeconds(), entity.getTargetPace(),
				entity.getTargetPaceSeconds(), entity.getIntensity(), entity.getNotes());
	}

	private WorkoutSwimmingSetDto mapSwimmingSetToDto(WorkoutSwimmingSetEntity entity) {
		return new WorkoutSwimmingSetDto(entity.getId(), entity.getOrderIndex(), entity.getStroke(),
				entity.getDistanceMeters(), entity.getRepetitions(), entity.getTargetTime(),
				entity.getTargetPaceSeconds(), entity.getRestSeconds(), entity.getEquipment(), entity.getNotes());
	}

}