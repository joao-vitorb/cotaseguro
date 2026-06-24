package com.cotaseguro.repository;

import com.cotaseguro.domain.Quote;
import com.cotaseguro.domain.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Page<Quote> findByCustomerId(Long customerId, Pageable pageable);

    Page<Quote> findByStatus(QuoteStatus status, Pageable pageable);

    Page<Quote> findByCustomerIdAndStatus(Long customerId, QuoteStatus status, Pageable pageable);

}
