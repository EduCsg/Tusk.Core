package com.hydra.core.dtos;

import java.time.LocalDateTime;

public record TeamMemberDto(String id, String userId, String userName, String userEmail, String username, String role,
							String invitedByName, LocalDateTime joinedAt, LocalDateTime createdAt) {

}