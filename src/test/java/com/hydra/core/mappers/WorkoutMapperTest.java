package com.hydra.core.mappers;

import com.hydra.core.dtos.*;
import com.hydra.core.entity.*;
import com.hydra.core.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutMapperTest {

	private WorkoutMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new WorkoutMapper();
	}

	@Test
	void toDto_whenEntityIsNull_returnsNull() {
		assertThat(mapper.toDto(null)).isNull();
	}

	@Test
	void toDto_mapsScalarFieldsCorrectly() {
		WorkoutEntity entity = buildMinimalWorkout();
		entity.setTitle("Morning Run");
		entity.setDescription("Easy pace");
		entity.setDurationMinutes(45);

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.id()).isEqualTo("1");
		assertThat(dto.title()).isEqualTo("Morning Run");
		assertThat(dto.description()).isEqualTo("Easy pace");
		assertThat(dto.durationMinutes()).isEqualTo(45);
	}

	@Test
	void toDto_whenTeamIsPresent_mapsTeamIdAndName() {
		TeamEntity team = new TeamEntity();
		team.setId("1");
		team.setName("Team Alpha");

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setTeam(team);

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.teamId()).isEqualTo("1");
		assertThat(dto.teamName()).isEqualTo("Team Alpha");
	}

	@Test
	void toDto_whenTeamIsNull_teamFieldsAreNull() {
		WorkoutEntity entity = buildMinimalWorkout();
		entity.setTeam(null);

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.teamId()).isNull();
		assertThat(dto.teamName()).isNull();
	}

	@Test
	void toDto_whenCreatedByIsPresent_mapsCreatorIdAndName() {
		UserEntity user = new UserEntity();
		user.setId("1");
		user.setName("Coach Bob");

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setCreatedBy(user);

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.createdById()).isEqualTo("1");
		assertThat(dto.createdByName()).isEqualTo("Coach Bob");
	}

	@Test
	void toDto_whenCreatedByIsNull_creatorFieldsAreNull() {
		WorkoutEntity entity = buildMinimalWorkout();
		entity.setCreatedBy(null);

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.createdById()).isNull();
		assertThat(dto.createdByName()).isNull();
	}

	@Test
	void toDto_mapsExercisesCorrectly() {
		ExerciseEntity exercise = new ExerciseEntity();
		exercise.setId("1");
		exercise.setName("Squat");

		WorkoutExerciseSetEntity set = new WorkoutExerciseSetEntity();
		set.setId("2");
		set.setSetNumber(1);
		set.setReps(10);
		set.setWeight(BigDecimal.valueOf(80.0));
		set.setRpe(BigDecimal.valueOf(7.5));
		set.setRestSeconds(60);
		set.setNotes("Focus on depth");

		WorkoutExerciseEntity exerciseEntity = new WorkoutExerciseEntity();
		exerciseEntity.setId("3");
		exerciseEntity.setExercise(exercise);
		exerciseEntity.setOrderIndex(1);
		exerciseEntity.setTechnique(ExerciseTechnique.CLUSTER_SET);
		exerciseEntity.setRestBetweenSetsSeconds(90);
		exerciseEntity.setNotes("Keep back straight");
		exerciseEntity.setSets(List.of(set));

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setExercises(List.of(exerciseEntity));

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.exercises()).hasSize(1);
		WorkoutExerciseDto exDto = dto.exercises().getFirst();
		assertThat(exDto.id()).isEqualTo("3");
		assertThat(exDto.exerciseId()).isEqualTo("1");
		assertThat(exDto.exerciseName()).isEqualTo("Squat");
		assertThat(exDto.sets()).hasSize(1);

		WorkoutExerciseSetDto setDto = exDto.sets().getFirst();
		assertThat(setDto.id()).isEqualTo("2");
		assertThat(setDto.reps()).isEqualTo(10);
		assertThat(setDto.weight()).isEqualTo(BigDecimal.valueOf(80.0));
	}

	@Test
	void toDto_whenExerciseRefIsNull_exerciseIdAndNameAreNull() {
		WorkoutExerciseEntity exerciseEntity = new WorkoutExerciseEntity();
		exerciseEntity.setId("1");
		exerciseEntity.setExercise(null);
		exerciseEntity.setSets(List.of());

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setExercises(List.of(exerciseEntity));

		WorkoutDto dto = mapper.toDto(entity);

		WorkoutExerciseDto exDto = dto.exercises().getFirst();
		assertThat(exDto.exerciseId()).isNull();
		assertThat(exDto.exerciseName()).isNull();
	}

	@Test
	void toDto_mapsRunningSegmentsCorrectly() {
		WorkoutRunningSegmentEntity segment = new WorkoutRunningSegmentEntity();
		segment.setId("1");
		segment.setOrderIndex(1);
		segment.setSegmentType(RunningSegmentType.CONTINUOUS);
		segment.setDistanceMeters(400);
		segment.setDurationSeconds(90);
		segment.setTargetPace("4:30/km");
		segment.setTargetPaceSeconds(270);
		segment.setIntensity(WorkoutIntensity.HIGH);
		segment.setNotes("Sprint");

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setRunningSegments(List.of(segment));

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.runningSegments()).hasSize(1);
		WorkoutRunningSegmentDto segDto = dto.runningSegments().getFirst();
		assertThat(segDto.id()).isEqualTo("1");
		assertThat(segDto.segmentType()).isEqualTo(RunningSegmentType.CONTINUOUS);
		assertThat(segDto.intensity()).isEqualTo(WorkoutIntensity.HIGH);
		assertThat(segDto.distanceMeters()).isEqualTo(400);
		assertThat(segDto.targetPace()).isEqualTo("4:30/km");
	}

	@Test
	void toDto_mapsSwimmingSetsCorrectly() {
		WorkoutSwimmingSetEntity swimmingSet = new WorkoutSwimmingSetEntity();
		swimmingSet.setId("1");
		swimmingSet.setOrderIndex(1);
		swimmingSet.setStroke(SwimmingStroke.FREESTYLE);
		swimmingSet.setDistanceMeters(100);
		swimmingSet.setRepetitions(4);
		swimmingSet.setTargetTime("1:30");
		swimmingSet.setTargetPaceSeconds(90);
		swimmingSet.setRestSeconds(30);
		swimmingSet.setEquipment(SwimmingEquipment.NONE);
		swimmingSet.setNotes("Easy");

		WorkoutEntity entity = buildMinimalWorkout();
		entity.setSwimmingSets(List.of(swimmingSet));

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.swimmingSets()).hasSize(1);
		WorkoutSwimmingSetDto setDto = dto.swimmingSets().getFirst();
		assertThat(setDto.id()).isEqualTo("1");
		assertThat(setDto.stroke()).isEqualTo(SwimmingStroke.FREESTYLE);
		assertThat(setDto.repetitions()).isEqualTo(4);
		assertThat(setDto.equipment()).isEqualTo(SwimmingEquipment.NONE);
	}

	@Test
	void toDto_mapsTimestampsCorrectly() {
		LocalDateTime now = LocalDateTime.now();
		WorkoutEntity entity = buildMinimalWorkout();
		entity.setCreatedAt(now);
		entity.setUpdatedAt(now.plusHours(1));

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.createdAt()).isEqualTo(now);
		assertThat(dto.updatedAt()).isEqualTo(now.plusHours(1));
	}

	@Test
	void toDto_emptyCollections_returnEmptyLists() {
		WorkoutEntity entity = buildMinimalWorkout();

		WorkoutDto dto = mapper.toDto(entity);

		assertThat(dto.exercises()).isEmpty();
		assertThat(dto.runningSegments()).isEmpty();
		assertThat(dto.swimmingSets()).isEmpty();
	}

	private WorkoutEntity buildMinimalWorkout() {
		WorkoutEntity entity = new WorkoutEntity();
		entity.setId("1");
		entity.setTeam(null);
		entity.setCreatedBy(null);
		entity.setExercises(List.of());
		entity.setRunningSegments(List.of());
		entity.setSwimmingSets(List.of());
		return entity;
	}

}