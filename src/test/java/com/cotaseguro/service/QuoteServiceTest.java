package com.cotaseguro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.InsuranceType;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private PremiumCalculator premiumCalculator;

    @Spy
    private QuoteMapper quoteMapper;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @InjectMocks
    private QuoteService quoteService;

    private Customer adultCustomer() {
        Customer customer = new Customer();
        customer.setId(5L);
        customer.setBirthDate(LocalDate.now().minusYears(30));
        return customer;
    }

    @Test
    void createComputesPremiumAndStartsPending() {
        when(customerRepository.findById(5L)).thenReturn(Optional.of(adultCustomer()));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> {
            Quote quote = invocation.getArgument(0);
            quote.setId(1L);
            return quote;
        });

        QuoteResponse response = quoteService.create(
                new QuoteRequest(5L, InsuranceType.AUTO, new BigDecimal("50000.00")));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.customerId()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo(QuoteStatus.PENDING);
        assertThat(response.premium()).isEqualByComparingTo("2500.00");
    }

    @Test
    void createWithUnknownCustomerThrowsNotFound() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.create(
                new QuoteRequest(5L, InsuranceType.AUTO, new BigDecimal("50000.00"))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(quoteRepository, never()).save(any(Quote.class));
    }

    @Test
    void approveChangesPendingQuoteToApproved() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setCustomer(adultCustomer());
        quote.setStatus(QuoteStatus.PENDING);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteResponse response = quoteService.approve(1L);

        assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
    }

    @Test
    void approveNonPendingQuoteThrowsConflict() {
        Quote quote = new Quote();
        quote.setId(1L);
        quote.setStatus(QuoteStatus.APPROVED);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

        assertThatThrownBy(() -> quoteService.approve(1L))
                .isInstanceOf(ConflictException.class);

        verify(quoteRepository, never()).save(any(Quote.class));
    }

    @Test
    void rejectChangesPendingQuoteToRejected() {
        when(quoteRepository.findById(10L)).thenReturn(Optional.of(pendingQuote()));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteResponse response = quoteService.reject(10L);

        assertThat(response.status()).isEqualTo(QuoteStatus.REJECTED);
    }

    @Test
    void getByIdReturnsQuote() {
        when(quoteRepository.findById(10L)).thenReturn(Optional.of(pendingQuote()));

        QuoteResponse response = quoteService.getById(10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void listByCustomerAndStatusUsesCombinedQuery() {
        when(quoteRepository.findByCustomerIdAndStatus(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(pendingQuote())));

        PageResponse<QuoteResponse> result = quoteService.list(5L, QuoteStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listByStatusOnlyUsesStatusQuery() {
        when(quoteRepository.findByStatus(any(), any()))
                .thenReturn(new PageImpl<>(List.of(pendingQuote())));

        PageResponse<QuoteResponse> result = quoteService.list(null, QuoteStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listByCustomerOnlyUsesCustomerQuery() {
        when(quoteRepository.findByCustomerId(any(), any()))
                .thenReturn(new PageImpl<>(List.of(pendingQuote())));

        PageResponse<QuoteResponse> result = quoteService.list(5L, null, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listWithoutFiltersUsesFindAll() {
        when(quoteRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(pendingQuote())));

        PageResponse<QuoteResponse> result = quoteService.list(null, null, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    private Quote pendingQuote() {
        Quote quote = new Quote();
        quote.setId(10L);
        quote.setCustomer(adultCustomer());
        quote.setInsuranceType(InsuranceType.AUTO);
        quote.setCoverageAmount(new BigDecimal("50000.00"));
        quote.setPremium(new BigDecimal("2500.00"));
        quote.setStatus(QuoteStatus.PENDING);
        return quote;
    }

}
