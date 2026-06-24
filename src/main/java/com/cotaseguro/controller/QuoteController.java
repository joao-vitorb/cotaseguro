package com.cotaseguro.controller;

import com.cotaseguro.domain.QuoteStatus;
import com.cotaseguro.dto.PageResponse;
import com.cotaseguro.dto.QuoteRequest;
import com.cotaseguro.dto.QuoteResponse;
import com.cotaseguro.service.QuoteService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping
    public ResponseEntity<QuoteResponse> create(
            @Valid @RequestBody QuoteRequest request,
            UriComponentsBuilder uriBuilder) {
        QuoteResponse response = quoteService.create(request);
        URI location = uriBuilder.path("/api/v1/quotes/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public PageResponse<QuoteResponse> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) QuoteStatus status,
            Pageable pageable) {
        return quoteService.list(customerId, status, pageable);
    }

    @GetMapping("/{id}")
    public QuoteResponse getById(@PathVariable Long id) {
        return quoteService.getById(id);
    }

    @PostMapping("/{id}/approve")
    public QuoteResponse approve(@PathVariable Long id) {
        return quoteService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public QuoteResponse reject(@PathVariable Long id) {
        return quoteService.reject(id);
    }

}
