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
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.exception.ConflictException;
import com.cotaseguro.exception.ResourceNotFoundException;
import com.cotaseguro.mapper.CustomerMapper;
import com.cotaseguro.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
                .isInstanceOf(ConflictException.class);

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getByIdWithUnknownIdThrowsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteWithUnknownIdThrowsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void listWithoutNameReturnsAllCustomers() {
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer()));
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<CustomerResponse> result = customerService.list(null, PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(customerRepository, never()).findByNameContainingIgnoreCase(any(), any(Pageable.class));
    }

    @Test
    void listWithNameFiltersByName() {
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer()));
        when(customerRepository.findByNameContainingIgnoreCase(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<CustomerResponse> result = customerService.list("ali", PageRequest.of(0, 10));

        assertThat(result.content()).hasSize(1);
        verify(customerRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void updateAppliesChanges() {
        Customer existing = sampleCustomer();
        existing.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByDocumentAndIdNot("11111111111", 1L)).thenReturn(false);
        when(customerRepository.existsByEmailAndIdNot("alice@example.com", 1L)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.update(1L, sampleRequest());

        assertThat(response.name()).isEqualTo("Alice Martins");
    }

    @Test
    void updateWithConflictingDocumentThrowsConflict() {
        Customer existing = sampleCustomer();
        existing.setId(1L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByDocumentAndIdNot("11111111111", 1L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.update(1L, sampleRequest()))
                .isInstanceOf(ConflictException.class);

        verify(customerRepository, never()).save(any(Customer.class));
    }

    private Customer sampleCustomer() {
        Customer customer = new Customer();
        customer.setName("Alice Martins");
        customer.setDocument("11111111111");
        customer.setBirthDate(LocalDate.of(1990, 4, 12));
        customer.setEmail("alice@example.com");
        return customer;
    }

}
