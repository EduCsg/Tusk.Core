package com.hydra.core.exceptions;

import com.hydra.core.dtos.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class CustomExceptions {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {

		String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
		ResponseDto response = new ResponseDto(errorMessage, null, false);

		return ResponseEntity.status(400).body(response);
	}

}