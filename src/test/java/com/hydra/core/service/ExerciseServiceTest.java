package com.hydra.core.service;

import com.hydra.core.dtos.CreateExerciseDto;
import com.hydra.core.dtos.ExerciseDto;
import com.hydra.core.entity.ExerciseEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.Difficulty;
import com.hydra.core.enums.Equipment;
import com.hydra.core.enums.MuscleGroup;
import com.hydra.core.repository.ExerciseRepository;
import com.hydra.core.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

	private static final String USER_ID = "user-1";
	private static final String EXERCISE_ID = "exercise-1";

	@Mock
	private ExerciseRepository exerciseRepository;

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private ExerciseService exerciseService;

	private UserEntity buildUser() {
		UserEntity user = new UserEntity();
		user.setId(USER_ID);
		user.setName("John Doe");
		return user;
	}

	private ExerciseEntity buildExercise(UserEntity createdBy) {
		ExerciseEntity e = new ExerciseEntity();
		e.setId(EXERCISE_ID);
		e.setName("Squat");
		e.setDescription("Leg exercise");
		e.setMuscleGroup(MuscleGroup.LEGS);
		e.setSecondaryMuscles("Glutes");
		e.setEquipment(Equipment.BARBELL);
		e.setDifficulty(Difficulty.INTERMEDIATE);
		e.setVideoUrl("http://video.url");
		e.setImageUrl("http://image.url");
		e.setInstructions("Keep back straight");
		e.setIsCustom(false);
		e.setCreatedBy(createdBy);
		e.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
		return e;
	}

	private CreateExerciseDto buildCreateDto() {
		return new CreateExerciseDto("Squat", "Leg exercise", MuscleGroup.LEGS, "Glutes", Equipment.BARBELL,
				Difficulty.INTERMEDIATE, "http://video.url", "http://image.url", "Keep back straight");
	}

	@Nested
	class SearchExercises {

		@Test
		void whenQueryIsProvided_searchesByName() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById("squat",
					USER_ID)).thenReturn(List.of(entity));

			List<ExerciseDto> result = exerciseService.searchExercises("squat", null, USER_ID);

			assertThat(result).hasSize(1);
			assertThat(result.get(0).name()).isEqualTo("Squat");
			verify(exerciseRepository).findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById("squat", USER_ID);
			verifyNoMoreInteractions(exerciseRepository);
		}

		@Test
		void whenQueryIsBlank_doesNotSearchByName() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findByIsCustomFalseOrCreatedById(USER_ID)).thenReturn(List.of(entity));

			List<ExerciseDto> result = exerciseService.searchExercises("   ", null, USER_ID);

			assertThat(result).hasSize(1);
			verify(exerciseRepository).findByIsCustomFalseOrCreatedById(USER_ID);
			verifyNoMoreInteractions(exerciseRepository);
		}

		@Test
		void whenQueryIsNull_andMuscleGroupIsProvided_searchesByMuscleGroup() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findByMuscleGroupAndIsCustomFalseOrCreatedById(MuscleGroup.LEGS,
					USER_ID)).thenReturn(List.of(entity));

			List<ExerciseDto> result = exerciseService.searchExercises(null, MuscleGroup.LEGS, USER_ID);

			assertThat(result).hasSize(1);
			verify(exerciseRepository).findByMuscleGroupAndIsCustomFalseOrCreatedById(MuscleGroup.LEGS, USER_ID);
			verifyNoMoreInteractions(exerciseRepository);
		}

		@Test
		void whenQueryIsNull_andMuscleGroupIsNull_returnsAll() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findByIsCustomFalseOrCreatedById(USER_ID)).thenReturn(List.of(entity));

			List<ExerciseDto> result = exerciseService.searchExercises(null, null, USER_ID);

			assertThat(result).hasSize(1);
			verify(exerciseRepository).findByIsCustomFalseOrCreatedById(USER_ID);
			verifyNoMoreInteractions(exerciseRepository);
		}

		@Test
		void whenQueryIsEmpty_andMuscleGroupIsNull_returnsAll() {
			when(exerciseRepository.findByIsCustomFalseOrCreatedById(USER_ID)).thenReturn(List.of());

			List<ExerciseDto> result = exerciseService.searchExercises("", null, USER_ID);

			assertThat(result).isEmpty();
			verify(exerciseRepository).findByIsCustomFalseOrCreatedById(USER_ID);
		}

		@Test
		void whenQueryTakesPriority_overMuscleGroup() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById("bench",
					USER_ID)).thenReturn(List.of(entity));

			// Both query and muscleGroup provided — query wins
			List<ExerciseDto> result = exerciseService.searchExercises("bench", MuscleGroup.LEGS, USER_ID);

			assertThat(result).hasSize(1);
			verify(exerciseRepository).findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById("bench", USER_ID);
			verify(exerciseRepository, never()).findByMuscleGroupAndIsCustomFalseOrCreatedById(any(), any());
		}

		@Test
		void returnsEmptyList_whenRepositoryReturnsNothing() {
			when(exerciseRepository.findByIsCustomFalseOrCreatedById(USER_ID)).thenReturn(List.of());

			List<ExerciseDto> result = exerciseService.searchExercises(null, null, USER_ID);

			assertThat(result).isEmpty();
		}

	}

	@Nested
	class CreateCustomExercise {

		@Test
		void whenUserExists_createsAndReturnsExercise() {
			UserEntity user = buildUser();
			CreateExerciseDto dto = buildCreateDto();

			ExerciseEntity saved = buildExercise(user);
			saved.setIsCustom(true);

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(exerciseRepository.save(any(ExerciseEntity.class))).thenReturn(saved);

			ExerciseDto result = exerciseService.createCustomExercise(dto, USER_ID);

			assertThat(result.id()).isEqualTo(EXERCISE_ID);
			assertThat(result.name()).isEqualTo("Squat");
			assertThat(result.isCustom()).isTrue();
			assertThat(result.createdByName()).isEqualTo("John Doe");

			verify(userRepository).findById(USER_ID);
			verify(exerciseRepository).save(any(ExerciseEntity.class));
		}

		@Test
		void whenUserExists_setsAllFieldsOnEntity() {
			UserEntity user = buildUser();
			CreateExerciseDto dto = buildCreateDto();
			ExerciseEntity saved = buildExercise(user);
			saved.setIsCustom(true);

			when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
			when(exerciseRepository.save(any(ExerciseEntity.class))).thenAnswer(inv -> {
				ExerciseEntity e = inv.getArgument(0);
				// Assert all fields were set before save
				assertThat(e.getName()).isEqualTo(dto.name());
				assertThat(e.getDescription()).isEqualTo(dto.description());
				assertThat(e.getMuscleGroup()).isEqualTo(dto.muscleGroup());
				assertThat(e.getSecondaryMuscles()).isEqualTo(dto.secondaryMuscles());
				assertThat(e.getEquipment()).isEqualTo(dto.equipment());
				assertThat(e.getDifficulty()).isEqualTo(dto.difficulty());
				assertThat(e.getVideoUrl()).isEqualTo(dto.videoUrl());
				assertThat(e.getImageUrl()).isEqualTo(dto.imageUrl());
				assertThat(e.getInstructions()).isEqualTo(dto.instructions());
				assertThat(e.getIsCustom()).isTrue();
				assertThat(e.getCreatedBy()).isEqualTo(user);
				return saved;
			});

			exerciseService.createCustomExercise(dto, USER_ID);

			verify(exerciseRepository).save(any(ExerciseEntity.class));
		}

		@Test
		void whenUserNotFound_throwsEntityNotFoundException() {
			when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

			CreateExerciseDto dto = buildCreateDto();

			assertThatThrownBy(() -> exerciseService.createCustomExercise(dto, USER_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Usuário não encontrado");

			verify(exerciseRepository, never()).save(any());
		}

	}

	@Nested
	class GetExerciseById {

		@Test
		void whenExerciseExists_returnsDto() {
			ExerciseEntity entity = buildExercise(buildUser());
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(entity));

			ExerciseDto result = exerciseService.getExerciseById(EXERCISE_ID);

			assertThat(result.id()).isEqualTo(EXERCISE_ID);
			assertThat(result.name()).isEqualTo("Squat");
		}

		@Test
		void whenExerciseNotFound_throwsEntityNotFoundException() {
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> exerciseService.getExerciseById(EXERCISE_ID)).isInstanceOf(
					EntityNotFoundException.class).hasMessageContaining("Exercício não encontrado");
		}

	}

	@Nested
	class MapToDto {

		@Test
		void whenCreatedByIsNull_createdByNameIsNull() {
			ExerciseEntity entity = buildExercise(null);
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(entity));

			ExerciseDto dto = exerciseService.getExerciseById(EXERCISE_ID);

			assertThat(dto.createdByName()).isNull();
		}

		@Test
		void whenCreatedByIsPresent_createdByNameIsMapped() {
			UserEntity user = buildUser();
			ExerciseEntity entity = buildExercise(user);
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(entity));

			ExerciseDto dto = exerciseService.getExerciseById(EXERCISE_ID);

			assertThat(dto.createdByName()).isEqualTo("John Doe");
		}

		@Test
		void allFieldsAreMappedCorrectly() {
			UserEntity user = buildUser();
			ExerciseEntity entity = buildExercise(user);
			when(exerciseRepository.findById(EXERCISE_ID)).thenReturn(Optional.of(entity));

			ExerciseDto dto = exerciseService.getExerciseById(EXERCISE_ID);

			assertThat(dto.id()).isEqualTo(EXERCISE_ID);
			assertThat(dto.name()).isEqualTo("Squat");
			assertThat(dto.description()).isEqualTo("Leg exercise");
			assertThat(dto.muscleGroup()).isEqualTo(MuscleGroup.LEGS);
			assertThat(dto.secondaryMuscles()).contains("Glutes");
			assertThat(dto.equipment()).isEqualTo(Equipment.BARBELL);
			assertThat(dto.difficulty()).isEqualTo(Difficulty.INTERMEDIATE);
			assertThat(dto.videoUrl()).isEqualTo("http://video.url");
			assertThat(dto.imageUrl()).isEqualTo("http://image.url");
			assertThat(dto.instructions()).isEqualTo("Keep back straight");
			assertThat(dto.isCustom()).isFalse();
			assertThat(dto.createdByName()).isEqualTo("John Doe");
			assertThat(dto.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
		}

	}

}