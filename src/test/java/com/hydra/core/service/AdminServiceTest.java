package com.hydra.core.service;

import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.mappers.UserMapper;
import com.hydra.core.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private AdminService adminService;

	private UserEntity buildUser(String id, String name) {
		UserEntity user = new UserEntity();
		user.setId(id);
		user.setName(name);
		return user;
	}

	@Nested
	class GetAllUsers {

		@Test
		void returnsHttpStatus200() {
			when(userRepository.findAll()).thenReturn(List.of());

			try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
				mapperMock.when(() -> UserMapper.entitiesToDtos(List.of())).thenReturn(List.of());

				ResponseEntity<ResponseDto> response = adminService.getAllUsers();

				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			}
		}

		@Test
		void responseBodySuccessIsTrue() {
			when(userRepository.findAll()).thenReturn(List.of());

			try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
				mapperMock.when(() -> UserMapper.entitiesToDtos(List.of())).thenReturn(List.of());

				ResponseEntity<ResponseDto> response = adminService.getAllUsers();

				assertThat(response.getBody()).isNotNull();
				assertThat(response.getBody().isSuccess()).isTrue();
			}
		}

		@Test
		void whenRepositoryReturnsEmptyList_responseDataIsEmpty() {
			when(userRepository.findAll()).thenReturn(List.of());

			try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
				mapperMock.when(() -> UserMapper.entitiesToDtos(List.of())).thenReturn(List.of());

				ResponseEntity<ResponseDto> response = adminService.getAllUsers();

				assertThat(response.getBody()).isNotNull();
				assertThat((List<?>) response.getBody().getData()).isEmpty();
			}
		}

		@Test
		void whenRepositoryReturnsUsers_delegatesToUserMapper() {
			UserEntity user1 = buildUser("1", "Alice");
			UserEntity user2 = buildUser("2", "Bob");
			List<UserEntity> entities = List.of(user1, user2);

			when(userRepository.findAll()).thenReturn(entities);

			try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
				List<Object> mappedDtos = List.of(new Object(), new Object());
				mapperMock.when(() -> UserMapper.entitiesToDtos(entities)).thenReturn(mappedDtos);

				ResponseEntity<ResponseDto> response = adminService.getAllUsers();

				Assertions.assertNotNull(response.getBody());
				mapperMock.verify(() -> UserMapper.entitiesToDtos(entities));
				assertThat((List<?>) response.getBody().getData()).hasSize(2);
			}
		}

		@Test
		void callsUserRepositoryFindAll() {
			when(userRepository.findAll()).thenReturn(List.of());

			try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
				mapperMock.when(() -> UserMapper.entitiesToDtos(anyList())).thenReturn(List.of());

				adminService.getAllUsers();

				verify(userRepository).findAll();
			}
		}

	}

}