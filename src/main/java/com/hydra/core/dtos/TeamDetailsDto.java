package com.hydra.core.dtos;

import java.time.LocalDateTime;

public record TeamDetailsDto(String id, String name, String description, String city, String uf, String color,
							 String role, String imageUrl, LocalDateTime createdAt) {

}