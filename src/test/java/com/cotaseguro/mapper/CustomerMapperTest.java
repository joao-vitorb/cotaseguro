package com.cotaseguro.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.dto.CustomerResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void toEntityCopiesRequestFields() {
        CustomerRequest request = new CustomerRequest(
                "Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");

        Customer customer = mapper.toEntity(request);

        assertThat(customer.getName()).isEqualTo("Alice Martins");
        assertThat(customer.getDocument()).isEqualTo("11111111111");
        assertThat(customer.getBirthDate()).isEqualTo(LocalDate.of(1990, 4, 12));
        assertThat(customer.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void toResponseCopiesEntityFields() {
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setName("Bruno Carvalho");
        customer.setDocument("22222222222");
        customer.setBirthDate(LocalDate.of(1985, 9, 30));
        customer.setEmail("bruno@example.com");
        customer.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        CustomerResponse response = mapper.toResponse(customer);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("Bruno Carvalho");
        assertThat(response.document()).isEqualTo("22222222222");
        assertThat(response.birthDate()).isEqualTo(LocalDate.of(1985, 9, 30));
        assertThat(response.email()).isEqualTo("bruno@example.com");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

}
