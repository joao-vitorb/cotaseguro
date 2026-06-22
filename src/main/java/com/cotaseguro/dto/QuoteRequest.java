package com.cotaseguro.dto;

import com.cotaseguro.domain.InsuranceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record QuoteRequest(
        @NotNull Long customerId,
        @NotNull InsuranceType insuranceType,
        @NotNull @Positive BigDecimal coverageAmount
) {
}
