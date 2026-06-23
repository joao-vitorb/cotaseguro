package com.cotaseguro.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234567890";

    private final JwtService jwtService = new JwtService(SECRET, 3600000L);

    @Test
    void generatedTokenExposesUsername() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
    }

    @Test
    void tamperedTokenIsNotValid() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertThat(jwtService.isValid(token + "tampered")).isFalse();
    }

    @Test
    void tokenSignedWithAnotherSecretIsNotValid() {
        String token = jwtService.generateToken("admin", "ADMIN");
        JwtService otherService = new JwtService("another-secret-another-secret-1234567890", 3600000L);

        assertThat(otherService.isValid(token)).isFalse();
    }

}
