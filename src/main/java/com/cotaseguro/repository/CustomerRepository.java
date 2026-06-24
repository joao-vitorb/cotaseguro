package com.cotaseguro.repository;

import com.cotaseguro.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByDocument(String document);

    boolean existsByEmail(String email);

    boolean existsByDocumentAndIdNot(String document, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

}
