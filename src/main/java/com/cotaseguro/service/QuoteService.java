package com.cotaseguro.service;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.Quote;
import com.cotaseguro.domain.QuoteStatus;
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.dto.QuoteRequest;
import com.cotaseguro.dto.QuoteResponse;
import com.cotaseguro.exception.ConflictException;
import com.cotaseguro.exception.ResourceNotFoundException;
import com.cotaseguro.mapper.QuoteMapper;
import com.cotaseguro.observability.ApplicationMetrics;
import com.cotaseguro.repository.CustomerRepository;
import com.cotaseguro.repository.QuoteRepository;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final PremiumCalculator premiumCalculator;
    private final QuoteMapper quoteMapper;
    private final ApplicationMetrics applicationMetrics;

    public QuoteService(
            QuoteRepository quoteRepository,
            CustomerRepository customerRepository,
            PremiumCalculator premiumCalculator,
            QuoteMapper quoteMapper,
            ApplicationMetrics applicationMetrics) {
        this.quoteRepository = quoteRepository;
        this.customerRepository = customerRepository;
        this.premiumCalculator = premiumCalculator;
        this.quoteMapper = quoteMapper;
        this.applicationMetrics = applicationMetrics;
    }

    @Transactional
    public QuoteResponse create(QuoteRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        BigDecimal premium = premiumCalculator.calculate(
                request.insuranceType(), request.coverageAmount(), customer.getBirthDate());

        Quote quote = new Quote();
        quote.setCustomer(customer);
        quote.setInsuranceType(request.insuranceType());
        quote.setCoverageAmount(request.coverageAmount());
        quote.setPremium(premium);
        quote.setStatus(QuoteStatus.PENDING);

        Quote savedQuote = quoteRepository.save(quote);
        applicationMetrics.quoteGenerated();
        return quoteMapper.toResponse(savedQuote);
    }

    @Transactional(readOnly = true)
    public PageResponse<QuoteResponse> list(Long customerId, QuoteStatus status, Pageable pageable) {
        Page<Quote> quotes = findQuotes(customerId, status, pageable);
        return PageResponse.from(quotes.map(quoteMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public QuoteResponse getById(Long id) {
        return quoteMapper.toResponse(findQuoteOrThrow(id));
    }

    @Transactional
    public QuoteResponse approve(Long id) {
        return changeStatus(id, QuoteStatus.APPROVED);
    }

    @Transactional
    public QuoteResponse reject(Long id) {
        return changeStatus(id, QuoteStatus.REJECTED);
    }

    private QuoteResponse changeStatus(Long id, QuoteStatus newStatus) {
        Quote quote = findQuoteOrThrow(id);
        ensureQuoteIsPending(quote);

        quote.setStatus(newStatus);
        return quoteMapper.toResponse(quoteRepository.save(quote));
    }

    private Page<Quote> findQuotes(Long customerId, QuoteStatus status, Pageable pageable) {
        if (customerId != null && status != null) {
            return quoteRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        }
        if (customerId != null) {
            return quoteRepository.findByCustomerId(customerId, pageable);
        }
        if (status != null) {
            return quoteRepository.findByStatus(status, pageable);
        }
        return quoteRepository.findAll(pageable);
    }

    private Quote findQuoteOrThrow(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));
    }

    private void ensureQuoteIsPending(Quote quote) {
        if (quote.getStatus() != QuoteStatus.PENDING) {
            throw new ConflictException("Quote is not pending");
        }
    }

}
