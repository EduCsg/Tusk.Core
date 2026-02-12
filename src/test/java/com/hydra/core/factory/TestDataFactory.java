package com.hydra.core.factory;

import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestComponent;

import java.util.UUID;

@TestComponent
public class TestDataFactory {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TeamRepository teamRepository;

	public UserEntity createUser() {
		UserEntity user = new UserEntity();
		user.setName("User Test");
		user.setEmail(UUID.randomUUID() + "@test.com");
		user.setUsername(UUID.randomUUID().toString());
		user.setPassword("123");
		return user;
	}

	public TeamEntity createTeam(UserEntity user) {
		TeamEntity team = new TeamEntity();
		team.setName("Team Test");
		team.setCity("SP");
		team.setUf("SP");
		team.setColor("#000000");
		team.setCreatedBy(user);
		return team;
	}

}