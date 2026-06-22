package com.cotaseguro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CustomerRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 20) @Pattern(regexp = "\\d+", message = "document must contain only digits") String document,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email @Size(max = 255) String email
) {
}
