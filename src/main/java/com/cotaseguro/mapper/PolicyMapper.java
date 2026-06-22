package com.cotaseguro.mapper;

import com.cotaseguro.domain.Policy;
import com.cotaseguro.dto.PolicyResponse;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    public PolicyResponse toResponse(Policy policy) {
        return new PolicyResponse(
                policy.getId(),
                policy.getQuote().getId(),
                policy.getCustomer().getId(),
                policy.getNumber(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getCreatedAt()
        );
    }

}
