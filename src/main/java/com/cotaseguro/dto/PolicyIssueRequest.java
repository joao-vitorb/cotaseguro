package com.cotaseguro.dto;

import jakarta.validation.constraints.NotNull;

public record PolicyIssueRequest(
        @NotNull Long quoteId
) {
}
