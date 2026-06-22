package com.cotaseguro.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.cotaseguro.domain.Customer;
import com.cotaseguro.domain.InsuranceType;
import com.cotaseguro.domain.Quote;
import com.cotaseguro.domain.QuoteStatus;
import com.cotaseguro.dto.QuoteResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class QuoteMapperTest {

    private final QuoteMapper mapper = new QuoteMapper();

    @Test
    void toResponseFlattensCustomerId() {
        Customer customer = new Customer();
        customer.setId(3L);

        Quote quote = new Quote();
        quote.setId(10L);
        quote.setCustomer(customer);
        quote.setInsuranceType(InsuranceType.AUTO);
        quote.setCoverageAmount(new BigDecimal("50000.00"));
        quote.setPremium(new BigDecimal("1200.00"));
        quote.setStatus(QuoteStatus.PENDING);
        quote.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));

        QuoteResponse response = mapper.toResponse(quote);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.customerId()).isEqualTo(3L);
        assertThat(response.insuranceType()).isEqualTo(InsuranceType.AUTO);
        assertThat(response.coverageAmount()).isEqualByComparingTo("50000.00");
        assertThat(response.premium()).isEqualByComparingTo("1200.00");
        assertThat(response.status()).isEqualTo(QuoteStatus.PENDING);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
    }

}
