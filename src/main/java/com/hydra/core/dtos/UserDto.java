package com.hydra.core.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserDto(

		String id, //
		String token, //

		@NotBlank(message = "O nome de usuário é obrigatório!") //
		String username, //

		@NotBlank(message = "O nome é obrigatório!") //
		String name,

		@Email(message = "Informe um e-mail válido!") //
		@NotBlank(message = "O e-mail é obrigatório!") //
		@Size(max = 100, message = "O e-mail deve ter no máximo 100 caracteres!") //
		@Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "Informe um e-mail válido!") //
		String email,

		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //
		@NotBlank(message = "A senha é obrigatória!") //
		@Size(min = 8, max = 40, message = "A senha deve ter entre 8 e 40 caracteres!") //
		@Pattern(regexp = ".*[^a-zA-Z0-9 ].*", message = "A senha deve conter ao menos um caractere especial!") //
		String password,

		String role

) {

}