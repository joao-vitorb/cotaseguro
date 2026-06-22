package com.cotaseguro.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        String name,
        String document,
        LocalDate birthDate,
        String email,
        LocalDateTime createdAt
) {
}
