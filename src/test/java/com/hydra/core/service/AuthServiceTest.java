package com.hydra.core.service;

import com.hydra.core.config.ModelMapperConfig;
import com.hydra.core.dtos.AuthResponseDto;
import com.hydra.core.dtos.LoginDto;
import com.hydra.core.dtos.ResponseDto;
import com.hydra.core.dtos.UserDto;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.repository.UserRepository;
import com.hydra.core.security.JwtService;
import com.hydra.core.utils.BCrypt;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final String USER_ID = "user-1";
	private static final String USERNAME = "johndoe";
	private static final String EMAIL = "john@example.com";
	private static final String NAME = "John Doe";
	private static final String PASSWORD = "Password123!";
	private static final String JWT_TOKEN = "jwt.token.here";

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtService jwtService;

	@Mock
	private ModelMapperConfig modelMapperConfig;

	@Mock
	private ModelMapper modelMapper;

	private AuthService authService;

	@org.junit.jupiter.api.BeforeEach
	void setUp() {
		when(modelMapperConfig.modelMapper()).thenReturn(modelMapper);
		authService = new AuthService(userRepository, jwtService, modelMapperConfig);
	}

	private UserEntity userEntity() {
		UserEntity user = new UserEntity();
		user.setId(USER_ID);
		user.setUsername(USERNAME);
		user.setEmail(EMAIL);
		user.setName(NAME);
		user.setPassword(BCrypt.hashpw(PASSWORD));
		return user;
	}

	private UserDto userDto() {
		return new UserDto(null, null, USERNAME, NAME, EMAIL, PASSWORD);
	}

	private LoginDto loginDto() {
		return new LoginDto(EMAIL, PASSWORD);
	}

	@Nested
	class RegisterUser {

		@Test
		void whenEmailAlreadyExists_returnsBadRequest() {
			UserEntity existingUser = userEntity();
			when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME)).thenReturn(
					Optional.of(existingUser));

			ResponseEntity<ResponseDto> response = authService.registerUser(userDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMessage()).isEqualTo("O email já está em uso!");
			assertThat(response.getBody().isSuccess()).isFalse();
			verify(userRepository, never()).save(any());
		}

		@Test
		void whenUsernameAlreadyExists_returnsBadRequest() {
			UserEntity existingUser = userEntity();
			existingUser.setEmail("other@example.com");
			when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME)).thenReturn(
					Optional.of(existingUser));

			ResponseEntity<ResponseDto> response = authService.registerUser(userDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMessage()).isEqualTo("O nome de usuário já está em uso!");
			assertThat(response.getBody().isSuccess()).isFalse();
			verify(userRepository, never()).save(any());
		}

		@Test
		void whenValid_savesUserWithHashedPassword() {
			when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME)).thenReturn(
					Optional.empty());

			UserEntity mappedUser = new UserEntity();
			mappedUser.setUsername(USERNAME);
			mappedUser.setEmail(EMAIL);
			mappedUser.setName(NAME);
			when(modelMapper.map(any(UserDto.class), eq(UserEntity.class))).thenReturn(mappedUser);
			when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
				UserEntity user = invocation.getArgument(0);
				user.setId(USER_ID);
				return user;
			});
			when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString())).thenReturn(JWT_TOKEN);

			ResponseEntity<ResponseDto> response = authService.registerUser(userDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).isEqualTo("User registered successfully");

			ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
			verify(userRepository).save(captor.capture());

			UserEntity savedUser = captor.getValue();
			assertThat(savedUser.getPassword()).isNotEqualTo(PASSWORD);
			assertThat(BCrypt.checkpw(PASSWORD, savedUser.getPassword())).isTrue();
		}

		@Test
		void whenValid_returnsAuthResponseWithToken() {
			when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME)).thenReturn(
					Optional.empty());

			UserEntity mappedUser = new UserEntity();
			mappedUser.setUsername(USERNAME);
			mappedUser.setEmail(EMAIL);
			mappedUser.setName(NAME);
			when(modelMapper.map(any(UserDto.class), eq(UserEntity.class))).thenReturn(mappedUser);
			when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
				UserEntity user = invocation.getArgument(0);
				user.setId(USER_ID);
				return user;
			});
			when(jwtService.generateToken(USER_ID, USERNAME, EMAIL, NAME)).thenReturn(JWT_TOKEN);

			ResponseEntity<ResponseDto> response = authService.registerUser(userDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getData()).isInstanceOf(AuthResponseDto.class);

			AuthResponseDto authResponse = (AuthResponseDto) response.getBody().getData();
			assertThat(authResponse.userId()).isEqualTo(USER_ID);
			assertThat(authResponse.token()).isEqualTo(JWT_TOKEN);

			verify(jwtService).generateToken(USER_ID, USERNAME, EMAIL, NAME);
		}

		@Test
		void whenValid_callsRepositoryWithCorrectEmailAndUsername() {
			when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME)).thenReturn(
					Optional.empty());

			UserEntity mappedUser = new UserEntity();
			mappedUser.setUsername(USERNAME);
			mappedUser.setEmail(EMAIL);
			mappedUser.setName(NAME);
			when(modelMapper.map(any(UserDto.class), eq(UserEntity.class))).thenReturn(mappedUser);
			when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
				UserEntity user = invocation.getArgument(0);
				user.setId(USER_ID);
				return user;
			});
			when(jwtService.generateToken(anyString(), anyString(), anyString(), anyString())).thenReturn(JWT_TOKEN);

			authService.registerUser(userDto());

			verify(userRepository).findByEmailIgnoreCaseOrUsernameIgnoreCase(EMAIL, USERNAME);
		}

	}

	@Nested
	class LoginUser {

		@Test
		void whenUserNotFound_returnsUnauthorized() {
			when(userRepository.findByEmailOrUsername(EMAIL, EMAIL)).thenReturn(Optional.empty());

			ResponseEntity<ResponseDto> response = authService.loginUser(loginDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMessage()).isEqualTo("Usuário ou senha inválidos");
			assertThat(response.getBody().isSuccess()).isFalse();
			verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
		}

		@Test
		void whenPasswordIsIncorrect_returnsUnauthorized() {
			UserEntity user = userEntity();
			when(userRepository.findByEmailOrUsername(EMAIL, EMAIL)).thenReturn(Optional.of(user));

			LoginDto wrongPasswordDto = new LoginDto(EMAIL, "WrongPassword123!");
			ResponseEntity<ResponseDto> response = authService.loginUser(wrongPasswordDto);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMessage()).isEqualTo("Usuário ou senha inválidos");
			assertThat(response.getBody().isSuccess()).isFalse();
			verify(jwtService, never()).generateToken(anyString(), anyString(), anyString(), anyString());
		}

		@Test
		void whenValid_returnsAuthResponseWithToken() {
			UserEntity user = userEntity();
			when(userRepository.findByEmailOrUsername(EMAIL, EMAIL)).thenReturn(Optional.of(user));
			when(jwtService.generateToken(USER_ID, USERNAME, EMAIL, NAME)).thenReturn(JWT_TOKEN);

			ResponseEntity<ResponseDto> response = authService.loginUser(loginDto());

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().isSuccess()).isTrue();
			assertThat(response.getBody().getMessage()).isEqualTo("Login successful");
			assertThat(response.getBody().getData()).isInstanceOf(AuthResponseDto.class);

			AuthResponseDto authResponse = (AuthResponseDto) response.getBody().getData();
			assertThat(authResponse.userId()).isEqualTo(USER_ID);
			assertThat(authResponse.token()).isEqualTo(JWT_TOKEN);
		}

		@Test
		void whenValid_callsJwtServiceWithCorrectParameters() {
			UserEntity user = userEntity();
			when(userRepository.findByEmailOrUsername(EMAIL, EMAIL)).thenReturn(Optional.of(user));
			when(jwtService.generateToken(USER_ID, USERNAME, EMAIL, NAME)).thenReturn(JWT_TOKEN);

			authService.loginUser(loginDto());

			verify(jwtService).generateToken(USER_ID, USERNAME, EMAIL, NAME);
		}

		@Test
		void whenLoginWithUsername_findsUserByUsername() {
			UserEntity user = userEntity();
			when(userRepository.findByEmailOrUsername(USERNAME, USERNAME)).thenReturn(Optional.of(user));
			when(jwtService.generateToken(USER_ID, USERNAME, EMAIL, NAME)).thenReturn(JWT_TOKEN);

			LoginDto usernameLoginDto = new LoginDto(USERNAME, PASSWORD);
			ResponseEntity<ResponseDto> response = authService.loginUser(usernameLoginDto);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			verify(userRepository).findByEmailOrUsername(USERNAME, USERNAME);
		}

		@Test
		void whenValid_verifiesPasswordWithBCrypt() {
			UserEntity user = userEntity();
			String hashedPassword = user.getPassword();
			when(userRepository.findByEmailOrUsername(EMAIL, EMAIL)).thenReturn(Optional.of(user));
			when(jwtService.generateToken(USER_ID, USERNAME, EMAIL, NAME)).thenReturn(JWT_TOKEN);

			authService.loginUser(loginDto());

			// Verifica que a senha foi verificada corretamente
			assertThat(BCrypt.checkpw(PASSWORD, hashedPassword)).isTrue();
		}

	}

}