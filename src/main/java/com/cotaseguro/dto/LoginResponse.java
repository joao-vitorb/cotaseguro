package com.cotaseguro.dto;

import com.cotaseguro.domain.Role;

public record LoginResponse(
        String token,
        String tokenType,
        String username,
        Role role
) {
}
