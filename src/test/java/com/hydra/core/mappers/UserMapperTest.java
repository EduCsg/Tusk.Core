package com.hydra.core.mappers;

import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

	@Test
	void shouldThrowExceptionWhenInstantiate() {
		assertThrows(UnsupportedOperationException.class, UserMapper::new);
	}

	@Test
	void shouldConvertEntityToDto() {
		UserEntity entity = new UserEntity();
		entity.setId("1");
		entity.setUsername("admin");
		entity.setName("Admin User");
		entity.setEmail("admin@test.com");

		UserDto dto = UserMapper.entityToDto(entity);

		assertEquals("1", dto.id());
		assertEquals("admin", dto.username());
		assertEquals("Admin User", dto.name());
		assertEquals("admin@test.com", dto.email());
	}

	@Test
	void shouldReturnNullWhenEntityIsNull() {
		assertNull(UserMapper.entityToDto(null));
	}

	@Test
	void shouldReturnEmptyListWhenEntitiesIsNullOrEmpty() {
		List<UserDto> result = UserMapper.entitiesToDtos(List.of());
		assertTrue(result.isEmpty());
	}

	@Test
	void shouldReturnConvertedListWhenEntitiesIsNotEmpty() {
		UserEntity entity1 = new UserEntity();
		entity1.setId("1");
		entity1.setUsername("admin");
		entity1.setName("Admin User");
		entity1.setEmail("admin@test.com");

		UserEntity entity2 = new UserEntity();
		entity2.setId("2");
		entity2.setUsername("user");
		entity2.setName("Regular User");
		entity2.setEmail("user@test.com");

		List<UserDto> result = UserMapper.entitiesToDtos(List.of(entity1, entity2));

		assertEquals(2, result.size());

		UserDto first = result.getFirst();
		assertEquals("1", first.id());
		assertEquals("admin", first.username());
		assertEquals("Admin User", first.name());
		assertEquals("admin@test.com", first.email());

		UserDto second = result.get(1);
		assertEquals("2", second.id());
		assertEquals("user", second.username());
		assertEquals("Regular User", second.name());
		assertEquals("user@test.com", second.email());
	}

}