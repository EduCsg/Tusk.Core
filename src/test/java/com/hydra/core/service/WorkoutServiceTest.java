package com.hydra.core.service;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.*;
import com.hydra.core.enums.*;
import com.hydra.core.exceptions.UnauthorizedException;
import com.hydra.core.mappers.WorkoutMapper;
import com.hydra.core.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

	private static final String TEAM_ID = "team-1";
	private static final String USER_ID = "user-1";
	private static final String WORKOUT_ID = "workout-1";
	private static final String EXERCISE_ID = "exercise-1";

	@Mock
	private WorkoutRepository workoutRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ExerciseRepository exerciseRepository;

	@Mock
	private TeamMemberRepository teamMemberRepository;

	@Mock
	private WorkoutMapper workoutMapper;

	@InjectMocks
	private WorkoutService workoutService;

	private UserEntity userEntity() {
		UserEntity u = new UserEntity();
		u.setId(USER_ID);
		u.setName("Coach Ana");
		return u;
	}

	private TeamEntity teamEntity() {
		TeamEntity t = new TeamEntity();
		t.setId(TEAM_ID);
		t.setName("Hydra FC");
		return t;
	}

	private TeamMemberEntity memberOf(TeamRole role) {
		TeamMemberEntity m = new TeamMemberEntity();
		m.setUser(userEntity());
		m.setTeam(teamEntity());
		m.setRole(role);
		return m;
	}

	private ExerciseEntity exerciseEntity() {
		ExerciseEntity e = new ExerciseEntity();
		e.setId(EXERCISE_ID);
		e.setName("Squat");
		return e;
	}

	private WorkoutEntity workoutEntity() {
		return WorkoutEntity.builder().id(WORKOUT_ID).team(teamEntity()).createdBy(userEntity()).title("Treino A")
							.modality(WorkoutModality.WEIGHTLIFTING).createdAt(LocalDateTime.now()).build();
	}

	private WorkoutDto workoutDto() {
		return new WorkoutDto(WORKOUT_ID, TEAM_ID, "Hydra FC", USER_ID, "Coach Ana", "Treino A", null,
				WorkoutModality.WEIGHTLIFTING, null, null, null, null, null, List.of(), List.of(), List.of(),
				LocalDateTime.now(), LocalDateTime.now());
	}

	/** Mocks comuns para o caminho feliz de criação de qualquer tipo de treino */
	private void mockHappyPathBase() {
		when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
		when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
				Optional.of(memberOf(TeamRole.COACH)));
		when(workoutRepository.save(any(WorkoutEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(workoutMapper.toDto(any(WorkoutEntity.class))).thenReturn(workoutDto());
	}

	@Nested
	class CreateWeightliftingWorkout {

		private CreateWeightliftingWorkoutDto validDto() {
			CreateWorkoutSetDto set = new CreateWorkoutSetDto(1, 10, BigDecimal.valueOf(80), BigDecimal.valueOf(7.5),
					60, "ok");
			CreateWorkoutExerciseDto exercise = new CreateWorkoutExerciseDto(EXERCISE_ID, ExerciseTechnique.NORMAL, 90,
					"foco", List.of(set));
			return new CreateWeightliftingWorkoutDto(TEAM_ID, "Treino A", null, LocalDate.now(), LocalTime.of(7, 0), 60,
					WorkoutIntensity.HIGH, null, List.of(exercise));
		}

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createWeightliftingWorkout(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado");
		}

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createWeightliftingWorkout(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado");
		}

		@Test
		void whenUserNotMember_throwsUnauthorizedException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createWeightliftingWorkout(dto, USER_ID)).isInstanceOf(
					UnauthorizedException.class).hasMessageContaining("não é membro");
		}

		@Test
		void whenUserIsAthlete_throwsUnauthorizedException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.ATHLETE)));

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createWeightliftingWorkout(dto, USER_ID)).isInstanceOf(
					UnauthorizedException.class).hasMessageContaining("coaches e donos");
		}

		@Test
		void whenExerciseNotFound_throwsEntityNotFoundException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.COACH)));
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createWeightliftingWorkout(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Exercício não encontrado");
		}

		@Test
		void whenValidAsCoach_savesWorkoutWithExercisesAndSets() {
			mockHappyPathBase();
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exerciseEntity()));

			WorkoutDto result = workoutService.createWeightliftingWorkout(validDto(), USER_ID);

			assertThat(result).isNotNull();
			assertThat(result.id()).isEqualTo(WORKOUT_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			WorkoutEntity saved = captor.getValue();
			assertThat(saved.getModality()).isEqualTo(WorkoutModality.WEIGHTLIFTING);
			assertThat(saved.getExercises()).hasSize(1);
			assertThat(saved.getExercises().getFirst().getSets()).hasSize(1);
			assertThat(saved.getExercises().getFirst().getOrderIndex()).isEqualTo(1);
		}

		@Test
		void whenValidAsOwner_savesWorkout() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.OWNER)));
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exerciseEntity()));
			when(workoutRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
			when(workoutMapper.toDto(any())).thenReturn(workoutDto());

			WorkoutDto result = workoutService.createWeightliftingWorkout(validDto(), USER_ID);

			assertThat(result).isNotNull();
		}

		@Test
		void whenMultipleExercisesWithMultipleSets_orderIndexIsCorrect() {
			CreateWorkoutSetDto set1 = new CreateWorkoutSetDto(1, 8, BigDecimal.valueOf(100), BigDecimal.valueOf(8), 60,
					null);
			CreateWorkoutSetDto set2 = new CreateWorkoutSetDto(2, 8, BigDecimal.valueOf(100), BigDecimal.valueOf(8), 60,
					null);
			CreateWorkoutExerciseDto ex1 = new CreateWorkoutExerciseDto(EXERCISE_ID, ExerciseTechnique.NORMAL, 90, null,
					List.of(set1, set2));
			CreateWorkoutExerciseDto ex2 = new CreateWorkoutExerciseDto(EXERCISE_ID, ExerciseTechnique.NORMAL, 60, null,
					List.of(set1));
			CreateWeightliftingWorkoutDto dto = new CreateWeightliftingWorkoutDto(TEAM_ID, "Treino B", null,
					LocalDate.now(), LocalTime.of(8, 0), 60, WorkoutIntensity.HIGH, null, List.of(ex1, ex2));

			mockHappyPathBase();
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(exerciseEntity()));

			workoutService.createWeightliftingWorkout(dto, USER_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			List<WorkoutExerciseEntity> exercises = captor.getValue().getExercises();
			assertThat(exercises).hasSize(2);
			assertThat(exercises.getFirst().getOrderIndex()).isEqualTo(1);
			assertThat(exercises.get(1).getOrderIndex()).isEqualTo(2);
			assertThat(exercises.getFirst().getSets()).hasSize(2);
			assertThat(exercises.get(1).getSets()).hasSize(1);
		}

	}

	@Nested
	class CreateRunningWorkout {

		private CreateRunningWorkoutDto validDto() {
			CreateRunningSegmentDto segment = new CreateRunningSegmentDto(RunningSegmentType.INTERVAL, 400, 90,
					"4:30/km", 270, WorkoutIntensity.HIGH, null);
			return new CreateRunningWorkoutDto(TEAM_ID, "Corrida intervalada", null, LocalDate.now(),
					LocalTime.of(6, 30), 45, WorkoutIntensity.HIGH, null, List.of(segment));
		}

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createRunningWorkout(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado");
		}

		@Test
		void whenUserNotMember_throwsUnauthorizedException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createRunningWorkout(dto, USER_ID)).isInstanceOf(
					UnauthorizedException.class);
		}

		@Test
		void whenUserIsAthlete_throwsUnauthorizedException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.ATHLETE)));

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createRunningWorkout(dto, USER_ID)).isInstanceOf(
					UnauthorizedException.class);
		}

		@Test
		void whenValid_savesWorkoutWithSegmentsAndCorrectOrderIndex() {
			CreateRunningSegmentDto seg1 = new CreateRunningSegmentDto(RunningSegmentType.WARMUP, 1000, 360, null, null,
					WorkoutIntensity.LOW, null);
			CreateRunningSegmentDto seg2 = new CreateRunningSegmentDto(RunningSegmentType.INTERVAL, 400, 90, "4:00/km",
					240, WorkoutIntensity.HIGH, null);
			CreateRunningWorkoutDto dto = new CreateRunningWorkoutDto(TEAM_ID, "Corrida", null, LocalDate.now(), null,
					40, WorkoutIntensity.HIGH, null, List.of(seg1, seg2));

			mockHappyPathBase();

			workoutService.createRunningWorkout(dto, USER_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			WorkoutEntity saved = captor.getValue();
			assertThat(saved.getModality()).isEqualTo(WorkoutModality.RUNNING);
			assertThat(saved.getRunningSegments()).hasSize(2);
			assertThat(saved.getRunningSegments().getFirst().getOrderIndex()).isEqualTo(1);
			assertThat(saved.getRunningSegments().get(1).getOrderIndex()).isEqualTo(2);
			assertThat(saved.getRunningSegments().getFirst().getSegmentType()).isEqualTo(RunningSegmentType.WARMUP);
			assertThat(saved.getRunningSegments().get(1).getSegmentType()).isEqualTo(RunningSegmentType.INTERVAL);
		}

		@Test
		void whenValid_mapsAllSegmentFields() {
			mockHappyPathBase();

			workoutService.createRunningWorkout(validDto(), USER_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			WorkoutRunningSegmentEntity seg = captor.getValue().getRunningSegments().getFirst();
			assertThat(seg.getSegmentType()).isEqualTo(RunningSegmentType.INTERVAL);
			assertThat(seg.getDistanceMeters()).isEqualTo(400);
			assertThat(seg.getDurationSeconds()).isEqualTo(90);
			assertThat(seg.getTargetPace()).isEqualTo("4:30/km");
			assertThat(seg.getTargetPaceSeconds()).isEqualTo(270);
			assertThat(seg.getIntensity()).isEqualTo(WorkoutIntensity.HIGH);
		}

	}

	@Nested
	class CreateSwimmingWorkout {

		private CreateSwimmingWorkoutDto validDto() {
			CreateSwimmingSetDto set = new CreateSwimmingSetDto(SwimmingStroke.FREESTYLE, 100, 4, "1:30", 90, 30,
					SwimmingEquipment.FINS, null);
			return new CreateSwimmingWorkoutDto(TEAM_ID, "Natação", null, LocalDate.now(), LocalTime.of(7, 0), 60,
					WorkoutIntensity.HIGH, null, List.of(set));
		}

		@Test
		void whenTeamNotFound_throwsEntityNotFoundException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createSwimmingWorkout(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Time não encontrado");
		}

		@Test
		void whenUserIsAthlete_throwsUnauthorizedException() {
			when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(teamEntity()));
			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(userEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.ATHLETE)));

			var dto = validDto();
			assertThatThrownBy(() -> workoutService.createSwimmingWorkout(dto, USER_ID)).isInstanceOf(
					UnauthorizedException.class);
		}

		@Test
		void whenValid_savesWorkoutWithSetsAndCorrectOrderIndex() {
			CreateSwimmingSetDto set1 = new CreateSwimmingSetDto(SwimmingStroke.FREESTYLE, 100, 4, "1:30", 90, 30,
					SwimmingEquipment.FINS, null);
			CreateSwimmingSetDto set2 = new CreateSwimmingSetDto(SwimmingStroke.BACKSTROKE, 50, 6, "1:00", 60, 20, null,
					"relaxado");
			CreateSwimmingWorkoutDto dto = new CreateSwimmingWorkoutDto(TEAM_ID, "Natação", null, LocalDate.now(), null,
					60, WorkoutIntensity.HIGH, null, List.of(set1, set2));

			mockHappyPathBase();

			workoutService.createSwimmingWorkout(dto, USER_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			WorkoutEntity saved = captor.getValue();
			assertThat(saved.getModality()).isEqualTo(WorkoutModality.SWIMMING);
			assertThat(saved.getSwimmingSets()).hasSize(2);
			assertThat(saved.getSwimmingSets().getFirst().getOrderIndex()).isEqualTo(1);
			assertThat(saved.getSwimmingSets().get(1).getOrderIndex()).isEqualTo(2);
		}

		@Test
		void whenValid_mapsAllSwimmingSetFields() {
			mockHappyPathBase();

			workoutService.createSwimmingWorkout(validDto(), USER_ID);

			ArgumentCaptor<WorkoutEntity> captor = ArgumentCaptor.forClass(WorkoutEntity.class);
			verify(workoutRepository).save(captor.capture());

			WorkoutSwimmingSetEntity s = captor.getValue().getSwimmingSets().getFirst();
			assertThat(s.getStroke()).isEqualTo(SwimmingStroke.FREESTYLE);
			assertThat(s.getDistanceMeters()).isEqualTo(100);
			assertThat(s.getRepetitions()).isEqualTo(4);
			assertThat(s.getTargetTime()).isEqualTo("1:30");
			assertThat(s.getTargetPaceSeconds()).isEqualTo(90);
			assertThat(s.getRestSeconds()).isEqualTo(30);
			assertThat(s.getEquipment()).isEqualTo(SwimmingEquipment.FINS);
		}

	}

	@Nested
	class GetTeamWorkouts {

		@Test
		void whenTeamHasWorkouts_returnsMappedList() {
			WorkoutEntity w1 = workoutEntity();
			WorkoutEntity w2 = workoutEntity();
			when(workoutRepository.findByTeamIdOrderByScheduledDateDesc(TEAM_ID)).thenReturn(List.of(w1, w2));
			when(workoutMapper.toDto(any(WorkoutEntity.class))).thenReturn(workoutDto());

			List<WorkoutDto> result = workoutService.getTeamWorkouts(TEAM_ID);

			assertThat(result).hasSize(2);
			verify(workoutMapper, times(2)).toDto(any(WorkoutEntity.class));
		}

		@Test
		void whenTeamHasNoWorkouts_returnsEmptyList() {
			when(workoutRepository.findByTeamIdOrderByScheduledDateDesc(TEAM_ID)).thenReturn(List.of());

			List<WorkoutDto> result = workoutService.getTeamWorkouts(TEAM_ID);

			assertThat(result).isEmpty();
		}

	}

	@Nested
	class GetWorkoutById {

		@Test
		void whenWorkoutNotFound_throwsEntityNotFoundException() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> workoutService.getWorkoutById(WORKOUT_ID, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Treino não encontrado");
		}

		@Test
		void whenUserNotMember_throwsUnauthorizedException() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workoutEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> workoutService.getWorkoutById(WORKOUT_ID, USER_ID)).isInstanceOf(
					UnauthorizedException.class).hasMessageContaining("permissão");
		}

		@Test
		void whenValid_returnsMappedDto() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workoutEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.ATHLETE)));
			when(workoutMapper.toDto(any(WorkoutEntity.class))).thenReturn(workoutDto());

			WorkoutDto result = workoutService.getWorkoutById(WORKOUT_ID, USER_ID);

			assertThat(result.id()).isEqualTo(WORKOUT_ID);
		}

		@Test
		void whenUserIsCoach_canViewWorkout() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workoutEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.COACH)));
			when(workoutMapper.toDto(any(WorkoutEntity.class))).thenReturn(workoutDto());

			assertThat(workoutService.getWorkoutById(WORKOUT_ID, USER_ID)).isNotNull();
		}

	}

	@Nested
	class DeleteWorkout {

		@Test
		void whenWorkoutNotFound_throwsEntityNotFoundException() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> workoutService.deleteWorkout(WORKOUT_ID, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Treino não encontrado");
		}

		@Test
		void whenUserNotMember_throwsUnauthorizedException() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workoutEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> workoutService.deleteWorkout(WORKOUT_ID, USER_ID)).isInstanceOf(
					UnauthorizedException.class).hasMessageContaining("não é membro");
		}

		@Test
		void whenUserIsAthlete_throwsUnauthorizedException() {
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workoutEntity()));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.ATHLETE)));

			assertThatThrownBy(() -> workoutService.deleteWorkout(WORKOUT_ID, USER_ID)).isInstanceOf(
					UnauthorizedException.class).hasMessageContaining("coaches e donos");
		}

		@Test
		void whenUserIsCoach_deletesWorkout() {
			WorkoutEntity workout = workoutEntity();
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workout));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.COACH)));

			workoutService.deleteWorkout(WORKOUT_ID, USER_ID);

			verify(workoutRepository).delete(workout);
		}

		@Test
		void whenUserIsOwner_deletesWorkout() {
			WorkoutEntity workout = workoutEntity();
			when(workoutRepository.findById(WORKOUT_ID)).thenReturn(Optional.of(workout));
			when(teamMemberRepository.findByTeamIdAndUserId(TEAM_ID, USER_ID)).thenReturn(
					Optional.of(memberOf(TeamRole.OWNER)));

			workoutService.deleteWorkout(WORKOUT_ID, USER_ID);

			verify(workoutRepository).delete(workout);
		}

	}

}