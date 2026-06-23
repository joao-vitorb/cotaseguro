package com.cotaseguro.dto;

import com.cotaseguro.domain.Role;

public record UserResponse(
        Long id,
        String username,
        Role role
) {
}
