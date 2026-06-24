package com.cotaseguro.repository;

import com.cotaseguro.domain.Policy;
import com.cotaseguro.domain.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    boolean existsByQuoteId(Long quoteId);

    Page<Policy> findByCustomerId(Long customerId, Pageable pageable);

    Page<Policy> findByStatus(PolicyStatus status, Pageable pageable);

    Page<Policy> findByCustomerIdAndStatus(Long customerId, PolicyStatus status, Pageable pageable);

}
