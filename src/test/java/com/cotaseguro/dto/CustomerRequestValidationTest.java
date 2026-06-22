package com.cotaseguro.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CustomerRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void validRequestHasNoViolations() {
        CustomerRequest request = new CustomerRequest(
                "Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankNameIsRejected() {
        CustomerRequest request = new CustomerRequest(
                " ", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("name"));
    }

    @Test
    void nonDigitDocumentIsRejected() {
        CustomerRequest request = new CustomerRequest(
                "Alice Martins", "abc123", LocalDate.of(1990, 4, 12), "alice@example.com");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("document"));
    }

    @Test
    void invalidEmailIsRejected() {
        CustomerRequest request = new CustomerRequest(
                "Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "not-an-email");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("email"));
    }

    @Test
    void futureBirthDateIsRejected() {
        CustomerRequest request = new CustomerRequest(
                "Alice Martins", "11111111111", LocalDate.now().plusDays(1), "alice@example.com");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertThat(violations).anyMatch(violation -> violation.getPropertyPath().toString().equals("birthDate"));
    }

}
