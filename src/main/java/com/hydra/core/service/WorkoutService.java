package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.*;
import com.hydra.core.enums.TeamRole;
import com.hydra.core.enums.WorkoutModality;
import com.hydra.core.exceptions.UnauthorizedException;
import com.hydra.core.mappers.WorkoutMapper;
import com.hydra.core.models.CreateWorkoutRequest;
import com.hydra.core.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

	private static final String TEAM_NOT_FOUND_MESSAGE = "Time não encontrado";
	private static final String USER_NOT_FOUND_MESSAGE = "Usuário não encontrado";
	private static final String NOT_TEAM_MEMBER_MESSAGE = "Você não é membro deste time";
	private static final String WORKOUT_NOT_FOUND_MESSAGE = "Treino não encontrado";

	private final WorkoutRepository workoutRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private final ExerciseRepository exerciseRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final WorkoutMapper workoutMapper;

	private WorkoutRunningSegmentEntity getWorkoutRunningSegmentEntity(CreateRunningWorkoutDto dto, int i,
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

		CreateWorkoutRequest request = new CreateWorkoutRequest(dto.teamId(), userId, dto.title(), dto.description(),
				WorkoutModality.WEIGHTLIFTING, dto.scheduledDate(), dto.scheduledTime(), dto.durationMinutes(),
				dto.intensity(), dto.notes());

		WorkoutEntity workout = createBaseWorkout(request);

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

		return saveAndMap(workout);
	}

	@Transactional
	public WorkoutDto createRunningWorkout(CreateRunningWorkoutDto dto, String userId) {

		CreateWorkoutRequest request = new CreateWorkoutRequest(dto.teamId(), userId, dto.title(), dto.description(),
				WorkoutModality.RUNNING, dto.scheduledDate(), dto.scheduledTime(), dto.durationMinutes(),
				dto.intensity(), dto.notes());

		WorkoutEntity workout = createBaseWorkout(request);

		for (int i = 0; i < dto.segments().size(); i++) {
			WorkoutRunningSegmentEntity segment = getWorkoutRunningSegmentEntity(dto, i, workout);

			workout.getRunningSegments().add(segment);
		}

		return saveAndMap(workout);
	}

	@Transactional
	public WorkoutDto createSwimmingWorkout(CreateSwimmingWorkoutDto dto, String userId) {

		CreateWorkoutRequest request = new CreateWorkoutRequest(dto.teamId(), userId, dto.title(), dto.description(),
				WorkoutModality.SWIMMING, dto.scheduledDate(), dto.scheduledTime(), dto.durationMinutes(),
				dto.intensity(), dto.notes());

		WorkoutEntity workout = createBaseWorkout(request);

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

		return saveAndMap(workout);
	}

	public List<WorkoutDto> getTeamWorkouts(String teamId) {
		List<WorkoutEntity> workouts = workoutRepository.findByTeamIdOrderByScheduledDateDesc(teamId);
		return workouts.stream().map(workoutMapper::toDto).toList();
	}

	public WorkoutDto getWorkoutById(String workoutId, String userId) {
		WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow(
				() -> new EntityNotFoundException(WORKOUT_NOT_FOUND_MESSAGE));

		// Valida se o usuário é membro do time
		teamMemberRepository.findByTeamIdAndUserId(workout.getTeam().getId(), userId).orElseThrow(
				() -> new UnauthorizedException("Você não tem permissão para ver este treino"));

		return workoutMapper.toDto(workout);
	}

	@Transactional
	public void deleteWorkout(String workoutId, String userId) {
		WorkoutEntity workout = workoutRepository.findById(workoutId).orElseThrow(
				() -> new EntityNotFoundException(WORKOUT_NOT_FOUND_MESSAGE));

		// Valida se o usuário é coach/owner do time
		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(workout.getTeam().getId(), userId)
														  .orElseThrow(() -> new UnauthorizedException(
																  NOT_TEAM_MEMBER_MESSAGE));

		validateCoachOrOwner(membership);

		workoutRepository.delete(workout);
	}

	private void validateCoachOrOwner(TeamMemberEntity membership) {
		if (membership.getRole() != TeamRole.OWNER && membership.getRole() != TeamRole.COACH)
			throw new UnauthorizedException("Apenas coaches e donos são autorizados a fazer esta ação");
	}

	private WorkoutDto saveAndMap(WorkoutEntity workout) {
		return workoutMapper.toDto(workoutRepository.save(workout));
	}

	private WorkoutEntity createBaseWorkout(CreateWorkoutRequest request) {
		TeamEntity team = teamRepository.findById(request.teamId())
										.orElseThrow(() -> new EntityNotFoundException(TEAM_NOT_FOUND_MESSAGE));

		UserEntity user = userRepository.findById(request.userId())
										.orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

		TeamMemberEntity membership = teamMemberRepository.findByTeamIdAndUserId(request.teamId(), request.userId())
														  .orElseThrow(() -> new UnauthorizedException(
																  NOT_TEAM_MEMBER_MESSAGE));

		validateCoachOrOwner(membership);

		return WorkoutEntity.builder().team(team).createdBy(user).title(request.title())
							.description(request.description()).modality(request.modality())
							.scheduledDate(request.scheduledDate()).scheduledTime(request.scheduledTime())
							.durationMinutes(request.duration()).intensity(request.intensity()).notes(request.notes())
							.build();
	}

}