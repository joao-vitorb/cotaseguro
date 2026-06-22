package com.cotaseguro.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.Policy;
import com.cotaseguro.domain.PolicyStatus;
import com.cotaseguro.domain.Quote;
import com.cotaseguro.dto.PolicyResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PolicyMapperTest {

    private final PolicyMapper mapper = new PolicyMapper();

    @Test
    void toResponseFlattensQuoteAndCustomerIds() {
        Customer customer = new Customer();
        customer.setId(3L);

        Quote quote = new Quote();
        quote.setId(10L);

        Policy policy = new Policy();
        policy.setId(20L);
        policy.setQuote(quote);
        policy.setCustomer(customer);
        policy.setNumber("POL-0001");
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.of(2026, 1, 1));
        policy.setEndDate(LocalDate.of(2027, 1, 1));
        policy.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        PolicyResponse response = mapper.toResponse(policy);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.quoteId()).isEqualTo(10L);
        assertThat(response.customerId()).isEqualTo(3L);
        assertThat(response.number()).isEqualTo("POL-0001");
        assertThat(response.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

}
