package com.hydra.core.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ResponseDto {

	private String message;
	private Object data;
	private boolean success;

	public ResponseDto(String message) {
		this.message = message;
	}

}