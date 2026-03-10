package com.services.backend.entities.dtos;

import com.services.backend.entities.enums.UserRole;

public record RegisterDTO(String name, String email, String password, UserRole role) {
}
