package com.cotaseguro.service;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.dto.CustomerResponse;
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.mapper.CustomerMapper;
import com.cotaseguro.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        ensureDocumentIsAvailable(request.document());
        ensureEmailIsAvailable(request.email());

        Customer customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(String name, Pageable pageable) {
        Page<Customer> customers = StringUtils.hasText(name)
                ? customerRepository.findByNameContainingIgnoreCase(name, pageable)
                : customerRepository.findAll(pageable);

        return PageResponse.from(customers.map(customerMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(findCustomerOrThrow(id));
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);
        ensureDocumentIsAvailableForUpdate(request.document(), id);
        ensureEmailIsAvailableForUpdate(request.email(), id);

        customerMapper.applyRequest(customer, request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private void ensureDocumentIsAvailable(String document) {
        if (customerRepository.existsByDocument(document)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document already registered");
        }
    }

    private void ensureEmailIsAvailable(String email) {
        if (customerRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }

    private void ensureDocumentIsAvailableForUpdate(String document, Long id) {
        if (customerRepository.existsByDocumentAndIdNot(document, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Document already registered");
        }
    }

    private void ensureEmailIsAvailableForUpdate(String email, Long id) {
        if (customerRepository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
    }

}
