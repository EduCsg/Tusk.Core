package com.hydra.core.repository;

import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.factory.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestDataFactory.class)
class TeamRepositoryIntegrationTest {

	@Autowired
	private TeamRepository teamRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TestDataFactory factory;

	@Test
	@DisplayName("Should save and retrieve a Team")
	void shouldSaveAndRetrieveTeam() {
		// Arrange
		UserEntity user = factory.createUser();
		entityManager.persist(user);

		TeamEntity team = factory.createTeam(user);

		// Act
		TeamEntity savedTeam = teamRepository.save(team);
		entityManager.flush();
		entityManager.clear();

		Optional<TeamEntity> foundTeam = teamRepository.findById(savedTeam.getId());

		// Assert
		assertThat(foundTeam).isPresent();
		assertThat(foundTeam.get().getId()).isEqualTo(team.getId());
		assertThat(foundTeam.get().getName()).isEqualTo(team.getName());
	}

	@Test
	@DisplayName("Should return empty when Team does not exist")
	void shouldReturnEmptyWhenTeamNotFound() {
		// Act
		Optional<TeamEntity> foundTeam = teamRepository.findById("nope");

		// Assert
		assertThat(foundTeam).isEmpty();
	}

	@Test
	@DisplayName("Should delete a Team")
	void shouldDeleteTeam() {
		// Arrange
		UserEntity user = factory.createUser();
		entityManager.persist(user);

		TeamEntity team = factory.createTeam(user);
		teamRepository.save(team);
		entityManager.flush();

		// Act
		teamRepository.deleteById(team.getId());
		entityManager.flush();

		Optional<TeamEntity> deletedTeam = teamRepository.findById(team.getId());

		// Assert
		assertThat(deletedTeam).isEmpty();
	}

	@Test
	@DisplayName("Should list all Teams")
	void shouldListAllTeams() {
		// Arrange
		UserEntity user = factory.createUser();
		entityManager.persist(user);

		TeamEntity team1 = factory.createTeam(user);
		TeamEntity team2 = factory.createTeam(user);

		teamRepository.save(team1);
		teamRepository.save(team2);
		entityManager.flush();

		// Act
		var teams = teamRepository.findAll();

		// Assert
		assertThat(teams).extracting(TeamEntity::getId).containsExactlyInAnyOrder(team1.getId(), team2.getId());
		assertThat(teams).extracting(TeamEntity::getName).containsExactlyInAnyOrder(team1.getName(), team2.getName());
	}

	@Test
	@DisplayName("Should update a Team")
	void shouldUpdateTeam() {
		// Arrange
		UserEntity user = factory.createUser();
		entityManager.persist(user);

		TeamEntity team = factory.createTeam(user);
		teamRepository.save(team);

		entityManager.flush();
		entityManager.clear();

		// Act
		TeamEntity teamToUpdate = teamRepository.findById(team.getId()).orElseThrow();
		teamToUpdate.setName("Team Atualizado");
		entityManager.flush();
		entityManager.clear();

		// Assert
		TeamEntity updatedTeam = teamRepository.findById(team.getId()).orElseThrow();
		assertThat(updatedTeam.getName()).isEqualTo("Team Atualizado");
	}

}