package com.cotaseguro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.dto.CustomerResponse;
import com.cotaseguro.mapper.CustomerMapper;
import com.cotaseguro.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest sampleRequest() {
        return new CustomerRequest("Alice Martins", "11111111111", LocalDate.of(1990, 4, 12), "alice@example.com");
    }

    @Test
    void createPersistsAndReturnsCustomer() {
        when(customerRepository.existsByDocument("11111111111")).thenReturn(false);
        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(1L);
            return customer;
        });

        CustomerResponse response = customerService.create(sampleRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Alice Martins");
    }

    @Test
    void createWithExistingDocumentThrowsConflict() {
        when(customerRepository.existsByDocument("11111111111")).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(sampleRequest()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception ->
                        assertThat(((ResponseStatusException) exception).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getByIdWithUnknownIdThrowsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception ->
                        assertThat(((ResponseStatusException) exception).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteWithUnknownIdThrowsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception ->
                        assertThat(((ResponseStatusException) exception).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));

        verify(customerRepository, never()).delete(any(Customer.class));
    }

}
