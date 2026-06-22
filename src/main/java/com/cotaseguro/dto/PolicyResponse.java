package com.cotaseguro.dto;

import com.cotaseguro.domain.PolicyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PolicyResponse(
        Long id,
        Long quoteId,
        Long customerId,
        String number,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt
) {
}
