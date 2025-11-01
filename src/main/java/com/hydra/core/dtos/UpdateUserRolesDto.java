package com.hydra.core.dtos;

import java.util.List;

public record UpdateUserRolesDto(String userId, List<String> roleIds) {

}