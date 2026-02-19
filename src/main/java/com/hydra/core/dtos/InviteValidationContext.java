package com.hydra.core.dtos;

import com.hydra.core.entity.TeamEntity;
import com.hydra.core.entity.UserEntity;
import com.hydra.core.enums.TeamRole;

public record InviteValidationContext(TeamRole role, TeamEntity team, UserEntity invitedUser, UserEntity inviter) {

}