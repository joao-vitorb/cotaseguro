package com.cotaseguro.service;

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
import com.cotaseguro.repository.PolicyRepository;
import com.cotaseguro.repository.QuoteRepository;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PolicyService {

    private static final int POLICY_DURATION_YEARS = 1;

    private final PolicyRepository policyRepository;
    private final QuoteRepository quoteRepository;
    private final PolicyMapper policyMapper;

    public PolicyService(
            PolicyRepository policyRepository,
            QuoteRepository quoteRepository,
            PolicyMapper policyMapper) {
        this.policyRepository = policyRepository;
        this.quoteRepository = quoteRepository;
        this.policyMapper = policyMapper;
    }

    @Transactional
    public PolicyResponse issue(PolicyIssueRequest request) {
        Quote quote = quoteRepository.findById(request.quoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Quote not found"));

        ensureQuoteIsApproved(quote);
        ensurePolicyIsNotAlreadyIssued(quote.getId());

        LocalDate startDate = LocalDate.now();

        Policy policy = new Policy();
        policy.setQuote(quote);
        policy.setCustomer(quote.getCustomer());
        policy.setNumber(buildPolicyNumber(quote.getId()));
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(startDate);
        policy.setEndDate(startDate.plusYears(POLICY_DURATION_YEARS));

        return policyMapper.toResponse(policyRepository.save(policy));
    }

    @Transactional(readOnly = true)
    public PageResponse<PolicyResponse> list(Long customerId, PolicyStatus status, Pageable pageable) {
        Page<Policy> policies = findPolicies(customerId, status, pageable);
        return PageResponse.from(policies.map(policyMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PolicyResponse getById(Long id) {
        return policyMapper.toResponse(findPolicyOrThrow(id));
    }

    @Transactional
    public PolicyResponse cancel(Long id) {
        Policy policy = findPolicyOrThrow(id);
        ensurePolicyIsActive(policy);

        policy.setStatus(PolicyStatus.CANCELLED);
        return policyMapper.toResponse(policyRepository.save(policy));
    }

    private Page<Policy> findPolicies(Long customerId, PolicyStatus status, Pageable pageable) {
        if (customerId != null && status != null) {
            return policyRepository.findByCustomerIdAndStatus(customerId, status, pageable);
        }
        if (customerId != null) {
            return policyRepository.findByCustomerId(customerId, pageable);
        }
        if (status != null) {
            return policyRepository.findByStatus(status, pageable);
        }
        return policyRepository.findAll(pageable);
    }

    private Policy findPolicyOrThrow(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));
    }

    private void ensureQuoteIsApproved(Quote quote) {
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            throw new ConflictException("Quote is not approved");
        }
    }

    private void ensurePolicyIsNotAlreadyIssued(Long quoteId) {
        if (policyRepository.existsByQuoteId(quoteId)) {
            throw new ConflictException("Policy already issued for quote");
        }
    }

    private void ensurePolicyIsActive(Policy policy) {
        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new ConflictException("Policy is not active");
        }
    }

    private String buildPolicyNumber(Long quoteId) {
        return String.format("POL-%06d", quoteId);
    }

}
