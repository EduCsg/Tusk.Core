package com.hydra.core.mappers;

import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.utils.ValidationUtils;

import java.util.List;
import java.util.Set;

public class UserMapper {

	public static List<UserDto> entitiesToDtos(List<UserEntity> entities) {
		if (ValidationUtils.isEmpty(entities))
			return List.of();

		return entities.stream().map(UserMapper::entityToDto).toList();
	}

	public static List<UserDto> entitiesToDtos(Set<UserEntity> entities) {
		if (ValidationUtils.isEmpty(entities))
			return List.of();

		return entities.stream().map(UserMapper::entityToDto).toList();
	}

	public static UserDto entityToDto(UserEntity entity) {
		return new UserDto(entity.getId(), null, entity.getUsername(), entity.getName(), entity.getEmail(), null,
				entity.getRole());
	}

}