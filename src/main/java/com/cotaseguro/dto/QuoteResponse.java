package com.cotaseguro.dto;

import com.cotaseguro.domain.InsuranceType;
import com.cotaseguro.domain.QuoteStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record QuoteResponse(
        Long id,
        Long customerId,
        InsuranceType insuranceType,
        BigDecimal coverageAmount,
        BigDecimal premium,
        QuoteStatus status,
        LocalDateTime createdAt
) {
}
