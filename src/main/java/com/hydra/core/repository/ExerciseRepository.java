package com.hydra.core.repository;

import com.hydra.core.entity.ExerciseEntity;
import com.hydra.core.enums.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, String> {

	// Busca exercícios globais ou customizados do usuário por nome
	@Query("SELECT e FROM ExerciseEntity e WHERE " + "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) AND e.isCustom = false) OR " + "(LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) AND e.createdBy.id = :userId)")
	List<ExerciseEntity> findByNameContainingIgnoreCaseAndIsCustomFalseOrCreatedById(@Param("query") String query,
			@Param("userId") String userId);

	// Busca exercícios globais ou customizados do usuário por grupo muscular
	@Query("SELECT e FROM ExerciseEntity e WHERE " + "(e.muscleGroup = :muscleGroup AND e.isCustom = false) OR " + "(e.muscleGroup = :muscleGroup AND e.createdBy.id = :userId)")
	List<ExerciseEntity> findByMuscleGroupAndIsCustomFalseOrCreatedById(@Param("muscleGroup") MuscleGroup muscleGroup,
			@Param("userId") String userId);

	// Busca todos os exercícios globais + customizados do usuário
	@Query("SELECT e FROM ExerciseEntity e WHERE e.isCustom = false OR e.createdBy.id = :userId ORDER BY e.name ASC")
	List<ExerciseEntity> findByIsCustomFalseOrCreatedById(@Param("userId") String userId);

}