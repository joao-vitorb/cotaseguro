package com.cotaseguro.mapper;

import com.cotaseguro.domain.Quote;
import com.cotaseguro.dto.QuoteResponse;
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    public QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getCustomer().getId(),
                quote.getInsuranceType(),
                quote.getCoverageAmount(),
                quote.getPremium(),
                quote.getStatus(),
                quote.getCreatedAt()
        );
    }

}
