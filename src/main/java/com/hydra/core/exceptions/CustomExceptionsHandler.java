package com.hydra.core.exceptions;

import com.hydra.core.dtos.ResponseDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class CustomExceptionsHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseDto> methodArgumentNotValid(MethodArgumentNotValidException ex) {
		String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
		ResponseDto response = new ResponseDto(errorMessage, null, false);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ResponseDto> handleUnauthorized(UnauthorizedException ex) {
		ResponseDto response = new ResponseDto(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ResponseDto> entityNotFound(EntityNotFoundException ex) {
		ResponseDto response = new ResponseDto(ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ResponseDto> handleInvalidToken(InvalidTokenException ex) {
		ResponseDto response = new ResponseDto(ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

}