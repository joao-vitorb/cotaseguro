package com.cotaseguro.mapper;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.dto.CustomerRequest;
import com.cotaseguro.dto.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        applyRequest(customer, request);
        return customer;
    }

    public void applyRequest(Customer customer, CustomerRequest request) {
        customer.setName(request.name());
        customer.setDocument(request.document());
        customer.setBirthDate(request.birthDate());
        customer.setEmail(request.email());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocument(),
                customer.getBirthDate(),
                customer.getEmail(),
                customer.getCreatedAt()
        );
    }

}
