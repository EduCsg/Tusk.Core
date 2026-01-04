package com.hydra.core.dtos;

import java.util.List;

public record TeamUsersDto(List<UserDto> coaches, List<UserDto> athletes) {

}