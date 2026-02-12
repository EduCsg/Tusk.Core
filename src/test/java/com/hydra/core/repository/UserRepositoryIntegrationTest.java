package com.hydra.core.repository;

import com.hydra.core.entity.UserEntity;
import com.hydra.core.factory.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
class UserRepositoryIntegrationTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestDataFactory factory;

	@Test
	@DisplayName("Should find user by email")
	void shouldFindByEmail() {
		UserEntity user = factory.createUser();
		userRepository.saveAndFlush(user);

		Optional<UserEntity> result = userRepository.findByEmailOrUsername(user.getEmail(), "nope");

		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	@DisplayName("Should find user by username")
	void shouldFindByUsername() {
		UserEntity user = factory.createUser();
		userRepository.saveAndFlush(user);

		Optional<UserEntity> result = userRepository.findByEmailOrUsername("nope@test.com", user.getUsername());

		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
	}

	@Test
	@DisplayName("Should find user ignoring case for email")
	void shouldFindByEmailIgnoreCase() {
		UserEntity user = factory.createUser();
		userRepository.saveAndFlush(user);

		Optional<UserEntity> result = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(
				user.getEmail().toUpperCase(), "nope");

		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
	}

	@Test
	@DisplayName("Should find user ignoring case for username")
	void shouldFindByUsernameIgnoreCase() {
		UserEntity user = factory.createUser();
		userRepository.saveAndFlush(user);

		Optional<UserEntity> result = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("other@test.com",
				user.getUsername().toUpperCase());

		assertThat(result).isPresent();
		assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
	}

	@Test
	@DisplayName("Should return empty when user does not exist")
	void shouldReturnEmptyWhenNotFound() {
		Optional<UserEntity> result = userRepository.findByEmailOrUsername("notfound@test.com", "notfound");

		assertThat(result).isEmpty();
	}

}