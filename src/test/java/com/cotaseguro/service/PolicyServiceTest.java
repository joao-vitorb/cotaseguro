package com.cotaseguro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.Policy;
import com.cotaseguro.domain.PolicyStatus;
import com.cotaseguro.domain.Quote;
import com.cotaseguro.domain.QuoteStatus;
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.dto.PolicyIssueRequest;
import com.cotaseguro.dto.PolicyResponse;
import com.cotaseguro.exception.ConflictException;
import com.cotaseguro.exception.ResourceNotFoundException;
import com.cotaseguro.mapper.PolicyMapper;
import com.cotaseguro.observability.ApplicationMetrics;
import com.cotaseguro.repository.PolicyRepository;
import com.cotaseguro.repository.QuoteRepository;
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
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Spy
    private PolicyMapper policyMapper;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @InjectMocks
    private PolicyService policyService;

    private Quote quoteWithStatus(QuoteStatus status) {
        Customer customer = new Customer();
        customer.setId(5L);

        Quote quote = new Quote();
        quote.setId(7L);
        quote.setCustomer(customer);
        quote.setStatus(status);
        return quote;
    }

    @Test
    void issueFromApprovedQuoteCreatesActivePolicy() {
        when(quoteRepository.findById(7L)).thenReturn(Optional.of(quoteWithStatus(QuoteStatus.APPROVED)));
        when(policyRepository.existsByQuoteId(7L)).thenReturn(false);
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> {
            Policy policy = invocation.getArgument(0);
            policy.setId(1L);
            return policy;
        });

        PolicyResponse response = policyService.issue(new PolicyIssueRequest(7L));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.quoteId()).isEqualTo(7L);
        assertThat(response.customerId()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(response.number()).isEqualTo("POL-000007");
    }

    @Test
    void issueFromPendingQuoteThrowsConflict() {
        when(quoteRepository.findById(7L)).thenReturn(Optional.of(quoteWithStatus(QuoteStatus.PENDING)));

        assertThatThrownBy(() -> policyService.issue(new PolicyIssueRequest(7L)))
                .isInstanceOf(ConflictException.class);

        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void issueWhenPolicyAlreadyExistsThrowsConflict() {
        when(quoteRepository.findById(7L)).thenReturn(Optional.of(quoteWithStatus(QuoteStatus.APPROVED)));
        when(policyRepository.existsByQuoteId(7L)).thenReturn(true);

        assertThatThrownBy(() -> policyService.issue(new PolicyIssueRequest(7L)))
                .isInstanceOf(ConflictException.class);

        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void issueUnknownQuoteThrowsNotFound() {
        when(quoteRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> policyService.issue(new PolicyIssueRequest(7L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelActivePolicyChangesStatusToCancelled() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setQuote(quoteWithStatus(QuoteStatus.APPROVED));
        policy.setCustomer(policy.getQuote().getCustomer());
        policy.setStatus(PolicyStatus.ACTIVE);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = policyService.cancel(1L);

        assertThat(response.status()).isEqualTo(PolicyStatus.CANCELLED);
    }

    @Test
    void cancelNonActivePolicyThrowsConflict() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.CANCELLED);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertThatThrownBy(() -> policyService.cancel(1L))
                .isInstanceOf(ConflictException.class);

        verify(policyRepository, never()).save(any(Policy.class));
    }

    @Test
    void getByIdReturnsPolicy() {
        when(policyRepository.findById(20L)).thenReturn(Optional.of(activePolicy()));

        PolicyResponse response = policyService.getById(20L);

        assertThat(response.id()).isEqualTo(20L);
    }

    @Test
    void listByStatusUsesStatusQuery() {
        when(policyRepository.findByStatus(any(), any()))
                .thenReturn(new PageImpl<>(List.of(activePolicy())));

        PageResponse<PolicyResponse> result = policyService.list(null, PolicyStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listByCustomerAndStatusUsesCombinedQuery() {
        when(policyRepository.findByCustomerIdAndStatus(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(activePolicy())));

        PageResponse<PolicyResponse> result = policyService.list(5L, PolicyStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listByCustomerOnlyUsesCustomerQuery() {
        when(policyRepository.findByCustomerId(any(), any()))
                .thenReturn(new PageImpl<>(List.of(activePolicy())));

        PageResponse<PolicyResponse> result = policyService.list(5L, null, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listWithoutFiltersUsesFindAll() {
        when(policyRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activePolicy())));

        PageResponse<PolicyResponse> result = policyService.list(null, null, PageRequest.of(0, 10));

        assertThat(result.totalElements()).isEqualTo(1);
    }

    private Policy activePolicy() {
        Quote quote = quoteWithStatus(QuoteStatus.APPROVED);

        Policy policy = new Policy();
        policy.setId(20L);
        policy.setQuote(quote);
        policy.setCustomer(quote.getCustomer());
        policy.setNumber("POL-000007");
        policy.setStatus(PolicyStatus.ACTIVE);
        return policy;
    }

}
